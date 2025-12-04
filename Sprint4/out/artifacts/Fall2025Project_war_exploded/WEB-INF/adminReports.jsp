<%--
  Created by IntelliJ IDEA.
  User: Daniel Sanchez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.sql.*, dao.ReportDao, model.MonthlyRevenue, model.DailySale" %>

<%
    String ctx = request.getContextPath();

    // Simple role check (same as adminDashboard.jsp)
    String role = (String) session.getAttribute("role");
    if (role == null || !"admin".equalsIgnoreCase(role)) {
        response.sendRedirect("unauthorized.jsp");
        return;
    }

    // ====== Pre-seeded summary data ======
    ReportDao reportDao = new ReportDao();

    // Current date / month / year
    Calendar cal = Calendar.getInstance();
    int currentYear = cal.get(Calendar.YEAR);
    int currentMonth = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based

    // Helper to get month name
    String[] monthNames = {
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
    };

    // --- This month and last month revenue ---
    MonthlyRevenue thisMonthRevenue = null;
    MonthlyRevenue lastMonthRevenue = null;

    try {
        thisMonthRevenue = reportDao.getMonthlyRevenue(currentMonth, currentYear);

        int lastMonth = currentMonth - 1;
        int lastMonthYear = currentYear;
        if (lastMonth < 1) {
            lastMonth = 12;
            lastMonthYear = currentYear - 1;
        }
        lastMonthRevenue = reportDao.getMonthlyRevenue(lastMonth, lastMonthYear);
    } catch (Exception e) {
        e.printStackTrace();
    }

    // --- Last 3 months (before current month) summary ---
    List<MonthlyRevenue> lastThreeMonths = new ArrayList<MonthlyRevenue>();
    try {
        for (int i = 3; i >= 1; i--) {
            int m = currentMonth - i;
            int y = currentYear;
            if (m < 1) {
                m += 12;
                y -= 1;
            }
            lastThreeMonths.add(reportDao.getMonthlyRevenue(m, y));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    // --- Weekly sales (last 7 days including today) ---
    double weeklyTotal = 0.0;
    try {
        java.sql.Date endDate = new java.sql.Date(System.currentTimeMillis());
        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DAY_OF_MONTH, -6); // last 7 days
        java.sql.Date startDate = new java.sql.Date(c2.getTimeInMillis());

        List<DailySale> weekSales = reportDao.getDailySales(startDate, endDate);
        for (DailySale s : weekSales) {
            weeklyTotal += s.getTotal();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    String errorMessage = (String) request.getAttribute("errorMessage");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Reports</title>
    <style>
        body {
            font-family: Segoe UI, Arial, sans-serif;
            background: #f9f9f9;
            margin: 0;
            padding: 40px;
            color: #333;
        }
        h2 {
            color: #2c3e50;
        }
        h3 {
            color: #c0392b;
        }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            border: none;
            color: #fff;
            text-decoration: none;
            border-radius: 5px;
            margin-right: 10px;
            margin-top: 10px;
            cursor: pointer;
        }
        .btn-blue { background: #3498db; }
        .btn-blue:hover { background: #2980b9; }
        .btn-green { background: #27ae60; }
        .btn-green:hover { background: #1e8449; }
        .btn-red { background: #e74c3c; }
        .btn-red:hover { background: #c0392b; }
        .btn-orange { background: #d35400; }
        .btn-orange:hover { background: #a84300; }
        .btn-purple { background: #8e44ad; }
        .btn-purple:hover { background: #6c3483; }

        .section {
            margin-top: 30px;
        }

        .card-row {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            margin-top: 15px;
        }
        .card {
            background: #fff;
            border-radius: 8px;
            padding: 15px 20px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
            min-width: 220px;
        }
        .card-title {
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 5px;
        }
        .card-value {
            font-size: 18px;
            margin-top: 5px;
        }

        table {
            width: 95%;
            margin: 20px auto;
            border-collapse: collapse;
            background: white;
            box-shadow: 0 2px 6px rgba(0,0,0,0.1);
        }
        th, td {
            padding: 8px 10px;
            border-bottom: 1px solid #ddd;
            text-align: center;
        }
        th {
            background: #e74c3c;
            color: white;
        }
        tr:nth-child(even) {
            background: #f9f9f9;
        }

        .error {
            background: #ffdddd;
            border-left: 4px solid #e74c3c;
            padding: 10px 15px;
            margin-top: 15px;
            color: #c0392b;
        }

        .form-inline {
            margin-bottom: 10px;
        }
        .form-inline input,
        .form-inline select {
            padding: 5px 8px;
            margin-right: 8px;
        }
    </style>
</head>
<body>

<h2>📊 Reports Center</h2>
<p>You are logged in as: <strong>Admin</strong></p>

<!-- Back button -->
<a href="<%= ctx %>/adminDashboard.jsp" class="btn btn-red">⬅ Back to Admin Dashboard</a>

<% if (errorMessage != null) { %>
<div class="error">
    <strong>Error:</strong> <%= errorMessage %>
</div>
<% } %>

<!-- SUMMARY CARDS -->
<div class="section">
    <h3>Quick Summary</h3>
    <div class="card-row">
        <div class="card">
            <div class="card-title">Last 7 Days Sales</div>
            <div class="card-value">$<%= String.format("%.2f", weeklyTotal) %></div>
        </div>

        <div class="card">
            <div class="card-title">This Month (<%= monthNames[currentMonth - 1] %> <%= currentYear %>)</div>
            <div class="card-value">
                $<%= (thisMonthRevenue != null)
                    ? String.format("%.2f", thisMonthRevenue.getRevenue())
                    : "0.00" %>
            </div>
        </div>

        <div class="card">
            <div class="card-title">Last Month</div>
            <div class="card-value">
                <% if (lastMonthRevenue != null) { %>
                <%
                    int lm = lastMonthRevenue.getMonth();
                    int ly = lastMonthRevenue.getYear();
                %>
                <%= monthNames[lm - 1] %> <%= ly %>:
                $<%= String.format("%.2f", lastMonthRevenue.getRevenue()) %>
                <% } else { %>
                No data
                <% } %>
            </div>
        </div>
    </div>
</div>

<!-- LAST 3 MONTHS TABLE -->
<div class="section">
    <h3>Monthly Sales (Last 3 Months Before Current)</h3>
    <table>
        <tr>
            <th>Month</th>
            <th>Year</th>
            <th>Total Revenue</th>
        </tr>
        <%
            if (lastThreeMonths == null || lastThreeMonths.isEmpty()) {
        %>
        <tr>
            <td colspan="3" style="color:#777;">No revenue data found for the last 3 months.</td>
        </tr>
        <%
        } else {
            for (MonthlyRevenue mr : lastThreeMonths) {
                int m = mr.getMonth();
                int y = mr.getYear();
        %>
        <tr>
            <td><%= monthNames[m - 1] %></td>
            <td><%= y %></td>
            <td>$<%= String.format("%.2f", mr.getRevenue()) %></td>
        </tr>
        <%
                }
            }
        %>
    </table>
</div>

<!-- GENERATE REPORTS -->
<div class="section">
    <h3>Generate Reports</h3>

    <!-- Daily Sales: DATE RANGE -->
    <form class="form-inline" action="<%= ctx %>/report" method="get" target="reportPane">
        <input type="hidden" name="action" value="dailySales">
        <label for="dailyStart">Daily Sales From:</label>
        <input type="date" id="dailyStart" name="startDate" required>

        <label for="dailyEnd">To:</label>
        <input type="date" id="dailyEnd" name="endDate">

        <button type="submit" class="btn btn-blue">View Daily Sales</button>
    </form>

    <!-- Monthly Revenue: SHOW ALL ORDERS IN MONTH -->
    <form class="form-inline" action="<%= ctx %>/report" method="get" target="reportPane">
        <input type="hidden" name="action" value="monthlyRevenue">
        <label for="month">Month:</label>
        <select id="month" name="month" required>
            <% for (int m = 1; m <= 12; m++) { %>
            <option value="<%= m %>" <%= (m == currentMonth ? "selected" : "") %>>
                <%= monthNames[m - 1] %>
            </option>
            <% } %>
        </select>

        <label for="year">Year:</label>
        <input type="number" id="year" name="year" value="<%= currentYear %>" required>

        <button type="submit" class="btn btn-green">View Monthly Revenue</button>
    </form>

    <!-- Inventory Report -->
    <form class="form-inline" action="<%= ctx %>/report" method="get" target="reportPane">
        <input type="hidden" name="action" value="inventory">
        <label for="threshold">Low-stock threshold (optional):</label>
        <input type="number" id="threshold" name="threshold" placeholder="e.g. 10">
        <button type="submit" class="btn btn-orange">View Inventory Report</button>
    </form>
</div>

<!-- EXPORT CSV -->
<div class="section">
    <h3>Export CSV</h3>

    <!-- Daily CSV -->
    <form class="form-inline" action="<%= ctx %>/report" method="get">
        <input type="hidden" name="action" value="exportCsv">
        <input type="hidden" name="type" value="daily">
        <label for="csvDailyDate">Daily Sales Date:</label>
        <input type="date" id="csvDailyDate" name="date" required>
        <button type="submit" class="btn btn-blue">Export Daily Sales CSV</button>
    </form>

    <!-- Monthly CSV -->
    <form class="form-inline" action="<%= ctx %>/report" method="get">
        <input type="hidden" name="action" value="exportCsv">
        <input type="hidden" name="type" value="monthly">
        <label for="csvMonth">Month:</label>
        <select id="csvMonth" name="month" required>
            <% for (int m = 1; m <= 12; m++) { %>
            <option value="<%= m %>" <%= (m == currentMonth ? "selected" : "") %>>
                <%= monthNames[m - 1] %>
            </option>
            <% } %>
        </select>

        <label for="csvYear">Year:</label>
        <input type="number" id="csvYear" name="year" value="<%= currentYear %>" required>

        <button type="submit" class="btn btn-green">Export Monthly Revenue CSV</button>
    </form>

    <!-- Inventory CSV -->
    <form class="form-inline" action="<%= ctx %>/report" method="get">
        <input type="hidden" name="action" value="exportCsv">
        <input type="hidden" name="type" value="inventory">
        <label for="csvThreshold">Low-stock threshold (optional):</label>
        <input type="number" id="csvThreshold" name="threshold" placeholder="e.g. 10">
        <button type="submit" class="btn btn-orange">Export Inventory CSV</button>
    </form>
</div>

<!-- REPORT OUTPUT PANE -->
<div class="section">
    <h3>Report Output</h3>
    <iframe name="reportPane"
            style="width: 100%; height: 400px; border: 1px solid #ccc; background:white;">
    </iframe>
</div>

</body>
</html>
