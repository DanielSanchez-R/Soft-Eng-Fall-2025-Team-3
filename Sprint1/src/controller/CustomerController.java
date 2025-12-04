package controller;

import dao.CustomerDAO;
import model.Customer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CustomerController is a servlet responsible for handling
 * customer account registration and password reset requests within
 * the Pizza 505 ENMU online ordering system.
 * <p>
 * It communicates with the {@link CustomerDAO} for database operations,
 * validates user input, and forwards requests to the appropriate JSP
 * pages for feedback and navigation.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Register new customer accounts.</li>
 *   <li>Validate email uniqueness and password strength.</li>
 *   <li>Handle customer password resets.</li>
 *   <li>Provide feedback messages for success or failure states.</li>
 * </ul>
 *
 * <h3>Access:</h3>
 * <ul>
 *   <li>This controller is open to public access (for registration and password reset).</li>
 *   <li>Registered users are stored in both customers and users tables for login consistency.</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li><b>POST /customer?action=register</b> — Register a new customer.</li>
 *   <li><b>POST /customer?action=forgot</b> — Reset a customer’s password.</li>
 * </ul>
 *
 * <h3>JSP Views:</h3>
 * <ul>
 *   <li>registerCustomer.jsp — Customer registration form.</li>
 *   <li>forgotPassword.jsp — Password reset page.</li>
 *   <li>login.jsp — Redirect target after successful registration or reset.</li>
 * </ul>
 *
 * <h3>Example Workflow:</h3>
 * <pre>
 * // Customer Registration
 * POST /customer?action=register
 * name=Alice Smith&email=alice@example.com&password=Secure@123&phone=575-555-4421
 *
 * -> doPost() -> registerCustomer()
 * -> Validates input and inserts new record
 * -> Forwards to login.jsp with success message
 * </pre>
 *
 * @author Daniel
 * @version 1.0
 * @since 2025-10
 */
public class CustomerController extends HttpServlet {

    /** DAO responsible for all customer database operations. */
    private final CustomerDAO customerDAO = new CustomerDAO();

    /**
     * Handles POST /customer requests.
     * <p>
     * Routes actions based on the action request parameter:
     * registration or password reset.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} containing form parameters
     * @param response the {@link HttpServletResponse} used for forwarding or redirection
     * @throws ServletException if a servlet-level error occurs
     * @throws IOException      if a forwarding or I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) action = "register";

        switch (action) {
            case "register":
                registerCustomer(request, response);
                break;
            case "forgot":
                resetPassword(request, response);
                break;
            default:
                response.sendRedirect("index.jsp");
        }
    }

    /**
     * Handles customer registration form submissions.
     * <p>
     * Validates password length and email uniqueness before creating
     * a new {@link Customer} account. On success, mirrors the data into
     * the {users table and redirects to the login page.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} containing form data
     * @param response the {@link HttpServletResponse} used for forwarding
     * @throws IOException      if a forwarding or I/O error occurs
     * @throws ServletException if servlet-level validation fails
     */
    private void registerCustomer(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");

        if (password.length() < 8) {
            request.setAttribute("error", "Password must be at least 8 characters.");
            request.getRequestDispatcher("registerCustomer.jsp").forward(request, response);
            return;
        }

        if (customerDAO.emailExists(email)) {
            request.setAttribute("error", "Email already exists.");
            request.getRequestDispatcher("registerCustomer.jsp").forward(request, response);
            return;
        }

        Customer c = new Customer(name, email, password, phone);
        boolean added = customerDAO.addCustomer(c);

        if (added) {
            request.setAttribute("message", "Account created! Please login.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Error creating account.");
            request.getRequestDispatcher("registerCustomer.jsp").forward(request, response);
        }
    }

    /**
     * Handles customer password reset requests.
     * <p>
     * Verifies that the provided email exists and updates the password
     * using {@link CustomerDAO#updatePassword(String, String)}. On success,
     * the user is redirected to login.jsp; otherwise, an error
     * message is displayed on forgotPassword.jsp.
     * </p>
     *
     * @param request  the {@link HttpServletRequest} containing email and new password
     * @param response the {@link HttpServletResponse} used for forwarding
     * @throws IOException      if forwarding or redirection fails
     * @throws ServletException if database or validation errors occur
     */
    private void resetPassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String email = request.getParameter("email");
        String newPass = request.getParameter("newPassword");

        if (!customerDAO.emailExists(email)) {
            request.setAttribute("error", "No account found with that email.");
            request.getRequestDispatcher("forgotPassword.jsp").forward(request, response);
            return;
        }

        boolean updated = customerDAO.updatePassword(email, newPass);
        if (updated) {
            request.setAttribute("message", "Password updated. Please login.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Could not update password.");
            request.getRequestDispatcher("forgotPassword.jsp").forward(request, response);
        }
    }
}
