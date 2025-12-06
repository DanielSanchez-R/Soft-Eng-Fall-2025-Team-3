<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Set Your Password - Pizzas 505 ENMU</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        body {
            font-family: "Segoe UI", Arial, sans-serif;
            background: #fff8f0;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 400px;
            margin: 80px auto;
            background: white;
            padding: 25px 30px;
            border-radius: 10px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.15);
        }
        h2 {
            text-align: center;
            color: #c0392b;
        }
        p {
            text-align: center;
            color: #444;
            margin-top: -10px;
            margin-bottom: 20px;
        }
        input[type="password"],
        input[type="text"] {
            width: 100%;
            padding: 12px;
            border: 1px solid #ccc;
            border-radius: 6px;
            margin-bottom: 15px;
            font-size: 15px;
        }
        input[type="submit"] {
            width: 100%;
            background-color: #c0392b;
            color: white;
            border: none;
            padding: 12px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 15px;
            transition: 0.2s;
        }
        input[type="submit"]:hover {
            background-color: #a93226;
        }
        .error {
            color: #e74c3c;
            text-align: center;
            font-weight: bold;
        }
        .success {
            color: #27ae60;
            text-align: center;
            font-weight: bold;
        }
        .footer {
            text-align: center;
            margin-top: 25px;
            color: #888;
            font-size: 14px;
        }
    </style>
</head>
<body>

<div class="container">
    <h2>Set Your Password</h2>
    <p>Welcome to Pizzas 505 ENMU! Please create your new password below.</p>

    <%
        String error = (String) request.getAttribute("error");
        String success = (String) request.getAttribute("success");
        String email = (String) request.getAttribute("email");
        String token = (String) request.getAttribute("token");
        if (error != null) {
    %>
    <div class="error"><%= error %></div>
    <% } else if (success != null) { %>
    <div class="success"><%= success %></div>
    <% } %>

    <form action="auth" method="post" onsubmit="return validatePasswords();">
        <input type="hidden" name="action" value="savePassword">
        <input type="hidden" name="token" value="<%= token != null ? token : "" %>">

        <label><b>Email:</b></label><br>
        <input type="text" value="<%= email != null ? email : "Unknown" %>" readonly><br><br>

        <label><b>New Password:</b></label><br>
        <input type="password" name="password" placeholder="Enter new password" required><br>

        <label><b>Confirm Password:</b></label><br>
        <input type="password" id="confirmPassword" placeholder="Confirm password" required><br>

        <input type="submit" value="Set Password">
    </form>

    <div class="footer">
        © 2025 Pizzas 505 ENMU
    </div>
</div>

<% if (success != null) { %>
<script>
    setTimeout(() => {
        window.location.href = 'login.jsp';
    }, 2500);
</script>
<% } %>

<script>
    function validatePasswords() {
        const pw1 = document.querySelector('input[name="password"]').value;
        const pw2 = document.getElementById('confirmPassword').value;
        if (pw1 !== pw2) {
            alert("Passwords do not match!");
            return false;
        }
        return true;
    }
</script>

</body>
</html>

