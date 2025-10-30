package dao;

import model.MenuItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuItemDaoImpl provides database access methods for performing CRUD
 * (Create, Read, Update, Delete) operations on {@link MenuItem} entities.
 * <p>
 * This class interacts with the {@code MenuItem} database table and converts
 * between SQL data and Java {@link MenuItem} model objects. It ensures that
 * menu items are properly stored, updated, and retrieved for use in both
 * the administrative and customer-facing components of the system.
 * </p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Insert new menu items into the database.</li>
 *   <li>Update existing menu item details such as price or availability.</li>
 *   <li>Delete menu items by ID when removed from the system.</li>
 *   <li>Retrieve specific or all menu items for display or editing.</li>
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
 * MenuItemDaoImpl dao = new MenuItemDaoImpl(conn);
 *
 * MenuItem pizza = new MenuItem("Pepperoni Pizza", "Classic pie with mozzarella", "Entrée", 12.99, true, false);
 * dao.addMenuItem(pizza);
 *
 * List<MenuItem> allItems = dao.getAllMenuItems();
 * allItems.forEach(i -> System.out.println(i.getName() + ": $" + i.getPrice()));
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class MenuItemDaoImpl {

    /** Active JDBC connection to the database. */
    private Connection conn;

    /**
     * Constructs a new MenuItemDaoImpl using the specified database connection.
     *
     * @param conn the active {@link Connection} for executing SQL operations
     */
    public MenuItemDaoImpl(Connection conn) {
        this.conn = conn;
    }

    /**
     * Inserts a new {@link MenuItem} record into the database.
     *
     * @param item the menu item to be added
     * @return true if the operation succeeded; false otherwise
     */
    public boolean addMenuItem(MenuItem item) {
        String sql = "INSERT INTO MenuItem (name, description, category, price, available, draft) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getCategory());
            ps.setDouble(4, item.getPrice());
            ps.setBoolean(5, item.isAvailable());
            ps.setBoolean(6, item.isDraft());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing menu item record in the database.
     *
     * @param item the {@link MenuItem} containing updated details
     * @return true if the update succeeded; false otherwise
     */
    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE MenuItem SET name=?, description=?, category=?, price=?, available=?, draft=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getCategory());
            ps.setDouble(4, item.getPrice());
            ps.setBoolean(5, item.isAvailable());
            ps.setBoolean(6, item.isDraft());
            ps.setInt(7, item.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a menu item record from the database by its ID.
     *
     * @param id the unique ID of the menu item to delete
     * @return true if the deletion was successful; false otherwise
     */
    public boolean deleteMenuItem(int id) {
        String sql = "DELETE FROM MenuItem WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a single menu item by its unique ID.
     *
     * @param id the unique ID of the menu item
     * @return a {@link MenuItem} object if found; otherwise null
     */
    public MenuItem getMenuItemById(int id) {
        String sql = "SELECT * FROM MenuItem WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all menu items from the database, ordered by category and name.
     *
     * @return a list of all {@link MenuItem} objects
     */
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM MenuItem ORDER BY category, name";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                MenuItem item = new MenuItem();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setCategory(rs.getString("category"));
                item.setPrice(rs.getDouble("price"));
                item.setAvailable(rs.getBoolean("available"));
                item.setDraft(rs.getBoolean("draft"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}
