package controller;

import model.MenuItem;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * RemoveFromCartController is a servlet responsible for handling
 * item-removal actions from a customer's shopping cart within the
 * Pizza 505 ENMU online ordering system.
 * <p>
 * This controller operates exclusively via POST requests and ensures
 * that only authenticated users with an active session can modify their cart.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Safely remove a single instance of a selected customized {@link MenuItem}
 *       from the session cart using its unique key.</li>
 *   <li>Ensure cart state consistency after item removal.</li>
 *   <li>Redirect the user back to the cart page after the operation.</li>
 *   <li>Prevent unauthorized users (no session) from modifying cart contents.</li>
 * </ul>
 *
 * <h3>Session Attributes:</h3>
 * <ul>
 *   <li><b>cart</b> — List<MenuItem> representing the user's current shopping cart.</li>
 * </ul>
 *
 * <h3>Request Parameters:</h3>
 * <ul>
 *   <li><b>key</b> — A unique identifier composed of:
 *       name + size + toppings + price.</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>POST /removeFromCart</b> — Removes the specified customized item
 *       from the current session cart.</li>
 * </ul>
 *
 * <h3>Workflow Example:</h3>
 * <pre>
 * // POST request
 * key=Pepperoni Pizza_large_[pepperoni, mushrooms]_13.50
 *
 * -> doPost()
 * -> Computes each cart item's key and compares to the provided key
 * -> Removes only the matching customized item
 * -> Redirects to cart.jsp
 * </pre>
 *
 * <h3>Redirect Behavior:</h3>
 * <ul>
 *   <li>Redirects to login.jsp if no active session exists.</li>
 *   <li>Redirects to cart.jsp after successful removal.</li>
 * </ul>
 *
 * @author Daniel Sanchez
 * @version 1.d2
 * @since 2025-10
 */
public class RemoveFromCartController extends HttpServlet {

    /**
     * Handles POST /removeFromCart requests.
     * <p>
     * Removes one matching {@link MenuItem} from the session cart
     * based on the unique <b>key</b> composed of:
     * <br>
     * name + size + toppings + price.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} containing the item key to remove
     * @param response the {@link HttpServletResponse} for redirection
     * @throws ServletException if servlet-level error occurs
     * @throws IOException      if input/output or redirection fails
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        List<MenuItem> cart = (List<MenuItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect("cart.jsp");
            return;
        }

        // Unique key sent from cart.jsp
        String key = request.getParameter("key");
        if (key == null || key.trim().isEmpty()) {
            response.sendRedirect("cart.jsp");
            return;
        }

        try {
            for (int i = 0; i < cart.size(); i++) {
                MenuItem item = cart.get(i);

                // Construct this item's unique key
                String itemKey =
                        item.getName() + "_" +
                                item.getSize() + "_" +
                                item.getToppings() + "_" +
                                String.format("%.2f", item.getPrice());

                if (itemKey.equals(key)) {
                    cart.remove(i);
                    System.out.println("🗑 Removed one instance of item key: " + key);
                    break; // remove only ONE
                }
            }

            session.setAttribute("cart", cart);

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect("cart.jsp");
    }
}

