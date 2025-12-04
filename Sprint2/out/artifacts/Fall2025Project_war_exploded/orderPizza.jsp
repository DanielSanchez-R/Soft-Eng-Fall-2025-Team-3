<%@ page import="java.util.*, java.sql.*, dao.DBConnection, dao.MenuItemDao, model.MenuItem" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Order Pizza - Pizzas 505 ENMU</title>
    <style>
        body {
            font-family: Segoe UI, Arial, sans-serif;
            background: #fff8f0;
            margin: 0;
            padding: 0;
        }
        header {
            background: #c0392b;
            color: white;
            text-align: center;
            padding: 15px 0;
            font-size: 20px;
        }
        .container {
            width: 60%;
            margin: 30px auto;
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 3px 10px rgba(0,0,0,0.1);
        }
        h2 {
            color: #c0392b;
            text-align: center;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }
        th, td {
            border-bottom: 1px solid #ddd;
            padding: 10px;
            text-align: left;
        }
        th {
            background: #e74c3c;
            color: white;
        }
        tr.unavailable td {
            color: #aaa;
            text-decoration: line-through;
        }
        button {
            background: #27ae60;
            color: white;
            border: none;
            padding: 8px 12px;
            border-radius: 5px;
            cursor: pointer;
        }
        button:hover {
            background: #1e8449;
        }
        .back-btn {
            display: block;
            text-align: center;
            margin-top: 25px;
        }
        .back-btn a {
            color: white;
            background: #c0392b;
            padding: 10px 20px;
            border-radius: 6px;
            text-decoration: none;
        }
        .back-btn a:hover {
            background: #a93226;
        }
    </style>
</head>
<body>
<header>Place Your Order — Pizzas 505 ENMU</header>
<div class="container">
    <h2>Available Menu</h2>

    <%
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            MenuItemDao dao = new MenuItemDao(conn);
            List<MenuItem> items = dao.getAllMenuItems();

            // Java 7 compatible grouping (NO lambdas, NO <> operator)
            Map grouped = new LinkedHashMap();
            for (int i = 0; i < items.size(); i++) {
                MenuItem item = items.get(i);
                if (item.isAvailable() && !item.isDraft()) {
                    String cat = item.getCategory();
                    List list = (List) grouped.get(cat);
                    if (list == null) {
                        list = new ArrayList();
                        grouped.put(cat, list);
                    }
                    list.add(item);
                }
            }

            if (grouped.isEmpty()) {
    %>
    <p style="text-align:center; color:#999;">No available menu items right now. Please check back soon!</p>
    <%
    } else {
        for (Object entryObj : grouped.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            String category = (String) entry.getKey();
            List categoryItems = (List) entry.getValue();
    %>
    <h3 style="color:#c0392b;"><%= category %></h3>
    <table>
        <tr>
            <th>Name</th><th>Description</th><th>Price</th><th>Order</th>
        </tr>
        <%
            for (int j = 0; j < categoryItems.size(); j++) {
                MenuItem m = (MenuItem) categoryItems.get(j);
        %>
        <tr>
            <td><%= m.getName() %></td>
            <td><%= m.getDescription() %></td>
            <td>$<%= String.format("%.2f", m.getPrice()) %></td>
            <td>
                <form action="order" method="post">
                    <input type="hidden" name="itemId" value="<%= m.getId() %>">
                    <input type="number" name="quantity" min="1" max="10" value="1" style="width:50px;">
                    <button type="submit">Add to Order</button>
                </form>
            </td>
        </tr>
        <%
            }
        %>
    </table>
    <%
                }
            }
        } catch (Exception e) {
            out.println("<p style='color:red;text-align:center;'>Error loading menu: " + e.getMessage() + "</p>");
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    %>

    <div class="back-btn">
        <a href="customerDashboard.jsp">⬅️ Back to Dashboard</a>
    </div>
</div>
</body>
</html>

