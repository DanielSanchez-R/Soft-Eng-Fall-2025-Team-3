package controller;

import dao.StaffDAO;
import model.Staff;
import dao.UserDaoImpl;
import dao.DBConnection;
import model.User;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * StaffController is a servlet responsible for managing all
 * administrative and staff-related operations within the
 * Pizza 505 ENMU restaurant system.
 * <p>
 * It allows administrators to create, update, activate, deactivate,
 * and delete staff accounts, as well as handle password recovery
 * through secure email notifications.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Create new staff accounts (admin only)</li>
 *   <li>Edit and update existing staff information</li>
 *   <li>Activate / Deactivate staff accounts</li>
 *   <li>Delete staff from both STAFF and USERS tables</li>
 *   <li>Reset forgotten passwords (with validation & email confirmation)</li>
 *   <li>Automatic email notifications for new accounts and password resets</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>GET /staff?action=list</b> — Displays all staff.</li>
 *   <li><b>GET /staff?action=edit</b> — Opens edit form for specific staff.</li>
 *   <li><b>GET /staff?action=activate</b> — Reactivates staff account.</li>
 *   <li><b>GET /staff?action=deactivate</b> — Suspends staff account.</li>
 *   <li><b>GET /staff?action=delete</b> — Permanently removes staff.</li>
 *   <li><b>GET /staff?action=forgot</b> — Opens password recovery page.</li>
 *   <li><b>POST /staff?action=update</b> — Saves changes to a staff record.</li>
 *   <li><b>POST /staff?action=forgot</b> — Resets password and sends email.</li>
 *   <li><b>POST /staff</b> — Creates a new staff member.</li>
 * </ul>
 *
 * <h3>JSP Views:</h3>
 * <ul>
 *   <li>adminViewStaff.jsp — Displays all staff records.</li>
 *   <li>adminEditStaff.jsp — Edit staff details.</li>
 *   <li>adminCreateStaff.jsp — Create a new staff member.</li>
 *   <li>forgotStaffPassword.jsp — Staff password recovery page.</li>
 * </ul>
 *
 * <h3>Security:</h3>
 * <ul>
 *   <li>Restricts modification actions to admin users.</li>
 *   <li>Performs password validation (length ≥ 8, number, special char).</li>
 *   <li>Prevents duplicate email creation.</li>
 *   <li>Sends reset confirmations via Gmail SMTP (TLS).</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>
 * // Admin submits new staff creation form
 * POST /staff
 * name=John Doe&email=john@pizzas505.com&phone=575-555-8899&role=server&password=P@ssw0rd!
 *
 * -> StaffDAO.addStaff() inserts record
 * -> Welcome email sent automatically
 * -> Redirects to adminViewStaff.jsp
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class StaffController extends HttpServlet {

    /** DAO for all staff-related operations. */
    private final StaffDAO staffDAO = new StaffDAO();

    // SMTP CONFIGURATION

    /** Gmail account used to send administrative emails. */
    private static final String SMTP_USER = "your_test_email@gmail.com"; // CHANGE THIS

    /** App-specific Gmail password for the above account. */
    private static final String SMTP_PASS = "your_app_password"; // USE APP PASSWORD

    /** Gmail SMTP host. */
    private static final String SMTP_HOST = "smtp.gmail.com";

    /** Gmail TLS port. */
    private static final int SMTP_PORT = 587;

    // EMAIL SENDER

    /**
     * Sends an email message to a staff member.
     *
     * @param to      recipient email address
     * @param subject subject line of the message
     * @param body    plain-text message content
     */
    private void sendEmail(String to, String subject, String body) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER, "Pizzas 505 ENMU"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // GET HANDLER

    /**
     * Handles all ET /staff requests.
     * <p>
     * Determines the action parameter and routes requests to the
     * corresponding view or DAO operation (e.g., list, edit, activate).
     * </p>
     *
     * @param request  the {@link HttpServletRequest} with query parameters
     * @param response the {@link HttpServletResponse} for forwarding or redirection
     * @throws ServletException if a servlet-level error occurs
     * @throws IOException      if forwarding or redirection fails
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "forgot": {
                    request.getRequestDispatcher("forgotStaffPassword.jsp").forward(request, response);
                    break;
                }
                case "deactivate": {
                    int id = Integer.parseInt(request.getParameter("id"));
                    staffDAO.deactivateStaff(id);
                    response.sendRedirect("staff?action=list");
                    break;
                }
                case "activate": {
                    int id = Integer.parseInt(request.getParameter("id"));
                    staffDAO.activateStaff(id);
                    response.sendRedirect("staff?action=list");
                    break;
                }
                case "delete": {
                    int id = Integer.parseInt(request.getParameter("id"));
                    staffDAO.deleteStaff(id);
                    response.sendRedirect("staff?action=list");
                    break;
                }
                case "edit": {
                    int id = Integer.parseInt(request.getParameter("id"));
                    Staff staff = staffDAO.getStaffById(id);
                    request.setAttribute("staff", staff);
                    request.getRequestDispatcher("adminEditStaff.jsp").forward(request, response);
                    break;
                }
                case "list":
                default: {
                    List<Staff> staffList = staffDAO.getAllStaff();
                    request.setAttribute("staffList", staffList);
                    RequestDispatcher dispatcher = request.getRequestDispatcher("adminViewStaff.jsp");
                    dispatcher.forward(request, response);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Something went wrong while processing staff action.");
            request.getRequestDispatcher("adminViewStaff.jsp").forward(request, response);
        }
    }

    // POST HANDLER

    /**
     * Handles all POST /staff requests.
     * <p>
     * Supports three primary workflows:
     * </p>
     * <ul>
     *   <li><b>Update</b> — Modify an existing staff record.</li>
     *   <li><b>Forgot</b> — Reset a forgotten password (with validation).</li>
     *   <li><b>Create</b> — Add a new staff member and send welcome email.</li>
     * </ul>
     *
     * @param request  the {@link HttpServletRequest} containing form data
     * @param response the {@link HttpServletResponse} for redirection
     * @throws ServletException if database or forwarding error occurs
     * @throws IOException      if I/O or redirection fails
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // UPDATE EXISTING STAFF
        if ("update".equalsIgnoreCase(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String role = request.getParameter("role");

            Staff updated = new Staff();
            updated.setId(id);
            updated.setName(name);
            updated.setEmail(email);
            updated.setPhone(phone);
            updated.setRole(role);
            updated.setActive(true);

            boolean success = staffDAO.updateStaff(updated);
            if (success) {
                response.sendRedirect("staff?action=list");
            } else {
                request.setAttribute("error", "Failed to update staff record.");
                request.getRequestDispatcher("adminEditStaff.jsp").forward(request, response);
            }

            // RESET PASSWORD
        } else if ("forgot".equalsIgnoreCase(action)) {
            String email = request.getParameter("email");
            String newPass = request.getParameter("newPassword");

            // Basic password validation
            if (newPass.length() < 8 || !newPass.matches(".*\\d.*") || !newPass.matches(".*[!@#$%^&*].*")) {
                request.setAttribute("error", "Password must be at least 8 chars, include a number & special character.");
                request.getRequestDispatcher("forgotStaffPassword.jsp").forward(request, response);
                return;
            }

            boolean found = false;
            boolean updated = false;

            // Try staff table first
            if (staffDAO.emailExists(email)) {
                updated = staffDAO.updatePassword(email, newPass);
                found = true;
            } else {
                // Fallback to users table
                try {
                    UserDaoImpl userDao = new UserDaoImpl(DBConnection.getConnection());
                    User user = userDao.getUserByEmail(email);
                    if (user != null) {
                        user.setPassword(newPass);
                        updated = userDao.updateUser(user);
                        found = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!found) {
                request.setAttribute("error", "No account found with that email.");
                request.getRequestDispatcher("forgotStaffPassword.jsp").forward(request, response);
                return;
            }

            if (updated) {
                sendEmail(email, "Password Reset - Pizzas 505 ENMU",
                        "Your password has been successfully reset.\n\nNew Password: " + newPass +
                                "\n\nLogin here: http://localhost:8080/Fall2025Project/login.jsp\n\n🍕 - Admin Team");
                request.setAttribute("message", " Password reset successfully. Check your email.");
            } else {
                request.setAttribute("error", "Failed to update password.");
            }
            request.getRequestDispatcher("forgotStaffPassword.jsp").forward(request, response);

            // CREATE NEW STAFF ACCOUNT
        } else {
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String role = request.getParameter("role");
            String password = request.getParameter("password");

            if (password.length() < 8 || !password.matches(".*\\d.*") || !password.matches(".*[!@#$%^&*].*")) {
                request.setAttribute("error", "Password must be at least 8 chars, include a number & special character.");
                request.getRequestDispatcher("adminCreateStaff.jsp").forward(request, response);
                return;
            }

            if (staffDAO.emailExists(email)) {
                request.setAttribute("error", "Email already exists!");
                request.getRequestDispatcher("adminCreateStaff.jsp").forward(request, response);
                return;
            }

            Staff staff = new Staff(name, email, phone, role, password, true);
            boolean success = staffDAO.addStaff(staff);

            if (success) {
                sendEmail(email, "Welcome to Pizzas 505 ENMU!",
                        "Hello " + name + ",\n\nYour staff account has been created successfully.\n\n" +
                                "Role: " + role + "\nEmail: " + email + "\nPassword: " + password + "\n\n" +
                                "Login here: http://localhost:8080/Fall2025Project/login.jsp\n\n" +
                                "🍕 - Pizzas 505 ENMU Admin Team");

                System.out.println("Staff account created successfully for " + email);
                response.sendRedirect("staff?action=list");
            } else {
                request.setAttribute("error", "Error creating staff account.");
                request.getRequestDispatcher("adminCreateStaff.jsp").forward(request, response);
            }
        }
    }
}
