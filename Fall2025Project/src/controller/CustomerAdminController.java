package controller;

import dao.CustomerDAO;
import model.Customer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Admin controller for managing customer accounts (view/delete).
 * Independent from the public CustomerController used for registration.
 *
 * @author Daniel Sanchez
 * @version 1.0
 * @since 2025-10
 */
public class CustomerAdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "delete":
                    deleteCustomer(req, resp);
                    break;
                default:
                    listCustomers(req, resp);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error managing customers: " + e.getMessage());
            req.getRequestDispatcher("adminDashboard.jsp").forward(req, resp);
        }
    }

    /** Display all registered customers for admin */
    private void listCustomers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            CustomerDAO dao = new CustomerDAO();
            List<Customer> customers = dao.getAllCustomers();
            req.setAttribute("customers", customers);
            req.getRequestDispatcher("manageCustomers.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to load customer list.");
            req.getRequestDispatcher("adminDashboard.jsp").forward(req, resp);
        }
    }

    /** Delete a customer by ID (from both customers + users tables) */
    private void deleteCustomer(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect("customersAdmin?action=list");
            return;
        }

        int id = Integer.parseInt(idStr);
        try {
            CustomerDAO dao = new CustomerDAO();
            boolean success = dao.deleteById(id);
            if (!success) System.out.println("⚠ Failed to delete customer ID " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        resp.sendRedirect("customersAdmin?action=list");
    }
}
