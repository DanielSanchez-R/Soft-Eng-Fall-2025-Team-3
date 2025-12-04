<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Restaurant Login</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #fafafa;
            text-align: center;
            margin-top: 100px;
        }
        form {
            background-color: white;
            border: 1px solid #ddd;
            display: inline-block;
            padding: 30px 40px;
            border-radius: 8px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.15);
        }
        input[type=text], input[type=password] {
            width: 220px;
            padding: 6px;
            margin: 5px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }
        input[type=submit] {
            margin-top: 10px;
            width: 100%;
            padding: 8px;
            border: none;
            background-color: #0066cc;
            color: white;
            border-radius: 4px;
            cursor: pointer;
        }
        input[type=submit]:hover {
            background-color: #004c99;
        }
        .error {
            color: red;
            margin-top: 10px;
        }
    </style>
</head>
<body>
<h2>Login</h2>

<form action="auth" method="post">
    <label>Email:</label><br>
    <input type="text" name="email" required /><br>
    <label>Password:</label><br>
    <input type="password" name="password" required /><br>
    <input type="submit" value="Login" />
</form>

<% if (request.getAttribute("error") != null) { %>
<div class="error"><%= request.getAttribute("error") %></div>
<% } %>

</body>
</html>

