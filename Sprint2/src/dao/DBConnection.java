package dao;

import java.io.File;
import java.sql.*;
import java.util.TimeZone;

/**
 * DBConnection provides a unified, persistent connection
 * to the H2 database for all modules in the Pizza 505 ENMU
 * restaurant management system.
 * <p>
 * This class ensures the H2 database is initialized, all required
 * tables exist, and default data is automatically seeded on startup.
 * It is designed to be loaded once by the application server
 * (e.g., Tomcat) and shared across DAO components.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Manages a single persistent connection to the H2 file-based database.</li>
 *   <li>Automatically initializes the schema for users, staff, customers, and menu items.</li>
 *   <li>Seeds default users and menu data if the tables are empty.</li>
 *   <li>Ensures compatibility with MySQL syntax via MODE=MySQL.</li>
 *   <li>Verifies connection validity and reconnects if necessary.</li>
 * </ul>
 *
 * <h3>Connection Details:</h3>
 * <ul>
 *   <li><b>Driver:</b> org.h2.Driver</li>
 *   <li><b>URL Format:</b> jdbc:h2:file:{project_root}/data/restaurantdb</li>
 *   <li><b>Username:</b> sa</li>
 *   <li><b>Password:</b> (empty)</li>
 *   <li><b>Options:</b> AUTO_SERVER=TRUE, MODE=MySQL, TIME ZONE=America/Denver</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * Connection conn = DBConnection.getConnection();
 * Statement st = conn.createStatement();
 * ResultSet rs = st.executeQuery("SELECT * FROM users");
 * while (rs.next()) {
 *     System.out.println(rs.getString("name") + " (" + rs.getString("role") + ")");
 * }
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d2
 * @since 2025-10
 */
public class DBConnection {

    /** Default database username. */
    private static final String USER = "sa";

    /** Default database password (blank for H2). */
    private static final String PASS = "";

    /** Cached persistent database connection instance. */
    //private static Connection conn;

    // =============================
    // Static Initialization Block
    // =============================

    /**
     * Static initializer runs automatically when the class is first loaded.
     * <p>
     * Ensures a database connection is established and tables are verified.
     * </p>
     */
    static {
        try {
            //  Only set JVM timezone
            TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
            System.out.println(" Timezone set to: " + TimeZone.getDefault().getID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================
    // Public Connection Accessor
    // =============================

    /**
     * Returns a synchronized database {@link Connection} to the H2 instance.
     * <p>
     * Automatically reconnects if the existing connection is closed or invalid.
     * Creates required tables and seeds data during the first connection.
     * </p>
     *
     * @return an active {@link Connection} to the H2 database
     */
    public static synchronized Connection getConnection() {
        try {
            String basePath = new File(System.getProperty("user.dir"), "data/restaurantdb").getAbsolutePath();
            String url = "jdbc:h2:file:" + basePath + ";AUTO_SERVER=TRUE;MODE=MySQL";

            Class.forName("org.h2.Driver");

            // ❗ Return a new connection each time instead of reusing a possibly closed one
            Connection conn = DriverManager.getConnection(url, USER, PASS);
            if (conn != null && conn.isValid(2)) {
                initializeTables(conn);
                // System.out.println(" Connected to H2 database at: " + basePath);
            }
            return conn;
        } catch (Exception e) {
            System.err.println(" Failed to connect to H2 DB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /**
     * Checks if the current database connection is valid.
     *
     * @param connection the {@link Connection} to validate
     * @return true if valid; false otherwise
     */
    private static boolean isConnectionValid(Connection connection) {
        try {
            return connection != null && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    // =============================
    // Schema Initialization
    // =============================

    /**
     * Creates all required database tables if they do not already exist
     * and seeds initial data (default users and menu items).
     *
     * @param conn the active {@link Connection} to the database
     */
    private static void initializeTables(Connection conn) {
        try (Statement st = conn.createStatement()) {

            // ===== USERS TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "role VARCHAR(50), " +
                    "phone VARCHAR(20), " +
                    "active BOOLEAN DEFAULT TRUE)");

            // ===== STAFF TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS staff (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "phone VARCHAR(20), " +
                    "role VARCHAR(50) DEFAULT 'server', " +
                    "password VARCHAR(255), " +
                    "active BOOLEAN DEFAULT TRUE)");

            // ===== CUSTOMERS TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "phone VARCHAR(20), " +
                    "active BOOLEAN DEFAULT TRUE)");

            // ===== MENU ITEM TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS MenuItem (" +
                    "id IDENTITY PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "description VARCHAR(255), " +
                    "category VARCHAR(50), " +
                    "price DECIMAL(8,2), " +
                    "available BOOLEAN DEFAULT TRUE, " +
                    "draft BOOLEAN DEFAULT FALSE)");

            // ===== TABLES TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS Tables (" +
                    "id IDENTITY PRIMARY KEY, " +
                    "table_number VARCHAR(50) UNIQUE NOT NULL, " +
                    "capacity INT CHECK (capacity > 0), " +
                    "zone VARCHAR(50), " +
                    "base_price DECIMAL(8,2) CHECK (base_price >= 0), " +
                    "surcharge DECIMAL(8,2) CHECK (surcharge >= 0))");

            // ===== RESERVATIONS TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "customer_name VARCHAR(100), " +
                    "contact VARCHAR(100), " +
                    "table_id INT, " +
                    "date_time TIMESTAMP, " +
                    "party_size INT, " +
                    "status VARCHAR(50) DEFAULT 'confirmed', " +
                    "reference_id VARCHAR(30) UNIQUE, " +
                    "notes TEXT, " +
                    "customer_id INT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

            // ===== BUSINESS HOURS TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS business_hours (" +
                    "day_of_week INT PRIMARY KEY, " +
                    "open_time TIME, " +
                    "close_time TIME)");

            //  Seed business hours if empty
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM business_hours")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    st.execute("INSERT INTO business_hours (day_of_week, open_time, close_time) VALUES " +
                            "(1, '11:00:00', '22:00:00'), " +
                            "(2, '11:00:00', '22:00:00'), " +
                            "(3, '11:00:00', '22:00:00'), " +
                            "(4, '11:00:00', '22:00:00'), " +
                            "(5, '11:00:00', '23:00:00'), " +
                            "(6, '11:00:00', '23:00:00'), " +
                            "(7, '12:00:00', '21:00:00')");
                    System.out.println(" Business hours seeded.");
                }
            }

            // ===== RESERVATION POLICIES TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS reservation_policies (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "policy_type VARCHAR(50) UNIQUE, " +
                    "hours_before INT, " +
                    "description TEXT)");

            //  Seed reservation policies if empty
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM reservation_policies")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    st.execute("INSERT INTO reservation_policies (policy_type, hours_before, description) VALUES " +
                            "('cancellation', 2, 'Reservations must be cancelled at least 2 hours before the scheduled time'), " +
                            "('modification', 2, 'Reservations can be modified up to 2 hours before the scheduled time')");
                    System.out.println(" Reservation policies seeded.");
                }
            }

            // ===== OPTIONAL SAFE FOREIGN KEYS =====
            try {
                st.execute("ALTER TABLE reservations ADD CONSTRAINT IF NOT EXISTS fk_res_table " +
                        "FOREIGN KEY (table_id) REFERENCES Tables(id)");
                st.execute("ALTER TABLE reservations ADD CONSTRAINT IF NOT EXISTS fk_res_customer " +
                        "FOREIGN KEY (customer_id) REFERENCES customers(id)");
            } catch (SQLException ignored) {
                // ignore if already exists
            }

            System.out.println(" Verified: all core and reservation-related tables exist.");

            // ===== SEED DEFAULT USERS & MENU =====
            seedDefaultUsersIfEmpty(st, conn);
            seedMenuItemsIfEmpty(st, conn);

        } catch (SQLException e) {
            System.err.println(" Table initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds default admin, manager, and customer users if the users table is empty.
     *
     * @param st   the active {@link Statement} for executing SQL commands
     * @param conn the active {@link Connection} to the database
     * @throws SQLException if a database error occurs
     */
    private static void seedDefaultUsersIfEmpty(Statement st, Connection conn) throws SQLException {
        try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) {
                System.out.println(" Seeding default admin/staff/customer users...");

                st.addBatch("INSERT INTO users (name,email,password,role,active) VALUES " +
                        "('Admin User','admin@restaurant.com','1234','admin',TRUE)");
                st.addBatch("INSERT INTO users (name,email,password,role,active) VALUES " +
                        "('Manager One','manager@restaurant.com','1234','manager',TRUE)");
                st.addBatch("INSERT INTO users (name,email,password,role,active) VALUES " +
                        "('Customer One','customer@restaurant.com','1234','customer',TRUE)");
                st.executeBatch();

                // Mirror to STAFF and CUSTOMERS tables
                st.execute("INSERT INTO staff (name,email,phone,role,password,active) VALUES " +
                        "('Manager One','manager@restaurant.com','555-1000','manager','1234',TRUE)");
                st.execute("INSERT INTO customers (name,email,password,phone,active) VALUES " +
                        "('Customer One','customer@restaurant.com','1234','555-2000',TRUE)");

                System.out.println(" Default accounts seeded successfully!");
            } else {
                System.out.println(" Users table already contains " + count + " rows.");
            }
        }
    }

    /**
     * Seeds sample MenuItem data if the table is empty.
     *
     * @param st   the active {@link Statement} for executing SQL commands
     * @param conn the active {@link Connection} to the database
     * @throws SQLException if a database error occurs
     */
    private static void seedMenuItemsIfEmpty(Statement st, Connection conn) throws SQLException {
        try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM MenuItem")) {
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) {
                System.out.println(" Seeding default menu items...");

                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Margherita Pizza','Classic tomato, mozzarella, basil','Entree',10.99,TRUE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Pepperoni Pizza','Loaded with pepperoni','Entree',11.99,TRUE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Meat Lovers Pizza','Pepperoni, bacon, sausage, ham','Entree',13.49,TRUE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Hawaiian Pizza','Ham, pineapple, mozzarella','Entree',11.79,FALSE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Mozzarella Sticks','Fried mozzarella with marinara','Appetizer',6.99,TRUE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Loaded Nachos','Chips, cheese, jalapeños, salsa','Appetizer',8.49,TRUE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Buffalo Wings','Spicy wings with ranch dip','Appetizer',9.99,TRUE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Chocolate Lava Cake','Warm cake with molten center','Dessert',6.49,TRUE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Tiramisu','Espresso-soaked layers','Dessert',6.99,TRUE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Coca-Cola','Chilled soft drink','Drink',2.49,TRUE,FALSE)");
                st.addBatch("INSERT INTO MenuItem (name,description,category,price,available,draft) VALUES " +
                        "('Espresso','Strong Italian coffee','Drink',2.99,TRUE,FALSE)");
                st.executeBatch();

                System.out.println(" MenuItem table seeded successfully!");
            } else {
                System.out.println(" MenuItem table already contains " + count + " items.");
            }
        }
    }
}

