package controller;

import dao.DBConnection;
import dao.TableDao;
import model.TableInfo;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * TableController is a servlet responsible for handling all
 * administrative operations related to restaurant table management
 * within the Pizza 505 ENMU system.
 * <p>
 * This controller enforces role-based access control, allowing only
 * users with the admin role to view, add, or delete tables.
 * It interfaces with {@link TableDao} for database operations and
 * forwards results to JSP views for display.
 * </p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Restricts access to administrative users via session role checks.</li>
 *   <li>Displays all existing tables on the management page.</li>
 *   <li>Adds new tables with validation for capacity and pricing values.</li>
 *   <li>Deletes existing tables by ID.</li>
 *   <li>Redirects unauthorized users to appropriate error or login pages.</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>GET /tables</b> — Displays all tables for admin management.</li>
 *   <li><b>POST /tables?action=add</b> — Adds a new table to the system.</li>
 *   <li><b>POST /tables?action=delete</b> — Deletes an existing table by ID.</li>
 * </ul>
 *
 * <h3>JSP Views:</h3>
 * <ul>
 *   <li>manageTables.jsp — Displays table listings and forms.</li>
 *   <li>unauthorized.jsp — Shown for non-admin access attempts.</li>
 *   <li>login.jsp — Redirected to if no valid session exists.</li>
 * </ul>
 *
 * <h3>Example Workflow:</h3>
 * <pre>
 * // Admin navigates to /tables (GET)
 * -> doGet() retrieves all tables via TableDao
 * -> Forwards to manageTables.jsp
 *
 * // Admin submits new table form (POST with action=add)
 * -> doPost() validates input and calls TableDao.addTable()
 * -> Redirects back to /tables to refresh the list
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class TableController extends HttpServlet {

    /**
     * Handles GET /tables requests.
     * <p>
     * Displays the list of all restaurant tables in the system.
     * Restricted to admin users; redirects unauthorized users to
     * unauthorized.jsp or login.jsp
     * </p>
     *
     * @param request  the {@link HttpServletRequest} object
     * @param response the {@link HttpServletResponse} object
     * @throws ServletException if a servlet-level error occurs
     * @throws IOException      if a redirection or I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"admin".equalsIgnoreCase(role)) {
            response.sendRedirect("unauthorized.jsp");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            TableDao dao = new TableDao(conn);
            List<TableInfo> tables = dao.getAllTables();
            request.setAttribute("tables", tables);

            RequestDispatcher rd = request.getRequestDispatcher("manageTables.jsp");
            rd.forward(request, response);
        } catch (Exception e) {
            throw new ServletException(" Error loading tables", e);
        }
    }

    /**
     * Handles POST /tables requests.
     * <p>
     * Supports both adding and deleting tables via the action
     * request parameter. Only accessible by admin users.
     * </p>
     *
     * <h4>Supported Actions:</h4>
     * <ul>
     *   <li><b>add</b> — Adds a new table using parameters:
     *     table_number, capacity, zone,
     *     base_price, surcharge.</li>
     *   <li><b>delete</b> — Deletes an existing table by id.</li>
     * </ul>
     *
     * @param request  the {@link HttpServletRequest} containing form data
     * @param response the {@link HttpServletResponse} for redirection
     * @throws ServletException if a servlet or database error occurs
     * @throws IOException      if a redirect or forwarding error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (!"admin".equalsIgnoreCase(role)) {
            response.sendRedirect("unauthorized.jsp");
            return;
        }

        String action = request.getParameter("action");

        try (Connection conn = DBConnection.getConnection()) {
            TableDao dao = new TableDao(conn);

            if ("add".equals(action)) {
                String number = request.getParameter("table_number");
                int capacity = Integer.parseInt(request.getParameter("capacity"));
                String zone = request.getParameter("zone");
                double base = Double.parseDouble(request.getParameter("base_price"));
                double surcharge = Double.parseDouble(request.getParameter("surcharge"));

                // Input validation
                if (capacity <= 0 || base < 0 || surcharge < 0) {
                    request.setAttribute("error", "Invalid capacity or pricing value.");
                    request.getRequestDispatcher("manageTables.jsp").forward(request, response);
                    return;
                }

                TableInfo table = new TableInfo(number, capacity, zone, base, surcharge);
                dao.addTable(table);
                System.out.println(" Table added by admin: " + number);

            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                dao.deleteTable(id);
                System.out.println("🗑 Table deleted by admin (ID: " + id + ")");
            }

            // Refresh list after modification
            response.sendRedirect("tables");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(" Error managing tables", e);
        }
    }
}
