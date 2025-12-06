package controller;

import model.MenuItem;
import dao.MenuItemDao;
import dao.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

/**
 * MenuController is a servlet responsible for managing all
 * menu-related operations for users with <b>manager</b> or <b>admin</b> roles.
 * <p>
 * It supports full CRUD operations on {@link MenuItem} objects and integrates
 * with the {@link MenuItemDao} class for database interactions.
 * This controller is restricted to authorized management roles and ensures
 * proper access control, validation, and redirection to JSP views.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Display all menu items (view mode).</li>
 *   <li>Add new menu items with validation.</li>
 *   <li>Edit and update existing items.</li>
 *   <li>Delete menu items by ID.</li>
 *   <li>Toggle item availability (published/draft).</li>
 * </ul>
 *
 * <h3>Role Restrictions:</h3>
 * <ul>
 *   <li>Accessible only by users with roles <b>manager</b> or <b>admin</b>.</li>
 *   <li>Unauthorized users are redirected to unauthorized.jsp.</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>GET /menu</b> — Displays the full menu list (default).</li>
 *   <li><b>GET /menu?action=edit</b> — Loads a menu item for editing.</li>
 *   <li><b>GET /menu?action=delete</b> — Deletes a menu item by ID.</li>
 *   <li><b>GET /menu?action=toggle</b> — Toggles an item’s availability.</li>
 *   <li><b>POST /menu?action=add</b> — Adds a new menu item.</li>
 *   <li><b>POST /menu?action=update</b> — Updates an existing menu item.</li>
 *   <li><b>POST /menu?action=delete</b> — Deletes a menu item.</li>
 * </ul>
 *
 * <h3>JSP Views:</h3>
 * <ul>
 *   <li>managersMenu.jsp — Displays menu item list.</li>
 *   <li>menuForm.jsp — Add or edit menu item form.</li>
 *   <li>unauthorized.jsp — Access denied page.</li>
 *   <li>login.jsp — Redirect for unauthenticated users.</li>
 * </ul>
 *
 * <h3>Example Workflow:</h3>
 * <pre>
 * // Manager adds a new pizza
 * POST /menu?action=add
 * name=BBQ Chicken Pizza
 * description=Grilled chicken with BBQ sauce
 * category=Entree
 * price=12.99
 * available=true
 * draft=false
 *
 * -> MenuItemDao.addMenuItem()
 * -> Redirects back to /menu
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class MenuController extends HttpServlet {

    /**
     * Handles GET /menu requests.
     * <p>
     * Routes requests based on the action parameter:
     * displays menu list, loads edit form, deletes items,
     * or toggles availability.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} with query parameters
     * @param response the {@link HttpServletResponse} for forwarding or redirection
     * @throws ServletException if servlet-level or database error occurs
     * @throws IOException      if forwarding or redirection fails
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String role = ((String) session.getAttribute("role")).toLowerCase();
        if (!(role.equals("manager") || role.equals("admin"))) {
            response.sendRedirect("unauthorized.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try (Connection conn = DBConnection.getConnection()) {
            MenuItemDao dao = new MenuItemDao(conn);

            switch (action) {
                case "edit":
                    int editId = Integer.parseInt(request.getParameter("id"));
                    MenuItem item = dao.getMenuItemById(editId);
                    request.setAttribute("menuItem", item);
                    request.getRequestDispatcher("menuForm.jsp").forward(request, response);
                    break;

                case "delete":
                    int delId = Integer.parseInt(request.getParameter("id"));
                    dao.deleteMenuItem(delId);
                    response.sendRedirect("menu");
                    break;

                case "toggle":
                    int toggleId = Integer.parseInt(request.getParameter("id"));
                    boolean available = Boolean.parseBoolean(request.getParameter("available"));
                    dao.toggleAvailability(toggleId, !available);
                    response.sendRedirect("menu");
                    break;

                default:
                    List<MenuItem> menuList = dao.getAllMenuItems();
                    request.setAttribute("menuList", menuList);
                    request.getRequestDispatcher("managersMenu.jsp").forward(request, response);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(" Error loading or modifying menu items.", e);
        }
    }

    /**
     * Handles POST /menu requests.
     * <p>
     * Processes create, update, delete, and toggle actions based on
     * the action parameter. Ensures that only authorized
     * management roles can modify menu data.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} containing form data
     * @param response the {@link HttpServletResponse} for redirecting after success
     * @throws ServletException if a servlet or DAO error occurs
     * @throws IOException      if I/O or redirection fails
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String role = ((String) session.getAttribute("role")).toLowerCase();
        if (!(role.equals("manager") || role.equals("admin"))) {
            response.sendRedirect("unauthorized.jsp");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) action = "list";

        try (Connection conn = DBConnection.getConnection()) {
            MenuItemDao dao = new MenuItemDao(conn);

            switch (action) {
                case "add":
                    addMenuItem(request, dao);
                    break;
                case "update":
                    updateMenuItem(request, dao);
                    break;
                case "delete":
                    int delId = Integer.parseInt(request.getParameter("id"));
                    dao.deleteMenuItem(delId);
                    break;
                case "toggle":
                    toggleAvailability(request, dao);
                    break;
                default:
                    System.out.println(" Unknown POST action: " + action);
                    break;
            }

            response.sendRedirect("menu");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(" Error processing menu action.", e);
        }
    }

    // Helper Methods

    /**
     * Creates a new {@link MenuItem} from request parameters and inserts it
     * into the database.
     *
     * @param request the {@link HttpServletRequest} containing form data
     * @param dao     the {@link MenuItemDao} used for database operations
     * @throws Exception if a DAO or parsing error occurs
     */
    private void addMenuItem(HttpServletRequest request, MenuItemDao dao) throws Exception {
        String name = safe(request.getParameter("name"));
        String description = safe(request.getParameter("description"));
        String category = safe(request.getParameter("category"));
        double price = parseDoubleSafe(request.getParameter("price"));
        boolean available = request.getParameter("available") != null;
        boolean draft = request.getParameter("draft") != null;

        MenuItem item = new MenuItem(name, description, category, price, available, draft);
        dao.addMenuItem(item);
        System.out.println(" Added new menu item: " + name);
    }

    /**
     * Updates an existing {@link MenuItem} record in the database.
     *
     * @param request the {@link HttpServletRequest} containing form data
     * @param dao     the {@link MenuItemDao} for database access
     * @throws Exception if a DAO or parsing error occurs
     */
    private void updateMenuItem(HttpServletRequest request, MenuItemDao dao) throws Exception {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = safe(request.getParameter("name"));
        String description = safe(request.getParameter("description"));
        String category = safe(request.getParameter("category"));
        double price = parseDoubleSafe(request.getParameter("price"));
        boolean available = request.getParameter("available") != null;
        boolean draft = request.getParameter("draft") != null;

        MenuItem item = new MenuItem(name, description, category, price, available, draft);
        item.setId(id);
        dao.updateMenuItem(item);
        System.out.println(" Updated menu item ID: " + id);
    }

    /**
     * Toggles the availability of a {@link MenuItem} between available and draft states.
     *
     * @param request the {@link HttpServletRequest} with toggle parameters
     * @param dao     the {@link MenuItemDao} used for database updates
     * @throws Exception if a DAO or parsing error occurs
     */
    private void toggleAvailability(HttpServletRequest request, MenuItemDao dao) throws Exception {
        int id = Integer.parseInt(request.getParameter("id"));
        boolean available = Boolean.parseBoolean(request.getParameter("available"));
        dao.toggleAvailability(id, !available);
        System.out.println(" Toggled availability for ID: " + id);
    }

    /**
     * Safely trims a string parameter or returns an empty string if null.
     *
     * @param s the input string
     * @return a trimmed non-null string
     */
    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    /**
     * Safely parses a double from a string. Returns 0.0 on invalid input.
     *
     * @param s the string to parse
     * @return the parsed double value, or 0.0 if invalid
     */
    private double parseDoubleSafe(String s) {
        try {
            return (s == null || s.isEmpty()) ? 0.0 : Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

