<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Reservation" %>
<%@ page import="model.TableInfo" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reassign Reservation - Pizzas 505 ENMU</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 800px;
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
        .current-info {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        .current-info h3 {
            color: #667eea;
            margin-bottom: 15px;
        }
        .info-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 15px;
        }
        .info-item {
            display: flex;
            flex-direction: column;
        }
        .info-label {
            font-size: 12px;
            color: #6c757d;
            text-transform: uppercase;
            margin-bottom: 5px;
        }
        .info-value {
            font-size: 16px;
            font-weight: 600;
            color: #333;
        }
        .form-group {
            margin-bottom: 25px;
        }
        label {
            display: block;
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
        }
        input, select {
            width: 100%;
            padding: 12px;
            border: 2px solid #e9ecef;
            border-radius: 8px;
            font-size: 16px;
            transition: all 0.3s;
        }
        input:focus, select:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102,126,234,0.1);
        }
        .warning-box {
            background: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 15px;
            margin-bottom: 25px;
            border-radius: 4px;
        }
        .warning-box strong {
            display: block;
            margin-bottom: 8px;
            color: #856404;
        }
        .form-actions {
            display: flex;
            gap: 15px;
            justify-content: center;
            margin-top: 30px;
        }
        .btn {
            padding: 14px 28px;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 16px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.3s;
        }
        .btn-primary {
            background: #667eea;
            color: white;
        }
        .btn-primary:hover {
            background: #5568d3;
            transform: translateY(-2px);
        }
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        .btn-secondary:hover {
            background: #5a6268;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>🔄 Reassign Reservation</h1>

    <%
        Reservation reservation = (Reservation) request.getAttribute("reservation");
        List<TableInfo> tables = (List<TableInfo>) request.getAttribute("tables");

        if (reservation != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
    %>

    <div class="current-info">
        <h3>Current Reservation Details</h3>
        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">Reference ID</div>
                <div class="info-value"><%= reservation.getReferenceId() %></div>
            </div>
            <div class="info-item">
                <div class="info-label">Customer</div>
                <div class="info-value"><%= reservation.getCustomerName() %></div>
            </div>
            <div class="info-item">
                <div class="info-label">Current Date/Time</div>
                <div class="info-value"><%= reservation.getDateTime().format(displayFormatter) %></div>
            </div>
            <div class="info-item">
                <div class="info-label">Current Table</div>
                <div class="info-value">Table #<%= reservation.getTableId() %></div>
            </div>
            <div class="info-item">
                <div class="info-label">Party Size</div>
                <div class="info-value"><%= reservation.getPartySize() %> Guests</div>
            </div>
            <div class="info-item">
                <div class="info-label">Status</div>
                <div class="info-value"><%= reservation.getStatus() %></div>
            </div>
        </div>
    </div>

    <div class="warning-box">
        <strong>⚠️ Staff Notice:</strong>
        Reassigning this reservation will update the table and/or time. The system will check for conflicts before confirming the change.
    </div>

    <form action="reservation" method="post">
        <input type="hidden" name="action" value="reassign">
        <input type="hidden" name="id" value="<%= reservation.getId() %>">

        <div class="form-group">
            <label for="newDateTime">New Date & Time</label>
            <input type="datetime-local" id="newDateTime" name="newDateTime"
                   value="<%= reservation.getDateTime().format(formatter) %>" required>
        </div>

        <div class="form-group">
            <label for="newTableId">New Table</label>
            <select id="newTableId" name="newTableId" required>
                <% if (tables != null) {
                    for (TableInfo table : tables) { %>
                <option value="<%= table.getId() %>"
                        <%= table.getId() == reservation.getTableId() ? "selected" : "" %>>
                    Table <%= table.getTableNumber() %> -
                    <%= table.getZone() %> -
                    Seats <%= table.getCapacity() %> -
                    $<%= String.format("%.2f", table.getTotalPrice()) %>
                </option>
                <% }
                } %>
            </select>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn btn-primary">✅ Reassign Reservation</button>
            <a href="reservation?action=list" class="btn btn-secondary">Cancel</a>
        </div>
    </form>

    <% } else { %>
    <div class="warning-box">
        <strong>Error:</strong> Reservation not found.
    </div>
    <div class="form-actions">
        <a href="reservation?action=list" class="btn btn-secondary">Back to Reservations</a>
    </div>
    <% } %>
</div>
</body>
</html>