package controller;

import dao.DBConnection;
import dao.ReservationDaoImpl;
import model.Reservation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

        String action = req.getParameter("action");
        if (action == null) action = "list";

        ReservationDaoImpl dao = new ReservationDaoImpl(DBConnection.getConnection());

        switch (action) {
            case "new":
                req.getRequestDispatcher("reservationForm.jsp").forward(req, resp);
                break;

            case "edit":
                int id = Integer.parseInt(req.getParameter("id"));
                Reservation existing = dao.getReservationById(id);
                req.setAttribute("reservation", existing);
                req.getRequestDispatcher("reservationForm.jsp").forward(req, resp);
                break;

            case "delete":
                int deleteId = Integer.parseInt(req.getParameter("id"));
                dao.deleteReservation(deleteId);
                resp.sendRedirect("reservation");
                break;

            default: // list all reservations
                List<Reservation> list = dao.getAllReservations();
                req.setAttribute("reservations", list);
                req.getRequestDispatcher("reservationList.jsp").forward(req, resp);
                break;
        }
    }

    /**
     * Handles POST /reservation requests for creating or updating reservations.
     * <p>
     * Parses form parameters, converts date-time strings to {@link LocalDateTime},
     * and delegates database persistence to {@link ReservationDaoImpl}.
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

        ReservationDaoImpl dao = new ReservationDaoImpl(DBConnection.getConnection());

        String idParam = req.getParameter("id");
        String customerName = req.getParameter("customerName");
        String contact = req.getParameter("contact");
        int tableId = Integer.parseInt(req.getParameter("tableId"));
        int partySize = Integer.parseInt(req.getParameter("partySize"));
        String status = req.getParameter("status");
        String dateTimeStr = req.getParameter("dateTime");

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        Reservation reservation = new Reservation();
        reservation.setCustomerName(customerName);
        reservation.setContact(contact);
        reservation.setTableId(tableId);
        reservation.setDateTime(dateTime);
        reservation.setPartySize(partySize);
        reservation.setStatus(status);

        boolean success;
        if (idParam == null || idParam.isEmpty()) {
            success = dao.addReservation(reservation);
        } else {
            reservation.setId(Integer.parseInt(idParam));
            success = dao.updateReservation(reservation);
        }

        if (success) {
            resp.sendRedirect("reservation");
        } else {
            req.setAttribute("error", "Failed to save reservation!");
            req.getRequestDispatcher("reservationForm.jsp").forward(req, resp);
        }
    }
}
