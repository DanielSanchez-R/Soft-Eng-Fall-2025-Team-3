<%@ page import="java.util.List" %>
<%@ page import="model.Reservation" %>
<%
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
%>
<html>
<head><title>Reservations</title></head>
<body>
<h2>Reservation Management</h2>
<a href="reservation?action=new">Add New Reservation</a>
<table border="1" cellpadding="6">
<tr><th>ID</th><th>Name</th><th>Contact</th><th>Table</th><th>Date/Time</th><th>Party</th><th>Status</th><th>Actions</th></tr>
<%
if (reservations != null) {
    for (Reservation r : reservations) {
%>
<tr>
<td><%= r.getId() %></td>
<td><%= r.getCustomerName() %></td>
<td><%= r.getContact() %></td>
<td><%= r.getTableId() %></td>
<td><%= r.getDateTime() %></td>
<td><%= r.getPartySize() %></td>
<td><%= r.getStatus() %></td>
<td>
<a href="reservation?action=edit&id=<%= r.getId() %>">Edit</a> |
<a href="reservation?action=delete&id=<%= r.getId() %>">Delete</a>
</td>
</tr>
<%
    }
}
%>
</table>
</body>
</html>
