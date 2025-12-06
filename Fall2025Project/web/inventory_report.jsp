<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, model.InventoryReportItem" %>

<%
    String ctx = request.getContextPath();
    String role = (String) session.getAttribute("role");
    if (role == null || !"admin".equalsIgnoreCase(role)) {
        response.sendRedirect(ctx + "/unauthorized.jsp");
        return;
    }

    // Attribute name should match what ReportController sets.
    // If your controller uses a different name, change "items" here.
    List<InventoryReportItem> items =
            (List<InventoryReportItem>) request.getAttribute("items");

    Integer thresholdObj = (Integer) request.getAttribute("threshold");
    int threshold = (thresholdObj != null ? thresholdObj : 10);
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Inventory Report</title>

    <style>
        body {
            font-family: Segoe UI, Arial, sans-serif;
            background:#ffffff;
            padding:20px;
            margin:0;
            color:#333;
        }
        h2 { color:#2c3e50; margin-top:0; }
        p  { margin:5px 0 10px 0; }

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
        .low-row {
            background: #fdecea;          /* light red */
        }
        .badge-low {
            display:inline-block;
            padding: 3px 8px;
            border-radius: 10px;
            background:#e74c3c;
            color:white;
            font-size: 12px;
        }
        .badge-ok {
            display:inline-block;
            padding: 3px 8px;
            border-radius: 10px;
            background:#27ae60;
            color:white;
            font-size: 12px;
        }

        .btn {
            display:inline-block;
            padding:8px 15px;
            border-radius:5px;
            color:white;
            text-decoration:none;
            margin-top:15px;
        }
        .btn-back { background:#e74c3c; }
        .btn-back:hover { background:#c0392b; }
    </style>
</head>
<body>

<h2>📦 Inventory Report</h2>
<p>Low-stock threshold: <strong><%= threshold %></strong> units or less.</p>

<%
    if (items == null || items.isEmpty()) {
%>
<p>No inventory items were found.</p>
<%
} else {
%>
<table>
    <tr>
        <th>Item ID</th>
        <th>Name</th>
        <th>Stock</th>
        <th>Status</th>
    </tr>
    <%
        for (InventoryReportItem it : items) {
            boolean low = it.isLowStock();
    %>
    <tr class="<%= low ? "low-row" : "" %>">
        <td><%= it.getItemId() %></td>
        <td><%= it.getName() %></td>
        <td><%= it.getStock() %></td>
        <td>
            <% if (low) { %>
            <span class="badge-low">Low</span>
            <% } else { %>
            <span class="badge-ok">OK</span>
            <% } %>
        </td>
    </tr>
    <% } %>
</table>
<%
    }
%>

<a href="<%= ctx %>/adminReports.jsp" class="btn btn-back">⬅ Back to Reports</a>

</body>
</html>

