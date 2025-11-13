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
        .edit-btn {
            background: #2980b9;
            margin-left: 5px;
        }
        .edit-btn:hover {
            background: #1f618d;
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
        .custom-section {
            display: none;
            background: #f9f9f9;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 6px;
            margin-top: 8px;
        }
        .errorMsg {
            color: red;
            font-weight: bold;
        }
        .updatedPriceLabel {
            color: #27ae60;
            font-weight: bold;
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
                boolean isPizza = m.getName().toLowerCase().contains("pizza");
        %>
        <tr>
            <td><%= m.getName() %></td>
            <td><%= m.getDescription() %></td>
            <td>
                $<span class="basePrice"><%= String.format("%.2f", m.getPrice()) %></span><br>
                <small class="updatedPriceLabel" id="livePrice_<%= m.getId() %>"></small>
            </td>
            <td>
                <form action="order" method="post" onsubmit="return syncPrice(this)">
                    <input type="hidden" name="itemId" value="<%= m.getId() %>">
                    <input type="number" name="quantity" min="1" max="10" value="1" style="width:50px;">
                    <!-- hidden custom price stays updated -->
                    <input type="hidden"
                           name="customPrice"
                           id="customPrice_<%= m.getId() %>"
                           value="<%= String.format("%.2f", m.getPrice()) %>">

                    <% if (isPizza) { %>
                    <button type="button" class="edit-btn" onclick="toggleCustomize(this)">Customize</button>

                    <!-- customization panel -->
                    <div class="custom-section">
                        <label>Size:
                            <select name="size" onchange="updatePrice(this, <%= m.getId() %>)">
                                <option value="small" data-price="0">Small</option>
                                <option value="medium" data-price="2">Medium (+$2)</option>
                                <option value="large" data-price="4">Large (+$4)</option>
                            </select>
                        </label><br>

                        <!-- toppings -->
                        <label>
                            <input type="checkbox" name="toppings" value="pepperoni"
                                   data-price="1"
                                   onchange="updatePrice(this, <%= m.getId() %>)">
                            Pepperoni (+$1)
                        </label><br>

                        <label>
                            <input type="checkbox" name="toppings" value="mushrooms"
                                   data-price="1"
                                   onchange="updatePrice(this, <%= m.getId() %>)">
                            Mushrooms (+$1)
                        </label><br>

                        <label>
                            <input type="checkbox" name="toppings" value="extra_cheese"
                                   data-price="1.5"
                                   onchange="updatePrice(this, <%= m.getId() %>)">
                            Extra Cheese (+$1.50)
                        </label><br>

                        <label>
                            <input type="checkbox" name="toppings" value="vegan"
                                   data-price="0"
                                   onchange="updatePrice(this, <%= m.getId() %>)">
                            Vegan (no cheese)
                        </label><br>

                        <p class="errorMsg"></p>
                        <p>
                            <strong>Updated Price:</strong>
                            $<span class="updatedPrice">
                                <%= String.format("%.2f", m.getPrice()) %>
                            </span>
                        </p>
                    </div>
                    <% } %>

                    <!-- SUBMIT BUTTON MOVED TO BOTTOM OF FORM -->
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

<script>
    function toggleCustomize(btn) {
        const section = btn.parentElement.querySelector(".custom-section");
        section.style.display = section.style.display === "block" ? "none" : "block";
    }

    function updatePrice(elem, id) {
        const section = elem.closest(".custom-section");
        const form = elem.closest("form");
        const base = parseFloat(form.querySelector(".basePrice").innerText);
        const updatedDisplay = section.querySelector(".updatedPrice");
        const hiddenPrice = document.getElementById("customPrice_" + id);
        const livePrice = document.getElementById("livePrice_" + id);
        const errorMsg = section.querySelector(".errorMsg");

        let total = base;
        let hasVegan = false;
        let hasExtra = false;

        // SIZE adjustment (Option B)
        const size = section.querySelector("select[name='size']");
        if (size) {
            const sizeVal = size.value;
            if (sizeVal === "small") total += 0.0;
            if (sizeVal === "medium") total += 2.0;
            if (sizeVal === "large") total += 4.0;
        }

        // TOPPINGS
        const toppings = section.querySelectorAll("input[type='checkbox']:checked");
        let hasPepperoni = false;
        let hasMushrooms = false;
        toppings.forEach(t => {
            const val = t.value;
            if (val === "pepperoni") hasPepperoni = true;
            if (val === "mushrooms") hasMushrooms = true;
            if (val === "vegan") hasVegan = true;
            if (val === "extra_cheese") hasExtra = true;
            total += parseFloat(t.dataset.price);
        });

        // BLOCK: Vegan + Extra Cheese
        if (hasVegan && hasExtra) {
            errorMsg.innerHTML = "Cannot select Extra Cheese and Vegan together.";
            const extra = section.querySelector("input[value='extra_cheese']");
            if (extra && extra.checked) {
                extra.checked = false;
                total -= 1.5; // revert topping price
            }
        } else {
            errorMsg.innerHTML = "";
        }

        total = Math.round(total * 100) / 100;

        updatedDisplay.innerText = total.toFixed(2);
        hiddenPrice.value = total.toFixed(2);
        livePrice.innerText = "Now $" + total.toFixed(2);
    }

    function syncPrice(form) {
        const baseSpan = form.querySelector(".basePrice");
        const hidden = form.querySelector("input[name='customPrice']");
        const section = form.querySelector(".custom-section");

        // if no custom section (non-pizza), always use base menu price
        if (!section) {
            if (baseSpan && hidden) {
                const base = parseFloat(baseSpan.innerText);
                hidden.value = base.toFixed(2);
            }
            return true;
        }

        // if custom panel is closed, still just use whatever is already in hidden
        if (section.style.display !== "block") {
            // hidden was kept in sync by updatePrice, so just trust it
            return true;
        }

        // custom panel open → sync from updatedPrice span
        const updated = section.querySelector(".updatedPrice");
        if (updated && hidden) {
            hidden.value = updated.innerText.trim();
        }
        return true;
    }
</script>
</body>
</html>


