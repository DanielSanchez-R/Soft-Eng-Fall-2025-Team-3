package dao;

import java.sql.Connection;
import java.sql.Statement;

/**
 * InitDB is a database initialization utility for the Pizza 505 ENMU
 * restaurant management system. It creates all required tables (users, menu items,
 * orders, etc.) in the H2 database and seeds them with default data.
 * <p>
 * This class can be executed once to bootstrap a new database instance for
 * development, testing, or first-time deployment. It uses H2’s SQL compatibility
 * features to ensure cross-compatibility with MySQL syntax.
 * </p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Creates core tables: users, MenuItem, Orders, OrderItems.</li>
 *   <li>Seeds default records for admin, staff, and customer accounts.</li>
 *   <li>Seeds sample menu items to verify menu-related CRUD functionality.</li>
 *   <li>Ensures database schema consistency before application startup.</li>
 * </ul>
 *
 * <h3>Database Schema Overview:</h3>
 * <ul>
 *   <li><b>users</b> — Stores admin, manager, staff, and customer credentials.</li>
 *   <li><b>MenuItem</b> — Stores menu data (name, category, availability, etc.).</li>
 *   <li><b>Orders</b> — Tracks customer orders with totals and timestamps.</li>
 *   <li><b>OrderItems</b> — Maps individual ordered items to an order.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>
 * // Run this class once to initialize the H2 database schema
 * InitDB.main(null);
 * </pre>
 *
 * <h3>Example Output:</h3>
 * <pre>
 * H2 schema and sample data successfully initialized!
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class InitDB {

    /**
     * Main entry point for initializing the H2 database schema and seeding default data.
     * <p>
     * Creates all necessary tables if they do not already exist, and populates
     * them with sample records for quick setup and testing.
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            // ===== USERS TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100) UNIQUE, " +
                    "password VARCHAR(100), " +
                    "role VARCHAR(20))");

            // ===== STAFF / CUSTOMERS / ADMINS =====
            st.execute("MERGE INTO users (id, name, email, password, role) KEY(email) VALUES " +
                    "(1,'Admin User','admin@test.com','1234','admin'), " +
                    "(2,'Manager User','manager@test.com','1234','manager'), " +
                    "(3,'Staff User','staff@test.com','1234','staff'), " +
                    "(4,'Customer One','customer@restaurant.com','1234','customer')");

            // ===== MENU ITEM TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS MenuItem (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "description VARCHAR(255), " +
                    "category VARCHAR(50), " +
                    "price DOUBLE, " +
                    "available BOOLEAN, " +
                    "draft BOOLEAN)");

            // ===== SAMPLE MENU SEED =====
            st.execute("MERGE INTO MenuItem (id,name,description,category,price,available,draft) KEY(id) VALUES " +
                    "(1,'Cheese Pizza','Classic mozzarella and tomato sauce','Entree',9.99,true,false), " +
                    "(2,'Pepperoni Pizza','Loaded with pepperoni and cheese','Entree',11.99,true,false), " +
                    "(3,'Garlic Bread','Toasted with garlic butter','Appetizer',4.99,true,false), " +
                    "(4,'Chocolate Cake','Rich slice of chocolate delight','Dessert',5.99,true,false)");

            // ===== ORDERS TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS Orders (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "customer_email VARCHAR(255), " +
                    "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "total DOUBLE)");

            // ===== ORDER ITEMS TABLE =====
            st.execute("CREATE TABLE IF NOT EXISTS OrderItems (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "order_id INT, " +
                    "item_name VARCHAR(255), " +
                    "price DOUBLE, " +
                    "quantity INT, " +
                    "FOREIGN KEY (order_id) REFERENCES Orders(id))");

            System.out.println("✅ H2 schema and sample data successfully initialized!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
