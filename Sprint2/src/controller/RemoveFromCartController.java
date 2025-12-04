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
 *   <li>Safely remove a single instance of a selected {@link MenuItem} from the session cart.</li>
 *   <li>Ensure cart state consistency after item removal.</li>
 *   <li>Redirect the user back to the cart page after the operation.</li>
 *   <li>Prevent unauthorized users (no session) from modifying cart contents.</li>
 * </ul>
 *
 * <h3>Session Attributes:</h3>
 * <ul>
 *   <li><b>cart</b> — List<MenuItem> representing the user’s current shopping cart.</li>
 * </ul>
 *
 * <h3>Request Parameters:</h3>
 * <ul>
 *   <li><b>name</b> — The name of the menu item to remove (case-sensitive match).</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>POST /removeFromCart</b> — Removes the specified item from the current session cart.</li>
 * </ul>
 *
 * <h3>Workflow Example:</h3>
 * <pre>
 * // POST request
 * name=Pepperoni Pizza
 *
 * -> doPost()
 * -> Finds first "Pepperoni Pizza" in session cart
 * -> Removes it and updates session
 * -> Redirects user back to cart.jsp
 * </pre>
 *
 * <h3>Redirect Behavior:</h3>
 * <ul>
 *   <li>Redirects to login.jsp if no active session exists.</li>
 *   <li>Redirects to cart.jsp after successful removal.</li>
 * </ul>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class RemoveFromCartController extends HttpServlet {

    /**
     * Handles POST /removeFromCart requests.
     * <p>
     * Removes one matching {@link MenuItem} from the session cart
     * based on the name parameter and refreshes the cart view.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} containing the item name to remove
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
        if (cart == null) {
            response.sendRedirect("cart.jsp");
            return;
        }

        try {
            String nameToRemove = request.getParameter("name");
            if (nameToRemove != null && !nameToRemove.trim().isEmpty()) {
                // Remove the first matching item by name
                for (int i = 0; i < cart.size(); i++) {
                    if (cart.get(i).getName().equals(nameToRemove)) {
                        cart.remove(i);
                        break;
                    }
                }
                session.setAttribute("cart", cart);
                System.out.println("🗑 Removed one instance of " + nameToRemove + " from cart.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Always redirect to cart page
        response.sendRedirect("cart.jsp");
    }
}
