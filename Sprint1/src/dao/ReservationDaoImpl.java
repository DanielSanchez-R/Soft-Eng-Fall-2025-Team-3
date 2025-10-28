package dao;

import model.Reservation;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ReservationDaoImpl provides a concrete implementation of the
 * {@link ReservationDao} interface for managing reservation records in the
 * restaurant booking system.
 * <p>
 * This DAO performs all CRUD (Create, Read, Update, Delete) operations on
 * the reservations table and supports lookup queries by ID, reference ID,
 * customer ID, and date. It converts between SQL {@link Timestamp} values
 * and Java {@link LocalDateTime} objects for consistent time handling.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Implements complete CRUD operations for reservations.</li>
 *   <li>Maps database rows to {@link Reservation} model objects.</li>
 *   <li>Supports filtering reservations by date, customer, and reference ID.</li>
 *   <li>Uses parameterized queries to prevent SQL injection.</li>
 *   <li>Handles new fields: referenceId, notes, customerId, timestamps.</li>
 *   <li>Provides status updates and reservation reassignment capabilities.</li>
 * </ul>
 *
 * <h3>Database Schema (expected):</h3>
 * <pre>
 * CREATE TABLE reservations (
 *   id INT AUTO_INCREMENT PRIMARY KEY,
 *   customer_name VARCHAR(100),
 *   contact VARCHAR(100),
 *   table_id INT,
 *   date_time TIMESTAMP,
 *   party_size INT,
 *   status VARCHAR(50) DEFAULT 'confirmed',
 *   reference_id VARCHAR(20) UNIQUE,
 *   notes TEXT,
 *   customer_id INT,
 *   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *   modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   FOREIGN KEY (table_id) REFERENCES Tables(id),
 *   FOREIGN KEY (customer_id) REFERENCES customers(id)
 * );
 * </pre>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * Connection conn = DBConnection.getConnection();
 * ReservationDaoImpl dao = new ReservationDaoImpl(conn);
 *
 * Reservation r = new Reservation();
 * r.setCustomerName("Alice Johnson");
 * r.setContact("575-555-8899");
 * r.setTableId(4);
 * r.setDateTime(LocalDateTime.of(2025, 10, 25, 19, 30));
 * r.setPartySize(4);
 * r.setStatus("confirmed");
 * r.setReferenceId("RES172987654321");
 *
 * dao.addReservation(r);
 * List<Reservation> today = dao.getReservationsByDate("2025-10-25");
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d2
 * @since 2025-10
 */
@SuppressWarnings("unused")
public class ReservationDaoImpl implements ReservationDao {

    /** Active JDBC connection for executing SQL operations. */
    private Connection conn;

    /**
     * Constructs a new ReservationDaoImpl with the provided
     * database connection.
     *
     * @param conn the active {@link Connection} to the database
     */
    public ReservationDaoImpl(Connection conn) {
        this.conn = conn;
    }

    /**
     * Inserts a new reservation into the reservations table.
     * <p>
     * This method handles all fields including the new reference ID,
     * notes, and customer ID. Timestamps are automatically set by
     * the database.
     * </p>
     *
     * @param r the {@link Reservation} object to insert
     * @return true if the reservation was successfully added; false otherwise
     */
    @Override
    public boolean addReservation(Reservation r) {
        String sql = "INSERT INTO reservations (customer_name, contact, table_id, date_time, " +
                "party_size, status, reference_id, notes, customer_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getCustomerName());
            ps.setString(2, r.getContact());
            ps.setInt(3, r.getTableId());
            ps.setTimestamp(4, Timestamp.valueOf(r.getDateTime()));
            ps.setInt(5, r.getPartySize());
            ps.setString(6, r.getStatus());
            ps.setString(7, r.getReferenceId());
            ps.setString(8, r.getNotes());

            // Handle nullable customer_id
            if (r.getCustomerId() != null) {
                ps.setInt(9, r.getCustomerId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing reservation record in the database.
     * <p>
     * This method updates all modifiable fields and automatically
     * updates the modified_at timestamp via database trigger.
     * </p>
     *
     * @param r the {@link Reservation} object containing updated details
     * @return true if the update succeeded; false otherwise
     */
    @Override
    public boolean updateReservation(Reservation r) {
        String sql = "UPDATE reservations SET customer_name=?, contact=?, table_id=?, " +
                "date_time=?, party_size=?, status=?, notes=? WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getCustomerName());
            ps.setString(2, r.getContact());
            ps.setInt(3, r.getTableId());
            ps.setTimestamp(4, Timestamp.valueOf(r.getDateTime()));
            ps.setInt(5, r.getPartySize());
            ps.setString(6, r.getStatus());
            ps.setString(7, r.getNotes());
            ps.setInt(8, r.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a reservation from the database by its unique ID.
     *
     * @param id the reservation's unique identifier
     * @return true if the record was deleted; false otherwise
     */
    @Override
    public boolean deleteReservation(int id) {
        String sql = "DELETE FROM reservations WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a single reservation by its unique ID.
     *
     * @param id the reservation's unique identifier
     * @return a {@link Reservation} object if found; otherwise null
     */
    @Override
    public Reservation getReservationById(int id) {
        String sql = "SELECT * FROM reservations WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapReservation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a reservation by its unique reference ID.
     * <p>
     * This method is used for customer lookups when they want to
     * view, modify, or cancel their reservation using the confirmation
     * number provided at booking.
     * </p>
     *
     * @param referenceId the unique booking reference ID (e.g., "RES172987654321")
     * @return a {@link Reservation} object if found; otherwise null
     */
    public Reservation getByReferenceId(String referenceId) {
        String sql = "SELECT * FROM reservations WHERE reference_id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapReservation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all reservations for a specific authenticated customer.
     * <p>
     * Results are ordered by date/time in descending order (most recent first).
     * This is used to display a customer's reservation history.
     * </p>
     *
     * @param customerId the ID of the authenticated customer
     * @return a {@link List} of {@link Reservation} objects for this customer
     */
    public List<Reservation> getByCustomerId(int customerId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE customer_id=? ORDER BY date_time DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves all reservations sorted by date and time in ascending order.
     *
     * @return a {@link List} of all {@link Reservation} objects
     */
    @Override
    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY date_time ASC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves all reservations for a specific date.
     * <p>
     * Filters records based on the date portion of the date_time field.
     * Useful for staff to view all bookings for a particular day.
     * </p>
     *
     * @param date the date string in YYYY-MM-DD format
     * @return a {@link List} of {@link Reservation} objects matching the specified date
     */
    @Override
    public List<Reservation> getReservationsByDate(String date) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE DATE(date_time)=? ORDER BY date_time ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Updates only the status field of a reservation.
     * <p>
     * This is used by staff to quickly mark reservations as seated,
     * cancelled, or no-show without modifying other fields.
     * The modified_at timestamp is automatically updated.
     * </p>
     *
     * @param id     the reservation's unique identifier
     * @param status the new status value (confirmed, seated, cancelled, no-show)
     * @return true if the update succeeded; false otherwise
     */
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE reservations SET status=?, modified_at=CURRENT_TIMESTAMP WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Reassigns a reservation to a new table and/or time.
     * <p>
     * This method is used by staff to move reservations when handling
     * conflicts or accommodating customer requests. It updates both
     * the table_id and date_time fields, as well as the modified_at timestamp.
     * </p>
     * <p>
     * <strong>Important:</strong> This method does NOT check for conflicts.
     * The caller should use {@link util.ReservationService#hasConflict}
     * before calling this method.
     * </p>
     *
     * @param reservationId the ID of the reservation to reassign
     * @param newTableId    the ID of the new table
     * @param newDateTime   the new date and time for the reservation
     * @return true if the reassignment succeeded; false otherwise
     */
    public boolean reassignReservation(int reservationId, int newTableId, LocalDateTime newDateTime) {
        String sql = "UPDATE reservations SET table_id=?, date_time=?, modified_at=CURRENT_TIMESTAMP WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newTableId);
            ps.setTimestamp(2, Timestamp.valueOf(newDateTime));
            ps.setInt(3, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Maps a {@link ResultSet} row to a {@link Reservation} object.
     * <p>
     * This helper method is used internally by all retrieval operations
     * to construct Reservation objects from database rows. It handles
     * all fields including nullable values and timestamps.
     * </p>
     *
     * @param rs the current {@link ResultSet} row positioned on a reservation record
     * @return a fully populated {@link Reservation} object
     * @throws SQLException if a result set access error occurs
     */
    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setCustomerName(rs.getString("customer_name"));
        r.setContact(rs.getString("contact"));
        r.setTableId(rs.getInt("table_id"));
        r.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
        r.setPartySize(rs.getInt("party_size"));
        r.setStatus(rs.getString("status"));
        r.setReferenceId(rs.getString("reference_id"));
        r.setNotes(rs.getString("notes"));

        // Handle nullable customer_id
        int customerId = rs.getInt("customer_id");
        r.setCustomerId(rs.wasNull() ? null : customerId);

        // Handle timestamps (may be null)
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            r.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp modifiedTimestamp = rs.getTimestamp("modified_at");
        if (modifiedTimestamp != null) {
            r.setModifiedAt(modifiedTimestamp.toLocalDateTime());
        }

        return r;
    }
}