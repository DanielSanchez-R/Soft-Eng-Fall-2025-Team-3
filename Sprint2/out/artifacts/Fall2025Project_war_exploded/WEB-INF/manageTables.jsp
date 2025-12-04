<%@ page import="java.util.*, model.TableInfo" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Manage Tables - Pizzas 505 ENMU</title>
    <style>
        body {
            font-family: Segoe UI, Arial, sans-serif;
            background: #fff8f0;
            padding: 20px;
            margin: 0;
        }
        h2 {
            color: #c0392b;
            text-align: center;
        }
        form {
            text-align: center;
            margin-bottom: 20px;
            background: #fff;
            padding: 15px;
            border-radius: 10px;
            width: 80%;
            margin-left: auto;
            margin-right: auto;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
        }
        input, button {
            padding: 6px;
            margin: 5px;
            border-radius: 4px;
            border: 1px solid #ccc;
        }
        button {
            background: #27ae60;
            color: white;
            border: none;
            cursor: pointer;
        }
        button:hover {
            background: #1e8449;
        }
        table {
            width: 90%;
            margin: 25px auto;
            border-collapse: collapse;
            background: white;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
        }
        th, td {
            padding: 10px;
            border-bottom: 1px solid #ddd;
            text-align: center;
        }
        th {
            background: #e74c3c;
            color: white;
        }
        .delete-btn {
            background: #e74c3c;
            color: white;
            border: none;
            padding: 6px 10px;
            border-radius: 4px;
            cursor: pointer;
        }
        .delete-btn:hover {
            background: #c0392b;
        }
        .no-tables {
            text-align: center;
            color: #777;
            margin-top: 15px;
        }
        .back-btn {
            display: block;
            text-align: center;
            background: #c0392b;
            color: #fff;
            padding: 10px 20px;
            border-radius: 6px;
            width: fit-content;
            margin: 25px auto 0;
            text-decoration: none;
            font-weight: bold;
        }
        .back-btn:hover {
            background: #a93226;
        }
    </style>
</head>
<body>

<h2>🪑 Table Management Console</h2>

<!-- Add Table Form -->
<form action="tables" method="post">
    <input type="hidden" name="action" value="add">
    <label>Table #:</label>
    <input type="text" name="table_number" required>
    <label>Capacity:</label>
    <input type="number" name="capacity" min="1" required>
    <label>Zone:</label>
    <select name="zone">
        <option>Main</option>
        <option>Patio</option>
        <option>VIP</option>
    </select>
    <label>Base Price:</label>
    <input type="number" step="0.01" name="base_price" min="0" required>
    <label>Surcharge:</label>
    <input type="number" step="0.01" name="surcharge" min="0" value="0">
    <button type="submit">➕ Add Table</button>
</form>

<hr>

<!-- Table List -->
<table>
    <tr>
        <th>ID</th>
        <th>Table #</th>
        <th>Capacity</th>
        <th>Zone</th>
        <th>Base</th>
        <th>Surcharge</th>
        <th>Total</th>
        <th>Action</th>
    </tr>

    <%
        List<TableInfo> tables = (List<TableInfo>) request.getAttribute("tables");
        if (tables == null || tables.isEmpty()) {
    %>
    <tr><td colspan="8" class="no-tables">No tables found. Add one above.</td></tr>
    <%
    } else {
        for (TableInfo t : tables) {
    %>
    <tr>
        <td><%= t.getId() %></td>
        <td><%= t.getTableNumber() %></td>
        <td><%= t.getCapacity() %></td>
        <td><%= t.getZone() %></td>
        <td>$<%= String.format("%.2f", t.getBasePrice()) %></td>
        <td>$<%= String.format("%.2f", t.getSurcharge()) %></td>
        <td>$<%= String.format("%.2f", t.getTotalPrice()) %></td>
        <td>
            <form action="tables" method="post" style="display:inline;">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="<%= t.getId() %>">
                <button type="submit" class="delete-btn">🗑 Delete</button>
            </form>
        </td>
    </tr>
    <%
            }
        }
    %>
</table>

<!-- Back to Dashboard -->
<a href="adminDashboard.jsp" class="back-btn">⬅️ Back to Admin Dashboard</a>

</body>
</html>
