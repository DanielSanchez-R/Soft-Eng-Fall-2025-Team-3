package controller;

import dao.DBConnection;
import model.MenuItem;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * CheckoutController is a servlet responsible for processing
 * checkout operations in the Pizza 505 ENMU online ordering system.
 * <p>
 * It records all items in the customer’s cart into the database by
 * creating new entries in the Orders and OrderItems
 * tables, then clears the shopping cart from the session.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Validate session and ensure the user has items in their cart.</li>
 *   <li>Compute order total and persist it into the Orders table.</li>
 *   <li>Insert individual item details into the OrderItems table.</li>
 *   <li>Commit the transaction safely using JDBC batch execution.</li>
 *   <li>Clear the cart and redirect to a success confirmation page.</li>
 * </ul>
 *
 * <h3>Database Tables:</h3>
 * <ul>
 *   <li><b>Orders</b> — Stores overall order information (customer email, total, date).</li>
 *   <li><b>OrderItems</b> — Stores individual item details (item name, price, quantity).</li>
 * </ul>
 *
 * <h3>Workflow Summary:</h3>
 * <ol>
 *   <li>Validate user session.</li>
 *   <li>Fetch cart and compute total.</li>
 *   <li>Insert new order record into Orders.</li>
 *   <li>Insert each ordered item into OrderItems using batch inserts.</li>
 *   <li>Commit transaction and clear cart from session.</li>
 * </ol>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>POST /checkout</b> — Finalizes the current shopping cart and processes the order.</li>
 * </ul>
 *
 * <h3>Session Attributes:</h3>
 * <ul>
 *   <li><b>cart</b> — List<MenuItem> representing the customer’s current order.</li>
 *   <li><b>email</b> — The logged-in user’s email address (used for order tracking).</li>
 * </ul>
 *
 * <h3>Request Flow:</h3>
 * <ul>
 *   <li>If no session exists → Redirect to login.jsp.</li>
 *   <li>If cart is empty → Forward to cart.jsp with error message.</li>
 *   <li>If checkout succeeds → Redirect to orderSuccess.jsp.</li>
 *   <li>If checkout fails → Forward back to cart.jsp with an error.</li>
 * </ul>
 *
 * <h3>Example Workflow:</h3>
 * <pre>
 * // POST request (user clicks "Checkout")
 * session.cart = [ "Cheese Pizza", "Garlic Bread" ]
 * session.email = "alice@example.com"
 *
 * -> doPost()
 * -> Inserts new row into Orders table
 * -> Inserts item rows into OrderItems
 * -> Commits transaction and clears cart
 * -> Redirects to orderSuccess.jsp
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class CheckoutController extends HttpServlet {

    /**
     * Handles POST /checkout requests.
     * <p>
     * Persists the customer’s cart into the database as a new order.
     * Performs multi-step transactional inserts into the Orders
     * and OrderItems tables, then clears the session cart.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} containing session and cart data
     * @param response the {@link HttpServletResponse} for forwarding or redirecting
     * @throws ServletException if a servlet or SQL error occurs
     * @throws IOException      if I/O or redirect failure occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Retrieve cart contents
        List<MenuItem> cart = (List<MenuItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            request.setAttribute("error", "Your cart is empty!");
            request.getRequestDispatcher("cart.jsp").forward(request, response);
            return;
        }

        // Retrieve email (or assign guest placeholder)
        String customerEmail = (String) session.getAttribute("email");
        if (customerEmail == null) customerEmail = "guest@pizzas505.com";

        // Calculate total order price
        double total = 0;
        for (MenuItem item : cart) {
            total += item.getPrice();
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // === Insert order record ===
            PreparedStatement psOrder = conn.prepareStatement(
                    "INSERT INTO Orders (customer_email, total) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            psOrder.setString(1, customerEmail);
            psOrder.setDouble(2, total);
            psOrder.executeUpdate();

            ResultSet rs = psOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) orderId = rs.getInt(1);

            // === Insert order items ===
            PreparedStatement psItem = conn.prepareStatement(
                    "INSERT INTO OrderItems (order_id, item_name, price, quantity) VALUES (?, ?, ?, ?)"
            );

            // Count quantities for identical menu items
            Map<String, Integer> counts = new LinkedHashMap<>();
            Map<String, Double> prices = new HashMap<>();

            for (MenuItem m : cart) {
                counts.put(m.getName(), counts.getOrDefault(m.getName(), 0) + 1);
                prices.put(m.getName(), m.getPrice());
            }

            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                psItem.setInt(1, orderId);
                psItem.setString(2, e.getKey());
                psItem.setDouble(3, prices.get(e.getKey()));
                psItem.setInt(4, e.getValue());
                psItem.addBatch();
            }

            psItem.executeBatch();

            // Optional: update availability or stock status
            PreparedStatement psUpdate = conn.prepareStatement(
                    "UPDATE MenuItem SET available = TRUE WHERE name = ?"
            );
            for (String itemName : counts.keySet()) {
                psUpdate.setString(1, itemName);
                psUpdate.addBatch();
            }
            psUpdate.executeBatch();

            conn.commit();

            // Clear session cart after successful checkout
            session.removeAttribute("cart");

            System.out.println("✅ Order processed successfully for: " + customerEmail);
            response.sendRedirect("orderSuccess.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Checkout failed: " + e.getMessage());
            request.getRequestDispatcher("cart.jsp").forward(request, response);
        }
    }
}
