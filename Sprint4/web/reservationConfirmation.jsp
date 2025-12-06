<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Reservation" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Modify Reservation - Pizzas 505 ENMU</title>
    <style>
        /* Reuse same styles from createReservation.jsp */
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
            margin-bottom: 10px;
            text-align: center;
        }
        .subtitle {
            text-align: center;
            color: #6c757d;
            margin-bottom: 30px;
        }
        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 8px;
            font-weight: 500;
        }
        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .form-group {
            margin-bottom: 25px;
        }
        label {
            display: block;
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
            font-size: 14px;
        }
        input, select, textarea {
            width: 100%;
            padding: 12px;
            border: 2px solid #e9ecef;
            border-radius: 8px;
            font-size: 16px;
            transition: all 0.3s;
        }
        input:focus, select:focus, textarea:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102,126,234,0.1);
        }
        textarea {
            resize: vertical;
            min-height: 80px;
        }
        .form-actions {
            display: flex;
            gap: 15px;
            justify-content: center;
            margin-top: 30px;
        }
        .btn {
            padding: 14px 32px;
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
            box-shadow: 0 5px 15px rgba(102,126,234,0.3);
        }
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        .btn-secondary:hover {
            background: #5a6268;
        }
        .info-box {
            background: #e7f3ff;
            border-left: 4px solid #2196F3;
            padding: 15px;
            margin-bottom: 25px;
            border-radius: 4px;
        }
        .reference-display {
            font-size: 18px;
            font-weight: bold;
            color: #667eea;
            text-align: center;
            padding: 10px;
            background: #f8f9ff;
            border-radius: 8px;
            margin-bottom: 25px;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>✏️ Modify Your Reservation</h1>
    <p class="subtitle">Update your reservation details</p>

    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
    <div class="alert alert-error"><%= error %></div>
    <% } %>

    <%
        Reservation reservation = (Reservation) request.getAttribute("reservation");
        if (reservation != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    %>

    <div class="reference-display">
        Confirmation #: <%= reservation.getReferenceId() %>
    </div>

    <div class="info-box">
        <strong>⏰ Policy Reminder:</strong> Modifications must be made at least 2 hours before your reservation time.
    </div>

    <form action="customerReservation" method="post">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="referenceId" value="<%= reservation.getReferenceId() %>">

        <div class="form-group">
            <label for="dateTime">New Date & Time</label>
            <input type="datetime-local" id="dateTime" name="dateTime"
                   value="<%= reservation.getDateTime().format(formatter) %>" required>
        </div>

        <div class="form-group">
            <label for="partySize">Party Size</label>
            <input type="number" id="partySize" name="partySize"
                   value="<%= reservation.getPartySize() %>" min="1" max="20" required>
        </div>

        <div class="form-group">
            <label for="tableId">Table Number</label>
            <input type="number" id="tableId" name="tableId"
                   value="<%= reservation.getTableId() %>" required>
            <small style="color: #6c757d;">Current table: <%= reservation.getTableId() %></small>
        </div>

        <div class="form-group">
            <label for="notes">Special Requests</label>
            <textarea id="notes" name="notes"><%= reservation.getNotes() != null ? reservation.getNotes() : "" %></textarea>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn btn-primary">💾 Save Changes</button>
            <a href="customerReservation?action=list" class="btn btn-secondary">Cancel</a>
        </div>
    </form>

    <% } else { %>
    <div class="alert alert-error">Reservation not found.</div>
    <div style="text-align: center; margin-top: 20px;">
        <a href="customerReservation?action=list" class="btn btn-secondary">Back to My Reservations</a>
    </div>
    <% } %>
</div>
</body>
</html>
