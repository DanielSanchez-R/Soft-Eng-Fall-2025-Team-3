<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Register - Pizzas 505 ENMU</title>
    <style>
        body { font-family: Arial; background-color: #f8f9fa; text-align:center; }
        form {
            display:inline-block; margin-top:50px; background:white; padding:30px;
            border-radius:10px; box-shadow:0 3px 8px rgba(0,0,0,0.1);
        }
        input { display:block; width:250px; margin:10px auto; padding:8px; }
        button {
            background:#28a745; color:white; border:none; padding:10px 20px;
            border-radius:5px; cursor:pointer; font-weight:bold;
        }
        button:hover { background:#1e7e34; }
        .alert { color:red; margin-bottom:10px; }
        .msg { color:green; margin-bottom:10px; }
    </style>
</head>
<body>
<h2>Create Your Account</h2>
<% if (request.getAttribute("error") != null) { %>
<div class="alert"><%= request.getAttribute("error") %></div>
<% } %>
<% if (request.getAttribute("message") != null) { %>
<div class="msg"><%= request.getAttribute("message") %></div>
<% } %>
<form action="customer" method="post">
    <input type="hidden" name="action" value="register">
    <input type="text" name="name" placeholder="Full Name" required>
    <input type="email" name="email" placeholder="Email Address" required>
    <input type="password" name="password" placeholder="Password" required>
    <input type="text" name="phone" placeholder="Phone Number" required>
    <button type="submit">Register</button>
    <p><a href="login.jsp">Already have an account? Login here</a></p>
</form>
</body>
</html>
