package dao;

import model.Staff;
import util.EmailUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * StaffDAO provides data access methods for managing staff accounts
 * in the Pizza 505 ENMU restaurant management system.
 * <p>
 * This class maintains synchronization between the staff and users
 * tables to ensure consistent authentication and authorization data.
 * It also supports email-based onboarding by generating password setup tokens
 * and sending invitations via the {@link util.EmailUtil} service.
 * </p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Perform CRUD operations on staff records.</li>
 *   <li>Keep staff and users tables synchronized for login consistency.</li>
 *   <li>Manage activation/deactivation of staff accounts.</li>
 *   <li>Generate and store secure password reset tokens.</li>
 *   <li>Send staff account invitations via email.</li>
 * </ul>
 *
 * <h3>Database Integration:</h3>
 * <ul>
 *   <li>Uses H2 persistent storage configured in MySQL compatibility mode.</li>
 *   <li>Automatically ensures the password_reset_tokens table exists.</li>
 *   <li>Executes all multi-table updates in transactional mode to maintain integrity.</li>
 * </ul>
 *
 * <p><b>Tables Accessed:</b></p>
 * <ul>
 *   <li>staff</li>
 *   <li>users</li>
 *   <li>password_reset_tokens</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * StaffDAO dao = new StaffDAO();
 * Staff staff = new Staff("John Doe", "john@example.com", "575-555-1234", "manager", "temp123", true);
 * dao.addStaff(staff); // Inserts staff & user + sends email invite
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class StaffDAO {
    /**
     * Constructs a new StaffDAO and verifies connectivity to the H2 database.
     * Also ensures the password_reset_tokens table exists for password setup links.
     */
    public StaffDAO() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                System.out.println("StaffDAO connected to H2.");
                initializeTokenTable(conn); // Ensure token table exists
            }
        } catch (SQLException e) {
            System.out.println("StaffDAO failed to connect.");
            e.printStackTrace();
        }
    }

    /**
     * Establishes a fresh JDBC connection to the persistent H2 database file.
     * <p>
     * The connection uses MySQL compatibility mode and auto-server configuration
     * to enable multi-connection access.
     * </p>
     *
     * @return a new {@link Connection} to the H2 database
     * @throws SQLException if a connection error occurs
     */
    private Connection getConnection() throws SQLException {
        String url = "jdbc:h2:file:C:/Users/danie/Desktop/CS1/apache-tomcat-7.0.109-windows-x64/apache-tomcat-7.0.109/bin/data/restaurantdb;AUTO_SERVER=TRUE;MODE=MySQL";
        return DriverManager.getConnection(url, "sa", "");
    }

    /**
     * Ensures that the password_reset_tokens table exists.
     * <p>
     * This table stores tokens for secure password setup or reset links.
     * </p>
     *
     * @param conn an active database connection
     */
    private void initializeTokenTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS password_reset_tokens (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "email VARCHAR(100) NOT NULL, " +
                "token VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "used BOOLEAN DEFAULT FALSE)";
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
            System.out.println("Verified: 'password_reset_tokens' table exists.");
        } catch (SQLException e) {
            System.out.println("Failed to verify 'password_reset_tokens' table.");
            e.printStackTrace();
        }
    }

    /**
     * Checks whether a staff email already exists in the staff table.
     *
     * @param email the email address to verify
     * @return true if the email exists; false otherwise
     */
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM staff WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error checking email existence.");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds a new staff member to both the staff and users tables,
     * then sends an email invitation with a password setup link.
     * <p>
     * This method runs in transactional mode and automatically rolls back on failure.
     * </p>
     *
     * @param staff the {@link Staff} object to add
     * @return true if the operation succeeds; false otherwise
     */
    public boolean addStaff(Staff staff) {
        String sqlStaff = "INSERT INTO staff (name, email, phone, role, password, active) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlUser = "INSERT INTO users (name, email, password, role, phone, active) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // --- Insert into STAFF + USERS tables ---
            try (PreparedStatement ps1 = conn.prepareStatement(sqlStaff);
                 PreparedStatement ps2 = conn.prepareStatement(sqlUser)) {

                // STAFF
                ps1.setString(1, staff.getName());
                ps1.setString(2, staff.getEmail());
                ps1.setString(3, staff.getPhone());
                ps1.setString(4, staff.getRole());
                ps1.setString(5, staff.getPassword());
                ps1.setBoolean(6, staff.isActive());
                ps1.executeUpdate();

                // USERS
                ps2.setString(1, staff.getName());
                ps2.setString(2, staff.getEmail());
                ps2.setString(3, staff.getPassword());
                ps2.setString(4, staff.getRole());
                ps2.setString(5, staff.getPhone());
                ps2.setBoolean(6, staff.isActive());
                ps2.executeUpdate();

                conn.commit();
                System.out.println(" Added + committed staff to STAFF + USERS: " + staff.getEmail());
            }

            // --- Send email invite in a separate connection ---
            try (Connection mailConn = getConnection()) {
                String token = UUID.randomUUID().toString();
                String insertToken = "INSERT INTO password_reset_tokens (email, token) VALUES (?, ?)";
                try (PreparedStatement pst = mailConn.prepareStatement(insertToken)) {
                    pst.setString(1, staff.getEmail());
                    pst.setString(2, token);
                    pst.executeUpdate();
                }

                String resetLink = "http://localhost:8080/Fall2025Project/auth?action=setPassword&token=" + token;
                String body = "<h2>Welcome to Pizzas 505 ENMU!</h2>"
                        + "<p>Your staff account has been created by the admin.</p>"
                        + "<p><b>Email:</b> " + staff.getEmail() + "</p>"
                        + "<p>To set your password and activate your account, click below:</p>"
                        + "<p><a href='" + resetLink + "' style='background:#c0392b;color:#fff;"
                        + "padding:10px 15px;border-radius:6px;text-decoration:none;'>Set My Password</a></p>"
                        + "<p>This link will expire soon for security purposes.</p>";

                EmailUtil.sendEmail(
                        staff.getEmail(),
                        "Your Pizzas 505 ENMU Staff Account Invite",
                        body
                );
                System.out.println("📧 Invite email sent to " + staff.getEmail());
            } catch (Exception e) {
                System.out.println(" Failed to send invite email (non-blocking).");
                e.printStackTrace();
            }

            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Rolled back addStaff transaction.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Retrieves all staff records from the database.
     *
     * @return a list of {@link Staff} objects representing all staff members
     */
    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT * FROM staff ORDER BY id ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Staff s = new Staff();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setEmail(rs.getString("email"));
                s.setPhone(rs.getString("phone"));
                s.setRole(rs.getString("role"));
                s.setPassword(rs.getString("password"));
                s.setActive(rs.getBoolean("active"));
                staffList.add(s);
            }
            System.out.println("Loaded " + staffList.size() + " staff records.");
        } catch (SQLException e) {
            System.out.println("Failed to retrieve staff list.");
            e.printStackTrace();
        }
        return staffList;
    }

    /**
     * Retrieves a single staff record by ID.
     *
     * @param id the unique staff identifier
     * @return the matching {@link Staff} object, or null if not found
     */
    public Staff getStaffById(int id) {
        String sql = "SELECT * FROM staff WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Staff s = new Staff();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setEmail(rs.getString("email"));
                s.setPhone(rs.getString("phone"));
                s.setRole(rs.getString("role"));
                s.setPassword(rs.getString("password"));
                s.setActive(rs.getBoolean("active"));
                return s;
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve staff by ID.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates staff information in both the staff and users tables.
     * <p>
     * Ensures that both tables remain synchronized by using a single transaction.
     * </p>
     *
     * @param staff the {@link Staff} object containing updated details
     * @return true if the update succeeds; false otherwise
     */
    public boolean updateStaff(Staff staff) {
        String sqlStaff = "UPDATE staff SET name=?, email=?, phone=?, role=? WHERE id=?";
        String sqlUser = "UPDATE users SET name=?, email=?, phone=?, role=? WHERE email=?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement ps1 = conn.prepareStatement(sqlStaff);
                    PreparedStatement ps2 = conn.prepareStatement(sqlUser)
            ) {
                ps1.setString(1, staff.getName());
                ps1.setString(2, staff.getEmail());
                ps1.setString(3, staff.getPhone());
                ps1.setString(4, staff.getRole());
                ps1.setInt(5, staff.getId());
                ps1.executeUpdate();

                ps2.setString(1, staff.getName());
                ps2.setString(2, staff.getEmail());
                ps2.setString(3, staff.getPhone());
                ps2.setString(4, staff.getRole());
                ps2.setString(5, staff.getEmail());
                ps2.executeUpdate();

                conn.commit();
                System.out.println("Updated staff + user: " + staff.getEmail());
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Rolled back updateStaff.");
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deactivates a staff member in both the staff and users tables.
     *
     * @param id the staff member’s unique ID
     * @return true if successfully deactivated; false otherwise
     */
    public boolean deactivateStaff(int id) {
        String sqlStaff = "UPDATE staff SET active = FALSE WHERE id = ?";
        String sqlUser = "UPDATE users SET active = FALSE WHERE email = (SELECT email FROM staff WHERE id = ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement ps1 = conn.prepareStatement(sqlStaff);
                    PreparedStatement ps2 = conn.prepareStatement(sqlUser)
            ) {
                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                conn.commit();
                System.out.println("Deactivated staff & user ID: " + id);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reactivates a staff member in both staff and users tables.
     *
     * @param id the staff member’s unique ID
     * @return true if successfully activated; false otherwise
     */
    public boolean activateStaff(int id) {
        String sqlStaff = "UPDATE staff SET active = TRUE WHERE id = ?";
        String sqlUser = "UPDATE users SET active = TRUE WHERE email = (SELECT email FROM staff WHERE id = ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement ps1 = conn.prepareStatement(sqlStaff);
                    PreparedStatement ps2 = conn.prepareStatement(sqlUser)
            ) {
                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.executeUpdate();

                conn.commit();
                System.out.println("Activated staff & user ID: " + id);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Permanently deletes a staff record from both the staff and users tables.
     *
     * @param id the staff member’s unique ID
     * @return true if deletion succeeds; false otherwise
     */
    public boolean deleteStaff(int id) {
        String sqlStaff = "DELETE FROM staff WHERE id = ?";
        String sqlUser = "DELETE FROM users WHERE email = (SELECT email FROM staff WHERE id = ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement ps2 = conn.prepareStatement(sqlUser);
                    PreparedStatement ps1 = conn.prepareStatement(sqlStaff)
            ) {
                ps2.setInt(1, id);
                ps2.executeUpdate();

                ps1.setInt(1, id);
                ps1.executeUpdate();

                conn.commit();
                System.out.println("🗑️ Deleted staff + user ID: " + id);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates a staff member’s password in both staff and users tables.
     *
     * @param email       the staff member’s email
     * @param newPassword the new hashed password
     * @return {@code true} if the password was successfully updated
     */
    public boolean updatePassword(String email, String newPassword) {
        String sqlStaff = "UPDATE staff SET password=? WHERE email=?";
        String sqlUser = "UPDATE users SET password=? WHERE email=?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement ps1 = conn.prepareStatement(sqlStaff);
                    PreparedStatement ps2 = conn.prepareStatement(sqlUser)
            ) {
                ps1.setString(1, newPassword);
                ps1.setString(2, email);
                ps1.executeUpdate();

                ps2.setString(1, newPassword);
                ps2.setString(2, email);
                ps2.executeUpdate();

                conn.commit();
                System.out.println("Password reset for " + email);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Retrieves a staff member by their email address.
     *
     * @param email the staff email address
     * @return the {@link Staff} object if found; otherwise null
     */
    public Staff getStaffByEmail(String email) {
        String sql = "SELECT * FROM staff WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Staff s = new Staff();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setEmail(rs.getString("email"));
                s.setPhone(rs.getString("phone"));
                s.setRole(rs.getString("role"));
                s.setPassword(rs.getString("password"));
                s.setActive(rs.getBoolean("active"));
                return s;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
