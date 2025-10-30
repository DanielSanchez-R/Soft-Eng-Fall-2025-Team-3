<%@ page import="java.util.List" %>
<%@ page import="model.MenuItem" %>
<%
    List<MenuItem> menuItems = (List<MenuItem>) request.getAttribute("menuItems");
%>
<html>
<head><title>Menu Items</title></head>
<body>
<h2>Menu Management</h2>
<a href="menu?action=new">Add New Item</a>
<table border="1" cellpadding="6">
<tr><th>ID</th><th>Name</th><th>Category</th><th>Price</th><th>Available</th><th>Actions</th></tr>
<%
if (menuItems != null) {
    for (MenuItem item : menuItems) {
%>
<tr>
<td><%= item.getId() %></td>
<td><%= item.getName() %></td>
<td><%= item.getCategory() %></td>
<td>$<%= item.getPrice() %></td>
<td><%= item.isAvailable() ? "Yes" : "No" %></td>
<td>
<a href="menu?action=edit&id=<%= item.getId() %>">Edit</a> |
<a href="menu?action=delete&id=<%= item.getId() %>">Delete</a>
</td>
</tr>
<%
    }
}
%>
</table>
</body>
</html>
