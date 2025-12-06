package controller;

import model.MenuItem;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

/**
 * UpdateCartController handles in-cart updates:
 * - Increase/decrease item quantity
 * - Add or edit per-item notes
 *
 * Uses a unique key (name + size + toppings + price) so that
 * each customized item is handled independently.
 * @author Daniel Sanchez
 * @version 4.0
 */
public class UpdateCartController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        List<MenuItem> cart = (List<MenuItem>) session.getAttribute("cart");
        if (cart == null) {
            resp.sendRedirect("cart.jsp");
            return;
        }

        // KEY sent from cart.jsp (name + size + toppings + price)
        String key = req.getParameter("key");
        String action = req.getParameter("action");

        Map<String, String> notes = (Map<String, String>) session.getAttribute("notes");
        if (notes == null) {
            notes = new HashMap<String, String>();
        }

        if ("increase".equals(action)) {
            // find item by matching the key
            for (MenuItem item : cart) {
                String itemKey = item.getName() + "_" +
                        item.getSize() + "_" +
                        item.getToppings() + "_" +
                        String.format("%.2f", item.getPrice());

                if (itemKey.equals(key)) {
                    // add another copy of THIS exact customized item
                    MenuItem copy = new MenuItem();
                    copy.setId(item.getId());
                    copy.setName(item.getName());
                    copy.setDescription(item.getDescription());
                    copy.setCategory(item.getCategory());
                    copy.setAvailable(item.isAvailable());
                    copy.setDraft(item.isDraft());
                    copy.setPrice(item.getPrice());
                    copy.setSize(item.getSize());
                    copy.setToppings(new ArrayList<>(item.getToppings()));
                    cart.add(copy);
                    break;
                }
            }

        } else if ("decrease".equals(action)) {
            // remove exactly one occurrence of this exact customized item
            Iterator<MenuItem> it = cart.iterator();
            while (it.hasNext()) {
                MenuItem item = it.next();

                String itemKey = item.getName() + "_" +
                        item.getSize() + "_" +
                        item.getToppings() + "_" +
                        String.format("%.2f", item.getPrice());

                if (itemKey.equals(key)) {
                    it.remove();
                    break;
                }
            }

        } else if ("note".equals(action)) {
            // notes now stored PER UNIQUE ITEM
            String note = req.getParameter("note");
            notes.put(key, note != null ? note.trim() : "");
        }

        session.setAttribute("cart", cart);
        session.setAttribute("notes", notes);

        resp.sendRedirect("cart.jsp");
    }
}

