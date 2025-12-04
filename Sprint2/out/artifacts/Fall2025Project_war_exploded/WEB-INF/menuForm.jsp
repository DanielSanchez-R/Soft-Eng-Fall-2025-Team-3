<%@ page import="model.MenuItem" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Edit Menu Item - Pizzas 505 ENMU</title>
    <style>
        body {
            font-family: Segoe UI, Arial, sans-serif;
            background: #fff8f0;
            padding: 40px;
            color: #333;
        }
        h2 {
            color: #c0392b;
            text-align: center;
        }
        form {
            max-width: 500px;
            margin: auto;
            background: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
        }
        label {
            display: block;
            margin: 10px 0 4px;
            font-weight: bold;
        }
        input[type=text], select {
            width: 100%;
            padding: 8px;
            box-sizing: border-box;
            border: 1px solid #ccc;
            border-radius: 4px;
        }
        input[type=checkbox] {
            margin-right: 5px;
        }
        .buttons {
            text-align: center;
            margin-top: 20px;
        }
        button {
            background: #c0392b;
            color: #fff;
            border: none;
            padding: 10px 15px;
            border-radius: 4px;
            cursor: pointer;
            margin: 5px;
        }
        button:hover {
            background: #a93226;
        }
        .cancel {
            background: #7f8c8d;
        }
        .cancel:hover {
            background: #707b7c;
        }
    </style>
</head>
<body>

<%
    MenuItem item = (MenuItem) request.getAttribute("menuItem");
    boolean isEdit = (item != null);
%>

<h2><%= isEdit ? "Edit Menu Item" : "Add New Menu Item" %></h2>

<form action="menu" method="post">
    <input type="hidden" name="action" value="<%= isEdit ? "update" : "add" %>">
    <% if (isEdit) { %>
    <input type="hidden" name="id" value="<%= item.getId() %>">
    <% } %>

    <label>Name:</label>
    <input type="text" name="name" value="<%= isEdit ? item.getName() : "" %>" required>

    <label>Description:</label>
    <input type="text" name="description" value="<%= isEdit ? item.getDescription() : "" %>">

    <label>Category:</label>
    <select name="category">
        <option value="Appetizer" <%= isEdit && "Appetizer".equalsIgnoreCase(item.getCategory()) ? "selected" : "" %>>Appetizer</option>
        <option value="Entree" <%= isEdit && "Entree".equalsIgnoreCase(item.getCategory()) ? "selected" : "" %>>Entree</option>
        <option value="Dessert" <%= isEdit && "Dessert".equalsIgnoreCase(item.getCategory()) ? "selected" : "" %>>Dessert</option>
        <option value="Drink" <%= isEdit && "Drink".equalsIgnoreCase(item.getCategory()) ? "selected" : "" %>>Drink</option>
    </select>

    <label>Price:</label>
    <input type="text" name="price" value="<%= isEdit ? item.getPrice() : "" %>" required>

    <label><input type="checkbox" name="available" <%= isEdit && item.isAvailable() ? "checked" : "" %>> Available</label>
    <label><input type="checkbox" name="draft" <%= isEdit && item.isDraft() ? "checked" : "" %>> Draft</label>

    <div class="buttons">
        <button type="submit"><%= isEdit ? "💾 Save Changes" : "➕ Add Item" %></button>
        <button type="button" class="cancel" onclick="window.location='menu'">Cancel</button>
    </div>
</form>

</body>
</html>
