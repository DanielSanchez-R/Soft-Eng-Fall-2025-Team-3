package dao;

import model.DailySale;
import model.MonthlyRevenue;
import model.InventoryReportItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportDao
 * Data access object for generating reporting data:
 *  - Daily sales report
 *  - Monthly revenue report
 *  - Inventory status with low-stock flag
 *
 * This DAO is intentionally robust and defensive:
 *  - Uses try-with-resources to avoid leaks
 *  - Returns empty lists instead of null
 *  - Handles null aggregates (SUM) as 0.0
 * @author Daniel Sanchez
 * @version 4.0
 */
public class ReportDao {

    /** Default threshold for marking an inventory item as low stock. */
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    /**
     * Returns all sales (orders) for a specific calendar date.
     *
     * @param date the calendar day to report on (java.sql.Date)
     * @return list of DailySale objects (never null)
     * @throws SQLException if a database error occurs
     */
    public List<DailySale> getDailySales(Date date) throws SQLException {
        List<DailySale> sales = new ArrayList<>();

        String sql =
                "SELECT id, customer_email, total, order_time " +
                        "FROM Orders " +
                        "WHERE CAST(order_time AS DATE) = ? " +
                        "ORDER BY order_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, date);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailySale sale = new DailySale();
                    sale.setOrderId(rs.getInt("id"));
                    sale.setCustomerEmail(rs.getString("customer_email"));
                    sale.setTotal(rs.getDouble("total"));

                    // Use toString() for simple JSP display; can be formatted later
                    sale.setOrderTime(rs.getTimestamp("order_time").toString());

                    sales.add(sale);
                }
            }
        }

        return sales;
    }

    /**
     * Returns all sales (orders) for a date range, inclusive.
     * Useful if you later need custom reports for a whole week, etc.
     *
     * @param start start date (inclusive)
     * @param end   end date (inclusive)
     * @return list of DailySale objects (never null)
     * @throws SQLException if a database error occurs
     */
    public List<DailySale> getDailySales(Date start, Date end) throws SQLException {
        List<DailySale> sales = new ArrayList<>();

        String sql =
                "SELECT id, customer_email, total, order_time " +
                        "FROM Orders " +
                        "WHERE CAST(order_time AS DATE) BETWEEN ? AND ? " +
                        "ORDER BY order_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, start);
            ps.setDate(2, end);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailySale sale = new DailySale();
                    sale.setOrderId(rs.getInt("id"));
                    sale.setCustomerEmail(rs.getString("customer_email"));
                    sale.setTotal(rs.getDouble("total"));
                    sale.setOrderTime(rs.getTimestamp("order_time").toString());
                    sales.add(sale);
                }
            }
        }

        return sales;
    }

    /**
     * Computes the total revenue for a given month and year.
     * Uses SUM(total) over the Orders table.
     *
     * @param month the month (1-12)
     * @param year  the year (e.g. 2025)
     * @return MonthlyRevenue with revenue set to 0.0 if no data exists
     * @throws SQLException if a database error occurs
     */
    public MonthlyRevenue getMonthlyRevenue(int month, int year) throws SQLException {
        String sql =
                "SELECT SUM(total) AS revenue " +
                        "FROM Orders " +
                        "WHERE YEAR(order_time) = ? " +
                        "  AND MONTH(order_time) = ?";

        double revenue = 0.0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // SUM can return null when there are no rows
                    revenue = rs.getDouble("revenue");
                    if (rs.wasNull()) {
                        revenue = 0.0;
                    }
                }
            }
        }

        return new MonthlyRevenue(month, year, revenue);
    }

    /**
     * Returns the full inventory report using the default low-stock threshold.
     *
     * @return list of InventoryReportItem objects (never null)
     * @throws SQLException if a database error occurs
     */
    public List<InventoryReportItem> getInventoryReport() throws SQLException {
        return getInventoryReport(DEFAULT_LOW_STOCK_THRESHOLD);
    }

    /**
     * Returns the full inventory report with a custom low-stock threshold.
     *
     * @param lowStockThreshold any item with stock <= this value is considered low-stock
     * @return list of InventoryReportItem objects (never null)
     * @throws SQLException if a database error occurs
     */
    public List<InventoryReportItem> getInventoryReport(int lowStockThreshold) throws SQLException {
        List<InventoryReportItem> items = new ArrayList<>();

        String sql =
                "SELECT id, name, stock " +
                        "FROM MENUITEM " +
                        "ORDER BY name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int itemId = rs.getInt("id");
                String name = rs.getString("name");
                int stock = rs.getInt("stock");

                boolean lowStock = stock <= lowStockThreshold;

                InventoryReportItem item =
                        new InventoryReportItem(itemId, name, stock, lowStock);

                items.add(item);
            }
        }

        return items;
    }
}
