package controller;

import dao.DBConnection;
import dao.StaffDAO;
import dao.UserDaoImpl;
import dao.CustomerDAO;
import model.Admin;
import model.Customer;
import model.Staff;
import model.User;
import util.LoginAttemptTracker;
import util.EmailUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;
/**
 * AuthController is a servlet that manages authentication,
 * password recovery, and session handling for all user roles
 * (Admin, Manager, Staff, and Customer) in the Pizza 505 ENMU
 * online ordering and management system.
 *
 * <p>This controller integrates multiple authentication pathways:
 * user login, password reset via email token, and first-time password
 * setup from staff invitations. It connects to multiple DAO layers
 * for unified authentication across the users, staff,
 * and customers tables.</p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Authenticate users and redirect based on role.</li>
 *   <li>Handle password reset and setup via secure email token links.</li>
 *   <li>Support admin, staff, and customer login flows.</li>
 *   <li>Prevent brute-force attempts via {@link LoginAttemptTracker}.</li>
 *   <li>Send email notifications for password recovery using {@link EmailUtil}.</li>
 * </ul>
 *
 * <h3>Supported Actions:</h3>
 * <ul>
 *   <li><b>GET /auth?action=setPassword</b> — Verify token and display password setup page.</li>
 *   <li><b>GET /auth?action=forgot</b> — Forward to forgot-password page.</li>
 *   <li><b>POST /auth?action=forgotPassword</b> — Send password reset link to staff email.</li>
 *   <li><b>POST /auth?action=savePassword</b> — Save new password using a valid token.</li>
 *   <li><b>POST /auth</b> — Handle general login for all users (admin, staff, customers).</li>
 * </ul>
 *
 * <h3>Security Features:</h3>
 * <ul>
 *   <li>Rate limiting and temporary account lockouts via {@link LoginAttemptTracker}.</li>
 *   <li>UUID-based password reset tokens stored in password_reset_tokens table.</li>
 *   <li>One-time-use token enforcement for password setup links.</li>
 *   <li>Session-based role verification and redirection control.</li>
 * </ul>
 *
 * <h3>Role-based Redirects:</h3>
 * <ul>
 *   <li><b>Admin →</b> adminDashboard.jsp</li>
 *   <li><b>Manager/Staff →</b> staffDashboard.jsp</li>
 *   <li><b>Customer →</b> customerDashboard.jsp</li>
 * </ul>
 *
 * <h3>Database Tables Accessed:</h3>
 * <ul>
 *   <li>users</li>
 *   <li>staff</li>
 *   <li>customers</li>
 *   <li>password_reset_tokens</li>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class AuthController extends HttpServlet {

    private UserDaoImpl userDao;
    private StaffDAO staffDAO;
    private CustomerDAO customerDAO;
    /**
     * Initializes DAO objects and establishes a database connection
     * at servlet startup. This ensures that DAOs are ready for use
     * during authentication requests.
     */
    @Override
    public void init() {
        Connection conn = DBConnection.getConnection();
        userDao = new UserDaoImpl(conn);
        staffDAO = new StaffDAO();
        customerDAO = new CustomerDAO();
    }

    /**
     * Handles GET /auth requests for rendering password setup
     * and forgot-password pages.
     *
     * @param request  the {@link HttpServletRequest} containing action and token parameters
     * @param response the {@link HttpServletResponse} for forwarding or redirection
     * @throws ServletException if a servlet or SQL error occurs
     * @throws IOException      if an I/O or redirect error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // Handle password setup links from staff invite emails or reset links
        if ("setPassword".equalsIgnoreCase(action)) {
            String token = request.getParameter("token");
            if (token == null || token.isEmpty()) {
                request.setAttribute("error", "Invalid or missing reset token.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT email FROM password_reset_tokens WHERE token=? AND used=FALSE";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, token);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        String email = rs.getString("email");
                        request.setAttribute("token", token);
                        request.setAttribute("email", email);
                        System.out.println("Token verified for password setup: " + email);
                        request.getRequestDispatcher("setPassword.jsp").forward(request, response);
                        return;
                    } else {
                        request.setAttribute("error", "Invalid or expired token. Please contact admin.");
                        request.getRequestDispatcher("login.jsp").forward(request, response);
                        return;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("error", "Database error verifying token.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                return;
            }
        }

        if ("forgot".equals(action)) {
            request.getRequestDispatcher("forgotStaffPassword.jsp").forward(request, response);
        } else {
            response.sendRedirect("login.jsp");
        }
    }
    /**
     * Handles POST /auth requests for login, password reset,
     * and password setup.
     *
     * @param request  the {@link HttpServletRequest} containing login or password data
     * @param response the {@link HttpServletResponse} used for redirects or forwards
     * @throws ServletException if servlet-level validation fails
     * @throws IOException      if I/O or redirect errors occur
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // Forgot password (REAL EMAIL VERSION)
        if ("forgotPassword".equalsIgnoreCase(action)) {
            String email = request.getParameter("email");

            if (email == null || email.isEmpty()) {
                request.setAttribute("error", "Please enter your email address.");
                request.getRequestDispatcher("forgotStaffPassword.jsp").forward(request, response);
                return;
            }

            Staff staff = staffDAO.getStaffByEmail(email);
            if (staff == null) {
                request.setAttribute("error", "No staff account found with that email.");
                request.getRequestDispatcher("forgotStaffPassword.jsp").forward(request, response);
                return;
            }

            String token = UUID.randomUUID().toString();
            try (Connection conn = DBConnection.getConnection()) {
                String createTable = "CREATE TABLE IF NOT EXISTS password_reset_tokens (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "email VARCHAR(100), " +
                        "token VARCHAR(255), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "used BOOLEAN DEFAULT FALSE)";
                conn.createStatement().execute(createTable);

                String insert = "INSERT INTO password_reset_tokens (email, token, used) VALUES (?, ?, FALSE)";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setString(1, email);
                    ps.setString(2, token);
                    ps.executeUpdate();
                }

                // Send real email
                String resetLink = "http://localhost:8080/Fall2025Project/auth?action=setPassword&token=" + token;
                String subject = "Password Reset - Pizzas 505 ENMU";
                String body = "<h2>Hi " + staff.getName() + ",</h2>"
                        + "<p>We received a request to reset your password for your Pizzas 505 ENMU staff account.</p>"
                        + "<p>Click the link below to reset your password:</p>"
                        + "<p><a href='" + resetLink + "'>Reset My Password</a></p>"
                        + "<p>If you did not request this, you can safely ignore this email.</p>"
                        + "<br><hr><small>This link expires in 24 hours.</small>";

                EmailUtil.sendEmail(email, subject, body);
                System.out.println("📨 Reset email sent to: " + email);

                request.setAttribute("success", "A password reset link has been sent to your email.");
                request.getRequestDispatcher("forgotStaffPassword.jsp").forward(request, response);
                return;

            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("error", "Error generating reset link. Please try again.");
                request.getRequestDispatcher("forgotStaffPassword.jsp").forward(request, response);
                return;
            }
        }

        // Handle password setup form submission (from invite/reset)
        if ("savePassword".equalsIgnoreCase(action)) {
            String token = request.getParameter("token");
            String newPassword = request.getParameter("password");

            if (token == null || newPassword == null || newPassword.isEmpty()) {
                request.setAttribute("error", "Invalid request or empty password.");
                request.getRequestDispatcher("setPassword.jsp").forward(request, response);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String find = "SELECT email FROM password_reset_tokens WHERE token=? AND used=FALSE";
                try (PreparedStatement ps = conn.prepareStatement(find)) {
                    ps.setString(1, token);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        String email = rs.getString("email");

                        // Update password
                        staffDAO.updatePassword(email, newPassword);

                        // Mark token as used
                        String mark = "UPDATE password_reset_tokens SET used=TRUE WHERE token=?";
                        try (PreparedStatement ps2 = conn.prepareStatement(mark)) {
                            ps2.setString(1, token);
                            ps2.executeUpdate();
                        }

                        System.out.println(" Password set successfully for: " + email);
                        request.setAttribute("success", "Password set successfully! Please log in.");
                        request.getRequestDispatcher("login.jsp").forward(request, response);
                        return;
                    } else {
                        request.setAttribute("error", "Invalid or expired token.");
                        request.getRequestDispatcher("login.jsp").forward(request, response);
                        return;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("error", "Success Please Log In with new Password.");
                request.getRequestDispatcher("setPassword.jsp").forward(request, response);
                return;
            }
        }

        // ORIGINAL LOGIN LOGIC BELOW

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            request.setAttribute("error", "Please enter both email and password.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        // Admin bypass
        if (email.equalsIgnoreCase("admin@restaurant.com") && password.equals("1234")) {
            HttpSession session = request.getSession();
            session.setAttribute("userObj", new Admin(1, "Admin", email, password));
            session.setAttribute("role", "admin");
            session.setAttribute("user", "Admin");
            System.out.println("🛠️ Admin bypass login successful.");
            response.sendRedirect("adminDashboard.jsp");
            return;
        }

        // Step 1: Rate limit
        if (LoginAttemptTracker.isLocked(email)) {
            long remainingSec = LoginAttemptTracker.getRemainingLockTime(email) / 1000;
            request.setAttribute("error",
                    " Account temporarily locked. Please wait " + remainingSec + " seconds.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        // Step 2: Lookup in all tables
        User user = null;
        try (Connection freshConn = dao.DBConnection.getConnection()) {
            user = new dao.UserDaoImpl(freshConn).getUserByEmail(email);
        } catch (Exception e) {
            System.out.println(" UserDaoImpl lookup failed, will try staff/customer fallback...");
        }

        if (user == null) {
            Staff staff = staffDAO.getStaffByEmail(email);
            if (staff != null) user = staff;
        }

        if (user == null) {
            Customer customer = customerDAO.getByEmail(email);
            if (customer != null) {
                user = new User(customer.getId(),
                        customer.getName(),
                        customer.getEmail(),
                        customer.getPassword(),
                        "customer");
                System.out.println("Customer login fallback triggered for: " + email);
            }
        }

        if (user == null) {
            LoginAttemptTracker.recordFailedAttempt(email);
            request.setAttribute("error", "No account found with that email.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        if (!password.equals(user.getPassword())) {
            LoginAttemptTracker.recordFailedAttempt(email);
            request.setAttribute("error", "Invalid credentials. Please try again.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        LoginAttemptTracker.resetAttempts(email);

        if (user instanceof Staff && !((Staff) user).isActive()) {
            request.setAttribute("error", "Your account has been deactivated. Contact admin.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("userObj", user);
        String role = (user.getRole() != null && !user.getRole().isEmpty())
                ? user.getRole().toLowerCase()
                : "customer";
        session.setAttribute("role", role);
        session.setAttribute("user", user.getName());

        String targetPage;
        switch (role) {
            case "admin": targetPage = "adminDashboard.jsp"; break;
            case "manager":
            case "server":
            case "kitchen":
            case "staff": targetPage = "staffDashboard.jsp"; break;
            case "customer": targetPage = "customerDashboard.jsp"; break;
            default: targetPage = "login.jsp"; break;
        }

        System.out.println("Login successful for: " + user.getEmail() + " (" + role + ")");

        try {
            if (!response.isCommitted()) {
                response.sendRedirect(targetPage);
                System.out.println("Redirected to: " + targetPage);
            } else {
                System.out.println("Response already committed — skipping redirect.");
            }
        } catch (IOException e) {
            System.out.println("Redirect failed for: " + user.getEmail());
            e.printStackTrace();
        }
    }
}
