package controller;

import dao.DBConnection;
import model.MenuItem;
import util.EmailUtil;

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
 * @version 1.d1 - d3
 * @since 2025-10
 */
public class CheckoutController extends HttpServlet {

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
            response.sendRedirect("checkout.jsp?error=empty");
            return;
        }

        // Retrieve customer email
        String customerEmail = (String) session.getAttribute("email");
        if (customerEmail == null) customerEmail = "guest@pizzas505.com";

        // ADDED — capture selected time + method from checkout.jsp
        String deliveryMethod = request.getParameter("deliveryMethod");  // "delivery" or "pickup"
        String timeSelected   = request.getParameter("orderTime");       // user selected HH:MM

        // Store into session for success.jsp
        session.setAttribute("deliveryMethod", deliveryMethod);
        session.setAttribute("orderTime", timeSelected);

        // -------------------------------
        // PAYMENT VALIDATION (existing)
        // -------------------------------

        // Prevent duplicate submits
        Object token = session.getAttribute("payment_token");
        if (token != null) {
            response.sendRedirect("checkout.jsp?error=duplicate");
            return;
        }
        session.setAttribute("payment_token", true);

        // Get card info
        String cardNumber = request.getParameter("cardNumber");
        String cardName   = request.getParameter("cardName");
        String exp        = request.getParameter("exp");
        String cvv        = request.getParameter("cvv");

        // Validate all fields exist
        if (cardNumber == null || cardName == null || exp == null || cvv == null) {
            session.removeAttribute("payment_token");
            response.sendRedirect("checkout.jsp?error=invalid");
            return;
        }

        // Validate card length
        if (!cardNumber.matches("\\d{16}")) {
            session.removeAttribute("payment_token");
            response.sendRedirect("checkout.jsp?error=invalid");
            return;
        }

        // Fake approval rule
        if (!cardNumber.equals("1111111111111111")) {
            session.removeAttribute("payment_token");
            response.sendRedirect("checkout.jsp?error=declined");
            return;
        }

        // Payment approved — continue checkout

        // Calculate total order price
        double total = 0;
        for (MenuItem item : cart) {
            total += item.getPrice();
        }

        // Store total price for use on success page
        session.setAttribute("orderTotal", total);

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

            // Optional update (existing)
            PreparedStatement psUpdate = conn.prepareStatement(
                    "UPDATE MenuItem SET available = TRUE WHERE name = ?"
            );
            for (String itemName : counts.keySet()) {
                psUpdate.setString(1, itemName);
                psUpdate.addBatch();
            }
            psUpdate.executeBatch();

            conn.commit();

            // Clear cart
            session.removeAttribute("cart");

            // Allow new payment
            session.removeAttribute("payment_token");

            // Email Receipt (NEW)
            String emailBody =
                    "<h2>Your Pizzas 505 Order Confirmation</h2>" +
                            "<p><b>Order #" + orderId + "</b></p>" +
                            "<p><b>Total Paid:</b> $" + String.format("%.2f", total) + "</p>" +
                            "<p><b>Time Selected:</b> " + timeSelected + "</p>" +
                            "<p><b>Method:</b> " + deliveryMethod + "</p>" +
                            "<p>Thank you for ordering! 🍕🔥</p>";

            EmailUtil.sendEmail(customerEmail,
                    "Your Order Receipt #" + orderId,
                    emailBody);

            // Redirect with orderId
            response.sendRedirect("orderSuccess.jsp?orderId=" + orderId);

        } catch (Exception e) {
            e.printStackTrace();
            session.removeAttribute("payment_token");
            response.sendRedirect("checkout.jsp?error=server");
        }
    }
}

