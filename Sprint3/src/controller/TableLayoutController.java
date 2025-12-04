package controller;

import dao.DBConnection;
import dao.TableDao;
import model.TableInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * TableLayoutController
 * =====================
 * Displays the restaurant’s table layout for Admin, Manager, Staff, and Customer roles.
 * Shows real-time table availability (green = available, red = booked).
 *
 * <h3>Role Access:</h3>
 * - Admin  Full layout view
 * - Manager  Full layout view
 * - Staff  Full layout view
 * - Customer  Simplified layout view
 * - Guests  Redirected to login.jsp
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Filter layout by date, time, and party size</li>
 *   <li>Fetches all tables with live reservation status</li>
 *   <li>Renders the visual layout in different JSPs based on role</li>
 * </ul>
 *
 * @author Daniel
 * @version 1.f1
 * @since 2025-10
 */
public class TableLayoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ===  Validate session and role ===
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String role = ((String) session.getAttribute("role")).toLowerCase();

        // === Parse filters ===
        String dateParam = request.getParameter("date");
        String timeParam = request.getParameter("time");
        String sizeParam = request.getParameter("partySize");

        try (Connection conn = DBConnection.getConnection()) {
            TableDao tableDao = new TableDao(conn);

            // Default to today at 6:00 PM if no date/time chosen
            LocalDate date = (dateParam != null && !dateParam.isEmpty())
                    ? LocalDate.parse(dateParam)
                    : LocalDate.now();

            LocalTime time = (timeParam != null && !timeParam.isEmpty())
                    ? LocalTime.parse(timeParam)
                    : LocalTime.of(18, 0);

            LocalDateTime selectedDateTime = LocalDateTime.of(date, time);

            int partySize = 1;
            try {
                if (sizeParam != null && !sizeParam.isEmpty()) {
                    partySize = Integer.parseInt(sizeParam);
                }
            } catch (NumberFormatException ignored) {}

            // ===  Query database ===
            List<TableInfo> tables = tableDao.getAllTables();
            Map<Integer, Boolean> availabilityMap = tableDao.getTableAvailabilityMap(selectedDateTime);

            // Filter out tables too small for this party size
            if (partySize > 1) {
                final int requiredSize = partySize;
                tables.removeIf(t -> t.getCapacity() < requiredSize);
            }

            // ===  Attach attributes ===
            request.setAttribute("tables", tables);
            request.setAttribute("availabilityMap", availabilityMap);
            request.setAttribute("selectedDateTime", selectedDateTime);
            request.setAttribute("partySize", partySize);

            // ===  Forward based on role ===
            if ("admin".equals(role) || "manager".equals(role) || "staff".equals(role)) {
                request.getRequestDispatcher("tableLayout.jsp").forward(request, response);
            } else if ("customer".equals(role)) {
                request.getRequestDispatcher("tableLayout.jsp").forward(request, response);
            } else {
                response.sendRedirect("unauthorized.jsp");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading table layout: " + e.getMessage());
            request.getRequestDispatcher("customerDashboard.jsp").forward(request, response);
        }
    }
}


