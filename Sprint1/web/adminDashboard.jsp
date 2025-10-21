<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="model.User" %>
<%
    // Simple session check
    User user = (User) session.getAttribute("user");
    if (user == null || !"admin".equals(user.getRole())) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<html>
<head>
    <title>Admin Dashboard</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            background: #f4f4f4;
        }
        header {
            background-color: #2e3b4e;
            color: #fff;
            padding: 15px;
            text-align: center;
        }
        .container {
            margin: 30px auto;
            max-width: 900px;
            background: white;
            padding: 25px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        h2 {
            border-bottom: 2px solid #ddd;
            padding-bottom: 10px;
        }
        .panel {
            margin: 20px 0;
            padding: 15px;
            background: #f8f8f8;
            border-radius: 6px;
        }
        .panel a {
            text-decoration: none;
            color: #2e3b4e;
            font-weight: bold;
            background: #ddd;
            padding: 8px 12px;
            border-radius: 4px;
            margin-right: 10px;
        }
        .logout {
            float: right;
            color: #fff;
            background: #c0392b;
            border-radius: 4px;
            padding: 6px 10px;
            text-decoration: none;
        }
        .logout:hover {
            background: #e74c3c;
        }
    </style>
</head>

<body>
<header>
    <h1>Welcome, <%= user.getName() %> (Admin)</h1>
    <a href="logout.jsp" class="logout">Logout</a>
</header>

<div class="container">
    <h2>Admin Control Panel</h2>

    <div class="panel">
        <h3>Menu Management</h3>
        <p>View, add, edit, or remove menu items.</p>
        <a href="menu">Go to Menu Items</a>
    </div>

    <div class="panel">
        <h3>Reservation Management</h3>
        <p>Manage reservations, view customer bookings, or mark cancellations.</p>
        <a href="reservation">Go to Reservations</a>
    </div>

    <div class="panel">
        <h3>Staff Management</h3>
        <p>Add or deactivate staff accounts. (coming soon)</p>
        <a href="#" onclick="alert('Staff module coming in Sprint 2')">Manage Staff</a>
    </div>

    <div class="panel">
        <h3>Reports</h3>
        <p>View or export daily revenue and sales reports. (Sprint 4 feature)</p>
        <a href="#" onclick="alert('Reports module coming in Sprint 4')">View Reports</a>
    </div>
</div>
</body>
</html>
