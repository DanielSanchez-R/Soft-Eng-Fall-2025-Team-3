<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.MonthlyRevenue, java.util.List, model.DailySale" %>

<%
    String ctx = request.getContextPath();
    String role = (String) session.getAttribute("role");
    if (role == null || !"admin".equalsIgnoreCase(role)) {
        response.sendRedirect(ctx + "/unauthorized.jsp");
        return;
    }

    MonthlyRevenue revenue = (MonthlyRevenue) request.getAttribute("revenue");
    List<DailySale> monthlySales = (List<DailySale>) request.getAttribute("monthlySales");

    String[] monthNames = {
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
    };
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Monthly Revenue Report</title>

    <style>
        body { font-family: Segoe UI, Arial, sans-serif; background:#f9f9f9; padding:20px; }
        h2   { color:#2c3e50; }

        table {
            width: 60%;
            margin-top: 20px;
            border-collapse: collapse;
            background: white;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
        }
        th, td {
            padding: 10px;
            border-bottom: 1px solid #ddd;
            text-align: center;
        }
        th { background: #e74c3c; color: white; }

        .btn {
            display:inline-block; padding:8px 15px; border-radius:5px;
            color:white; text-decoration:none; margin-top:20px;
        }
        .btn-back { background:#e74c3c; }
        .btn-back:hover { background:#c0392b; }
    </style>
</head>

<body>

<h2>📈 Monthly Revenue Report</h2>

<%
    if (revenue == null) {
%>
<p>No revenue found for this month.</p>
<%
} else {
    int m = revenue.getMonth();
    int y = revenue.getYear();
%>

<!-- Summary table -->
<table>
    <tr>
        <th>Month</th>
        <th>Year</th>
        <th>Total Revenue</th>
    </tr>
    <tr>
        <td><%= monthNames[m - 1] %></td>
        <td><%= y %></td>
        <td>$<%= String.format("%.2f", revenue.getRevenue()) %></td>
    </tr>
</table>

<br/>

<% if (monthlySales != null && !monthlySales.isEmpty()) {
    double totalOrdersAmount = 0.0;
    for (DailySale s : monthlySales) { totalOrdersAmount += s.getTotal(); }
%>

<h3>Orders in This Month</h3>
<p>
    Total Orders: <%= monthlySales.size() %> |
    Sum of Orders: $<%= String.format("%.2f", totalOrdersAmount) %>
</p>

<table style="width: 90%;">
    <tr>
        <th>Order ID</th>
        <th>Customer Email</th>
        <th>Total</th>
        <th>Order Date/Time</th>
    </tr>
    <% for (DailySale s : monthlySales) { %>
    <tr>
        <td><%= s.getOrderId() %></td>
        <td><%= s.getCustomerEmail() %></td>
        <td>$<%= String.format("%.2f", s.getTotal()) %></td>
        <td><%= s.getOrderTime() %></td>
    </tr>
    <% } %>
</table>

<% } else { %>
<p>No orders were found for this month.</p>
<% } %> <!-- closes inner if(monthlySales...) -->

<% } %> <!-- closes outer if(revenue == null) -->

<a href="<%= ctx %>/adminReports.jsp" class="btn btn-back">⬅ Back to Reports</a>

</body>
</html>
