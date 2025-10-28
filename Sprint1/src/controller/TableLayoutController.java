package controller;

import dao.DBConnection;
import dao.TableDao;
import model.TableInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * TableLayoutController displays the restaurant table layout
 * with real-time availability status for customers.
 *
 * @author Anthony Marrs
 * @version 1.d1
 * @since 2025-10
 */
@WebServlet("/tableLayout")
public class TableLayoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String dateStr = request.getParameter("date");
        String timeStr = request.getParameter("time");
        String partySizeStr = request.getParameter("partySize");

        try (Connection conn = DBConnection.getConnection()) {
            TableDao dao = new TableDao(conn);

            // Default to current date/time if not provided
            LocalDateTime selectedDateTime;
            if (dateStr != null && timeStr != null && !dateStr.isEmpty() && !timeStr.isEmpty()) {
                String combined = dateStr + "T" + timeStr;
                selectedDateTime = LocalDateTime.parse(combined,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            } else {
                selectedDateTime = LocalDateTime.now().plusHours(1); // Default 1 hour from now
            }

            // Get all tables
            List<TableInfo> allTables = dao.getAllTables();

            // Get availability map
            Map<Integer, Boolean> availabilityMap = dao.getTableAvailabilityMap(selectedDateTime);

            // If party size specified, filter by capacity
            if (partySizeStr != null && !partySizeStr.isEmpty()) {
                int partySize = Integer.parseInt(partySizeStr);
                List<TableInfo> availableTables = dao.getAvailableTables(selectedDateTime, partySize);
                request.setAttribute("availableTables", availableTables);
            }

            request.setAttribute("tables", allTables);
            request.setAttribute("availabilityMap", availabilityMap);
            request.setAttribute("selectedDateTime", selectedDateTime);

            request.getRequestDispatcher("tableLayout.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading table layout: " + e.getMessage());
            request.getRequestDispatcher("customerDashboard.jsp").forward(request, response);
        }
    }
}