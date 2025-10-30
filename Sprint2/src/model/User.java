package model;

/**
 * User represents an authenticated user within the restaurant booking and
 * ordering system. It serves as a simple model class (JavaBean/POJO) containing
 * the essential attributes and accessors for application-level user data.
 * <p>
 * Each user has a unique identifier, personal details (name, email), a hashed password,
 * and an assigned role that determines access permissions throughout the system.
 * </p>
 *
 * <h3>Common Roles:</h3>
 * <ul>
 *   <li><b>admin</b> — Full access to all management features.</li>
 *   <li><b>staff</b> — Can manage reservations, menus, and orders.</li>
 *   <li><b>customer</b> — (Future use) Can browse menu, reserve tables, and order online.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <ul>
 *   <li>Used by controllers (e.g., AuthController) to represent logged-in users.</li>
 *   <li>Mapped to the Users table in the database via DAO classes.</li>
 *   <li>Supports multiple constructors for flexibility during instantiation.</li>
 * </ul>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class User {

    /** Unique numeric identifier for the user (primary key in the database). */
    private int id;

    /** Full name of the user. */
    private String name;

    /** User’s registered email address (used as login credential). */
    private String email;

    /** User’s password (should be stored in hashed form for security). */
    private String password;

    /** Role defining the user’s access level (e.g., admin, staff). */
    private String role;

    /**
     * Default no-argument constructor.
     * <p>
     * Required for frameworks that use reflection or serialization (e.g., JSP/Servlets,
     * JDBC ORM mappings).
     * </p>
     */
    public User() {}

    /**
     * Constructs a User with the specified ID, name, email, and password.
     * The role is automatically assigned as "staff" by default.
     *
     * @param id        the unique user ID
     * @param name      the user’s full name
     * @param email     the user’s email address
     * @param password  the user’s password (hashed)
     */
    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = "staff";   // default role
    }

    /**
     * Constructs a User with all properties explicitly defined.
     *
     * @param id        the unique user ID
     * @param name      the user’s full name
     * @param email     the user’s email address
     * @param password  the user’s password (hashed)
     * @param role      the user’s role (admin, staff, etc.)
     */
    public User(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /** @return the user’s unique ID */
    public int getId() { return id; }

    /** @return the user’s full name */
    public String getName() { return name; }

    /** @return the user’s email address */
    public String getEmail() { return email; }

    /** @return the user’s password (hashed) */
    public String getPassword() { return password; }

    /** @return the user’s assigned role */
    public String getRole() { return role; }

    /** @param id sets the unique ID of the user */
    public void setId(int id) { this.id = id; }

    /** @param name sets the full name of the user */
    public void setName(String name) { this.name = name; }

    /** @param email sets the email address of the user */
    public void setEmail(String email) { this.email = email; }

    /** @param password sets the user’s password (should be hashed before storage) */
    public void setPassword(String password) { this.password = password; }

    /** @param role sets the user’s role (e.g., admin, staff) */
    public void setRole(String role) { this.role = role; }

    /**
     * Returns the dashboard page associated with the user’s role.
     * <p>
     * This method can be overridden or expanded to support different dashboards
     * (e.g., admin vs. staff). Currently defaults to the staff dashboard.
     * </p>
     *
     * @return the JSP page name representing the user’s dashboard view
     */
    public String getDashboardPage() {
        return "staffDashboard.jsp";
    }
}
