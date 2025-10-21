package dao;

import model.MenuItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuItemDao provides direct data access operations for managing
 * {@link MenuItem} entities in the restaurant booking and ordering system.
 * <p>
 * This class interacts with the MenuItem table in an H2 or
 * MySQL-compatible database and supports full CRUD operations including
 * insertion, updates, deletion, and status toggling.
 * </p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Retrieve all or specific menu items from the database.</li>
 *   <li>Add, update, and delete menu items as part of admin/staff operations.</li>
 *   <li>Toggle item availability in real time for menu accuracy.</li>
 *   <li>Ensure safe data handling and prevent null pointer issues with input.</li>
 * </ul>
 *
 * <h3>Database Schema (expected):</h3>
 * <pre>
 * CREATE TABLE MenuItem (
 *   id INT AUTO_INCREMENT PRIMARY KEY,
 *   name VARCHAR(100) NOT NULL,
 *   description VARCHAR(255),
 *   category VARCHAR(50),
 *   price DECIMAL(8,2),
 *   available BOOLEAN DEFAULT TRUE,
 *   draft BOOLEAN DEFAULT FALSE
 * );
 * </pre>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * Connection conn = DBConnection.getConnection();
 * MenuItemDao dao = new MenuItemDao(conn);
 *
 * MenuItem item = new MenuItem("Cheese Pizza", "Classic mozzarella pizza",
 *                              "Entrée", 10.99, true, false);
 * dao.addMenuItem(item);
 *
 * item.setPrice(11.49);
 * dao.updateMenuItem(item);
 *
 * List<MenuItem> items = dao.getAllMenuItems();
 * items.forEach(i -> System.out.println(i.getName() + " $" + i.getPrice()));
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class MenuItemDao {

    /** Active JDBC connection used for executing all SQL operations. */
    private final Connection conn;

    /**
     * Constructs a new MenuItemDao with the given database connection.
     *
     * @param conn the JDBC {@link Connection} used for executing SQL statements
     */
    public MenuItemDao(Connection conn) {
        this.conn = conn;
    }

    /**
     * Retrieves all menu items from the database ordered by category and name.
     *
     * @return a {@link List} of all {@link MenuItem} records
     * @throws SQLException if a database access error occurs
     */
    public List<MenuItem> getAllMenuItems() throws SQLException {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM MenuItem ORDER BY category, name";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves a single {@link MenuItem} record by its unique ID.
     *
     * @param id the unique identifier of the menu item
     * @return the matching {@link MenuItem}, or null if not found
     * @throws SQLException if a database access error occurs
     */
    public MenuItem getMenuItemById(int id) throws SQLException {
        String sql = "SELECT * FROM MenuItem WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new menu item record into the database.
     *
     * @param item the {@link MenuItem} object containing item details
     * @return true if the insertion succeeded; false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean addMenuItem(MenuItem item) throws SQLException {
        String sql = "INSERT INTO MenuItem (name, description, category, price, available, draft) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, safe(item.getName()));
            ps.setString(2, safe(item.getDescription()));
            ps.setString(3, safe(item.getCategory()));
            ps.setDouble(4, item.getPrice());
            ps.setBoolean(5, item.isAvailable());
            ps.setBoolean(6, item.isDraft());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates an existing menu item record with full editable fields.
     *
     * @param item the {@link MenuItem} containing updated values
     * @return true if the update succeeded; false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean updateMenuItem(MenuItem item) throws SQLException {
        String sql = "UPDATE MenuItem SET name=?, description=?, category=?, price=?, available=?, draft=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, safe(item.getName()));
            ps.setString(2, safe(item.getDescription()));
            ps.setString(3, safe(item.getCategory()));
            ps.setDouble(4, item.getPrice());
            ps.setBoolean(5, item.isAvailable());
            ps.setBoolean(6, item.isDraft());
            ps.setInt(7, item.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Toggles the availability status of a menu item (e.g., mark as available/unavailable).
     *
     * @param id        the unique ID of the menu item
     * @param available the new availability status to set
     * @return true if the toggle succeeded; false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean toggleAvailability(int id, boolean available) throws SQLException {
        String sql = "UPDATE MenuItem SET available=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a menu item record from the database by its ID.
     *
     * @param id the unique identifier of the menu item to delete
     * @return true if the deletion succeeded; false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean deleteMenuItem(int id) throws SQLException {
        String sql = "DELETE FROM MenuItem WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Maps a {@link ResultSet} row to a corresponding {@link MenuItem} object.
     * <p>
     * This helper method is used internally by all retrieval operations.
     * </p>
     *
     * @param rs the current {@link ResultSet} row
     * @return a populated {@link MenuItem} object
     * @throws SQLException if a result set access error occurs
     */
    private MenuItem mapRow(ResultSet rs) throws SQLException {
        MenuItem item = new MenuItem();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setCategory(rs.getString("category"));
        item.setPrice(rs.getDouble("price"));
        item.setAvailable(rs.getBoolean("available"));
        item.setDraft(rs.getBoolean("draft"));
        return item;
    }

    /**
     * Returns a null-safe, trimmed string for database insertion or comparison.
     *
     * @param s the string to sanitize
     * @return a trimmed non-null string (empty if null)
     */
    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}

