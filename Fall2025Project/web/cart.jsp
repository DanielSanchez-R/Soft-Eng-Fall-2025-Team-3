<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page import="java.util.*, model.MenuItem" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Your Cart - Pizzas 505 ENMU</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; background:#fff8f0; margin:0; padding:0; }
        header { background:#c0392b; color:white; text-align:center; padding:15px; font-size:20px; }
        table { width:80%; margin:auto; border-collapse:collapse; margin-top:20px; background:white; }
        th,td { padding:10px; border-bottom:1px solid #ddd; text-align:left; }
        th { background:#e74c3c; color:white; }
        button { background:#27ae60; color:white; border:none; padding:6px 10px; border-radius:5px; cursor:pointer; }
        button:hover { background:#1e8449; }
        .remove-btn { background:#e74c3c; }
        .remove-btn:hover { background:#c0392b; }
        .remove-all-btn { background:#d35400; }
        .remove-all-btn:hover { background:#a04000; }
        .adjust-btn { background:#2980b9; }
        .adjust-btn:hover { background:#1f618d; }
        .note-toggle { background:#8e44ad; }
        .note-toggle:hover { background:#6c3483; }
        .note-field { margin-top:8px; display:none; }
        .checkout-btn { background:#27ae60; padding:10px 25px; font-size:16px; border-radius:6px; }
        .checkout-btn:hover { background:#1e8449; }
        .back { text-align:center; margin-top:20px; }
        .back a { color:white; background:#c0392b; padding:10px 20px; border-radius:6px; text-decoration:none; }
        .back a:hover { background:#a93226; }
        small.custom-info { color:#666; font-style:italic; }
    </style>
    <script>
        function toggleNote(id) {
            var el = document.getElementById("note-" + id);
            el.style.display = (el.style.display === "block") ? "none" : "block";
        }
    </script>
</head>
<body>
<header>🛒 Your Current Order</header>

<%
    List<MenuItem> cart = (List<MenuItem>) session.getAttribute("cart");
    Map<String, String> notes = (Map<String, String>) session.getAttribute("notes");
    if (notes == null) {
        notes = new HashMap<String, String>();
        session.setAttribute("notes", notes);
    }

    if (cart == null || cart.isEmpty()) {
%>
<p style="text-align:center; color:#999;">Your cart is empty.</p>
<%
} else {

    // ⭐ FIX: Group by name + size + toppings + price to avoid merging distinct pizzas
    Map<String, Map<String, Object>> grouped = new LinkedHashMap<String, Map<String, Object>>();
    double total = 0;

    for (MenuItem item : cart) {
        String key =
                item.getName() + "_" +
                        item.getSize() + "_" +
                        item.getToppings() + "_" +
                        String.format("%.2f", item.getPrice());

        if (!grouped.containsKey(key)) {
            Map<String, Object> info = new HashMap<String, Object>();
            info.put("item", item);
            info.put("qty", 1);
            grouped.put(key, info);
        } else {
            int qty = (Integer) grouped.get(key).get("qty");
            grouped.get(key).put("qty", qty + 1);
        }

        total += item.getPrice();
    }
%>

<table>
    <tr><th>Item</th><th>Quantity</th><th>Price Each</th><th>Subtotal</th><th>Actions</th></tr>
    <%
        int index = 0;
        for (Map.Entry<String, Map<String, Object>> entry : grouped.entrySet()) {
            MenuItem item = (MenuItem) entry.getValue().get("item");
            int qty = (Integer) entry.getValue().get("qty");
            double sub = item.getPrice() * qty;

            String itemName = item.getName();
            String noteValue = notes.getOrDefault(itemName, "");

            List<String> tps = item.getToppings();
    %>
    <tr>
        <td>
            <strong><%= itemName %></strong><br>

            <% if (item.getSize() != null) { %>
            <small class="custom-info">Size: <%= item.getSize() %></small><br>
            <% } %>

            <% if (tps != null && !tps.isEmpty()) { %>
            <small class="custom-info">Toppings: <%= String.join(", ", tps) %></small><br>
            <% } %>

            <small class="custom-info">Customized @ $<%= String.format("%.2f", item.getPrice()) %></small>
        </td>

        <td>
            <form action="updateCart" method="post" style="display:inline;">
                <input type="hidden" name="key" value="<%= entry.getKey() %>">
                <input type="hidden" name="action" value="decrease">
                <button type="submit" class="adjust-btn">−</button>
            </form>

            <%= qty %>

            <form action="updateCart" method="post" style="display:inline;">
                <input type="hidden" name="key" value="<%= entry.getKey() %>">
                <input type="hidden" name="action" value="increase">
                <button type="submit" class="adjust-btn">+</button>
            </form>
        </td>

        <td>$<%= String.format("%.2f", item.getPrice()) %></td>
        <td>$<%= String.format("%.2f", sub) %></td>

        <td>
            <form action="removeFromCart" method="post" style="display:inline;">
                <input type="hidden" name="key" value="<%= entry.getKey() %>">
                <button type="submit" class="remove-btn">🗑 Remove One</button>
            </form>

            <form action="removeAllFromCart" method="post" style="display:inline;">
                <input type="hidden" name="key" value="<%= entry.getKey() %>">
                <button type="submit" class="remove-all-btn">❌ Remove All</button>
            </form>

            <button type="button" class="note-toggle" onclick="toggleNote('<%= index %>')">📝 Add Note</button>
            <div id="note-<%= index %>" class="note-field">
                <form action="updateCart" method="post">
                    <input type="hidden" name="key" value="<%= entry.getKey() %>">
                    <input type="hidden" name="action" value="note">

                    <textarea name="note" rows="2" cols="25"
                              placeholder="Add special instructions..."><%= noteValue %></textarea><br>

                    <button type="submit" class="adjust-btn">💾 Save</button>
                </form>
            </div>
        </td>
    </tr>
    <%
            index++;
        }
    %>

    <tr>
        <th colspan="3" style="text-align:right;">Total:</th>
        <th>$<%= String.format("%.2f", total) %></th>
        <th></th>
    </tr>
</table>

<div style="text-align:center; margin-top:25px;">
    <form action="checkout.jsp" method="get">
        <button type="submit" class="checkout-btn">💰 Checkout</button>
    </form>
</div>

<%
    }
%>

<div class="back">
    <a href="orderPizza.jsp">⬅️ Continue Ordering</a>
</div>
</body>
</html>




