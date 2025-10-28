package controller;

import dao.DBConnection;
import dao.ReservationDaoImpl;
import dao.TableDao;
import model.Reservation;
import model.TableInfo;
import model.Customer;
import util.ReservationService;
import util.EmailUtil;

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
 * CustomerReservationController handles all customer-facing
 * reservation operations including viewing, creating, modifying,
 * and canceling reservations.
 *
 * @author Anthony Marrs
 * @version 1.d1
 * @since 2025-10
 */
@WebServlet("/customerReservation")
public class CustomerReservationController extends HttpServlet {

    /**
     * Handles GET requests for viewing reservations and reservation forms.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userObj") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try (Connection conn = DBConnection.getConnection()) {
            ReservationDaoImpl dao = new ReservationDaoImpl(conn);

            switch (action) {
                case "list":
                    viewMyReservations(request, response, dao, session);
                    break;

                case "new":
                    showReservationForm(request, response, conn);
                    break;

                case "modify":
                    loadModifyForm(request, response, dao, session);
                    break;

                case "cancel":
                    cancelReservation(request, response, dao, session);
                    break;

                case "viewConfirmation":
                    viewConfirmation(request, response, dao);
                    break;

                default:
                    response.sendRedirect("customerReservation?action=list");
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error processing reservation: " + e.getMessage());
            request.getRequestDispatcher("customerDashboard.jsp").forward(request, response);
        }
    }

    /**
     * Handles POST requests for creating and modifying reservations.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userObj") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.sendRedirect("customerReservation?action=list");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            ReservationDaoImpl dao = new ReservationDaoImpl(conn);

            switch (action) {
                case "create":
                    createReservation(request, response, dao, session);
                    break;

                case "update":
                    updateReservation(request, response, dao, session);
                    break;

                default:
                    response.sendRedirect("customerReservation?action=list");
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error processing reservation: " + e.getMessage());
            request.getRequestDispatcher("createReservation.jsp").forward(request, response);
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Displays customer's past and upcoming reservations.
     */
    private void viewMyReservations(HttpServletRequest request, HttpServletResponse response,
                                    ReservationDaoImpl dao, HttpSession session)
            throws ServletException, IOException {

        Object userObj = session.getAttribute("userObj");
        int customerId = 0;

        if (userObj instanceof Customer) {
            customerId = ((Customer) userObj).getId();
        }

        if (customerId == 0) {
            request.setAttribute("error", "Customer ID not found in session.");
            request.getRequestDispatcher("customerDashboard.jsp").forward(request, response);
            return;
        }

        List<Reservation> myReservations = dao.getByCustomerId(customerId);
        request.setAttribute("reservations", myReservations);
        request.getRequestDispatcher("myReservations.jsp").forward(request, response);
    }

    /**
     * Shows the reservation creation form with available tables.
     */
    private void showReservationForm(HttpServletRequest request, HttpServletResponse response,
                                     Connection conn)
            throws ServletException, IOException {

        try {
            TableDao tableDao = new TableDao(conn);
            List<TableInfo> allTables = tableDao.getAllTables();
            request.setAttribute("tables", allTables);
            request.getRequestDispatcher("createReservation.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading tables.");
            request.getRequestDispatcher("customerDashboard.jsp").forward(request, response);
        }
    }

    /**
     * Creates a new reservation with full validation.
     */
    private void createReservation(HttpServletRequest request, HttpServletResponse response,
                                   ReservationDaoImpl dao, HttpSession session)
            throws ServletException, IOException {

        // Get customer info
        Object userObj = session.getAttribute("userObj");
        Integer customerId = null;
        String customerName = request.getParameter("customerName");
        String contact = request.getParameter("contact");

        if (userObj instanceof Customer) {
            Customer customer = (Customer) userObj;
            customerId = customer.getId();
            if (customerName == null || customerName.isEmpty()) {
                customerName = customer.getName();
            }
            if (contact == null || contact.isEmpty()) {
                contact = customer.getEmail();
            }
        }

        // Parse form data
        int tableId = Integer.parseInt(request.getParameter("tableId"));
        int partySize = Integer.parseInt(request.getParameter("partySize"));
        String dateTimeStr = request.getParameter("dateTime");
        String notes = request.getParameter("notes");

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        // === VALIDATION ===

        // 1. Check business hours
        if (!ReservationService.validateBusinessHours(dateTime)) {
            request.setAttribute("error", "Selected time is outside business hours.");
            request.getRequestDispatcher("createReservation.jsp").forward(request, response);
            return;
        }

        // 2. Check party size vs table capacity
        if (!ReservationService.validatePartySize(tableId, partySize)) {
            request.setAttribute("error", "Party size exceeds table capacity.");
            request.getRequestDispatcher("createReservation.jsp").forward(request, response);
            return;
        }

        // 3. Check for conflicts
        if (ReservationService.hasConflict(tableId, dateTime, 0)) {
            request.setAttribute("error", "This table is already booked at the selected time. Please choose another time or table.");
            request.getRequestDispatcher("createReservation.jsp").forward(request, response);
            return;
        }

        // 4. Check if date/time is in the past
        if (dateTime.isBefore(LocalDateTime.now())) {
            request.setAttribute("error", "Cannot book reservations in the past.");
            request.getRequestDispatcher("createReservation.jsp").forward(request, response);
            return;
        }

        // === CREATE RESERVATION ===
        String referenceId = ReservationService.generateReferenceId();
        double pricing = ReservationService.calculatePricing(tableId);

        Reservation reservation = new Reservation();
        reservation.setCustomerName(customerName);
        reservation.setContact(contact);
        reservation.setTableId(tableId);
        reservation.setDateTime(dateTime);
        reservation.setPartySize(partySize);
        reservation.setStatus("confirmed");
        reservation.setReferenceId(referenceId);
        reservation.setNotes(notes);
        reservation.setCustomerId(customerId);

        boolean success = dao.addReservation(reservation);

        if (success) {
            // Send confirmation email
            sendConfirmationEmail(reservation, pricing);

            // Redirect to confirmation page
            response.sendRedirect("customerReservation?action=viewConfirmation&ref=" + referenceId);
        } else {
            request.setAttribute("error", "Failed to create reservation. Please try again.");
            request.getRequestDispatcher("createReservation.jsp").forward(request, response);
        }
    }

    /**
     * Loads the modification form for an existing reservation.
     */
    private void loadModifyForm(HttpServletRequest request, HttpServletResponse response,
                                ReservationDaoImpl dao, HttpSession session)
            throws ServletException, IOException {

        String referenceId = request.getParameter("ref");
        if (referenceId == null || referenceId.isEmpty()) {
            response.sendRedirect("customerReservation?action=list");
            return;
        }

        Reservation reservation = dao.getByReferenceId(referenceId);
        if (reservation == null) {
            request.setAttribute("error", "Reservation not found.");
            request.getRequestDispatcher("myReservations.jsp").forward(request, response);
            return;
        }

        // Verify ownership
        Object userObj = session.getAttribute("userObj");
        if (userObj instanceof Customer) {
            Customer customer = (Customer) userObj;
            if (reservation.getCustomerId() == null || !reservation.getCustomerId().equals(customer.getId())) {
                request.setAttribute("error", "You do not have permission to modify this reservation.");
                request.getRequestDispatcher("myReservations.jsp").forward(request, response);
                return;
            }
        }

        // Check cutoff policy
        if (!ReservationService.checkCancellationPolicy(reservation.getDateTime())) {
            request.setAttribute("error", "Cannot modify reservation within 2 hours of scheduled time.");
            request.getRequestDispatcher("myReservations.jsp").forward(request, response);
            return;
        }

        request.setAttribute("reservation", reservation);
        request.getRequestDispatcher("modifyReservation.jsp").forward(request, response);
    }

    /**
     * Updates an existing reservation.
     */
    private void updateReservation(HttpServletRequest request, HttpServletResponse response,
                                   ReservationDaoImpl dao, HttpSession session)
            throws ServletException, IOException {

        String referenceId = request.getParameter("referenceId");
        Reservation existing = dao.getByReferenceId(referenceId);

        if (existing == null) {
            request.setAttribute("error", "Reservation not found.");
            request.getRequestDispatcher("myReservations.jsp").forward(request, response);
            return;
        }

        // Verify ownership
        Object userObj = session.getAttribute("userObj");
        if (userObj instanceof Customer) {
            Customer customer = (Customer) userObj;
            if (existing.getCustomerId() == null || !existing.getCustomerId().equals(customer.getId())) {
                request.setAttribute("error", "You do not have permission to modify this reservation.");
                request.getRequestDispatcher("myReservations.jsp").forward(request, response);
                return;
            }
        }

        // Check cutoff
        if (!ReservationService.checkCancellationPolicy(existing.getDateTime())) {
            request.setAttribute("error", "Modification cutoff has passed.");
            request.getRequestDispatcher("myReservations.jsp").forward(request, response);
            return;
        }

        // Parse new data
        int newTableId = Integer.parseInt(request.getParameter("tableId"));
        int newPartySize = Integer.parseInt(request.getParameter("partySize"));
        String dateTimeStr = request.getParameter("dateTime");
        LocalDateTime newDateTime = LocalDateTime.parse(dateTimeStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        // Validate
        if (!ReservationService.validateBusinessHours(newDateTime)) {
            request.setAttribute("error", "Selected time is outside business hours.");
            request.setAttribute("reservation", existing);
            request.getRequestDispatcher("modifyReservation.jsp").forward(request, response);
            return;
        }

        if (!ReservationService.validatePartySize(newTableId, newPartySize)) {
            request.setAttribute("error", "Party size exceeds table capacity.");
            request.setAttribute("reservation", existing);
            request.getRequestDispatcher("modifyReservation.jsp").forward(request, response);
            return;
        }

        if (ReservationService.hasConflict(newTableId, newDateTime, existing.getId())) {
            request.setAttribute("error", "Time slot already booked.");
            request.setAttribute("reservation", existing);
            request.getRequestDispatcher("modifyReservation.jsp").forward(request, response);
            return;
        }

        // Update
        existing.setTableId(newTableId);
        existing.setPartySize(newPartySize);
        existing.setDateTime(newDateTime);
        existing.setNotes(request.getParameter("notes"));

        boolean success = dao.updateReservation(existing);

        if (success) {
            request.setAttribute("message", "Reservation updated successfully!");
            response.sendRedirect("customerReservation?action=viewConfirmation&ref=" + referenceId);
        } else {
            request.setAttribute("error", "Failed to update reservation.");
            request.setAttribute("reservation", existing);
            request.getRequestDispatcher("modifyReservation.jsp").forward(request, response);
        }
    }

    /**
     * Cancels a reservation.
     */
    private void cancelReservation(HttpServletRequest request, HttpServletResponse response,
                                   ReservationDaoImpl dao, HttpSession session)
            throws ServletException, IOException {

        String referenceId = request.getParameter("ref");
        Reservation reservation = dao.getByReferenceId(referenceId);

        if (reservation == null) {
            request.setAttribute("error", "Reservation not found.");
            response.sendRedirect("customerReservation?action=list");
            return;
        }

        // Verify ownership
        Object userObj = session.getAttribute("userObj");
        if (userObj instanceof Customer) {
            Customer customer = (Customer) userObj;
            if (reservation.getCustomerId() == null || !reservation.getCustomerId().equals(customer.getId())) {
                request.setAttribute("error", "You do not have permission to cancel this reservation.");
                response.sendRedirect("customerReservation?action=list");
                return;
            }
        }

        // Check cutoff
        if (!ReservationService.checkCancellationPolicy(reservation.getDateTime())) {
            request.setAttribute("error", "Cannot cancel within 2 hours of reservation time.");
            response.sendRedirect("customerReservation?action=list");
            return;
        }

        boolean success = dao.updateStatus(reservation.getId(), "cancelled");

        if (success) {
            // Send cancellation email
            sendCancellationEmail(reservation);
            request.setAttribute("message", "Reservation cancelled successfully.");
        } else {
            request.setAttribute("error", "Failed to cancel reservation.");
        }

        response.sendRedirect("customerReservation?action=list");
    }

    /**
     * Displays reservation confirmation page.
     */
    private void viewConfirmation(HttpServletRequest request, HttpServletResponse response,
                                  ReservationDaoImpl dao)
            throws ServletException, IOException {

        String referenceId = request.getParameter("ref");
        Reservation reservation = dao.getByReferenceId(referenceId);

        if (reservation == null) {
            request.setAttribute("error", "Reservation not found.");
            request.getRequestDispatcher("customerDashboard.jsp").forward(request, response);
            return;
        }

        double pricing = ReservationService.calculatePricing(reservation.getTableId());

        request.setAttribute("reservation", reservation);
        request.setAttribute("pricing", pricing);
        request.getRequestDispatcher("reservationConfirmation.jsp").forward(request, response);
    }

    /**
     * Sends reservation confirmation email.
     */
    private void sendConfirmationEmail(Reservation r, double pricing) {
        String subject = "Reservation Confirmation - Pizzas 505 ENMU";
        String body = "<h2>Your Reservation is Confirmed!</h2>" +
                "<p>Thank you for choosing Pizzas 505 ENMU.</p>" +
                "<hr>" +
                "<p><b>Confirmation Number:</b> " + r.getReferenceId() + "</p>" +
                "<p><b>Name:</b> " + r.getCustomerName() + "</p>" +
                "<p><b>Date & Time:</b> " + r.getDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) + "</p>" +
                "<p><b>Party Size:</b> " + r.getPartySize() + " guests</p>" +
                "<p><b>Table:</b> #" + r.getTableId() + "</p>" +
                "<p><b>Total Price:</b> $" + String.format("%.2f", pricing) + "</p>" +
                (r.getNotes() != null && !r.getNotes().isEmpty() ? "<p><b>Notes:</b> " + r.getNotes() + "</p>" : "") +
                "<hr>" +
                "<p>To modify or cancel this reservation, use your confirmation number.</p>" +
                "<p style='font-size:12px; color:#777;'>Cancellations must be made at least 2 hours before your reservation time.</p>";

        try {
            EmailUtil.sendEmail(r.getContact(), subject, body);
        } catch (Exception e) {
            System.out.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    /**
     * Sends cancellation confirmation email.
     */
    private void sendCancellationEmail(Reservation r) {
        String subject = "Reservation Cancelled - Pizzas 505 ENMU";
        String body = "<h2>Reservation Cancelled</h2>" +
                "<p>Your reservation has been successfully cancelled.</p>" +
                "<p><b>Confirmation Number:</b> " + r.getReferenceId() + "</p>" +
                "<p><b>Original Date & Time:</b> " + r.getDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")) + "</p>" +
                "<p>We hope to see you again soon!</p>";

        try {
            EmailUtil.sendEmail(r.getContact(), subject, body);
        } catch (Exception e) {
            System.out.println("Failed to send cancellation email: " + e.getMessage());
        }
    }
}