package model;

/**
 * DailySale
 * Represents a single order entry in the Daily Sales Report.
 *
 * @author Daniel Sanchez
 * @version 4.0
 */
public class DailySale {
    private int orderId;
    private String customerEmail;
    private double total;
    private String orderTime;  // stored as string for easy display

    /**
     * Default constructor.
     */
    public DailySale() {}

    /**
     * Constructs a DailySale record with all fields initialized.
     *
     * @param orderId        the ID of the order
     * @param customerEmail  the email of the customer
     * @param total          the total amount of the order
     * @param orderTime      the time the order was placed
     */
    public DailySale(int orderId, String customerEmail, double total, String orderTime) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.total = total;
        this.orderTime = orderTime;
    }

    /**
     * @return the order ID
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID.
     *
     * @param orderId the ID to assign
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the customer's email address
     */
    public String getCustomerEmail() {
        return customerEmail;
    }

    /**
     * Sets the customer's email.
     *
     * @param customerEmail the email to assign
     */
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    /**
     * @return the total amount of the order
     */
    public double getTotal() {
        return total;
    }

    /**
     * Sets the order total.
     *
     * @param total the total amount to assign
     */
    public void setTotal(double total) {
        this.total = total;
    }

    /**
     * @return the time the order was placed
     */
    public String getOrderTime() {
        return orderTime;
    }

    /**
     * Sets the order time.
     *
     * @param orderTime the time string to assign
     */
    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }
}
