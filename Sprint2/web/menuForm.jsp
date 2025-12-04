<%@ page import="model.MenuItem" %>
<%
    MenuItem item = (MenuItem) request.getAttribute("menuItem");
%>
<html>
<head><title>Edit Menu Item</title></head>
<body>
<h2><%= (item != null) ? "Edit Menu Item" : "Add New Menu Item" %></h2>
<form method="post" action="menu">
    <input type="hidden" name="id" value="<%= item != null ? item.getId() : "" %>"/>
    Name: <input type="text" name="name" value="<%= item != null ? item.getName() : "" %>"/><br>
    Category: <input type="text" name="category" value="<%= item != null ? item.getCategory() : "" %>"/><br>
    Price: <input type="number" step="0.01" name="price" value="<%= item != null ? item.getPrice() : "" %>"/><br>
    Available: <input type="checkbox" name="available" <%= (item != null && item.isAvailable()) ? "checked" : "" %> /><br>
    <input type="submit" value="Save"/>
</form>
<p style="color:red;"><%= request.getAttribute("error") == null ? "" : request.getAttribute("error") %></p>
<a href="menu">Back to Menu List</a>
</body>
</html>
