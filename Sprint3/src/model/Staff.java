package model;

/**
 * Staff represents an employee account within the restaurant booking
 * and ordering system. It extends the {@link User} class by including additional
 * fields specific to staff management, such as phone number, employment role,
 * and activation status.
 * <p>
 * This model is primarily used by administrators and managers to create,
 * manage, and display staff records. It is also utilized by authentication
 * and authorization mechanisms to determine access levels and redirect users
 * to the correct dashboard view.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Extends User with staff-specific attributes.</li>
 *   <li>Supports multiple constructors for database mapping and creation flows.</li>
 *   <li>Overrides getDashboardPage() to provide role-based redirection.</li>
 *   <li>Includes toString() for clear debugging and logging output.</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * Staff s = new Staff("John Doe", "john@example.com",
 *                     "575-555-1234", "manager", "securePass123", true);
 * System.out.println(s.getDashboardPage()); // "adminDashboard.jsp" if role = admin
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class Staff extends User {

    /** Contact phone number for the staff member. */
    private String phone;

    /** Role of the staff member (e.g., server, manager, kitchen staff). */
    private String role;

    /** Indicates whether the staff account is active (enabled for login). */
    private boolean active;

    // ===== Constructors =====

    /**
     * Default no-argument constructor.
     * <p>
     * Required for frameworks, ORM tools, and reflective instantiation.
     * </p>
     */
    public Staff() {
        super();
    }

    /**
     * Full constructor used internally or during database retrieval.
     * <p>
     * Initializes all attributes, including inherited {@link User} fields
     * such as ID, name, email, password, and role.
     * </p>
     *
     * @param id        the unique identifier for the staff member
     * @param name      the staff member’s name
     * @param email     the staff member’s email address
     * @param password  the staff member’s password (hashed)
     * @param phone     the staff member’s contact number
     * @param role      the staff member’s assigned role (e.g., server, manager)
     * @param active    whether the account is currently active
     */
    public Staff(int id, String name, String email, String password,
                 String phone, String role, boolean active) {
        super(id, name, email, password, role);
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    /**
     * Constructor used when creating new staff accounts from controller logic.
     * <p>
     * Matches the common call format:
     * {@code new Staff(name, email, phone, role, password, active)}.
     * </p>
     *
     * @param name      the staff member’s name
     * @param email     the staff member’s email address
     * @param phone     the contact number
     * @param role      the role (e.g., server, manager)
     * @param password  the password (hashed)
     * @param active    account activation flag
     */
    public Staff(String name, String email, String phone, String role, String password, boolean active) {
        this(0, name, email, password, phone, role, active);
    }

    /**
     * Alternate constructor assuming a default active status of {@code true}.
     *
     * @param name      the staff member’s name
     * @param email     the staff member’s email address
     * @param phone     the contact number
     * @param role      the role (e.g., server, manager)
     * @param password  the password (hashed)
     */
    public Staff(String name, String email, String phone, String role, String password) {
        this(0, name, email, password, phone, role, true);
    }

    // ===== Getters & Setters =====

    /** @return the staff member’s phone number */
    public String getPhone() {
        return phone;
    }

    /** @param phone sets the staff member’s phone number */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** @return the staff member’s assigned role (e.g., server, manager, kitchen) */
    @Override
    public String getRole() {
        return role;
    }

    /** @param role sets the staff member’s assigned role */
    public void setRole(String role) {
        this.role = role;
    }

    /** @return {@code true} if the staff account is active, otherwise false */
    public boolean isActive() {
        return active;
    }

    /** @param active sets the activation state of the staff account */
    public void setActive(boolean active) {
        this.active = active;
    }

    // ===== Logic =====

    /**
     * Returns the JSP dashboard page based on the staff member’s role.
     * <p>
     * Admin users are redirected to adminDashboard.jsp,
     * while all other roles default to staffDashboard.jsp.
     * </p>
     *
     * @return the name of the dashboard JSP page corresponding to the user’s role
     */
    @Override
    public String getDashboardPage() {
        if ("admin".equalsIgnoreCase(role)) {
            return "adminDashboard.jsp";
        }
        return "staffDashboard.jsp";
    }

    /**
     * Returns a string representation of the Staff object, useful for
     * debugging, logging, or administrative console display.
     *
     * @return a formatted string containing staff details
     */
    @Override
    public String toString() {
        return "Staff{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}
