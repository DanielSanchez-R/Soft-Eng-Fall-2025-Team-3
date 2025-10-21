<%@ page import="java.util.*, model.MenuItem" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Your Cart - Pizzas 505 ENMU</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; background: #fff8f0; margin:0; padding:0; }
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
        .checkout-btn { background:#27ae60; padding:10px 25px; font-size:16px; border-radius:6px; }
        .checkout-btn:hover { background:#1e8449; }
        .back { text-align:center; margin-top:20px; }
        .back a { color:white; background:#c0392b; padding:10px 20px; border-radius:6px; text-decoration:none; }
        .back a:hover { background:#a93226; }
    </style>
</head>
<body>
<header>🛒 Your Current Order</header>

<%
    List<MenuItem> cart = (List<MenuItem>) session.getAttribute("cart");
    if (cart == null || cart.isEmpty()) {
%>
<p style="text-align:center; color:#999;">Your cart is empty.</p>
<%
} else {
    // Count items and calculate totals
    Map<String, Map<String, Object>> counts = new LinkedHashMap<String, Map<String, Object>>();
    double total = 0;

    for (MenuItem item : cart) {
        String name = item.getName();
        if (!counts.containsKey(name)) {
            Map<String, Object> info = new HashMap<String, Object>();
            info.put("item", item);
            info.put("qty", 1);
            counts.put(name, info);
        } else {
            Map<String, Object> info = counts.get(name);
            int qty = (Integer) info.get("qty");
            info.put("qty", qty + 1);
        }
        total += item.getPrice();
    }
%>

<table>
    <tr><th>Item</th><th>Quantity</th><th>Price Each</th><th>Subtotal</th><th>Actions</th></tr>
    <%
        for (Map.Entry<String, Map<String, Object>> entry : counts.entrySet()) {
            MenuItem item = (MenuItem) entry.getValue().get("item");
            int qty = (Integer) entry.getValue().get("qty");
            double sub = item.getPrice() * qty;
    %>
    <tr>
        <td><%= item.getName() %></td>
        <td><%= qty %></td>
        <td>$<%= String.format("%.2f", item.getPrice()) %></td>
        <td>$<%= String.format("%.2f", sub) %></td>
        <td>
            <!-- Remove one -->
            <form action="removeFromCart" method="post" style="display:inline;">
                <input type="hidden" name="name" value="<%= item.getName() %>">
                <button type="submit" class="remove-btn">🗑 Remove One</button>
            </form>

            <!-- Remove all -->
            <form action="removeAllFromCart" method="post" style="display:inline;">
                <input type="hidden" name="name" value="<%= item.getName() %>">
                <button type="submit" class="remove-all-btn">❌ Remove All</button>
            </form>
        </td>
    </tr>
    <%
        }
    %>
    <tr>
        <th colspan="3" style="text-align:right;">Total:</th>
        <th>$<%= String.format("%.2f", total) %></th>
        <th></th>
    </tr>
</table>

<!-- Checkout button -->
<div style="text-align:center; margin-top:25px;">
    <form action="checkout" method="post">
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

