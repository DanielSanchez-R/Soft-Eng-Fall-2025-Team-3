package dao;

import model.TableInfo;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * TableDao provides data access methods for managing restaurant table records
 * in the Pizza 505 ENMU system. It performs all CRUD operations (Create, Read, Update, Delete)
 * on the Tables database table and enforces business constraints for table management.
 * <p>
 * This class is typically used by administrative or staff modules to create,
 * update, list, and delete tables within the restaurant layout configuration.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Automatically creates the Tables table schema if it does not exist.</li>
 *   <li>Enforces validation rules before insertion:
 *       <ul>
 *         <li>Capacity must be greater than 0.</li>
 *         <li>Base price and surcharge must be non-negative.</li>
 *         <li>Table numbers must be unique.</li>
 *       </ul>
 *   </li>
 *   <li>Supports listing all tables sorted by zone and table number.</li>
 *   <li>Provides utility methods for table existence checks.</li>
 *   <li>Includes reservation-aware methods for availability checking.</li>
 * </ul>
 *
 * <h3>Database Schema (Auto-Created):</h3>
 * <pre>
 * CREATE TABLE IF NOT EXISTS Tables (
 *   id IDENTITY PRIMARY KEY,
 *   table_number VARCHAR(50) UNIQUE NOT NULL,
 *   capacity INT CHECK (capacity > 0),
 *   zone VARCHAR(50),
 *   base_price DECIMAL(8,2) CHECK (base_price >= 0),
 *   surcharge DECIMAL(8,2) CHECK (surcharge >= 0)
 * );
 * </pre>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * Connection conn = DBConnection.getConnection();
 * TableDao dao = new TableDao(conn);
 *
 * TableInfo t = new TableInfo("A1", 4, "Main Hall", 25.00, 0.00);
 * dao.addTable(t);
 *
 * List<TableInfo> tables = dao.getAllTables();
 * tables.forEach(tab -> System.out.println(tab.getTableNumber()));
 *
 * // Check availability at a specific time
 * LocalDateTime dinner = LocalDateTime.of(2025, 10, 25, 19, 30);
 * List<TableInfo> available = dao.getAvailableTables(dinner, 4);
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d2
 * @since 2025-10
 */
public class TableDao {

    /** Active JDBC connection to the database. */
    private final Connection conn;

    /**
     * Constructs a new TableDao with an active database connection.
     * <p>
     * Automatically verifies that the Tables table exists and creates
     * it if necessary using {@link #ensureTableExists()}.
     * </p>
     *
     * @param conn the JDBC {@link Connection} to use for database operations
     */
    public TableDao(Connection conn) {
        this.conn = conn;
        ensureTableExists();
    }

    /**
     * Ensures that the Tables schema exists with proper constraints.
     * <p>
     * If the table does not already exist, it is created automatically with
     * validation constraints for capacity, base price, and surcharge.
     * This method is called automatically during TableDao construction.
     * </p>
     * <p>
     * <strong>Constraints Created:</strong>
     * <ul>
     *   <li>capacity must be > 0</li>
     *   <li>base_price must be >= 0</li>
     *   <li>surcharge must be >= 0</li>
     *   <li>table_number must be unique</li>
     * </ul>
     * </p>
     */
    private void ensureTableExists() {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS Tables (" +
                    "id IDENTITY PRIMARY KEY, " +
                    "table_number VARCHAR(50) UNIQUE NOT NULL, " +
                    "capacity INT CHECK (capacity > 0), " +
                    "zone VARCHAR(50), " +
                    "base_price DECIMAL(8,2) CHECK (base_price >= 0), " +
                    "surcharge DECIMAL(8,2) CHECK (surcharge >= 0)" +
                    ")");
            System.out.println("✅ Verified: Tables table exists.");
        } catch (SQLException e) {
            System.err.println("❌ Failed to verify/create Tables table: " + e.getMessage());
        }
    }

    /**
     * Inserts a new table record into the Tables database.
     * <p>
     * Validates input constraints before insertion:
     * <ul>
     *   <li>Capacity must be greater than 0.</li>
     *   <li>Base price and surcharge must be non-negative.</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Note:</strong> The database enforces unique table_number constraint.
     * If a duplicate table number is provided, this method will throw a SQLException.
     * </p>
     *
     * @param table the {@link TableInfo} object containing the table details
     * @throws SQLException if a database access error occurs
     * @throws IllegalArgumentException if capacity or pricing values are invalid
     */
    public void addTable(TableInfo table) throws SQLException {
        if (table.getCapacity() <= 0)
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        if (table.getBasePrice() < 0 || table.getSurcharge() < 0)
            throw new IllegalArgumentException("Pricing values must be >= 0.");

        String sql = "INSERT INTO Tables (table_number, capacity, zone, base_price, surcharge) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table.getTableNumber());
            ps.setInt(2, table.getCapacity());
            ps.setString(3, table.getZone());
            ps.setDouble(4, table.getBasePrice());
            ps.setDouble(5, table.getSurcharge());
            ps.executeUpdate();
        }
    }

    /**
     * Retrieves all tables from the database, sorted by zone and table number.
     * <p>
     * This method returns all table records regardless of their availability
     * status. For availability-aware queries, use
     * {@link #getAvailableTables(LocalDateTime, int)}.
     * </p>
     *
     * @return a list of {@link TableInfo} objects representing all tables
     * @throws SQLException if a database access error occurs
     */
    public List<TableInfo> getAllTables() throws SQLException {
        List<TableInfo> list = new ArrayList<>();
        String sql = "SELECT * FROM Tables ORDER BY zone, table_number";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                TableInfo t = new TableInfo();
                t.setId(rs.getInt("id"));
                t.setTableNumber(rs.getString("table_number"));
                t.setCapacity(rs.getInt("capacity"));
                t.setZone(rs.getString("zone"));
                t.setBasePrice(rs.getDouble("base_price"));
                t.setSurcharge(rs.getDouble("surcharge"));
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Deletes a table record by its unique ID.
     * <p>
     * <strong>Warning:</strong> This operation will fail if there are
     * existing reservations referencing this table due to foreign key
     * constraints. Consider implementing soft-delete or archive functionality
     * for production use.
     * </p>
     *
     * @param id the unique table identifier
     * @throws SQLException if a database access error occurs or foreign key constraint violated
     */
    public void deleteTable(int id) throws SQLException {
        String sql = "DELETE FROM Tables WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Checks whether a given table number already exists in the database.
     * <p>
     * This method is useful for validation before creating new tables
     * to provide user-friendly error messages instead of database exceptions.
     * </p>
     *
     * @param tableNumber the unique table identifier (e.g., "A1", "B2")
     * @return true if the table number exists; false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean tableExists(String tableNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Tables WHERE table_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Gets all tables available at a specific date/time for a given party size.
     * <p>
     * This method returns tables that:
     * <ul>
     *   <li>Have sufficient capacity for the party size</li>
     *   <li>Are not already reserved at the specified date/time</li>
     *   <li>Do not have active reservations (excludes 'cancelled' and 'no-show')</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Use Case:</strong> Display available tables to customers when
     * they are creating a reservation for a specific time and party size.
     * </p>
     *
     * @param dateTime  the date and time to check availability
     * @param partySize the minimum capacity required
     * @return a list of available {@link TableInfo} objects sorted by zone and number
     * @throws SQLException if a database access error occurs
     */
    public List<TableInfo> getAvailableTables(LocalDateTime dateTime, int partySize) throws SQLException {
        List<TableInfo> available = new ArrayList<>();
        String sql = "SELECT t.* FROM Tables t " +
                "WHERE t.capacity >= ? " +
                "AND t.id NOT IN (" +
                "  SELECT table_id FROM reservations " +
                "  WHERE date_time = ? " +
                "  AND status NOT IN ('cancelled', 'no-show')" +
                ") ORDER BY t.zone, t.table_number";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partySize);
            ps.setTimestamp(2, Timestamp.valueOf(dateTime));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TableInfo t = new TableInfo();
                t.setId(rs.getInt("id"));
                t.setTableNumber(rs.getString("table_number"));
                t.setCapacity(rs.getInt("capacity"));
                t.setZone(rs.getString("zone"));
                t.setBasePrice(rs.getDouble("base_price"));
                t.setSurcharge(rs.getDouble("surcharge"));
                available.add(t);
            }
        }
        return available;
    }

    /**
     * Gets a map of table availability (table ID → available boolean) for a specific time.
     * <p>
     * This method is useful for rendering visual table layouts where you need
     * to show the availability status of all tables at once.
     * </p>
     * <p>
     * <strong>Map Structure:</strong>
     * <ul>
     *   <li>Key: Table ID (Integer)</li>
     *   <li>Value: true if available, false if booked</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Algorithm:</strong>
     * <ol>
     *   <li>Initialize all tables as available (true)</li>
     *   <li>Query active reservations for the specified time</li>
     *   <li>Mark booked tables as unavailable (false)</li>
     * </ol>
     * </p>
     *
     * @param dateTime the date and time to check availability
     * @return a map where keys are table IDs and values indicate availability
     * @throws SQLException if a database access error occurs
     */
    public Map<Integer, Boolean> getTableAvailabilityMap(LocalDateTime dateTime) throws SQLException {
        Map<Integer, Boolean> map = new HashMap<>();

        // Initialize all tables as available
        List<TableInfo> allTables = getAllTables();
        for (TableInfo t : allTables) {
            map.put(t.getId(), true);
        }

        // Mark booked tables as unavailable
        String sql = "SELECT DISTINCT table_id FROM reservations " +
                "WHERE date_time = ? AND status NOT IN ('cancelled', 'no-show')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dateTime));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                map.put(rs.getInt("table_id"), false);
            }
        }
        return map;
    }

    /**
     * Retrieves a single table by its unique ID.
     * <p>
     * This method is useful when you need full table details for a specific
     * table, such as during reservation creation or modification.
     * </p>
     *
     * @param id the unique table identifier
     * @return a {@link TableInfo} object if found; null otherwise
     * @throws SQLException if a database access error occurs
     */
    public TableInfo getTableById(int id) throws SQLException {
        String sql = "SELECT * FROM Tables WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                TableInfo t = new TableInfo();
                t.setId(rs.getInt("id"));
                t.setTableNumber(rs.getString("table_number"));
                t.setCapacity(rs.getInt("capacity"));
                t.setZone(rs.getString("zone"));
                t.setBasePrice(rs.getDouble("base_price"));
                t.setSurcharge(rs.getDouble("surcharge"));
                return t;
            }
        }
        return null;
    }
}