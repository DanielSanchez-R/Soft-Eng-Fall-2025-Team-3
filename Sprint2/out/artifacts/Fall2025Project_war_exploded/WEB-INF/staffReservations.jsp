<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Reservation" %>
<!DOCTYPE html>
<html>
<head>
    <title>Staff Reservations</title>
    <style>
        body { font-family: Segoe UI, sans-serif; background: #f9f9f9; margin: 0; padding: 20px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; background: #fff; }
        th, td { padding: 10px; border-bottom: 1px solid #ddd; text-align: center; }
        th { background: #e74c3c; color: white; }
        tr:nth-child(even) { background: #f5f5f5; }
        a.btn { display: inline-block; padding: 8px 15px; margin-top: 20px; background: #3498db; color: white; border-radius: 5px; text-decoration: none; }
        a.btn:hover { background: #2980b9; }
    </style>
</head>
<body>
<h2>📋 Staff Reservations</h2>

<%
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    if (reservations == null || reservations.isEmpty()) {
%>
<p>No staff reservations found.</p>
<%
} else {
%>
<table>
    <tr>
        <th>ID</th><th>Customer</th><th>Table #</th><th>Date</th><th>Time</th><th>Staff</th><th>Status</th>
    </tr>
    <%
        for (Reservation r : reservations) {
    %>
    <tr>
        <td><%= r.getId() %></td>
        <td><%= r.getCustomerName() %></td>
        <td><%= r.getTableNumber() %></td>
        <td><%= r.getDate() %></td>
        <td><%= r.getTime() %></td>
        <td><%= r.getStaffName() %></td>
        <td><%= r.getStatus() %></td>
    </tr>
    <%
        }
    %>
</table>
<%
    }
%>

<a href="adminDashboard.jsp" class="btn">⬅ Back to Dashboard</a>
</body>
</html>
