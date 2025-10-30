<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.TableInfo" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Table Layout - Pizzas 505 ENMU</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            padding: 40px;
        }
        h1 {
            color: #333;
            margin-bottom: 30px;
            text-align: center;
        }
        .filters {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 30px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
        }
        .filter-group {
            display: flex;
            flex-direction: column;
        }
        .filter-group label {
            font-weight: 600;
            margin-bottom: 5px;
            color: #333;
            font-size: 14px;
        }
        .filter-group input, .filter-group select {
            padding: 10px;
            border: 2px solid #e9ecef;
            border-radius: 6px;
            font-size: 14px;
        }
        .filter-group input:focus, .filter-group select:focus {
            outline: none;
            border-color: #667eea;
        }
        .btn-filter {
            padding: 10px 20px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-weight: 600;
            align-self: flex-end;
            transition: all 0.3s;
        }
        .btn-filter:hover {
            background: #5568d3;
            transform: translateY(-2px);
        }
        .legend {
            display: flex;
            gap: 20px;
            justify-content: center;
            margin-bottom: 30px;
            flex-wrap: wrap;
        }
        .legend-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
        }
        .legend-color {
            width: 30px;
            height: 30px;
            border-radius: 6px;
            border: 2px solid #ddd;
        }
        .legend-available { background: #d4edda; border-color: #28a745; }
        .legend-booked { background: #f8d7da; border-color: #dc3545; }
        .table-layout {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .table-card {
            border: 3px solid #e9ecef;
            border-radius: 12px;
            padding: 20px;
            text-align: center;
            transition: all 0.3s;
            cursor: pointer;
            position: relative;
        }
        .table-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 20px rgba(0,0,0,0.1);
        }
        .table-card.available {
            background: #d4edda;
            border-color: #28a745;
        }
        .table-card.available:hover {
            background: #c3e6cb;
            border-color: #28a745;
        }
        .table-card.booked {
            background: #f8d7da;
            border-color: #dc3545;
            opacity: 0.7;
            cursor: not-allowed;
        }
        .table-card.booked:hover {
            transform: none;
        }
        .table-number {
            font-size: 36px;
            font-weight: bold;
            color: #333;
            margin-bottom: 10px;
        }
        .table-info {
            font-size: 13px;
            color: #6c757d;
            margin-bottom: 5px;
        }
        .table-zone {
            display: inline-block;
            padding: 4px 10px;
            background: #667eea;
            color: white;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
            margin-top: 8px;
        }
        .table-price {
            font-size: 18px;
            font-weight: bold;
            color: #28a745;
            margin-top: 10px;
        }
        .availability-badge {
            position: absolute;
            top: 10px;
            right: 10px;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 10px;
            font-weight: 700;
            text-transform: uppercase;
        }
        .badge-available {
            background: #28a745;
            color: white;
        }
        .badge-booked {
            background: #dc3545;
            color: white;
        }
        .back-btn {
            display: inline-block;
            padding: 12px 24px;
            background: #6c757d;
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-weight: 600;
            transition: all 0.3s;
        }
        .back-btn:hover {
            background: #5a6268;
        }
        .tooltip {
            position: relative;
            display: inline-block;
        }
        .tooltip .tooltiptext {
            visibility: hidden;
            width: 200px;
            background-color: #333;
            color: #fff;
            text-align: center;
            border-radius: 6px;
            padding: 10px;
            position: absolute;
            z-index: 1;
            bottom: 125%;
            left: 50%;
            margin-left: -100px;
            opacity: 0;
            transition: opacity 0.3s;
            font-size: 12px;
        }
        .tooltip:hover .tooltiptext {
            visibility: visible;
            opacity: 1;
        }
        .no-results {
            text-align: center;
            padding: 60px 20px;
            color: #6c757d;
        }
    </style>
    <script>
        function selectTable(tableId, isAvailable) {
            if (!isAvailable) {
                alert('This table is currently booked. Please select another table or time.');
                return;
            }

            if (confirm('Would you like to reserve Table #' + tableId + '?')) {
                // Get current filter values
                const dateTime = document.getElementById('filterDateTime').value;
                const partySize = document.getElementById('filterPartySize').value;

                // Redirect to reservation form with pre-filled data
                let url = 'customerReservation?action=new&tableId=' + tableId;
                if (dateTime) url += '&dateTime=' + encodeURIComponent(dateTime);
                if (partySize) url += '&partySize=' + partySize;

                window.location.href = url;
            }
        }

        function filterTables() {
            document.getElementById('filterForm').submit();
        }
    </script>
</head>
<body>
<div class="container">
    <h1>🪑 Table Layout & Availability</h1>

    <form id="filterForm" action="tableLayout" method="get">
        <div class="filters">
            <div class="filter-group">
                <label for="filterDate">Date</label>
                <input type="date" id="filterDate" name="date"
                       value="<%= request.getParameter("date") != null ? request.getParameter("date") : "" %>">
            </div>
            <div class="filter-group">
                <label for="filterTime">Time</label>
                <input type="time" id="filterTime" name="time"
                       value="<%= request.getParameter("time") != null ? request.getParameter("time") : "" %>">
            </div>
            <div class="filter-group">
                <label for="filterPartySize">Party Size</label>
                <input type="number" id="filterPartySize" name="partySize" min="1" max="20"
                       placeholder="# of guests"
                       value="<%= request.getParameter("partySize") != null ? request.getParameter("partySize") : "" %>">
            </div>
            <div class="filter-group">
                <label>&nbsp;</label>
                <button type="button" onclick="filterTables()" class="btn-filter">🔍 Check Availability</button>
            </div>
        </div>
    </form>

    <%
        LocalDateTime selectedDateTime = (LocalDateTime) request.getAttribute("selectedDateTime");
        if (selectedDateTime != null) {
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a");
    %>
    <div style="text-align: center; margin-bottom: 20px; padding: 15px; background: #e7f3ff; border-radius: 8px;">
        <strong>Showing availability for:</strong> <%= selectedDateTime.format(displayFormatter) %>
    </div>
    <% } %>

    <div class="legend">
        <div class="legend-item">
            <div class="legend-color legend-available"></div>
            <span><strong>Available</strong> - Click to reserve</span>
        </div>
        <div class="legend-item">
            <div class="legend-color legend-booked"></div>
            <span><strong>Booked</strong> - Not available</span>
        </div>
    </div>

    <div class="table-layout">
        <%
            List<TableInfo> tables = (List<TableInfo>) request.getAttribute("tables");
            Map<Integer, Boolean> availabilityMap = (Map<Integer, Boolean>) request.getAttribute("availabilityMap");

            if (tables != null && !tables.isEmpty()) {
                for (TableInfo table : tables) {
                    boolean isAvailable = availabilityMap != null &&
                            availabilityMap.getOrDefault(table.getId(), true);
                    String availabilityClass = isAvailable ? "available" : "booked";
                    String badgeClass = isAvailable ? "badge-available" : "badge-booked";
                    String badgeText = isAvailable ? "Available" : "Booked";
        %>
        <div class="table-card <%= availabilityClass %> tooltip"
             onclick="selectTable(<%= table.getId() %>, <%= isAvailable %>)">

            <div class="availability-badge <%= badgeClass %>"><%= badgeText %></div>

            <div class="table-number"><%= table.getTableNumber() %></div>

            <div class="table-info">
                <div>👥 Seats: <%= table.getCapacity() %></div>
                <div class="table-price">$<%= String.format("%.2f", table.getTotalPrice()) %></div>
            </div>

            <div class="table-zone"><%= table.getZone() %></div>

            <span class="tooltiptext">
                        <strong>Table <%= table.getTableNumber() %></strong><br>
                        Capacity: <%= table.getCapacity() %> guests<br>
                        Zone: <%= table.getZone() %><br>
                        Base Price: $<%= String.format("%.2f", table.getBasePrice()) %><br>
                        <% if (table.getSurcharge() > 0) { %>
                        Surcharge: $<%= String.format("%.2f", table.getSurcharge()) %><br>
                        <% } %>
                        <% if (isAvailable) { %>
                        <strong style="color: #4CAF50;">✓ Click to Reserve</strong>
                        <% } else { %>
                        <strong style="color: #f44336;">✗ Already Booked</strong>
                        <% } %>
                    </span>
        </div>
        <%
            }
        } else {
        %>
        <div class="no-results" style="grid-column: 1/-1;">
            <h2>No Tables Available</h2>
            <p>Please try a different date or time.</p>
        </div>
        <%
            }
        %>
    </div>

    <div style="text-align: center; margin-top: 30px;">
        <a href="customerDashboard.jsp" class="back-btn">← Back to Dashboard</a>
    </div>
</div>
</body>
</html>