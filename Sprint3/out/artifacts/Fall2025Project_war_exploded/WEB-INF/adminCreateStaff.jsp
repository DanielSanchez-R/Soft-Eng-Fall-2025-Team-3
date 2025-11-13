<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Create Staff Account</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 40px;
            background-color: #f8f9fa;
        }
        h2 { color: #2c3e50; }
        form {
            background: white;
            padding: 20px;
            border-radius: 8px;
            width: 400px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        label { display: block; margin-top: 10px; }
        input, select {
            width: 100%;
            padding: 8px;
            margin-top: 4px;
            border-radius: 5px;
            border: 1px solid #ccc;
        }
        button {
            margin-top: 15px;
            padding: 10px 15px;
            background-color: #28a745;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        button:hover { background-color: #218838; }
        .msg { margin-top: 15px; }
        .success { color: green; }
        .error { color: red; }
        a.back {
            display: inline-block;
            margin-top: 20px;
            color: #007bff;
            text-decoration: none;
        }
        a.back:hover { text-decoration: underline; }
    </style>
</head>
<body>
<h2>Create Staff Account</h2>

<form action="staff" method="post">
    <label>Name:</label>
    <input type="text" name="name" required>

    <label>Email:</label>
    <input type="email" name="email" required>

    <label>Phone:</label>
    <input type="text" name="phone">

    <label>Role:</label>
    <select name="role">
        <option value="server">Server</option>
        <option value="manager">Manager</option>
        <option value="kitchen">Kitchen Staff</option>
    </select>

    <label>Password:</label>
    <input type="password" name="password" required placeholder="Min 8 chars, 1 number, 1 special">

    <button type="submit">Create</button>
</form>

<div class="msg">
    <% if (request.getAttribute("message") != null) { %>
    <p class="success"><%= request.getAttribute("message") %></p>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
    <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>
</div>

<a href="adminDashboard.jsp" class="back">← Back to Dashboard</a>
</body>
</html>

