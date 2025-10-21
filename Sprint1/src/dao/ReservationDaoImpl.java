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
 * the reservations table and supports lookup queries by ID and date.
 * It converts between SQL {@link Timestamp} values and Java
 * {@link LocalDateTime} objects for consistent time handling.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Implements complete CRUD operations for reservations.</li>
 *   <li>Maps database rows to {@link Reservation} model objects.</li>
 *   <li>Supports filtering reservations by date.</li>
 *   <li>Uses parameterized queries to prevent SQL injection.</li>
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
 *   status VARCHAR(50)
 * );
 * </pre>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * Connection conn = DBConnection.getConnection();
 * ReservationDao dao = new ReservationDaoImpl(conn);
 *
 * Reservation r = new Reservation(
 *     0, "Alice Johnson", "575-555-8899", 4,
 *     LocalDateTime.of(2025, 10, 25, 19, 30),
 *     4, "confirmed"
 * );
 *
 * dao.addReservation(r);
 * List<Reservation> today = dao.getReservationsByDate("2025-10-25");
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
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
     *
     * @param r the {@link Reservation} object to insert
     * @return true if the reservation was successfully added; false otherwise
     */
    @Override
    public boolean addReservation(Reservation r) {
        String sql = "INSERT INTO reservations (customer_name, contact, table_id, date_time, party_size, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getCustomerName());
            ps.setString(2, r.getContact());
            ps.setInt(3, r.getTableId());
            ps.setTimestamp(4, Timestamp.valueOf(r.getDateTime()));
            ps.setInt(5, r.getPartySize());
            ps.setString(6, r.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing reservation record in the database.
     *
     * @param r the {@link Reservation} object containing updated details
     * @return true if the update succeeded; false otherwise
     */
    @Override
    public boolean updateReservation(Reservation r) {
        String sql = "UPDATE reservations SET customer_name=?, contact=?, table_id=?, date_time=?, party_size=?, status=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getCustomerName());
            ps.setString(2, r.getContact());
            ps.setInt(3, r.getTableId());
            ps.setTimestamp(4, Timestamp.valueOf(r.getDateTime()));
            ps.setInt(5, r.getPartySize());
            ps.setString(6, r.getStatus());
            ps.setInt(7, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a reservation from the database by its unique ID.
     *
     * @param id the reservation’s unique identifier
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
     * @param id the reservation’s unique identifier
     * @return a {@link Reservation} object if found; otherwise null
     */
    @Override
    public Reservation getReservationById(int id) {
        String sql = "SELECT * FROM reservations WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Reservation(
                        rs.getInt("id"),
                        rs.getString("customer_name"),
                        rs.getString("contact"),
                        rs.getInt("table_id"),
                        rs.getTimestamp("date_time").toLocalDateTime(),
                        rs.getInt("party_size"),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
                Reservation r = new Reservation(
                        rs.getInt("id"),
                        rs.getString("customer_name"),
                        rs.getString("contact"),
                        rs.getInt("table_id"),
                        rs.getTimestamp("date_time").toLocalDateTime(),
                        rs.getInt("party_size"),
                        rs.getString("status")
                );
                list.add(r);
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
     * </p>
     *
     * @param date the date string in YYYY-MM-DD format
     * @return a {@link List} of {@link Reservation} objects matching the specified date
     */
    @Override
    public List<Reservation> getReservationsByDate(String date) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE DATE(date_time)=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation(
                        rs.getInt("id"),
                        rs.getString("customer_name"),
                        rs.getString("contact"),
                        rs.getInt("table_id"),
                        rs.getTimestamp("date_time").toLocalDateTime(),
                        rs.getInt("party_size"),
                        rs.getString("status")
                );
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
