package controller;

import dao.DBConnection;
import dao.ReservationDaoImpl;
import dao.TableDao;
import model.Reservation;
import model.TableInfo;
import util.ReservationService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ReservationController is a servlet that manages all reservation-related
 * operations for the Pizza 505 ENMU restaurant system.
 * <p>
 * It handles CRUD actions (Create, Read, Update, Delete) for reservations,
 * interacts with the {@link ReservationDaoImpl} to perform database operations,
 * and forwards data to JSP pages for user interaction.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Display a list of all reservations.</li>
 *   <li>Provide forms for creating or editing reservations.</li>
 *   <li>Handle reservation creation and modification submissions.</li>
 *   <li>Delete existing reservations.</li>
 * </ul>
 *
 * <h3>Access Control:</h3>
 * <ul>
 *   <li>Intended for use by both staff and admin roles.</li>
 *   <li>No direct public access should be available without authentication.</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>GET /reservation?action=list</b> — Display all reservations.</li>
 *   <li><b>GET /reservation?action=new</b> — Display empty reservation form.</li>
 *   <li><b>GET /reservation?action=edit</b> — Load existing reservation for editing.</li>
 *   <li><b>GET /reservation?action=delete</b> — Delete reservation by ID.</li>
 *   <li><b>POST /reservation</b> — Save new or updated reservation.</li>
 * </ul>
 *
 * <h3>JSP Views:</h3>
 * <ul>
 *   <li>reservationList.jsp — Displays list of all reservations.</li>
 *   <li>reservationForm.jsp — Used for creating and editing reservations.</li>
 * </ul>
 *
 * <h3>Example Workflow:</h3>
 * <pre>
 * // Staff navigates to /reservation?action=new
 * -> doGet() forwards to reservationForm.jsp
 *
 * // Form submission (POST)
 * customerName=John Doe
 * contact=575-555-2211
 * tableId=4
 * partySize=3
 * dateTime=2025-10-21T18:00
 * status=confirmed
 *
 * -> doPost() creates new Reservation and calls dao.addReservation()
 * -> Redirects to /reservation to refresh list
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
@WebServlet("/reservation")
public class ReservationController extends HttpServlet {

    /**
     * Handles GET /reservation requests.
     * <p>
     * Routes requests based on the action parameter and forwards
     * to the appropriate JSP view or DAO operation.
     * </p>
     *
     * <h4>Supported Actions:</h4>
     * <ul>
     *   <li><b>list</b> — Displays all reservations.</li>
     *   <li><b>new</b> — Opens a blank reservation form.</li>
     *   <li><b>edit</b> — Loads an existing reservation for editing.</li>
     *   <li><b>delete</b> — Removes a reservation by ID.</li>
     * </ul>
     *
     * @param req  the {@link HttpServletRequest} object containing request data
     * @param resp the {@link HttpServletResponse} object for output or redirection
     * @throws ServletException if a servlet or DB-level error occurs
     * @throws IOException      if forwarding or redirection fails
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userObj") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "list";

        try {
            ReservationDaoImpl dao = new ReservationDaoImpl(DBConnection.getConnection());

            switch (action) {
                case "new":
                    showNewReservationForm(req, resp);
                    break;

                case "edit":
                    loadEditForm(req, resp, dao);
                    break;

                case "delete":
                    deleteReservation(req, resp, dao);
                    break;

                case "search":
                    searchByReference(req, resp, dao);
                    break;

                case "markSeated":
                    markAsSeated(req, resp, dao);
                    break;

                case "markNoShow":
                    markAsNoShow(req, resp, dao);
                    break;

                case "reassign":
                    loadReassignForm(req, resp, dao);
                    break;

                default: // list all reservations
                    listAllReservations(req, resp, dao);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error processing reservation: " + e.getMessage());
            req.getRequestDispatcher("reservationList.jsp").forward(req, resp);
        }
    }

    /**
     * Handles POST /reservation requests for creating or updating reservations.
     * <p>
     * Processes form submissions for creating new reservations, updating
     * existing ones, and reassigning tables/times. All operations require
     * a valid session and perform appropriate validation.
     * </p>
     *
     * @param req  the {@link HttpServletRequest} containing form submission data
     * @param resp the {@link HttpServletResponse} for redirecting after success
     * @throws ServletException if database or forwarding error occurs
     * @throws IOException      if a redirect or I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userObj") == null) {
            resp.sendRedirect("login.jsp");
            return;
        }

        String action = req.getParameter("action");

        try {
            ReservationDaoImpl dao = new ReservationDaoImpl(DBConnection.getConnection());

            if ("reassign".equals(action)) {
                handleReassignment(req, resp, dao);
            } else {
                saveReservation(req, resp, dao);
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error saving reservation: " + e.getMessage());
            req.getRequestDispatcher("reservationForm.jsp").forward(req, resp);
        }
    }

    // ===== HELPER METHODS - DISPLAY =====

    /**
     * Displays a list of all reservations sorted by date/time.
     * <p>
     * Retrieves all reservations from the database and forwards them
     * to the reservationList.jsp view for display. Staff can then
     * search, filter, or perform actions on individual reservations.
     * </p>
     *
     * @param req  the request object for setting attributes
     * @param resp the response object for forwarding
     * @param dao  the DAO instance for database access
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void listAllReservations(HttpServletRequest req, HttpServletResponse resp,
                                     ReservationDaoImpl dao)
            throws ServletException, IOException {

        List<Reservation> list = dao.getAllReservations();
        req.setAttribute("reservations", list);
        req.getRequestDispatcher("reservationList.jsp").forward(req, resp);
    }

    /**
     * Displays an empty reservation form for creating a new reservation.
     * <p>
     * Loads all available tables and forwards them along with the form
     * to allow staff to select a table when creating the reservation.
     * </p>
     *
     * @param req  the request object for setting attributes
     * @param resp the response object for forwarding
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void showNewReservationForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try (Connection conn = DBConnection.getConnection()) {
            TableDao tableDao = new TableDao(conn);
            List<TableInfo> tables = tableDao.getAllTables();
            req.setAttribute("tables", tables);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error loading tables.");
        }

        req.getRequestDispatcher("reservationForm.jsp").forward(req, resp);
    }

    /**
     * Loads an existing reservation for editing.
     * <p>
     * Retrieves the reservation by ID and forwards it to the
     * reservationForm.jsp view in edit mode. Also loads available
     * tables for the dropdown selector.
     * </p>
     *
     * @param req  the request object containing the reservation ID
     * @param resp the response object for forwarding
     * @param dao  the DAO instance for database access
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void loadEditForm(HttpServletRequest req, HttpServletResponse resp,
                              ReservationDaoImpl dao)
            throws ServletException, IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        Reservation existing = dao.getReservationById(id);

        if (existing == null) {
            req.setAttribute("error", "Reservation not found.");
            resp.sendRedirect("reservation?action=list");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            TableDao tableDao = new TableDao(conn);
            List<TableInfo> tables = tableDao.getAllTables();
            req.setAttribute("tables", tables);
        } catch (Exception e) {
            e.printStackTrace();
        }

        req.setAttribute("reservation", existing);
        req.getRequestDispatcher("reservationForm.jsp").forward(req, resp);
    }

    // ===== HELPER METHODS - CRUD OPERATIONS =====

    /**
     * Saves a new or updated reservation to the database.
     * <p>
     * This method handles both creation and update operations based on
     * whether an ID parameter is present. It performs the following:
     * <ol>
     *   <li>Parses form data and creates/updates a Reservation object</li>
     *   <li>Generates a unique reference ID for new reservations</li>
     *   <li>Calls the appropriate DAO method (add or update)</li>
     *   <li>Redirects to the reservation list on success</li>
     *   <li>Returns to form with error message on failure</li>
     * </ol>
     * </p>
     * <p>
     * <strong>Note:</strong> This method does not perform business rule
     * validation (e.g., conflict checking). For customer-facing bookings,
     * use {@link controller.CustomerReservationController} which includes
     * full validation via {@link util.ReservationService}.
     * </p>
     *
     * @param req  the request object containing form data
     * @param resp the response object for redirection
     * @param dao  the DAO instance for database access
     * @throws ServletException if forwarding fails
     * @throws IOException      if redirection fails
     */
    private void saveReservation(HttpServletRequest req, HttpServletResponse resp,
                                 ReservationDaoImpl dao)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        String customerName = req.getParameter("customerName");
        String contact = req.getParameter("contact");
        int tableId = Integer.parseInt(req.getParameter("tableId"));
        int partySize = Integer.parseInt(req.getParameter("partySize"));
        String status = req.getParameter("status");
        String dateTimeStr = req.getParameter("dateTime");
        String notes = req.getParameter("notes");

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        Reservation reservation = new Reservation();
        reservation.setCustomerName(customerName);
        reservation.setContact(contact);
        reservation.setTableId(tableId);
        reservation.setDateTime(dateTime);
        reservation.setPartySize(partySize);
        reservation.setStatus(status != null ? status : "confirmed");
        reservation.setNotes(notes);

        boolean success;

        if (idParam == null || idParam.isEmpty()) {
            // Creating new reservation
            reservation.setReferenceId(ReservationService.generateReferenceId());
            success = dao.addReservation(reservation);

            if (success) {
                System.out.println("✅ Created reservation: " + reservation.getReferenceId());
            }
        } else {
            // Updating existing reservation
            reservation.setId(Integer.parseInt(idParam));

            // Preserve existing reference ID
            Reservation existing = dao.getReservationById(reservation.getId());
            if (existing != null) {
                reservation.setReferenceId(existing.getReferenceId());
                reservation.setCustomerId(existing.getCustomerId());
            }

            success = dao.updateReservation(reservation);

            if (success) {
                System.out.println("✏️ Updated reservation ID: " + reservation.getId());
            }
        }

        if (success) {
            resp.sendRedirect("reservation?action=list");
        } else {
            req.setAttribute("error", "Failed to save reservation!");
            req.setAttribute("reservation", reservation);
            req.getRequestDispatcher("reservationForm.jsp").forward(req, resp);
        }
    }

    /**
     * Deletes a reservation by its unique ID.
     * <p>
     * Permanently removes the reservation from the database. This action
     * cannot be undone. For cancellations, consider using status updates
     * instead to maintain audit history.
     * </p>
     *
     * @param req  the request object containing the reservation ID
     * @param resp the response object for redirection
     * @param dao  the DAO instance for database access
     * @throws IOException if redirection fails
     */
    private void deleteReservation(HttpServletRequest req, HttpServletResponse resp,
                                   ReservationDaoImpl dao)
            throws IOException {

        int deleteId = Integer.parseInt(req.getParameter("id"));
        boolean success = dao.deleteReservation(deleteId);

        if (success) {
            System.out.println("🗑️ Deleted reservation ID: " + deleteId);
        } else {
            System.err.println("❌ Failed to delete reservation ID: " + deleteId);
        }

        resp.sendRedirect("reservation?action=list");
    }

    // ===== HELPER METHODS - STAFF OPERATIONS =====

    /**
     * Searches for a reservation by its unique reference ID.
     * <p>
     * This method provides quick lookup functionality for staff when
     * customers call or arrive with their confirmation number. The
     * search result is displayed prominently in the reservation list.
     * </p>
     * <p>
     * <strong>Search Behavior:</strong>
     * <ul>
     *   <li>Case-sensitive exact match on reference ID</li>
     *   <li>Returns single result if found</li>
     *   <li>Shows error message if not found</li>
     *   <li>Still displays full reservation list for context</li>
     * </ul>
     * </p>
     *
     * @param req  the request object containing the reference ID parameter
     * @param resp the response object for forwarding
     * @param dao  the DAO instance for database access
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void searchByReference(HttpServletRequest req, HttpServletResponse resp,
                                   ReservationDaoImpl dao)
            throws ServletException, IOException {

        String referenceId = req.getParameter("ref");

        if (referenceId == null || referenceId.trim().isEmpty()) {
            req.setAttribute("error", "Please enter a reference ID.");
            listAllReservations(req, resp, dao);
            return;
        }

        Reservation reservation = dao.getByReferenceId(referenceId.trim());

        if (reservation == null) {
            req.setAttribute("error", "No reservation found with reference: " + referenceId);
        } else {
            req.setAttribute("searchResult", reservation);
            System.out.println("🔍 Found reservation: " + referenceId);
        }

        // Still show full list for context
        listAllReservations(req, resp, dao);
    }

    /**
     * Marks a reservation as "seated".
     * <p>
     * Updates the reservation status to indicate that the customer
     * has arrived and been shown to their table. This helps staff
     * track which reservations are currently active vs still pending.
     * </p>
     * <p>
     * <strong>Status Flow:</strong>
     * <br>
     * confirmed → seated → (later marked as completed)
     * </p>
     *
     * @param req  the request object containing the reservation ID
     * @param resp the response object for redirection
     * @param dao  the DAO instance for database access
     * @throws IOException if redirection fails
     */
    private void markAsSeated(HttpServletRequest req, HttpServletResponse resp,
                              ReservationDaoImpl dao)
            throws IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        boolean success = dao.updateStatus(id, "seated");

        if (success) {
            System.out.println("✅ Marked reservation ID " + id + " as seated.");
        } else {
            System.err.println("❌ Failed to mark reservation ID " + id + " as seated.");
        }

        resp.sendRedirect("reservation?action=list");
    }

    /**
     * Marks a reservation as "no-show".
     * <p>
     * Updates the reservation status to indicate that the customer
     * did not arrive for their scheduled reservation. This status
     * automatically frees up the table and may be used for analytics
     * or repeat-offender tracking.
     * </p>
     * <p>
     * <strong>Business Impact:</strong>
     * <ul>
     *   <li>Table becomes available for walk-ins</li>
     *   <li>Excluded from conflict detection in future bookings</li>
     *   <li>May trigger follow-up communication policies</li>
     * </ul>
     * </p>
     *
     * @param req  the request object containing the reservation ID
     * @param resp the response object for redirection
     * @param dao  the DAO instance for database access
     * @throws IOException if redirection fails
     */
    private void markAsNoShow(HttpServletRequest req, HttpServletResponse resp,
                              ReservationDaoImpl dao)
            throws IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        boolean success = dao.updateStatus(id, "no-show");

        if (success) {
            System.out.println("🚫 Marked reservation ID " + id + " as no-show.");
        } else {
            System.err.println("❌ Failed to mark reservation ID " + id + " as no-show.");
        }

        resp.sendRedirect("reservation?action=list");
    }

    /**
     * Loads the reassignment form for staff to move a reservation.
     * <p>
     * Displays the current reservation details along with a list of
     * all available tables. Staff can then select a new table and/or
     * time to resolve conflicts or accommodate customer requests.
     * </p>
     * <p>
     * <strong>Use Cases:</strong>
     * <ul>
     *   <li>Resolving double-booking conflicts</li>
     *   <li>Moving customers to larger/smaller tables</li>
     *   <li>Accommodating special seating requests</li>
     *   <li>Shifting times due to kitchen delays</li>
     * </ul>
     * </p>
     *
     * @param req  the request object containing the reservation ID
     * @param resp the response object for forwarding
     * @param dao  the DAO instance for database access
     * @throws ServletException if forwarding fails
     * @throws IOException      if an I/O error occurs
     */
    private void loadReassignForm(HttpServletRequest req, HttpServletResponse resp,
                                  ReservationDaoImpl dao)
            throws ServletException, IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        Reservation reservation = dao.getReservationById(id);

        if (reservation == null) {
            req.setAttribute("error", "Reservation not found.");
            resp.sendRedirect("reservation?action=list");
            return;
        }

        // Load available tables
        try (Connection conn = DBConnection.getConnection()) {
            TableDao tableDao = new TableDao(conn);
            List<TableInfo> tables = tableDao.getAllTables();
            req.setAttribute("tables", tables);
        } catch (Exception e) {
            System.err.println("Error loading tables for reassignment: " + e.getMessage());
            e.printStackTrace();
        }

        req.setAttribute("reservation", reservation);
        req.getRequestDispatcher("reassignReservation.jsp").forward(req, resp);
    }

    /**
     * Handles staff reassignment of reservations to new tables or times.
     * <p>
     * This method processes the reassignment form submission and performs
     * the following operations:
     * <ol>
     *   <li>Validates that the new table/time combination is available</li>
     *   <li>Checks for conflicts using {@link ReservationService#hasConflict}</li>
     *   <li>Updates the reservation via {@link ReservationDaoImpl#reassignReservation}</li>
     *   <li>Logs the change for audit purposes</li>
     *   <li>Redirects to reservation list with success/error message</li>
     * </ol>
     * </p>
     * <p>
     * <strong>Validation Rules:</strong>
     * <ul>
     *   <li>New table must exist in the system</li>
     *   <li>New date/time must not conflict with other reservations</li>
     *   <li>Original reservation must still exist</li>
     * </ul>
     * </p>
     * <p>
     * <strong>Note:</strong> This method does NOT check business hours or
     * party size constraints. Staff are trusted to make appropriate decisions.
     * </p>
     *
     * @param req  the request object containing reassignment form data
     * @param resp the response object for redirection
     * @param dao  the DAO instance for database access
     * @throws ServletException if forwarding fails
     * @throws IOException      if redirection fails
     */
    private void handleReassignment(HttpServletRequest req, HttpServletResponse resp,
                                    ReservationDaoImpl dao)
            throws ServletException, IOException {

        int reservationId = Integer.parseInt(req.getParameter("id"));
        int newTableId = Integer.parseInt(req.getParameter("newTableId"));
        String dateTimeStr = req.getParameter("newDateTime");

        LocalDateTime newDateTime = LocalDateTime.parse(dateTimeStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        // Validate no conflict exists
        if (ReservationService.hasConflict(newTableId, newDateTime, reservationId)) {
            req.setAttribute("error", "The selected table/time is already booked. Please choose another.");
            loadReassignForm(req, resp, dao);
            return;
        }

        // Perform reassignment
        boolean success = dao.reassignReservation(reservationId, newTableId, newDateTime);

        if (success) {
            System.out.println("🔄 Reassigned reservation ID " + reservationId +
                    " to table " + newTableId + " at " + newDateTime);
            req.setAttribute("message", "Reservation reassigned successfully.");
        } else {
            System.err.println("❌ Failed to reassign reservation ID " + reservationId);
            req.setAttribute("error", "Failed to reassign reservation. Please try again.");
        }

        resp.sendRedirect("reservation?action=list");
    }
}