package controller;

import dao.ReportDao;
import model.DailySale;
import model.MonthlyRevenue;
import model.InventoryReportItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * ReportController
 *
 * Handles staff reporting features:
 *  - Daily sales report
 *  - Monthly revenue report
 *  - Inventory status (with low-stock flag)
 *  - CSV export for each report type
 *
 * URL pattern: /report
 * Actions (via ?action=...):
 *  - viewDashboard        -> show main reports menu
 *  - dailySales           -> show daily sales report
 *  - monthlyRevenue       -> show monthly revenue report
 *  - inventory            -> show inventory report
 *  - exportCsv            -> export a report as CSV
 * @author Daniel Sanchez
 * @version 4.0
 */
@WebServlet("/report")
public class ReportController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ReportDao reportDao;

    @Override
    public void init() throws ServletException {
        super.init();
        reportDao = new ReportDao();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    /**
     * Central dispatcher that routes all /report requests
     * based on the "action" parameter.
     */
    private void handleRequest(HttpServletRequest request,
                               HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            action = "viewDashboard";
        }

        try {
            switch (action) {
                case "dailySales":
                    handleDailySales(request, response);
                    break;
                case "monthlyRevenue":
                    handleMonthlyRevenue(request, response);
                    break;
                case "inventory":
                    handleInventory(request, response);
                    break;
                case "exportCsv":
                    handleExportCsv(request, response);
                    break;
                case "exportPdf":
                    handleExportPdf(request, response); // stub, to implement PDF logic
                    break;
                case "viewDashboard":
                default:
                    forwardToDashboard(request, response);
                    break;
            }
        } catch (SQLException e) {
            // Log and forward to error page / dashboard with message
            e.printStackTrace();
            request.setAttribute("errorMessage",
                    "A database error occurred while generating the report: " + e.getMessage());
            forwardToDashboard(request, response);
        } catch (IllegalArgumentException e) {
            // For invalid dates / numbers
            e.printStackTrace();
            request.setAttribute("errorMessage",
                    "Invalid input: " + e.getMessage());
            forwardToDashboard(request, response);
        }
    }

    /**
     * Shows the main report dashboard where staff can choose
     * which report to generate.
     */
    private void forwardToDashboard(HttpServletRequest request,
                                    HttpServletResponse response) throws ServletException, IOException {
        // Make sure you create /admin/reports.jsp
        request.getRequestDispatcher("/adminReports.jsp")
                .forward(request, response);
    }

    /**
     * Handles viewing of the daily sales report for a specific date.
     * Expects request parameter: date (yyyy-MM-dd)
     */
    private void handleDailySales(HttpServletRequest request,
                                  HttpServletResponse response) throws ServletException, IOException, SQLException {

        String startStr = request.getParameter("startDate");
        String endStr = request.getParameter("endDate");

        if (startStr == null || startStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Start date is required for daily sales report.");
        }

        // If end date is blank, use the same as start
        if (endStr == null || endStr.trim().isEmpty()) {
            endStr = startStr;
        }

        java.sql.Date startDate = java.sql.Date.valueOf(startStr);
        java.sql.Date endDate   = java.sql.Date.valueOf(endStr);

        // Ensure start <= end
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        List<DailySale> sales = reportDao.getDailySales(startDate, endDate);

        request.setAttribute("sales", sales);
        request.setAttribute("startDate", startStr);
        request.setAttribute("endDate", endStr);

        request.getRequestDispatcher("/daily_sales.jsp")
                .forward(request, response);
    }

    /**
     * Handles viewing of the monthly revenue report.
     * Expects request parameters: month (1-12), year (e.g. 2025)
     */
    private void handleMonthlyRevenue(HttpServletRequest request,
                                      HttpServletResponse response) throws ServletException, IOException, SQLException {

        String monthStr = request.getParameter("month");
        String yearStr  = request.getParameter("year");

        if (monthStr == null || yearStr == null ||
                monthStr.trim().isEmpty() || yearStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Month and year are required for monthly revenue report.");
        }

        int month = Integer.parseInt(monthStr);   // 1–12
        int year  = Integer.parseInt(yearStr);

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12.");
        }

        // 1) Summary total
        MonthlyRevenue revenue = reportDao.getMonthlyRevenue(month, year);

        // 2) Date range for that whole month
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.clear();
        cal.set(java.util.Calendar.YEAR, year);
        cal.set(java.util.Calendar.MONTH, month - 1); // 0-based
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        java.sql.Date startDate = new java.sql.Date(cal.getTimeInMillis());

        cal.add(java.util.Calendar.MONTH, 1);
        cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
        java.sql.Date endDate = new java.sql.Date(cal.getTimeInMillis());

        // 3) All sales in that month
        List<DailySale> monthlySales = reportDao.getDailySales(startDate, endDate);

        request.setAttribute("revenue", revenue);
        request.setAttribute("monthlySales", monthlySales);

        request.getRequestDispatcher("/monthly_revenue.jsp")
                .forward(request, response);
    }

    /**
     * Handles viewing of the inventory status report.
     * Optional request parameter: threshold (int), default from DAO if missing.
     */
    private void handleInventory(HttpServletRequest request,
                                 HttpServletResponse response) throws ServletException, IOException, SQLException {

        String thresholdStr = request.getParameter("threshold");
        List<InventoryReportItem> items;

        if (thresholdStr != null && !thresholdStr.trim().isEmpty()) {
            int threshold = Integer.parseInt(thresholdStr);
            items = reportDao.getInventoryReport(threshold);
            request.setAttribute("threshold", threshold);
        } else {
            items = reportDao.getInventoryReport();
        }

        request.setAttribute("items", items);

        request.getRequestDispatcher("/inventory_report.jsp")
                .forward(request, response);
    }

    /**
     * Handles CSV export for different report types.
     *
     * Expects:
     *   type=daily   + date=yyyy-MM-dd
     *   type=monthly + month=1-12 & year=YYYY
     *   type=inventory [+ threshold=int]
     */
    private void handleExportCsv(HttpServletRequest request,
                                 HttpServletResponse response) throws IOException, SQLException {

        String type = request.getParameter("type");
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Report type is required for CSV export.");
        }

        // Common CSV headers
        response.setContentType("text/csv; charset=UTF-8");
        String filename = type + "_report.csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        PrintWriter writer = response.getWriter();

        if ("daily".equalsIgnoreCase(type)) {
            exportDailySalesCsv(request, writer);
        } else if ("monthly".equalsIgnoreCase(type)) {
            exportMonthlyRevenueCsv(request, writer);
        } else if ("inventory".equalsIgnoreCase(type)) {
            exportInventoryCsv(request, writer);
        } else {
            throw new IllegalArgumentException("Unsupported CSV report type: " + type);
        }

        writer.flush();
        writer.close();
    }

    /**
     * Stub for PDF export. We can implement this with iText or PDFBox later.
     *
     * For now, this just sends a simple message so it compiles and runs.
     */
    private void handleExportPdf(HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().println(
                "PDF export is not implemented yet. " +
                        "Ask your friendly AI assistant to generate PdfUtil or iText code next 🙂"
        );
    }

    // ================== CSV EXPORT HELPERS ==================

    private void exportDailySalesCsv(HttpServletRequest request,
                                     PrintWriter writer) throws SQLException {

        String dateStr = request.getParameter("date");
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date is required for daily sales CSV export.");
        }

        Date sqlDate = Date.valueOf(dateStr);
        List<DailySale> sales = reportDao.getDailySales(sqlDate);

        // Header row
        writer.println("Order ID,Customer Email,Total,Order Time");

        // Data rows
        for (DailySale sale : sales) {
            writer.println(
                    sale.getOrderId() + "," +
                            safeCsv(sale.getCustomerEmail()) + "," +
                            sale.getTotal() + "," +
                            safeCsv(sale.getOrderTime())
            );
        }
    }

    private void exportMonthlyRevenueCsv(HttpServletRequest request,
                                         PrintWriter writer) throws SQLException {

        String monthStr = request.getParameter("month");
        String yearStr = request.getParameter("year");

        if (monthStr == null || yearStr == null ||
                monthStr.trim().isEmpty() || yearStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Month and year are required for monthly revenue CSV export.");
        }

        int month = Integer.parseInt(monthStr);
        int year = Integer.parseInt(yearStr);

        MonthlyRevenue revenue = reportDao.getMonthlyRevenue(month, year);

        // Simple one-row CSV: headers + data
        writer.println("Month,Year,Revenue");
        writer.println(
                revenue.getMonth() + "," +
                        revenue.getYear() + "," +
                        revenue.getRevenue()
        );
    }

    private void exportInventoryCsv(HttpServletRequest request,
                                    PrintWriter writer) throws SQLException {

        String thresholdStr = request.getParameter("threshold");
        List<InventoryReportItem> items;

        if (thresholdStr != null && !thresholdStr.trim().isEmpty()) {
            int threshold = Integer.parseInt(thresholdStr);
            items = reportDao.getInventoryReport(threshold);
        } else {
            items = reportDao.getInventoryReport();
        }

        writer.println("Item ID,Name,Stock,Low Stock");

        for (InventoryReportItem item : items) {
            writer.println(
                    item.getItemId() + "," +
                            safeCsv(item.getName()) + "," +
                            item.getStock() + "," +
                            item.isLowStock()
            );
        }
    }

    /**
     * Simple CSV field escaper:
     *  - Wraps in quotes if contains comma or quote
     *  - Doubles internal quotes
     */
    private String safeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String result = value.replace("\"", "\"\"");
        if (needsQuotes) {
            result = "\"" + result + "\"";
        }
        return result;
    }
}