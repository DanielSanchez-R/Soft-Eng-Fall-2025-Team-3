<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Staff" %>

<!DOCTYPE html>
<html>
<head>
    <title>Staff Management</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f8f9fa;
            margin: 0;
            padding: 0;
        }
        h2 {
            color: #333;
            text-align: center;
            margin-top: 20px;
        }
        .btn {
            padding: 8px 12px;
            background-color: #007bff;
            border: none;
            color: white;
            border-radius: 4px;
            text-decoration: none;
            margin: 5px;
        }
        .btn:hover {
            background-color: #0056b3;
        }
        .container {
            width: 90%;
            margin: 0 auto;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            background: white;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        th, td {
            padding: 10px;
            text-align: center;
            border-bottom: 1px solid #ddd;
        }
        th {
            background-color: #007bff;
            color: white;
        }
        tr:hover {
            background-color: #f2f2f2;
        }
        .status-active {
            color: green;
            font-weight: bold;
        }
        .status-inactive {
            color: red;
            font-weight: bold;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>Staff Accounts</h2>

    <div style="text-align: center;">
        <a href="adminDashboard.jsp" class="btn">🏠 Dashboard</a>
        <a href="adminCreateStaff.jsp" class="btn">➕ Add Staff</a>
    </div>

    <%
        List<Staff> staffList = (List<Staff>) request.getAttribute("staffList");
        if (staffList == null || staffList.isEmpty()) {
    %>
    <p style="text-align:center;">No staff accounts found.</p>
    <%
    } else {
    %>
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Role</th>
            <th>Status</th>
            <th>Action</th>
        </tr>
        </thead>
        <tbody>
        <% for (Staff staff : staffList) { %>
        <tr>
            <td><%= staff.getId() %></td>
            <td><%= staff.getName() %></td>
            <td><%= staff.getEmail() %></td>
            <td><%= staff.getPhone() %></td>
            <td><%= staff.getRole() %></td>
            <td>
                <% if (staff.isActive()) { %>
                <span class="status-active">Active</span>
                <% } else { %>
                <span class="status-inactive">Inactive</span>
                <% } %>
            </td>
            <td>
                <% if (staff.isActive()) { %>
                <a href="staff?action=deactivate&id=<%= staff.getId() %>" class="btn" style="background-color:red;">Deactivate</a>
                <% } else { %>
                <a href="staff?action=activate&id=<%= staff.getId() %>" class="btn" style="background-color:green;">Activate</a>
                <% } %>

                <a href="staff?action=edit&id=<%= staff.getId() %>" class="btn" style="background-color:orange;">✏️ Update</a>

                <a href="staff?action=delete&id=<%= staff.getId() %>"
                   class="btn" style="background-color:#6c757d;"
                   onclick="return confirm('Are you sure you want to permanently delete this staff account?');">
                    🗑️ Delete
                </a>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <%
        }
    %>
</div>
</body>
</html>
