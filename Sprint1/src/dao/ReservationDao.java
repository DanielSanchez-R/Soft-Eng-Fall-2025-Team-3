package dao;

import model.Reservation;
import java.util.List;

/**
 * ReservationDao defines the contract for all database operations
 * related to {@link Reservation} entities in the restaurant booking system.
 * <p>
 * This interface abstracts the CRUD (Create, Read, Update, Delete) operations
 * and lookup queries for reservations, allowing different implementations
 * (e.g., H2, MySQL) to be used interchangeably.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Add new reservations to the database.</li>
 *   <li>Update existing reservation records (date, party size, status, etc.).</li>
 *   <li>Delete reservations when cancelled or expired.</li>
 *   <li>Retrieve reservations by ID, all records, or by a specific date.</li>
 * </ul>
 *
 * <h3>Typical Use Cases:</h3>
 * <ul>
 *   <li>Customer booking and confirmation process.</li>
 *   <li>Staff console for viewing and managing active reservations.</li>
 *   <li>Admin reporting and analytics based on reservation data.</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>
 * ReservationDao dao = new ReservationDaoImpl(DBConnection.getConnection());
 *
 * Reservation r = new Reservation(
 *     0, "Alice Johnson", "575-555-8899", 4,
 *     LocalDateTime.of(2025, 10, 25, 19, 30),
 *     4, "confirmed"
 * );
 * dao.addReservation(r);
 * List<Reservation> todaysBookings = dao.getReservationsByDate("2025-10-25");
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public interface ReservationDao {

    /**
     * Inserts a new {@link Reservation} into the database.
     *
     * @param reservation the reservation object to add
     * @return true if the operation succeeded; false otherwise
     */
    boolean addReservation(Reservation reservation);

    /**
     * Updates an existing reservation record.
     *
     * @param reservation the reservation object containing updated data
     * @return true if the update succeeded; false otherwise
     */
    boolean updateReservation(Reservation reservation);

    /**
     * Deletes a reservation from the database by its unique ID.
     *
     * @param id the reservation’s unique identifier
     * @return true if the reservation was deleted; false otherwise
     */
    boolean deleteReservation(int id);

    /**
     * Retrieves a reservation record by its unique ID.
     *
     * @param id the reservation’s unique identifier
     * @return the matching {@link Reservation}, or null if not found
     */
    Reservation getReservationById(int id);

    /**
     * Retrieves all reservations from the database.
     *
     * @return a {@link List} of all {@link Reservation} objects
     */
    List<Reservation> getAllReservations();

    /**
     * Retrieves all reservations for a specific date.
     * <p>
     * The date must be in the format YYYY-MM-DD
     * </p>
     *
     * @param date the reservation date to filter by
     * @return a {@link List} of {@link Reservation} objects matching the date
     */
    List<Reservation> getReservationsByDate(String date);
}

