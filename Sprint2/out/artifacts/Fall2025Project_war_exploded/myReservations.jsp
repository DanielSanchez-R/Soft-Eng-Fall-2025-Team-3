<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Reservation" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Reservations - Pizzas 505 ENMU</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
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
            font-size: 2.5em;
        }
        .nav-buttons {
            display: flex;
            gap: 15px;
            margin-bottom: 30px;
            justify-content: center;
        }
        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 16px;
            text-decoration: none;
            transition: all 0.3s;
            display: inline-block;
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
        .btn-success {
            background: #28a745;
            color: white;
            padding: 8px 16px;
            font-size: 14px;
        }
        .btn-danger {
            background: #dc3545;
            color: white;
            padding: 8px 16px;
            font-size: 14px;
        }
        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 8px;
            font-weight: 500;
        }
        .alert-success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .reservations-grid {
            display: grid;
            gap: 20px;
        }
        .reservation-card {
            border: 2px solid #e9ecef;
            border-radius: 10px;
            padding: 20px;
            transition: all 0.3s;
        }
        .reservation-card:hover {
            border-color: #667eea;
            box-shadow: 0 5px 15px rgba(102,126,234,0.2);
        }
        .reservation-card.upcoming {
            border-left: 5px solid #28a745;
        }
        .reservation-card.past {
            border-left: 5px solid #6c757d;
            opacity: 0.8;
        }
        .reservation-card.cancelled {
            border-left: 5px solid #dc3545;
            opacity: 0.7;
        }
        .reservation-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        .reference-id {
            font-size: 1.2em;
            font-weight: bold;
            color: #667eea;
        }
        .status-badge {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: 600;
            text-transform: uppercase;
        }
        .status-confirmed { background: #d4edda; color: #155724; }
        .status-seated { background: #d1ecf1; color: #0c5460; }
        .status-cancelled { background: #f8d7da; color: #721c24; }
        .status-no-show { background: #fff3cd; color: #856404; }
        .reservation-details {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 15px;
        }
        .detail-item {
            display: flex;
            flex-direction: column;
        }
        .detail-label {
            font-size: 12px;
            color: #6c757d;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 5px;
        }
        .detail-value {
            font-size: 16px;
            color: #333;
            font-weight: 500;
        }
        .reservation-actions {
            display: flex;
            gap: 10px;
            margin-top: 15px;
            padding-top: 15px;
            border-top: 1px solid #e9ecef;
        }
        .no-reservations {
            text-align: center;
            padding: 60px 20px;
            color: #6c757d;
        }
        .no-reservations i {
            font-size: 4em;
            margin-bottom: 20px;
            opacity: 0.3;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>🍕 My Reservations</h1>

    <div class="nav-buttons">
        <a href="customerReservation?action=new" class="btn btn-primary">+ New Reservation</a>
        <a href="<%= request.getContextPath() %>/customerDashboard.jsp" class="btn btn-secondary">← Back to Dashboard</a>

    </div>

    <%
        String message = (String) request.getAttribute("message");
        String error = (String) request.getAttribute("error");
        if (message != null) {
    %>
    <div class="alert alert-success"><%= message %></div>
    <% } %>
    <% if (error != null) { %>
    <div class="alert alert-error"><%= error %></div>
    <% } %>

    <div class="reservations-grid">
        <%
            List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
            if (reservations == null || reservations.isEmpty()) {
        %>
        <div class="no-reservations">
            <div>📅</div>
            <h2>No Reservations Found</h2>
            <p>You haven't made any reservations yet.</p>
            <a href="customerReservation?action=new" class="btn btn-primary" style="margin-top: 20px;">Make Your First Reservation</a>
        </div>
        <%
        } else {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");

            for (Reservation r : reservations) {
                boolean isUpcoming = r.getDateTime().isAfter(now) &&
                        ("confirmed".equals(r.getStatus()) || "seated".equals(r.getStatus()));
                boolean isCancelled = "cancelled".equals(r.getStatus()) || "no-show".equals(r.getStatus());

                String cardClass = isUpcoming ? "upcoming" : (isCancelled ? "cancelled" : "past");
                String statusClass = "status-" + r.getStatus().toLowerCase();
        %>
        <div class="reservation-card <%= cardClass %>">
            <div class="reservation-header">
                <div class="reference-id">#<%= r.getReferenceId() %></div>
                <div class="status-badge <%= statusClass %>"><%= r.getStatus() %></div>
            </div>

            <div class="reservation-details">
                <div class="detail-item">
                    <div class="detail-label">Date & Time</div>
                    <div class="detail-value"><%= r.getDateTime().format(formatter) %></div>
                </div>
                <div class="detail-item">
                    <div class="detail-label">Party Size</div>
                    <div class="detail-value"><%= r.getPartySize() %> Guests</div>
                </div>
                <div class="detail-item">
                    <div class="detail-label">Table</div>
                    <div class="detail-value">Table #<%= r.getTableId() %></div>
                </div>
                <div class="detail-item">
                    <div class="detail-label">Contact</div>
                    <div class="detail-value"><%= r.getContact() %></div>
                </div>
            </div>

            <% if (r.getNotes() != null && !r.getNotes().isEmpty()) { %>
            <div class="detail-item" style="margin-top: 10px;">
                <div class="detail-label">Notes</div>
                <div class="detail-value"><%= r.getNotes() %></div>
            </div>
            <% } %>

            <% if (isUpcoming && "confirmed".equals(r.getStatus())) { %>
            <div class="reservation-actions">
                <a href="customerReservation?action=modify&ref=<%= r.getReferenceId() %>"
                   class="btn btn-success">✏️ Modify</a>
                <a href="customerReservation?action=cancel&ref=<%= r.getReferenceId() %>"
                   class="btn btn-danger"
                   onclick="return confirm('Are you sure you want to cancel this reservation?')">❌ Cancel</a>
            </div>
            <% } %>
        </div>
        <%
                }
            }
        %>
    </div>
</div>
</body>
</html>