package controller;

import model.MenuItem;
import dao.MenuItemDao;
import dao.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

/**
 * OrderController is a servlet responsible for handling
 * add-to-cart actions in the Pizza 505 ENMU online ordering system.
 * <p>
 * It retrieves selected {@link MenuItem} details from the database,
 * adds the item (with the specified quantity) to the user’s session cart,
 * and redirects the customer to their shopping cart view.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Retrieve menu item details by ID from the database.</li>
 *   <li>Maintain a session-level shopping cart using List<MenuItem>.</li>
 *   <li>Support adding multiple instances of the same item (quantity).</li>
 *   <li>Handle connection management via {@link DBConnection}.</li>
 *   <li>Redirect to cart.jsp after successful addition.</li>
 * </ul>
 *
 * <h3>Session Attributes:</h3>
 * <ul>
 *   <li><b>cart</b> — A List<MenuItem> representing the user's shopping cart.</li>
 * </ul>
 *
 * <h3>Request Parameters:</h3>
 * <ul>
 *   <li><b>itemId</b> — The unique ID of the {@link MenuItem} to add.</li>
 *   <li><b>quantity</b> — The number of items to add to the cart.</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>POST /order</b> — Adds a menu item (or multiple) to the session cart.</li>
 * </ul>
 *
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>If no session cart exists, a new one is created automatically.</li>
 *   <li>Each item added is stored as a {@link MenuItem} object in the session list.</li>
 *   <li>Redirects to cart.jsp after successful add-to-cart action.</li>
 *   <li>Redirects to orderPizza.jsp?error=true if an error occurs.</li>
 * </ul>
 *
 * <h3>Example Workflow:</h3>
 * <pre>
 * // POST request
 * itemId=5
 * quantity=3
 *
 * -> doPost()
 * -> Queries DB for MenuItem with id=5
 * -> Adds 3 copies of the item to session cart
 * -> Redirects to cart.jsp
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class OrderController extends HttpServlet {

    /**
     * Handles POST /order requests.
     * <p>
     * Retrieves the selected menu item by ID and adds it to the user's
     * session cart in the quantity specified. The cart is stored as a
     * List<MenuItem> within the active session.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} containing item and quantity parameters
     * @param response the {@link HttpServletResponse} used for redirection
     * @throws ServletException if a servlet-level error occurs
     * @throws IOException      if I/O or redirection fails
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        List<MenuItem> cart = (List<MenuItem>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<MenuItem>();
        }

        try (Connection conn = DBConnection.getConnection()) {
            int itemId = Integer.parseInt(request.getParameter("itemId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            // Fetch the menu item by ID
            MenuItemDao dao = new MenuItemDao(conn);
            List<MenuItem> allItems = dao.getAllMenuItems();
            MenuItem selected = null;

            for (MenuItem m : allItems) {
                if (m.getId() == itemId) {
                    selected = m;
                    break;
                }
            }

            // Add the item to the cart with the specified quantity
            if (selected != null) {
                for (int i = 0; i < quantity; i++) {
                    cart.add(selected);
                }
                session.setAttribute("cart", cart);
                System.out.println("✅ Added " + quantity + " x " + selected.getName() + " to cart.");
            }

            response.sendRedirect("cart.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("orderPizza.jsp?error=true");
        }
    }
}
