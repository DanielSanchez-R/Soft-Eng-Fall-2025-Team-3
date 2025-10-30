<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Customer" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manage Customers</title>
    <style>
        body { font-family: Segoe UI, Arial; background: #f8f9fa; padding: 30px; }
        h1 { text-align: center; color: #2c3e50; }
        table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        th, td { padding: 12px; border-bottom: 1px solid #ddd; text-align: left; }
        th { background: #16a085; color: white; }
        .btn { padding: 6px 12px; border-radius: 6px; text-decoration: none; color: white; font-weight: 600; }
        .btn-delete { background: #e74c3c; }
        .btn-delete:hover { background: #c0392b; }
        .btn-back { background: #95a5a6; }
        .btn-back:hover { background: #7f8c8d; }
    </style>
</head>
<body>
<h1>Manage Customers</h1>
<a href="adminDashboard.jsp" class="btn btn-back">← Back to Dashboard</a>
<table>
    <tr>
        <th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Status</th><th>Action</th>
    </tr>
    <%
        List<Customer> list = (List<Customer>) request.getAttribute("customers");
        if (list == null || list.isEmpty()) {
    %>
    <tr><td colspan="6" style="text-align:center;">No customers found.</td></tr>
    <%
    } else {
        for (Customer c : list) {
    %>
    <tr>
        <td><%= c.getId() %></td>
        <td><%= c.getName() %></td>
        <td><%= c.getEmail() %></td>
        <td><%= c.getPhone() %></td>
        <td><%= c.isActive() ? "Active" : "Inactive" %></td>
        <td>
            <a href="customersAdmin?action=delete&id=<%= c.getId() %>"
               class="btn btn-delete"
               onclick="return confirm('Delete this customer account?');">🗑 Delete</a>
        </td>
    </tr>
    <%
            }
        }
    %>
</table>
</body>
</html>
