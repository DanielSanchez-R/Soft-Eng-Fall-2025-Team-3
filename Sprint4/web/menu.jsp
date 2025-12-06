<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page import="java.util.*, model.MenuItem" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Manager Menu Console</title>
</head>
<body>
<h2>Manager Menu Management</h2>

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
    <button type="submit">Add Item</button>
</form>

<hr>

<h3>Current Menu Items</h3>
<table border="1">
    <tr><th>Name</th><th>Category</th><th>Price</th><th>Available</th><th>Draft</th><th>Actions</th></tr>
    <%
        List<MenuItem> menuList = (List<MenuItem>) request.getAttribute("menuList");
        if (menuList != null) {
            for (MenuItem item : menuList) {
    %>
    <tr>
        <td><%= item.getName() %></td>
        <td><%= item.getCategory() %></td>
        <td>$<%= item.getPrice() %></td>
        <td><%= item.isAvailable() ? "Yes" : "No" %></td>
        <td><%= item.isDraft() ? "Yes" : "No" %></td>
        <td>
            <form action="menu" method="post" style="display:inline;">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="<%= item.getId() %>">
                <button type="submit">Delete</button>
            </form>
        </td>
    </tr>
    <%
            }
        }
    %>
</table>
</body>
</html>
