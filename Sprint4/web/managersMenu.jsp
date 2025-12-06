<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page import="java.util.*, model.MenuItem" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manager Menu Console - Pizzas 505 ENMU</title>
    <style>
        body {
            font-family: Segoe UI, Arial, sans-serif;
            background: #fff8f0;
            padding: 20px;
            color: #333;
        }
        h2 {
            color: #c0392b;
            text-align: center;
        }
        form {
            margin-bottom: 20px;
        }
        label {
            display: inline-block;
            width: 120px;
            margin-bottom: 5px;
        }
        input[type=text], select {
            width: 200px;
            padding: 4px;
        }
        table {
            width: 90%;
            margin: 20px auto;
            border-collapse: collapse;
            background: #fff;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
        }
        th, td {
            padding: 10px;
            border-bottom: 1px solid #ddd;
            text-align: center;
        }
        th {
            background: #e74c3c;
            color: white;
        }
        tr.unavailable td {
            color: #999;
            text-decoration: line-through;
        }
        button {
            background: #c0392b;
            color: #fff;
            border: none;
            padding: 6px 10px;
            border-radius: 4px;
            cursor: pointer;
        }
        button:hover {
            background: #a93226;
        }
        hr {
            border: none;
            height: 1px;
            background: #ccc;
            margin: 30px 0;
        }
        .empty {
            text-align: center;
            color: #777;
        }
        .footer-buttons {
            text-align: center;
            margin-top: 40px;
        }
        .footer-buttons button {
            padding: 10px 20px;
            margin: 5px;
            border-radius: 6px;
            cursor: pointer;
            border: none;
            font-size: 15px;
        }
        .back-btn {
            background: #555;
            color: white;
        }
        .back-btn:hover {
            background: #333;
        }
        .logout-btn {
            background: #c0392b;
            color: white;
        }
        .logout-btn:hover {
            background: #a93226;
        }
    </style>
</head>
<body>

<h2>🍕 Manager Menu Management Console</h2>

<!-- ADD NEW ITEM FORM -->
<form action="menu" method="post">
    <input type="hidden" name="action" value="add">
    <label>Name:</label> <input type="text" name="name" required><br>
    <label>Description:</label> <input type="text" name="description"><br>
    <label>Category:</label>
    <select name="category">
        <option>Appetizer</option>
        <option>Entree</option>
        <option>Dessert</option>
        <option>Drink</option>
    </select><br>
    <label>Price:</label> <input type="text" name="price" required><br>
    <label>Available:</label> <input type="checkbox" name="available" value="true" checked><br>
    <label>Draft:</label> <input type="checkbox" name="draft" value="true"><br>
    <button type="submit">➕ Add Item</button>
</form>

<hr>

<h3 style="text-align:center;">Current Menu Items</h3>

<table>
    <tr>
        <th>Name</th><th>Category</th><th>Price</th>
        <th>Available</th><th>Draft</th><th>Actions</th>
    </tr>

    <%
        List<MenuItem> menuList = (List<MenuItem>) request.getAttribute("menuList");
        if (menuList == null || menuList.isEmpty()) {
    %>
    <tr><td colspan="6" class="empty">No menu items found.</td></tr>
    <%
    } else {
        for (MenuItem item : menuList) {
            String rowClass = item.isAvailable() ? "" : "unavailable";
    %>
    <tr class="<%= rowClass %>">
        <td><%= item.getName() %></td>
        <td><%= item.getCategory() %></td>
        <td>$<%= String.format("%.2f", item.getPrice()) %></td>
        <td><%= item.isAvailable() ? "✅" : "❌" %></td>
        <td><%= item.isDraft() ? "📝" : "—" %></td>
        <td>
            <!-- Edit -->
            <form action="menu" method="post" style="display:inline;">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="<%= item.getId() %>">
                <input type="text" name="name" value="<%= item.getName() %>" size="8">
                <input type="text" name="description" value="<%= item.getDescription() %>" size="10">
                <input type="text" name="category" value="<%= item.getCategory() %>" size="8">
                <input type="text" name="price" value="<%= item.getPrice() %>" size="5">
                <input type="checkbox" name="available" <%= item.isAvailable() ? "checked" : "" %>> Available
                <input type="checkbox" name="draft" <%= item.isDraft() ? "checked" : "" %>> Draft
                <button type="submit">💾 Save</button>
            </form>

            <!-- Toggle availability -->
            <form action="menu" method="post" style="display:inline;">
                <input type="hidden" name="action" value="toggle">
                <input type="hidden" name="id" value="<%= item.getId() %>">
                <input type="hidden" name="available" value="<%= item.isAvailable() %>">
                <button type="submit"><%= item.isAvailable() ? "🚫 Disable" : "✅ Enable" %></button>
            </form>

            <!-- Delete -->
            <form action="menu" method="post" style="display:inline;" onsubmit="return confirm('Delete this item?');">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="<%= item.getId() %>">
                <button type="submit">🗑 Delete</button>
            </form>
        </td>
    </tr>
    <%
            }
        }
    %>
</table>

<!-- Footer Buttons -->
<div class="footer-buttons">
    <form action="<%= request.getContextPath() %>/staffDashboard.jsp" method="get" style="display:inline;">
        <button type="submit" class="back-btn">Back to Dashboard</button>
    </form>

    <form action="logout.jsp" method="get" style="display:inline;">
        <button type="submit" class="logout-btn">🚪 Logout</button>
    </form>
</div>

</body>
</html>

