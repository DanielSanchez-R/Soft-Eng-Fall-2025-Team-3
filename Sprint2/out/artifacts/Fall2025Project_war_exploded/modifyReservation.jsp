<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.TableInfo" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Modify Your Reservation- Pizzas 505 ENMU</title>
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
        .table-selector {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
            gap: 15px;
            margin-top: 10px;
        }
        .table-option {
            border: 2px solid #e9ecef;
            border-radius: 10px;
            padding: 15px;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s;
        }
        .table-option:hover {
            border-color: #667eea;
            background: #f8f9ff;
        }
        .table-option input[type="radio"] {
            display: none;
        }
        .table-option input[type="radio"]:checked + .table-info {
            color: #667eea;
            font-weight: bold;
        }
        .table-option input[type="radio"]:checked ~ * {
            color: #667eea;
        }
        .table-option.selected {
            border-color: #667eea;
            background: #f8f9ff;
            box-shadow: 0 5px 15px rgba(102,126,234,0.2);
        }
        .table-number {
            font-size: 24px;
            font-weight: bold;
            margin-bottom: 5px;
        }
        .table-details {
            font-size: 12px;
            color: #6c757d;
        }
        .pricing-info {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin-top: 10px;
        }
        .pricing-info strong {
            color: #667eea;
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
        .required {
            color: #dc3545;
        }
    </style>
    <script>
        function updateTableSelection() {
            const tables = document.querySelectorAll('.table-option');
            tables.forEach(option => {
                const radio = option.querySelector('input[type="radio"]');
                if (radio.checked) {
                    option.classList.add('selected');
                } else {
                    option.classList.remove('selected');
                }
            });
        }

        function validateForm() {
            const dateTime = document.getElementById('dateTime').value;
            if (!dateTime) {
                alert('Please select a date and time for your reservation.');
                return false;
            }

            const selectedDateTime = new Date(dateTime);
            const now = new Date();

            if (selectedDateTime < now) {
                alert('Cannot book reservations in the past.');
                return false;
            }

            const tableId = document.querySelector('input[name="tableId"]:checked');
            if (!tableId) {
                alert('Please select a table.');
                return false;
            }

            return true;
        }
    </script>
</head>
<body>
<div class="container">
    <h1>🍕 Reserve Your Table</h1>
    <p class="subtitle">Book your dining experience at Pizzas 505 ENMU</p>

    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
    <div class="alert alert-error"><%= error %></div>
    <% } %>

    <form action="${pageContext.request.contextPath}/customerReservation" method="post" onsubmit="return validateForm()">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="referenceId" value="<%= ((model.Reservation)request.getAttribute("reservation")).getReferenceId() %>">

        <div class="form-group">
            <label for="customerName">Full Name <span class="required">*</span></label>
            <input type="text" id="customerName" name="customerName" required
                   value="<%= ((model.Reservation)request.getAttribute("reservation")).getCustomerName() %>"
                   placeholder="Enter your full name">
        </div>

        <div class="form-group">
            <label for="contact">Contact Email/Phone <span class="required">*</span></label>
            <input type="text" id="contact" name="contact" required
                   value="<%= ((model.Reservation)request.getAttribute("reservation")).getContact() %>"
                   placeholder="your.email@example.com or (555) 123-4567">
        </div>

        <div class="form-group">
            <label for="dateTime">New Date & Time <span class="required">*</span></label>
            <input type="datetime-local" id="dateTime" name="dateTime" required
                   value="<%= ((model.Reservation)request.getAttribute("reservation")).getDateTime().toString().replace('T','T') %>">
        </div>

        <div class="form-group">
            <label for="partySize">Party Size <span class="required">*</span></label>
            <input type="number" id="partySize" name="partySize" min="1" max="20" required
                   value="<%= ((model.Reservation)request.getAttribute("reservation")).getPartySize() %>"
                   placeholder="Number of guests">
        </div>

        <div class="form-group">
            <label>Select Your Table <span class="required">*</span></label>
            <div class="table-selector">
                <%
                    List<TableInfo> tables = (List<TableInfo>) request.getAttribute("tables");
                    model.Reservation r = (model.Reservation) request.getAttribute("reservation");
                    int currentTableId = (r != null) ? r.getTableId() : -1;

                    if (tables != null && !tables.isEmpty()) {
                        for (TableInfo table : tables) {
                            boolean selected = (table.getId() == currentTableId);
                %>
                <label class="table-option <%= selected ? "selected" : "" %>">
                    <input type="radio" name="tableId" value="<%= table.getId() %>"
                           onchange="updateTableSelection()" <%= selected ? "checked" : "" %> required>
                    <div class="table-info">
                        <div class="table-number">Table <%= table.getTableNumber() %></div>
                        <div class="table-details">
                            <div>Seats: <%= table.getCapacity() %></div>
                            <div>Zone: <%= table.getZone() %></div>
                            <div style="color: #28a745; font-weight: 600; margin-top: 5px;">
                                $<%= String.format("%.2f", table.getTotalPrice()) %>
                            </div>
                        </div>
                    </div>
                </label>
                <%
                    }
                } else {
                %>
                <p style="grid-column: 1/-1; text-align: center; color: #6c757d;">
                    No tables available. Please contact the restaurant.
                </p>
                <%
                    }
                %>
            </div>
        </div>

        <div class="form-group">
            <label for="notes">Special Requests (Optional)</label>
            <textarea id="notes" name="notes"
                      placeholder="Any special requests or dietary requirements..."><%= r != null ? r.getNotes() : "" %></textarea>
        </div>

        <div class="form-actions">
            <button type="submit" class="btn btn-primary">💾 Save Changes</button>
            <a href="${pageContext.request.contextPath}/customerReservation?action=list" class="btn btn-secondary">➡ Continue Without Modification</a>
        </div>
    </form>
</div>
</body>
</html>