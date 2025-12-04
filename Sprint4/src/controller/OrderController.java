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
 * <h2>OrderController</h2>
 *
 * The {@code OrderController} servlet is responsible for processing all
 * customer "Add to Order" actions from {@code orderPizza.jsp}.
 * <p>
 * It retrieves the requested {@link MenuItem} from the database, applies
 * Option B customization rules (size adjustments, topping adjustments),
 * calculates the true final price server-side, and stores the fully
 * customized item(s) in the user's session cart.
 *
 * <h3>Main Responsibilities</h3>
 * <ul>
 *     <li>Load the base menu item from the database using {@link MenuItemDao}.</li>
 *     <li>Read user-selected customization (size + toppings).</li>
 *     <li>Apply pricing rules server-side (Option B).</li>
 *     <li>Create one {@code MenuItem} copy per quantity ordered.</li>
 *     <li>Store customized items inside session attribute {@code cart}.</li>
 *     <li>Redirect the user to {@code cart.jsp} after processing.</li>
 * </ul>
 *
 * <h3>Price Calculation Rules (Option B)</h3>
 * Base Price = MenuItem.price (from DB)
 * <br><br>
 * Adjustments:
 * <ul>
 *     <li><b>Size:</b>
 *         <ul>
 *             <li>small: –$1.00</li>
 *             <li>medium: +$2.00</li>
 *             <li>large: +$4.00</li>
 *         </ul>
 *     </li>
 *     <li><b>Toppings:</b>
 *         <ul>
 *             <li>pepperoni: +$1.00</li>
 *             <li>mushrooms: +$1.00</li>
 *             <li>extra_cheese: +$1.50</li>
 *             <li>vegan: +$0.00</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h3>Final Price Formula</h3>
 * <pre>
 * finalPrice = basePrice + sizeAdjustment + toppingAdjustment
 * </pre>
 *
 * <h3>Session Attributes</h3>
 * <ul>
 *     <li>{@code cart} — List&lt;MenuItem&gt; containing all items the user has added.</li>
 * </ul>
 *
 * <h3>Request Parameters</h3>
 * <ul>
 *     <li>{@code itemId} — The menu item ID stored in the database.</li>
 *     <li>{@code quantity} — Number of units to add.</li>
 *     <li>{@code size} — Optional selected size ("small", "medium", "large").</li>
 *     <li>{@code toppings} — Optional multi-value parameter for selected toppings.</li>
 * </ul>
 *
 * <h3>Error Handling</h3>
 * If the item cannot be found or any exception occurs, the user is redirected
 * to:
 * <pre>orderPizza.jsp?error=true</pre>
 *
 * <h3>Author</h3>
 * Daniel Sanchez
 * Version 1.d2, Updated 2025-10
 */
public class OrderController extends HttpServlet {

    /**
     * Processes POST requests from the "Add to Order" button in orderPizza.jsp.
     *
     * @param request  the HTTP request containing menu item ID, quantity,
     *                 and optional customization options
     * @param response the HTTP response used for redirecting to cart.jsp
     * @throws ServletException if the servlet fails internally
     * @throws IOException      if the servlet cannot redirect properly
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<MenuItem> cart = (List<MenuItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            // --------------------------
            // Read request parameters
            // --------------------------
            int itemId = Integer.parseInt(request.getParameter("itemId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String size = request.getParameter("size");

            String[] toppingArray = request.getParameterValues("toppings");
            List<String> toppings = (toppingArray != null)
                    ? Arrays.asList(toppingArray)
                    : new ArrayList<>();

            // --------------------------
            // Retrieve the base item
            // --------------------------
            MenuItemDao dao = new MenuItemDao(conn);
            MenuItem selected = null;

            for (MenuItem m : dao.getAllMenuItems()) {
                if (m.getId() == itemId) {
                    selected = m;
                    break;
                }
            }

            if (selected == null) {
                response.sendRedirect("orderPizza.jsp?error=notfound");
                return;
            }

            // --------------------------
            // OPTION B: Calculate Price
            // --------------------------

            double finalPrice = selected.getPrice();   // Base DB price

            //  Size adjustments
            double sizeAdj = 0;
            if (size != null) {
                switch (size) {
                    case "small":  sizeAdj = 0.0; break;
                    case "medium": sizeAdj = 2.0;  break;
                    case "large":  sizeAdj = 4.0;  break;
                }
            }

            //  Topping adjustments
            double toppingsAdj = 0;
            for (String t : toppings) {
                t = t.trim().toLowerCase();   //normalize

                if (t.equals("pepperoni")) toppingsAdj += 1.0;
                if (t.equals("mushrooms")) toppingsAdj += 1.0;
                if (t.equals("extra_cheese")) toppingsAdj += 1.5;
                if (t.equals("vegan")) toppingsAdj += 0.0;
            }

            finalPrice = finalPrice + sizeAdj + toppingsAdj;
            finalPrice = Math.round(finalPrice * 100.0) / 100.0;

            // --------------------------
            // Add items to cart
            // --------------------------

            for (int i = 0; i < quantity; i++) {

                MenuItem itemCopy = new MenuItem();
                itemCopy.setId(selected.getId());
                itemCopy.setName(selected.getName());
                itemCopy.setDescription(selected.getDescription());
                itemCopy.setCategory(selected.getCategory());
                itemCopy.setAvailable(selected.isAvailable());
                itemCopy.setDraft(selected.isDraft());

                // Store final price + customization into cart object
                itemCopy.setPrice(finalPrice);
                itemCopy.setSize(size);
                itemCopy.setToppings(new ArrayList<>(toppings));

                cart.add(itemCopy);
            }

            session.setAttribute("cart", cart);

            System.out.println(
                    "🧾 Added " + quantity +
                            " x " + selected.getName() +
                            " | final price $" + finalPrice +
                            " | size=" + size +
                            " | toppings=" + toppings
            );

            response.sendRedirect("cart.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("orderPizza.jsp?error=true");
        }
    }
}



