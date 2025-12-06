<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String ctx = request.getContextPath();
    String role = (String) session.getAttribute("role");
    if (role == null || !"admin".equalsIgnoreCase(role)) {
        response.sendRedirect("unauthorized.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Create Staff Account</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; background: #fff8f0; margin:0; padding:40px; }
        h2 { color:#c0392b; }
        form {
            background:#fff; padding:20px; border-radius:10px;
            box-shadow:0 2px 6px rgba(0,0,0,0.1); width:400px; margin:auto;
        }
        label { display:block; margin-top:10px; color:#333; }
        input, select {
            width:100%; padding:8px; margin-top:5px;
            border:1px solid #ccc; border-radius:4px;
        }
        button {
            margin-top:20px; background:#27ae60; color:white;
            border:none; padding:10px 15px; border-radius:6px; cursor:pointer;
        }
        button:hover { background:#1e8449; }
        a { display:block; text-align:center; margin-top:20px; color:#c0392b; text-decoration:none; }
    </style>
</head>
<body>

<h2>Create New Staff Account</h2>

<form action="<%= ctx %>/staff" method="post">
    <input type="hidden" name="action" value="add">

    <label>Name:</label>
    <input type="text" name="name" required>

    <label>Email:</label>
    <input type="email" name="email" required>

    <label>Phone:</label>
    <input type="text" name="phone">

    <label>Role:</label>
    <select name="role" required>
        <option value="server">Server</option>
        <option value="manager">Manager</option>
        <option value="kitchen">Kitchen</option>
    </select>

    <label>Password:</label>
    <input type="password" name="password" required>

    <button type="submit">➕ Create Staff</button>
</form>

<a href="<%= ctx %>/adminDashboard.jsp">⬅️ Back to Dashboard</a>

</body>
</html>
