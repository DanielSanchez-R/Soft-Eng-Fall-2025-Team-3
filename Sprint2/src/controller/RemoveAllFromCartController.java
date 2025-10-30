package controller;

import model.MenuItem;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * RemoveAllFromCartController is a servlet responsible for handling
 * complete item removals from a customer's shopping cart within the
 * Pizza 505 ENMU online ordering system.
 * <p>
 * Unlike {@link RemoveFromCartController}, which removes only one instance
 * of a given item, this controller removes <b>all</b> matching items from
 * the session cart at once.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Remove all instances of a specific {@link MenuItem} from the cart.</li>
 *   <li>Ensure proper session handling and prevent unauthorized cart modifications.</li>
 *   <li>Maintain session state consistency after removal.</li>
 *   <li>Redirect the user back to cart.jsp after completion.</li>
 * </ul>
 *
 * <h3>Session Attributes:</h3>
 * <ul>
 *   <li><b>cart</b> — A List<MenuItem> representing the user’s active cart contents.</li>
 * </ul>
 *
 * <h3>Request Parameters:</h3>
 * <ul>
 *   <li><b>name</b> — The name of the item to remove from the cart (case-sensitive).</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>POST /removeAllFromCart</b> — Removes all matching menu items from the session cart.</li>
 * </ul>
 *
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>If no active session exists, redirects to login.jsp.</li>
 *   <li>If no cart exists, redirects to cart.jsp.</li>
 *   <li>Otherwise, removes all matching items and updates the session.</li>
 * </ul>
 *
 * <h3>Example Workflow:</h3>
 * <pre>
 * // POST request
 * name=Garlic Bread
 *
 * -> doPost()
 * -> Removes all "Garlic Bread" MenuItem objects from cart
 * -> Updates session and redirects to cart.jsp
 * </pre>
 *
 * @author Daniel
 * @version 1.0
 * @since 2025-10
 */
public class RemoveAllFromCartController extends HttpServlet {

    /**
     * Handles POST /removeAllFromCart requests.
     * <p>
     * Removes all items in the session cart that match the given name.
     * Requires an active session and an initialized cart.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} containing the item name to remove
     * @param response the {@link HttpServletResponse} used for redirection
     * @throws ServletException if a servlet-level error occurs
     * @throws IOException      if an I/O or redirection error occurs
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

        String nameToRemove = request.getParameter("name");
        if (nameToRemove != null && !nameToRemove.trim().isEmpty()) {
            cart.removeIf(item -> item.getName().equals(nameToRemove));
            System.out.println(" Removed ALL instances of " + nameToRemove + " from cart.");
        }

        session.setAttribute("cart", cart);
        response.sendRedirect("cart.jsp");
    }
}
