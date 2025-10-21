package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * TestDB is a simple utility class used to verify database
 * connectivity and data retrieval for the restaurant booking system.
 * <p>
 * It executes a basic SELECT * FROM users query and prints
 * the name and role of each user to the console, confirming that:
 * </p>
 * <ul>
 *   <li>The database connection is properly configured.</li>
 *   <li>The users table exists and contains valid data.</li>
 *   <li>JDBC operations (Connection, Statement, ResultSet) are functional.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>
 * // Run this class independently to verify database setup
 * // before starting the full web application.
 * TestDB.main(null);
 * </pre>
 *
 * <p>This class is typically used for debugging or environment setup
 * validation during early development (e.g., verifying H2 or MySQL connections).</p>
 *
 * <p><b>Expected Output Example:</b></p>
 * <pre>
 * Alice Johnson → admin
 * Bob Smith → staff
 * Carol Martinez → customer
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class TestDB {

    /**
     * Entry point for database connectivity testing.
     * <p>
     * Establishes a connection using {@link DBConnection#getConnection()},
     * executes a query to fetch all records from the users table,
     * and prints the results to the console.
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                System.out.println(rs.getString("name") + " → " + rs.getString("role"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
