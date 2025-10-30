<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Staff" %>

<!DOCTYPE html>
<html>
<head>
    <title>Edit Staff</title>
    <style>
        body { font-family: Arial; background-color: #f8f9fa; text-align:center; }
        form {
            display:inline-block; text-align:left; background:#fff; padding:20px;
            border-radius:8px; box-shadow:0 2px 8px rgba(0,0,0,0.1); margin-top:40px;
        }
        label { display:block; margin-top:10px; font-weight:bold; }
        input, select { width:250px; padding:6px; margin-top:4px; }
        button { margin-top:15px; padding:8px 12px; background:#007bff;
            border:none; color:white; border-radius:4px; cursor:pointer; }
        button:hover { background:#0056b3; }
        a { text-decoration:none; color:#007bff; }
    </style>
</head>
<body>

<%
    Staff staff = (Staff) request.getAttribute("staff");
    if (staff == null) {
%>
<h3>Staff record not found.</h3>
<%
} else {
%>
<h2>Edit Staff Account</h2>
<form action="staff" method="post">
    <input type="hidden" name="action" value="update">
    <input type="hidden" name="id" value="<%= staff.getId() %>">

    <label>Name:</label>
    <input type="text" name="name" value="<%= staff.getName() %>" required>

    <label>Email:</label>
    <input type="email" name="email" value="<%= staff.getEmail() %>" required>

    <label>Phone:</label>
    <input type="text" name="phone" value="<%= staff.getPhone() %>">

    <label>Role:</label>
    <select name="role">
        <option value="server" <%= staff.getRole().equals("server") ? "selected" : "" %>>Server</option>
        <option value="manager" <%= staff.getRole().equals("manager") ? "selected" : "" %>>Manager</option>
        <option value="kitchen" <%= staff.getRole().equals("kitchen") ? "selected" : "" %>>Kitchen</option>
    </select>

    <br>
    <button type="submit">💾 Save Changes</button>
    <a href="staff?action=list">Cancel</a>
</form>
<%
    }
%>

</body>
</html>
