<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.DailySale" %>

<%
    String ctx = request.getContextPath();
    String role = (String) session.getAttribute("role");
    if (role == null || !"admin".equalsIgnoreCase(role)) {
        response.sendRedirect(ctx + "/unauthorized.jsp");
        return;
    }

    List<DailySale> sales = (List<DailySale>) request.getAttribute("sales");
    String startDate = (String) request.getAttribute("startDate");
    String endDate   = (String) request.getAttribute("endDate");

    String headingRange;
    if (startDate != null && endDate != null && !startDate.equals(endDate)) {
        headingRange = startDate + " to " + endDate;
    } else {
        headingRange = (startDate != null ? startDate : "");
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Daily Sales Report</title>
    <style>
        body {
            font-family: Segoe UI, Arial, sans-serif;
            background: #ffffff;
            margin: 0;
            padding: 15px;
            color: #333;
        }
        h2 {
            color: #2c3e50;
            margin-top: 0;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            background: white;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
            margin-top: 10px;
        }
        th, td {
            padding: 8px 10px;
            border-bottom: 1px solid #ddd;
            text-align: center;
        }
        th {
            background: #e74c3c;
            color: white;
        }
        tr:nth-child(even) {
            background: #f9f9f9;
        }
        .summary {
            margin-top: 10px;
            font-weight: bold;
        }
    </style>
</head>
<body>

<h2>Daily Sales – <%= headingRange %></h2>

<%
    if (sales == null || sales.isEmpty()) {
%>
<p>No sales found for this date.</p>
<%
} else {
    double total = 0.0;
    for (DailySale s : sales) {
        total += s.getTotal();
    }
%>

<div class="summary">
    Total Orders: <%= sales.size() %> |
    Total Sales: $<%= String.format("%.2f", total) %>
</div>

<table>
    <tr>
        <th>Order ID</th>
        <th>Customer Email</th>
        <th>Total</th>
        <th>Order Time</th>
    </tr>
    <% for (DailySale s : sales) { %>
    <tr>
        <td><%= s.getOrderId() %></td>
        <td><%= s.getCustomerEmail() %></td>
        <td>$<%= String.format("%.2f", s.getTotal()) %></td>
        <td><%= s.getOrderTime() %></td>
    </tr>
    <% } %>
</table>

<%
    }
%>

</body>
</html>
