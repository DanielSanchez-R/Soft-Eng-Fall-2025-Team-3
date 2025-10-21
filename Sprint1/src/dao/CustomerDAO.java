package dao;

import model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerDAO provides database access methods for managing
 * {@link Customer} records in the Pizza 505 ENMU restaurant system.
 * <p>
 * This DAO safely performs CRUD operations (Create, Read, Update)
 * on the customers table while maintaining synchronization
 * with the shared users table used for authentication.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Establishes persistent H2 database connections via {@link DBConnection}.</li>
 *   <li>Automatically creates the customers table if missing.</li>
 *   <li>Mirrors new and updated customers into the users table.</li>
 *   <li>Auto-reconnects if the database connection is lost.</li>
 *   <li>Supports customer registration, password updates, and retrieval.</li>
 * </ul>
 *
 * <h3>Database Schema (expected):</h3>
 * <pre>
 * CREATE TABLE customers (
 *   id INT AUTO_INCREMENT PRIMARY KEY,
 *   name VARCHAR(100),
 *   email VARCHAR(100) UNIQUE,
 *   password VARCHAR(255),
 *   phone VARCHAR(20),
 *   active BOOLEAN DEFAULT TRUE
 * );
 * </pre>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * CustomerDAO dao = new CustomerDAO();
 * Customer c = new Customer("Alice Johnson", "alice@example.com", "pass123", "575-555-8899");
 * dao.addCustomer(c);
 *
 * Customer found = dao.getByEmail("alice@example.com");
 * System.out.println(found.getName()); // Alice Johnson
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class CustomerDAO {

    /**
     * Retrieves a live database connection from {@link DBConnection}.
     * <p>
     * If the connection is closed or lost, a new one is automatically
     * opened to ensure reliability.
     * </p>
     *
     * @return a valid {@link Connection} to the H2 database
     * @throws SQLException if a connection error occurs
     */
    private Connection getConnection() throws SQLException {
        Connection conn = DBConnection.getConnection();
        if (conn == null || conn.isClosed()) {
            System.out.println("Reopening lost H2 connection for CustomerDAO...");
            conn = DBConnection.getConnection();
        }
        return conn;
    }

    /**
     * Constructs a new CustomerDAO instance and verifies
     * database connectivity.
     */
    public CustomerDAO() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("CustomerDAO connected to H2 (live connection).");
            }
        } catch (SQLException e) {
            System.out.println("Failed to connect CustomerDAO.");
            e.printStackTrace();
        }
    }

    /**
     * Ensures that the customers table exists in the database.
     * <p>
     * Called automatically before performing customer insert operations.
     * </p>
     */
    public void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS customers (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "email VARCHAR(100) UNIQUE, " +
                "password VARCHAR(255), " +
                "phone VARCHAR(20), " +
                "active BOOLEAN DEFAULT TRUE" +
                ")";
        try (Statement st = getConnection().createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new {@link Customer} record to the database and mirrors it
     * into the users table for authentication.
     *
     * @param c the {@link Customer} to add
     * @return true if both inserts succeeded; false otherwise
     */
    public boolean addCustomer(Customer c) {
        ensureTable();
        String insertCustomer = "INSERT INTO customers (name,email,password,phone,active) VALUES (?,?,?,?,?)";
        String insertUser = "INSERT INTO users (name,email,password,role,phone,active) VALUES (?,?,?,?,?,?)";

        try (Connection conn = getConnection();
             PreparedStatement psCustomer = conn.prepareStatement(insertCustomer);
             PreparedStatement psUser = conn.prepareStatement(insertUser)) {

            // Insert into CUSTOMERS
            psCustomer.setString(1, c.getName());
            psCustomer.setString(2, c.getEmail());
            psCustomer.setString(3, c.getPassword());
            psCustomer.setString(4, c.getPhone());
            psCustomer.setBoolean(5, c.isActive());
            psCustomer.executeUpdate();

            // Mirror into USERS
            psUser.setString(1, c.getName());
            psUser.setString(2, c.getEmail());
            psUser.setString(3, c.getPassword());
            psUser.setString(4, "customer");
            psUser.setString(5, c.getPhone());
            psUser.setBoolean(6, c.isActive());
            psUser.executeUpdate();

            System.out.println("Customer added to CUSTOMERS + mirrored to USERS: " + c.getEmail());
            return true;

        } catch (SQLException e) {
            System.out.println("Error adding customer");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks whether a given email address is already registered
     * in the customers table.
     *
     * @param email the email address to check
     * @return true if the email exists; false otherwise
     */
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM customers WHERE email=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves a {@link Customer} record by their email address.
     *
     * @param email the customer’s email
     * @return the matching {@link Customer}, or null if not found
     */
    public Customer getByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            Customer customer = null;
            if (rs.next()) {
                customer = new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("phone")
                );
            }
            rs.close();
            return customer;
        } catch (SQLException e) {
            System.out.println("Error retrieving customer by email");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates the password for a given customer and mirrors the change
     * in the users table for consistency.
     *
     * @param email       the customer’s email address
     * @param newPassword the new (hashed) password
     * @return true if the password was successfully updated in either table
     */
    public boolean updatePassword(String email, String newPassword) {
        String sql1 = "UPDATE customers SET password=? WHERE email=?";
        String sql2 = "UPDATE users SET password=? WHERE email=? AND role='customer'";
        try (Connection conn = getConnection();
             PreparedStatement ps1 = conn.prepareStatement(sql1);
             PreparedStatement ps2 = conn.prepareStatement(sql2)) {

            ps1.setString(1, newPassword);
            ps1.setString(2, email);
            int c1 = ps1.executeUpdate();

            ps2.setString(1, newPassword);
            ps2.setString(2, email);
            int c2 = ps2.executeUpdate();

            return (c1 > 0 || c2 > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all customers from the customers table.
     * <p>
     * Typically used by admin dashboards for account management.
     * </p>
     *
     * @return a {@link List} of all {@link Customer} objects
     */
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY id ASC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer c = new Customer();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setEmail(rs.getString("email"));
                c.setPassword(rs.getString("password"));
                c.setPhone(rs.getString("phone"));
                c.setActive(rs.getBoolean("active"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
