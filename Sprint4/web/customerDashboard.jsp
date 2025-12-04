<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Customer, model.User" %>
<%
    // handle both Customer and User session types
    Object userObj = session.getAttribute("userObj");
    if (userObj == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String name = "";
    String email = "";
    String phone = "";

    // Works if session has a real Customer object
    if (userObj instanceof Customer) {
        Customer customer = (Customer) userObj;
        name = customer.getName();
        email = customer.getEmail();
        phone = customer.getPhone();
    }
    // Works if session has a generic User object with role "customer"
    else if (userObj instanceof User && "customer".equalsIgnoreCase(((User) userObj).getRole())) {
        User user = (User) userObj;
        name = user.getName();
        email = user.getEmail();
        phone = ""; // optional, User may not store phone
    }
    else {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Customer Dashboard - Pizzas 505 ENMU</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <style>
        :root {
            --primary:#c0392b;
            --accent:#e74c3c;
            --light:#fff8f0;
            --btn:#007bff;
            --btn-dark:#0056b3;
        }
        body {
            font-family: "Segoe UI", Arial, sans-serif;
            background: var(--light);
            margin: 0;
            padding: 0;
        }
        header {
            background: var(--primary);
            color: #fff;
            padding: 20px;
            text-align: center;
        }
        h1 {
            margin: 0;
            font-size: 1.8em;
        }
        .content {
            max-width: 800px;
            margin: 30px auto;
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 3px 8px rgba(0,0,0,0.1);
            padding: 25px;
        }
        h2 {
            color: var(--primary);
            border-bottom: 2px solid var(--accent);
            padding-bottom: 8px;
            text-align: center;
        }
        .info {
            text-align: center;
            margin-top: 15px;
            color: #444;
        }
        .info p {
            margin: 8px 0;
        }
        .buttons {
            display: flex;
            justify-content: center;
            flex-wrap: wrap;
            gap: 15px;
            margin-top: 30px;
        }
        .btn {
            background: var(--btn);
            color: white;
            text-decoration: none;
            padding: 12px 20px;
            border-radius: 6px;
            font-weight: bold;
            transition: 0.2s;
        }
        .btn:hover {
            background: var(--btn-dark);
        }
        .btn-teal {background: #16a085;}
        .btn-teal:hover {background: #12806b;}
        .logout {
            background: #888;
        }
        .logout:hover {
            background: #555;
        }
        footer {
            text-align: center;
            color: #999;
            padding: 18px;
            margin-top: 30px;
        }
    </style>
</head>
<body>

<header>
    <h1>🍕 Welcome to Pizzas 505 ENMU, <%= name %>!</h1>
</header>

<div class="content">
    <h2>Your Account Details</h2>
    <div class="info">
        <p><strong>Name:</strong> <%= name %></p>
        <p><strong>Email:</strong> <%= email %></p>
        <p><strong>Phone:</strong> <%= phone.isEmpty() ? "Not provided" : phone %></p>
    </div>

    <div class="buttons">
        <a href="customerReservation?action=form" class="btn">📅 Book a Table</a>
        <a href="customerReservation?action=list" class="btn">📖 View My Reservations</a>
        <a href="<%= request.getContextPath() %>/tableLayout" class="btn btn-teal">🪑 View Table Layout</a>
        <a href="orderPizza.jsp" class="btn">🍕 Order Pizza</a>
        <a href="viewOrders.jsp" class="btn">📦 View My Orders</a>
        <a href="logout.jsp" class="btn logout">🚪 Logout</a>
    </div>
</div>

<footer>
    © 2025 Pizzas 505 ENMU — Thank you for dining with us!
</footer>

</body>
</html>

