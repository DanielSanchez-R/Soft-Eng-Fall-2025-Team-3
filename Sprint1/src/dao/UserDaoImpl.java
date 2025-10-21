package dao;

import model.User;
import model.Admin;
import model.Customer;
import model.Staff;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDaoImpl provides a concrete implementation of the {@link UserDao} interface,
 * handling all CRUD (Create, Read, Update, Delete) operations and authentication queries
 * for {@link User} objects within the system.
 * <p>
 * This class integrates seamlessly with multiple user subclasses including
 * {@link Admin}, {@link Staff}, and {@link Customer}, ensuring that each user type
 * is reconstructed correctly from database records based on their role.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Performs complete CRUD operations on the <b>users</b> database table.</li>
 *   <li>Supports polymorphic mapping for Admin, Staff, and Customer roles.</li>
 *   <li>Implements flexible role recognition for various staff sub-roles (manager, server, kitchen, etc.).</li>
 *   <li>Uses parameterized SQL queries to prevent SQL injection.</li>
 * </ul>
 *
 * <p><b>Database Schema (expected columns):</b></p>
 * <pre>
 * id          INT PRIMARY KEY AUTO_INCREMENT
 * name        VARCHAR
 * email       VARCHAR UNIQUE
 * password    VARCHAR
 * role        VARCHAR
 * phone       VARCHAR (nullable)
 * active      BOOLEAN
 * </pre>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>
 * Connection conn = DBUtil.getConnection();
 * UserDao userDao = new UserDaoImpl(conn);
 *
 * // Add a new staff member
 * Staff staff = new Staff("John Doe", "john@example.com", "575-555-1122", "manager", "securePass123", true);
 * userDao.addUser(staff);
 *
 * // Retrieve user by email
 * User found = userDao.getUserByEmail("john@example.com");
 * System.out.println(found.getRole()); // "manager"
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class UserDaoImpl implements UserDao {

    /** Active JDBC connection used for executing SQL statements. */
    private final Connection conn;

    /**
     * Constructs a new UserDaoImpl with an existing database connection.
     *
     * @param conn an open JDBC {@link Connection} to the database
     */
    public UserDaoImpl(Connection conn) {
        this.conn = conn;
    }

    /**
     * Adds a new {@link User} to the database.
     * <p>
     * Handles all user types (Admin, Staff, Customer) and sets optional
     * fields like phone and active status based on instance type.
     * </p>
     *
     * @param user the {@link User} object to be added
     * @return true if the operation succeeded; false otherwise
     */
    @Override
    public boolean addUser(User user) {
        String sql = "INSERT INTO users(name, email, password, role, phone, active) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());

            if (user instanceof Staff) {
                Staff s = (Staff) user;
                ps.setString(5, s.getPhone());
                ps.setBoolean(6, s.isActive());
            } else if (user instanceof Customer) {
                Customer c = (Customer) user;
                ps.setString(5, c.getPhone());
                ps.setBoolean(6, c.isActive());
            } else {
                ps.setString(5, null);
                ps.setBoolean(6, true);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Failed to add user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a {@link User} by their email address.
     * <p>
     * The lookup is case-insensitive and automatically returns
     * the correct subclass instance based on the user's role.
     * </p>
     *
     * @param email the email address to search for
     * @return a {@link User} instance if found, or null if no match exists
     */
    @Override
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String pass = rs.getString("password");
                    String role = rs.getString("role");
                    String phone = rs.getString("phone") != null ? rs.getString("phone") : "";
                    boolean active = rs.getBoolean("active");

                    // Flexible role recognition and object reconstruction
                    if ("admin".equalsIgnoreCase(role)) {
                        return new Admin(id, name, email, pass);
                    } else if (
                            role.equalsIgnoreCase("staff") ||
                                    role.equalsIgnoreCase("manager") ||
                                    role.equalsIgnoreCase("server") ||
                                    role.equalsIgnoreCase("kitchen")
                    ) {
                        return new Staff(id, name, email, pass, phone, role, active);
                    } else if ("customer".equalsIgnoreCase(role)) {
                        return new Customer(id, name, email, pass, phone);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting user by email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing user record in the database.
     * <p>
     * Supports updating all core and extended fields including
     * phone number, role, and activation status.
     * </p>
     *
     * @param user the {@link User} object containing updated details
     * @return true if the record was successfully updated; false otherwise
     */
    @Override
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name=?, email=?, password=?, role=?, phone=?, active=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());

            if (user instanceof Staff) {
                Staff s = (Staff) user;
                ps.setString(5, s.getPhone());
                ps.setBoolean(6, s.isActive());
            } else if (user instanceof Customer) {
                Customer c = (Customer) user;
                ps.setString(5, c.getPhone());
                ps.setBoolean(6, c.isActive());
            } else {
                ps.setString(5, null);
                ps.setBoolean(6, true);
            }

            ps.setInt(7, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Failed to update user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a user record from the database by ID.
     *
     * @param id the unique identifier of the user to delete
     * @return true if the user was deleted; false otherwise
     */
    @Override
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Failed to delete user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all users from the database.
     * <p>
     * Returns a list containing all user records,
     * automatically instantiating the correct subclass type
     * for each record based on the role column.
     * </p>
     *
     * @return a List of all User objects in the system
     */
    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String pass = rs.getString("password");
                String role = rs.getString("role");
                String phone = rs.getString("phone") != null ? rs.getString("phone") : "";
                boolean active = rs.getBoolean("active");

                if ("admin".equalsIgnoreCase(role)) {
                    list.add(new Admin(id, name, email, pass));
                } else if (
                        role.equalsIgnoreCase("staff") ||
                                role.equalsIgnoreCase("manager") ||
                                role.equalsIgnoreCase("server") ||
                                role.equalsIgnoreCase("kitchen")
                ) {
                    list.add(new Staff(id, name, email, pass, phone, role, active));
                } else if ("customer".equalsIgnoreCase(role)) {
                    list.add(new Customer(id, name, email, pass, phone));
                }
            }

        } catch (SQLException e) {
            System.out.println("Failed to load users: " + e.getMessage());
        }
        return list;
    }
}
