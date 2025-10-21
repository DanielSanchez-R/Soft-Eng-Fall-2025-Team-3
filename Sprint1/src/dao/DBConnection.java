package dao;

import java.io.File;
import java.sql.*;

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
 *   <li><b>Options:</b> AUTO_SERVER=TRUE, MODE=MySQL</li>
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
 * @version 1.d1
 * @since 2025-10
 */
public class DBConnection {

    /** Default database username. */
    private static final String USER = "sa";

    /** Default database password (blank for H2). */
    private static final String PASS = "";

    /** Cached persistent database connection instance. */
    private static Connection conn;

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
            getConnection(); // triggers initialization
            System.out.println("✅ H2 DBConnection initialized and verified.");
        } catch (Exception e) {
            System.err.println("❌ DBConnection static init failed: " + e.getMessage());
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
            // Construct absolute DB path under /data directory
            String basePath = new File(System.getProperty("user.dir"), "data/restaurantdb").getAbsolutePath();
            String url = "jdbc:h2:file:" + basePath + ";AUTO_SERVER=TRUE;MODE=MySQL";

            // Reconnect if connection is null or invalid
            if (conn == null || conn.isClosed() || !isConnectionValid(conn)) {
                Class.forName("org.h2.Driver");
                conn = DriverManager.getConnection(url, USER, PASS);
                initializeTables(conn);
                System.out.println("✅ Connected to H2 database at: " + basePath + ".mv.db");
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to connect to H2 database!");
            e.printStackTrace();
        }
        return conn;
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

            // USERS TABLE
            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "role VARCHAR(50), " +
                    "phone VARCHAR(20), " +
                    "active BOOLEAN DEFAULT TRUE)");

            // STAFF TABLE
            st.execute("CREATE TABLE IF NOT EXISTS staff (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "phone VARCHAR(20), " +
                    "role VARCHAR(50) DEFAULT 'server', " +
                    "password VARCHAR(255), " +
                    "active BOOLEAN DEFAULT TRUE)");

            // CUSTOMERS TABLE
            st.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "phone VARCHAR(20), " +
                    "active BOOLEAN DEFAULT TRUE)");

            // MENU ITEM TABLE
            st.execute("CREATE TABLE IF NOT EXISTS MenuItem (" +
                    "id IDENTITY PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "description VARCHAR(255), " +
                    "category VARCHAR(50), " +
                    "price DECIMAL(8,2), " +
                    "available BOOLEAN DEFAULT TRUE, " +
                    "draft BOOLEAN DEFAULT FALSE)");

            System.out.println("✅ Verified: users, staff, customers, tokens, menuitem tables.");

            // Seed defaults if empty
            seedDefaultUsersIfEmpty(st, conn);
            seedMenuItemsIfEmpty(st, conn);

        } catch (SQLException e) {
            System.err.println("❌ Table initialization failed: " + e.getMessage());
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
                System.out.println("🌱 Seeding default admin/staff/customer users...");

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

                System.out.println("✅ Default accounts seeded successfully!");
            } else {
                System.out.println("ℹ️ Users table already contains " + count + " rows.");
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
                System.out.println("🌱 Seeding default menu items...");

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

                System.out.println("✅ MenuItem table seeded successfully!");
            } else {
                System.out.println("ℹ️ MenuItem table already contains " + count + " items.");
            }
        }
    }
}
