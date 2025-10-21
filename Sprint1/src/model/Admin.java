package model;

/**
 * Admin represents a system administrator within the
 * Pizza 505 ENMU restaurant booking and ordering system.
 * <p>
 * This class extends {@link User} to define administrative privileges
 * and redirect logic. Admin users have full control over menu management,
 * staff accounts, reporting, and system configuration.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Manage staff accounts (create, update, activate/deactivate).</li>
 *   <li>Oversee menu items, pricing, and table configuration.</li>
 *   <li>Generate and export business performance reports.</li>
 *   <li>Access full administrative dashboard and tools.</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * Admin admin = new Admin(1, "Daniel Sanchez",
 *                         "admin@pizzas505.com",
 *                         "securePass123");
 * System.out.println(admin.getDashboardPage()); // "adminDashboard.jsp"
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class Admin extends User {

    /**
     * Constructs an Admin user with the specified credentials and assigns
     * the default role of "admin".
     *
     * @param id        unique identifier for the admin
     * @param name      admin’s full name
     * @param email     admin’s email address
     * @param password  admin’s password (hashed)
     */
    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password, "admin");
    }

    /**
     * Returns the dashboard page associated with admin accounts.
     *
     * @return the JSP page name for the admin dashboard
     */
    @Override
    public String getDashboardPage() {
        return "adminDashboard.jsp";
    }
}
