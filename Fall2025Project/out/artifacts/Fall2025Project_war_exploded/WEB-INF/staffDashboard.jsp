<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User" %>
<%@ page import="model.Staff" %>

<%
    // Prevent customers from accessing staff/admin areas
    String roleCheck = (String) session.getAttribute("role");
    if ("customer".equalsIgnoreCase(roleCheck)) {
        response.sendRedirect("customerDashboard.jsp");
        return;
    }

    // Verify valid session
    Object obj = session.getAttribute("userObj");
    if (obj == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String role = "";
    String name = "";

    // Resolve user type safely (works for both Staff and User objects)
    if (obj instanceof Staff) {
        Staff staff = (Staff) obj;
        role = staff.getRole();
        name = staff.getName();
    } else if (obj instanceof User) {
        User user = (User) obj;
        role = user.getRole();
        name = user.getName();
    } else {
        response.sendRedirect("login.jsp");
        return;
    }

    // Build dynamic context path (e.g., /Fall2025Project)
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title><%= role.substring(0, 1).toUpperCase() + role.substring(1) %> Dashboard - Pizzas 505 ENMU</title>
    <style>
        body {
            font-family: "Segoe UI", Arial, sans-serif;
            background: #fff8f0;
            margin: 0;
            padding: 0;
        }
        header {
            background-color: #c0392b;
            color: white;
            text-align: center;
            padding: 20px 0;
        }
        .container {
            width: 80%;
            margin: 30px auto;
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 3px 10px rgba(0,0,0,0.1);
            padding: 25px 30px;
        }
        h2 { color: #c0392b; }
        .tool {
            background: #f6f6f6;
            border-left: 5px solid #c0392b;
            margin: 15px 0;
            padding: 12px;
            border-radius: 6px;
        }
        a {
            text-decoration: none;
            color: #007bff;
        }
        a:hover { text-decoration: underline; }

        .btn {
            display: inline-block;
            background: #007bff;
            color: white;
            padding: 10px 20px;
            border-radius: 6px;
            text-decoration: none;
            font-weight: bold;
            transition: background 0.3s;
        }
        .btn:hover { background: #0056b3; }

        hr { margin: 30px 0; }

        .logout {
            display: inline-block;
            background: #c0392b;
            color: white;
            padding: 10px 25px;
            border-radius: 6px;
            font-weight: bold;
            text-decoration: none;
        }
        .logout:hover { background: #a93226; }

        footer {
            text-align: center;
            color: #999;
            padding: 20px;
            margin-top: 40px;
        }
    </style>
</head>
<body>

<header>
    <h1>Welcome, <%= name %>!</h1>
    <h3>Role: <%= role.substring(0, 1).toUpperCase() + role.substring(1) %></h3>
</header>

<div class="container">

    <% if ("admin".equalsIgnoreCase(role)) { %>
    <h2>Admin Tools</h2>
    <div class="tool">🪑 <a href="<%= ctx %>/tables">Manage Tables</a></div>
    <div class="tool">👥 <a href="<%= ctx %>/staff">Manage Staff</a></div>
    <div class="tool">🍕 <a href="<%= ctx %>/menu">Manage Menu Items</a></div>
    <div class="tool">🗓 <a href="<%= ctx %>/tableLayout">View Table Layout</a></div>
    <div class="tool">📊 <a href="#">View Reports</a></div>

    <% } else if ("manager".equalsIgnoreCase(role)) { %>
    <h2>Manager Tools</h2>
    <div class="tool">🪑 <a href="<%= ctx %>/tableLayout">View Table Layout</a></div>
    <div class="tool">🧾 <a href="#">View Sales Reports</a></div>
    <div class="tool">📦 <a href="#">Reorder Inventory</a></div>
    <div class="tool">👥 <a href="#">Manage Staff Schedules</a></div>
    <div class="tool">🍕 <a href="<%= ctx %>/menu">Manage Menu Items</a></div>

    <% } else if ("server".equalsIgnoreCase(role)) { %>
    <h2>Server Tools</h2>
    <div class="tool">🪑 <a href="<%= ctx %>/tableLayout">View Table Layout</a></div>
    <div class="tool">🍕 <a href="#">View Assigned Tables</a></div>
    <div class="tool">💵 <a href="#">Input Customer Orders</a></div>
    <div class="tool">⏰ <a href="#">View Work Schedule</a></div>

    <% } else if ("kitchen".equalsIgnoreCase(role)) { %>
    <h2>Kitchen Tools</h2>
    <div class="tool">🪑 <a href="<%= ctx %>/tableLayout">View Table Layout</a></div>
    <div class="tool">🥫 <a href="#">View Orders to Prepare</a></div>
    <div class="tool">📋 <a href="#">Mark Orders as Ready</a></div>
    <div class="tool">🧼 <a href="#">View Cleaning Tasks</a></div>

    <% } else { %>
    <h2>General Staff Dashboard</h2>
    <div class="tool">🪑 <a href="<%= ctx %>/tableLayout">View Table Layout</a></div>
    <div class="tool">📄 <a href="#">View Announcements</a></div>
    <div class="tool">⏰ <a href="#">View Shift Schedule</a></div>
    <% } %>

    <hr>
    <div style="text-align:center;">
        <a href="logout.jsp" class="logout">🚪 Logout</a>
    </div>
</div>

<footer>
    © 2025 Pizzas 505 ENMU — Staff Portal
</footer>

</body>
</html>


