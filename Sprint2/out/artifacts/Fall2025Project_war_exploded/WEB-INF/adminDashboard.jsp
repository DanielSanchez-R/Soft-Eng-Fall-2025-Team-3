<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.sql.*, dao.DBConnection, dao.TableDao, model.TableInfo" %>
<%
    String ctx = request.getContextPath();
%>

<%
    //String ctx = request.getContextPath();
    String role = (String) session.getAttribute("role");
    if (role == null || !"admin".equalsIgnoreCase(role)) {
        response.sendRedirect("unauthorized.jsp");
        return;
    }

    // Load all tables from DB
    Connection conn = null;
    List<TableInfo> tables = null;
    try {
        conn = DBConnection.getConnection();
        TableDao dao = new TableDao(conn);
        tables = dao.getAllTables();
    } catch (Exception e) {
        e.printStackTrace();
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>
    <style>
        body {
            font-family: Segoe UI, Arial, sans-serif;
            background: #f9f9f9;
            margin: 0;
            padding: 40px;
            color: #333;
        }
        h2 { color: #2c3e50; }
        h3 { color: #c0392b; }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            border: none;
            color: #fff;
            text-decoration: none;
            border-radius: 5px;
            margin-right: 10px;
            cursor: pointer;
        }
        .btn-blue { background: #3498db; }
        .btn-blue:hover { background: #2980b9; }
        .btn-teal {background: #16a085; /* teal green */}
        .btn-teal:hover {background: #12806b;}
        .btn-green { background: #27ae60; }
        .btn-green:hover { background: #1e8449; }
        .btn-red { background: #e74c3c; }
        .btn-red:hover { background: #c0392b; }
        .btn-orange { background: #d35400; }
        .btn-orange:hover { background: #a84300; }
        .btn-purple { background: #8e44ad; }
        .btn-purple:hover { background: #6c3483; }
        .section {
            margin-top: 30px;
        }
        table {
            width: 90%;
            margin: 20px auto;
            border-collapse: collapse;
            background: white;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
        }
        th, td {
            padding: 10px;
            border-bottom: 1px solid #ddd;
            text-align: center;
        }
        th {
            background: #e74c3c;
            color: white;
        }
        tr:nth-child(even) { background: #f9f9f9; }
        .summary {
            text-align: center;
            margin-top: 15px;
            font-weight: bold;
            color: #2c3e50;
        }
    </style>
</head>
<body>

<h2>Welcome, Admin!</h2>
<p>You are logged in as: <strong>Admin</strong></p>

<!-- Management Buttons -->
<div class="section">
    <a href="<%= ctx %>/tableLayout" class="btn btn-teal">🪑 View Table Layout</a>
    <a href="<%= ctx %>/staff" class="btn btn-blue">👥 Manage Staff</a>
    <a href="<%= ctx %>/createStaff.jsp" class="btn btn-green">➕ Create Staff Account</a>
    <a href="<%= ctx %>/logout.jsp" class="btn btn-red">⏻ Logout</a>
</div>

<div class="section">
    <h3>Account Management</h3>
    <a href="<%= ctx %>/staff" class="btn btn-blue">👥 Manage Staff</a>
    <a href="<%= ctx %>/customersAdmin" class="btn btn-teal">🧍 Manage Customers</a>
</div>

<div class="section">
    <h3>Restaurant Management</h3>
    <a href="<%= ctx %>/menu" class="btn btn-blue">🍕 Manage Menu Items</a>
    <a href="<%= ctx %>/tables" class="btn btn-orange">🪑 Manage Tables</a>
    <a href="#" class="btn btn-green">📊 View Reports</a>
</div>

<!-- 🧾 Reservation Management Section -->
<div class="section">
    <h3>Reservation Management</h3>
    <a href="<%= ctx %>/reservation?action=list" class="btn btn-purple">📅 Manage Staff Reservations</a>
    <a href="<%= ctx %>/customerReservation?action=list" class="btn btn-green">🧍 Manage Customer Reservations</a>
</div>

<hr>

<!-- Table Overview -->
<h3 style="text-align:center;">🪑 Current Tables Overview</h3>

<table>
    <tr>
        <th>ID</th>
        <th>Table #</th>
        <th>Capacity</th>
        <th>Zone</th>
        <th>Base Price</th>
        <th>Surcharge</th>
        <th>Total Price</th>
    </tr>
    <%
        if (tables == null || tables.isEmpty()) {
    %>
    <tr><td colspan="7" style="text-align:center; color:#777;">No tables added yet.</td></tr>
    <%
    } else {
        double totalPriceSum = 0;
        int totalCapacity = 0;
        for (TableInfo t : tables) {
            totalPriceSum += t.getTotalPrice();
            totalCapacity += t.getCapacity();
    %>
    <tr>
        <td><%= t.getId() %></td>
        <td><%= t.getTableNumber() %></td>
        <td><%= t.getCapacity() %></td>
        <td><%= t.getZone() %></td>
        <td>$<%= String.format("%.2f", t.getBasePrice()) %></td>
        <td>$<%= String.format("%.2f", t.getSurcharge()) %></td>
        <td>$<%= String.format("%.2f", t.getTotalPrice()) %></td>
    </tr>
    <%
        } // end for
        double avgPrice = totalPriceSum / tables.size();
    %>
</table>

<div class="summary">
    🧾 Total Tables: <%= tables.size() %> |
    💺 Total Capacity: <%= totalCapacity %> |
    💵 Average Price: $<%= String.format("%.2f", avgPrice) %>
</div>

<%
    } // end else
    if (conn != null) conn.close();
%>

</body>
</html>




