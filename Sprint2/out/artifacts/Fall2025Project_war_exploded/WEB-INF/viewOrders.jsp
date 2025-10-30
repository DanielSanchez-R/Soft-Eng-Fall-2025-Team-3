<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User, model.Customer" %>
<%
    Object userObj = session.getAttribute("userObj");
    if (userObj == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String customerName = (userObj instanceof Customer)
            ? ((Customer) userObj).getName()
            : ((User) userObj).getName();
%>
<!DOCTYPE html>
<html>
<head>
    <title>My Orders - Pizzas 505 ENMU</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <style>
        body {
            font-family: "Segoe UI", Arial;
            background: #fff8f0;
            margin: 0; padding: 0;
        }
        header {
            background: #c0392b;
            color: white;
            text-align: center;
            padding: 15px 0;
        }
        .container {
            max-width: 800px;
            background: #fff;
            margin: 30px auto;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 3px 8px rgba(0,0,0,0.1);
        }
        table {
            width: 100%; border-collapse: collapse;
        }
        th, td {
            padding: 10px; border-bottom: 1px solid #ddd; text-align: center;
        }
        th {
            background: #e74c3c; color: white;
        }
        .btn {
            display: inline-block;
            background: #007bff; color: white;
            padding: 8px 15px; border-radius: 6px;
            text-decoration: none;
        }
        .btn:hover { background: #0056b3; }
    </style>
</head>
<body>

<header>
    <h1>📦 Your Orders, <%= customerName %></h1>
</header>

<div class="container">
    <h2 style="color:#c0392b;text-align:center;">Recent Orders</h2>
    <table>
        <tr>
            <th>Order ID</th>
            <th>Size</th>
            <th>Toppings</th>
            <th>Qty</th>
            <th>Status</th>
        </tr>
        <tr>
            <td>101</td>
            <td>Large</td>
            <td>Pepperoni, Mushrooms</td>
            <td>1</td>
            <td><span style="color:green;">Delivered</span></td>
        </tr>
        <tr>
            <td>102</td>
            <td>Medium</td>
            <td>Veggie</td>
            <td>2</td>
            <td><span style="color:orange;">Preparing</span></td>
        </tr>
    </table>

    <div style="text-align:center;margin-top:20px;">
        <a href="customerDashboard.jsp" class="btn">⬅ Back to Dashboard</a>
    </div>
</div>

</body>
</html>
