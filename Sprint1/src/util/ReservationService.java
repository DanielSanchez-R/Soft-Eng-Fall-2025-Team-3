package util;

import dao.DBConnection;
import model.Reservation;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ReservationService provides business logic and validation utilities
 * for reservation operations in the Pizza 505 ENMU restaurant system.
 * <p>
 * This service layer sits between controllers and DAOs, implementing
 * business rules such as:
 * <ul>
 *   <li>Business hours validation</li>
 *   <li>Party size vs table capacity checks</li>
 *   <li>Cancellation policy enforcement</li>
 *   <li>Conflict detection</li>
 *   <li>Pricing calculation</li>
 *   <li>Reference ID generation</li>
 * </ul>
 * </p>
 *
 * <h3>Design Pattern:</h3>
 * <p>
 * This class follows the Service Layer pattern, encapsulating business
 * logic separately from data access (DAO) and presentation (controllers).
 * All methods are static for convenient access across the application.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * // Validate before creating reservation
 * LocalDateTime dateTime = LocalDateTime.of(2025, 10, 25, 19, 30);
 *
 * if (!ReservationService.validateBusinessHours(dateTime)) {
 *     // Show error: outside business hours
 * }
 *
 * if (!ReservationService.validatePartySize(tableId, 6)) {
 *     // Show error: party too large for table
 * }
 *
 * if (ReservationService.hasConflict(tableId, dateTime, 0)) {
 *     // Show error: table already booked
 * }
 *
 * // Generate unique reference
 * String refId = ReservationService.generateReferenceId();
 *
 * // Calculate pricing
 * double price = ReservationService.calculatePricing(tableId);
 * </pre>
 *
 * @author Anthony Marrs
 * @version 1.d2
 * @since 2025-10
 */
public class ReservationService {

    /**
     * Generates a unique reservation reference ID.
     * <p>
     * The reference ID is constructed using the prefix "RES" followed by
     * the current timestamp in milliseconds and a random 3-digit number.
     * This format ensures uniqueness while remaining human-readable.
     * </p>
     * <p>
     * <strong>Format:</strong> RES{timestamp}{random}
     * <br>
     * <strong>Example:</strong> RES172987654321456
     * </p>
     *
     * @return a unique reference ID string that can be used for customer lookups
     */
    public static String generateReferenceId() {
        return "RES" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Validates if the given date/time falls within business hours.
     * <p>
     * This method queries the business_hours table to determine
     * the operating hours for the day of the week specified in the
     * provided LocalDateTime. Returns false if the restaurant is
     * closed or if the time falls outside operating hours.
     * </p>
     * <p>
     * <strong>Business Rules:</strong>
     * <ul>
     *   <li>Day of week is determined using ISO-8601 standard (1=Monday, 7=Sunday)</li>
     *   <li>Reservation time must be between open_time and close_time (inclusive)</li>
     *   <li>If no business hours are defined for a day, returns false</li>
     * </ul>
     * </p>
     *
     * @param dateTime the date and time to validate
     * @return true if the time falls within business hours; false otherwise
     */
    public static boolean validateBusinessHours(LocalDateTime dateTime) {
        int dayOfWeek = dateTime.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        LocalTime time = dateTime.toLocalTime();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT open_time, close_time FROM business_hours WHERE day_of_week = ?")) {

            ps.setInt(1, dayOfWeek);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                LocalTime openTime = rs.getTime("open_time").toLocalTime();
                LocalTime closeTime = rs.getTime("close_time").toLocalTime();

                // Check if time is within operating hours
                return !time.isBefore(openTime) && !time.isAfter(closeTime);
            }
        } catch (SQLException e) {
            System.err.println("Error validating business hours: " + e.getMessage());
            e.printStackTrace();
        }

        // Default to false if no hours found or error occurs
        return false;
    }

    /**
     * Validates if the party size is appropriate for the specified table.
     * <p>
     * This method checks the table's capacity against the requested party
     * size to ensure the table can accommodate the group. It also validates
     * that the party size is positive.
     * </p>
     * <p>
     * <strong>Validation Rules:</strong>
     * <ul>
     *   <li>Party size must be greater than 0</li>
     *   <li>Party size must not exceed table capacity</li>
     *   <li>Table must exist in the database</li>
     * </ul>
     * </p>
     *
     * @param tableId   the ID of the table to check
     * @param partySize the number of guests in the party
     * @return true if the party size is valid and fits the table; false otherwise
     */
    public static boolean validatePartySize(int tableId, int partySize) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT capacity FROM Tables WHERE id = ?")) {

            ps.setInt(1, tableId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int capacity = rs.getInt("capacity");
                return partySize > 0 && partySize <= capacity;
            }
        } catch (SQLException e) {
            System.err.println("Error validating party size: " + e.getMessage());
            e.printStackTrace();
        }

        // Default to false if table not found or error occurs
        return false;
    }

    /**
     * Checks if modification or cancellation is allowed based on policy cutoff time.
     * <p>
     * This method enforces the cancellation policy by checking if the
     * reservation time is far enough in the future to allow changes.
     * The cutoff period is configured in the reservation_policies table.
     * </p>
     * <p>
     * <strong>Policy Logic:</strong>
     * <br>
     * If the policy requires 2 hours notice, and the current time is 5:00 PM,
     * then reservations before 7:00 PM cannot be cancelled or modified.
     * </p>
     * <p>
     * <strong>Default Behavior:</strong>
     * <br>
     * If no policy is found in the database, the method returns false
     * (disallowing modification) as a safety precaution.
     * </p>
     *
     * @param reservationTime the scheduled date/time of the reservation
     * @return true if the reservation can be cancelled/modified; false if cutoff has passed
     */
    public static boolean checkCancellationPolicy(LocalDateTime reservationTime) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT hours_before FROM reservation_policies WHERE policy_type = 'cancellation'")) {

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int hoursBefore = rs.getInt("hours_before");
                LocalDateTime cutoff = LocalDateTime.now().plusHours(hoursBefore);

                // Reservation time must be after the cutoff
                return reservationTime.isAfter(cutoff);
            }
        } catch (SQLException e) {
            System.err.println("Error checking cancellation policy: " + e.getMessage());
            e.printStackTrace();
        }

        // Default to false (do not allow) if policy not found
        return false;
    }

    /**
     * Detects if there's a scheduling conflict at the given table and time.
     * <p>
     * This method checks if another active reservation already exists
     * for the specified table at the exact date and time. It excludes
     * cancelled and no-show reservations from the conflict check.
     * </p>
     * <p>
     * <strong>Conflict Detection Logic:</strong>
     * <ul>
     *   <li>Checks for exact date/time match (not time ranges)</li>
     *   <li>Ignores reservations with status 'cancelled' or 'no-show'</li>
     *   <li>Excludes the current reservation when checking for updates (via excludeReservationId)</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Usage:</strong>
     * <br>
     * For new reservations, pass 0 as excludeReservationId.
     * <br>
     * For updates/reassignments, pass the current reservation's ID.
     * </p>
     *
     * @param tableId               the ID of the table to check
     * @param dateTime              the date and time to check
     * @param excludeReservationId  the ID of a reservation to exclude from the check (0 for new reservations)
     * @return true if a conflict exists; false if the slot is available
     */
    public static boolean hasConflict(int tableId, LocalDateTime dateTime, int excludeReservationId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM reservations " +
                             "WHERE table_id = ? AND date_time = ? " +
                             "AND status NOT IN ('cancelled', 'no-show') " +
                             "AND id != ?")) {

            ps.setInt(1, tableId);
            ps.setTimestamp(2, Timestamp.valueOf(dateTime));
            ps.setInt(3, excludeReservationId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Conflict exists if count > 0
            }
        } catch (SQLException e) {
            System.err.println("Error checking for conflicts: " + e.getMessage());
            e.printStackTrace();
        }

        // Default to true (assume conflict) on error for safety
        return true;
    }

    /**
     * Calculates the total pricing for a table reservation.
     * <p>
     * The total price is the sum of the table's base price and any
     * applicable surcharge (e.g., for premium zones or peak times).
     * </p>
     * <p>
     * <strong>Pricing Components:</strong>
     * <ul>
     *   <li><strong>Base Price:</strong> Standard reservation fee for the table</li>
     *   <li><strong>Surcharge:</strong> Additional fee for VIP zones, window seats, etc.</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Example:</strong>
     * <br>
     * If a table has a base price of $25.00 and a VIP surcharge of $10.00,
     * this method returns $35.00.
     * </p>
     *
     * @param tableId the ID of the table to price
     * @return the total reservation price, or 0.0 if table not found or error occurs
     */
    public static double calculatePricing(int tableId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT base_price, surcharge FROM Tables WHERE id = ?")) {

            ps.setInt(1, tableId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double base = rs.getDouble("base_price");
                double surcharge = rs.getDouble("surcharge");
                return base + surcharge;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating pricing: " + e.getMessage());
            e.printStackTrace();
        }

        // Default to 0.0 if table not found or error occurs
        return 0.0;
    }

    /**
     * Validates a complete reservation before creation or modification.
     * <p>
     * This convenience method performs all standard validations in sequence:
     * <ol>
     *   <li>Business hours check</li>
     *   <li>Party size validation</li>
     *   <li>Conflict detection</li>
     *   <li>Date/time not in the past</li>
     * </ol>
     * </p>
     * <p>
     * <strong>Note:</strong> This method does NOT check cancellation policy.
     * For modifications, use {@link #checkCancellationPolicy(LocalDateTime)} separately.
     * </p>
     *
     * @param tableId               the ID of the table being reserved
     * @param dateTime              the requested date and time
     * @param partySize             the number of guests
     * @param excludeReservationId  the ID to exclude from conflict check (0 for new reservations)
     * @return true if all validations pass; false if any validation fails
     */
    public static boolean validateReservation(int tableId, LocalDateTime dateTime,
                                              int partySize, int excludeReservationId) {
        // Check if date/time is in the past
        if (dateTime.isBefore(LocalDateTime.now())) {
            System.out.println("Validation failed: Date/time is in the past");
            return false;
        }

        // Check business hours
        if (!validateBusinessHours(dateTime)) {
            System.out.println("Validation failed: Outside business hours");
            return false;
        }

        // Check party size
        if (!validatePartySize(tableId, partySize)) {
            System.out.println("Validation failed: Invalid party size for table");
            return false;
        }

        // Check for conflicts
        if (hasConflict(tableId, dateTime, excludeReservationId)) {
            System.out.println("Validation failed: Time slot already booked");
            return false;
        }

        return true;
    }
}