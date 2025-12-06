<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Reservation" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reservation List (Staff/Admin)</title>
</head>
<body style="font-family:Segoe UI, Arial; margin:40px;">
<h2>📅 Staff / Admin Reservation List</h2>

<%
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    if (reservations == null || reservations.isEmpty()) {
%>
<p>No reservations found in the system.</p>
<%
} else {
%>
<table border="1" cellspacing="0" cellpadding="6">
    <tr>
        <th>ID</th>
        <th>Customer</th>
        <th>Table</th>
        <th>Date/Time</th>
        <th>Party</th>
        <th>Status</th>
    </tr>
    <%
        for (Reservation r : reservations) {
    %>
    <tr>
        <td><%= r.getId() %></td>
        <td><%= r.getCustomerName() %></td>
        <td><%= r.getTableId() %></td>
        <td><%= r.getDateTime() %></td>
        <td><%= r.getPartySize() %></td>
        <td><%= r.getStatus() %></td>
    </tr>
    <%
        }
    %>
</table>
<%
    }
%>

<p><a href="adminDashboard.jsp">← Back to Admin Dashboard</a></p>
</body>
</html>
