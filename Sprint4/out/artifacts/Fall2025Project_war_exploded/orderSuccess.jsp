<%@ page import="java.util.*, model.MenuItem" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Order Complete - Pizzas 505 ENMU</title>
    <style>
        body { font-family:Segoe UI,Arial; background:#fff8f0; text-align:center; margin-top:100px; }
        .box { background:white; width:60%; margin:auto; padding:40px;
            border-radius:10px; box-shadow:0 3px 10px rgba(0,0,0,0.1); }
        h1 { color:#27ae60; }
        a { display:inline-block; margin-top:20px; background:#c0392b; color:white;
            text-decoration:none; padding:10px 25px; border-radius:6px; }
        a:hover { background:#a93226; }
        .details { margin-top:15px; color:#555; line-height: 1.6; }
    </style>
</head>
<body>

<%
    String orderId = request.getParameter("orderId");

    // Pull stored checkout data
    String email = (String) session.getAttribute("email");
    if (email == null) email = "guest@pizzas505.com";

    String method = (String) session.getAttribute("deliveryMethod");  // "pickup" or "delivery"
    String timeSelected = (String) session.getAttribute("orderTime");
    Double total = (Double) session.getAttribute("orderTotal");

    // Build estimate message
    String estimate;
    if ("delivery".equals(method)) {
        estimate = "Your estimated delivery time is 45 minutes after " + timeSelected + ".";
    } else {
        estimate = "Your estimated pickup time is 15 minutes after " + timeSelected + ".";
    }
%>

<div class="box">
    <h1>Thank You!</h1>
    <p>Your order has been successfully placed.</p>

    <div class="details">
        <p><b>Order Number:</b> <%= orderId != null ? orderId : "N/A" %></p>

        <p><b>Total Paid:</b>
            $<%= total != null ? String.format("%.2f", total) : "0.00" %>
        </p>

        <p><b>Order Time Selected:</b> <%= timeSelected != null ? timeSelected : "N/A" %></p>

        <p><b><%= estimate %></b></p>

        <p>A confirmation email with your full receipt was sent to:</p>
        <p><b><%= email %></b></p>
    </div>

    <a href="customerDashboard.jsp">Return to Dashboard</a>
</div>

</body>
</html>

