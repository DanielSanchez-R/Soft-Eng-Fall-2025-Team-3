package model;

/**
 * Customer represents a registered customer within the
 * Pizza 505 ENMU restaurant booking and ordering system.
 * <p>
 * This class extends {@link User} to include customer-specific
 * details and account state, such as a contact phone number
 * and an activation flag. A Customer can browse menus,
 * place online orders, and make table reservations.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Inherits core account data from {@link User} (ID, name, email, password, role).</li>
 *   <li>Automatically sets the role field to <b>"customer"</b>.</li>
 *   <li>Includes an active flag for account management or soft deactivation.</li>
 *   <li>Overrides getDashboardPage() to direct users to the customer dashboard.</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <ul>
 *   <li>Created when a new customer registers via the sign-up form.</li>
 *   <li>Used in authentication and session management to track logged-in users.</li>
 *   <li>Displayed in administrative views for customer account control.</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * Customer c = new Customer("Alice Doe", "alice@example.com",
 *                            "securePass123", "575-555-0123");
 * System.out.println(c.getDashboardPage()); // "customerDashboard.jsp"
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class Customer extends User {

    /** Contact phone number for the customer. */
    private String phone;

    /** Indicates whether the customer account is active. */
    private boolean active;

    /**
     * Default no-argument constructor.
     * <p>
     * Invokes {@link User#User()} and initializes active to true.
     * Required for frameworks and serialization tools.
     * </p>
     */
    public Customer() {
        super();
        this.active = true;
    }

    /**
     * Constructs a Customer with all identifying information.
     *
     * @param id        unique identifier for the customer
     * @param name      full name of the customer
     * @param email     customer’s email address
     * @param password  customer’s password (hashed)
     * @param phone     customer’s contact phone number
     */
    public Customer(int id, String name, String email, String password, String phone) {
        super(id, name, email, password, "customer");
        this.phone = phone;
        this.active = true;
    }

    /**
     * Constructs a Customer for new-account creation
     * (automatically assigns id = 0).
     *
     * @param name      full name of the customer
     * @param email     customer’s email address
     * @param password  customer’s password (hashed)
     * @param phone     customer’s contact phone number
     */
    public Customer(String name, String email, String password, String phone) {
        super(0, name, email, password, "customer");
        this.phone = phone;
        this.active = true;
    }

    /** @return the customer’s contact phone number */
    public String getPhone() { return phone; }

    /** @param phone sets the customer’s contact phone number */
    public void setPhone(String phone) { this.phone = phone; }

    /** @return true if the customer account is active, otherwise false */
    public boolean isActive() { return active; }

    /** @param active sets the customer’s active status */
    public void setActive(boolean active) { this.active = active; }

    /**
     * Returns the dashboard page associated with customer accounts.
     *
     * @return the JSP page name for the customer dashboard
     */
    @Override
    public String getDashboardPage() {
        return "customerDashboard.jsp";
    }

    /**
     * Returns a formatted string representation of the Customer object.
     * Useful for debugging, logging, or administrative output.
     *
     * @return a string describing this customer’s details
     */
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + phone + '\'' +
                ", active=" + active +
                '}';
    }
}
