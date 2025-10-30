<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Forgot Password</title>
    <style>
        body { font-family: Arial; background-color: #f8f9fa; text-align:center; }
        form {
            display:inline-block; margin-top:50px; background:white; padding:30px;
            border-radius:10px; box-shadow:0 3px 8px rgba(0,0,0,0.1);
        }
        input { display:block; width:250px; margin:10px auto; padding:8px; }
        button {
            background:#007bff; color:white; border:none; padding:10px 20px;
            border-radius:5px; cursor:pointer; font-weight:bold;
        }
        button:hover { background:#0056b3; }
    </style>
</head>
<body>
<h2>Reset Your Password</h2>

<% if (request.getAttribute("error") != null) { %>
<p style="color:red;"><%= request.getAttribute("error") %></p>
<% } %>
<% if (request.getAttribute("message") != null) { %>
<p style="color:green;"><%= request.getAttribute("message") %></p>
<% } %>

<form action="customer" method="post">
    <input type="hidden" name="action" value="forgot">
    <input type="email" name="email" placeholder="Your registered email" required>
    <input type="password" name="newPassword" placeholder="New password" required>
    <button type="submit">Update Password</button>
</form>

<p><a href="login.jsp">Back to Login</a></p>
</body>
</html>
