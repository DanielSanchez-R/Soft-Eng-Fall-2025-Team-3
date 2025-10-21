<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // If already logged in, bounce to the right dashboard
    Object userObj = session.getAttribute("userObj");   // e.g., model.User in your app
    if (userObj != null) {
        // If your User has getDashboardPage(), use it. Else send to a default page.
        try {
            String dash = (String) userObj.getClass().getMethod("getDashboardPage").invoke(userObj);
            response.sendRedirect(dash != null ? dash : "index.jsp");
            return;
        } catch (Exception ignore) {}
    }

    String error = (String) request.getAttribute("error");
    String message = (String) request.getAttribute("message");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Login - Pizzas 505 ENMU</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <style>
        :root {
            --primary:#007bff;
            --primary-dark:#0056b3;
            --bg:#f8f9fa;
            --brand:#c0392b;
        }
        body { font-family: Arial, sans-serif; background: var(--bg); margin:0; }
        header {
            background: var(--brand); color:#fff; text-align:center; padding:18px 10px;
        }
        header a { color:#fff; text-decoration:none; margin:0 10px; font-weight:bold; }
        .wrap {
            display:flex; justify-content:center; padding:32px 12px;
        }
        .card {
            width: 100%; max-width: 380px; background:#fff; border-radius:12px;
            box-shadow:0 6px 20px rgba(0,0,0,.08); padding:24px;
        }
        h2 { margin:0 0 8px; color:#333; text-align:center; }
        p.sub { margin:0 0 18px; color:#666; text-align:center; }
        form { margin-top:12px; }
        label { display:block; font-size:14px; color:#444; margin-top:12px; }
        input[type="email"], input[type="password"] {
            width:100%; padding:10px 12px; margin-top:6px; border:1px solid #ddd; border-radius:8px;
            font-size:14px; box-sizing:border-box;
        }
        button {
            width:100%; margin-top:18px; padding:12px; background:var(--primary); color:#fff;
            border:none; border-radius:8px; font-weight:bold; cursor:pointer; font-size:15px;
        }
        button:hover { background:var(--primary-dark); }
        .row { display:flex; justify-content:space-between; align-items:center; margin-top:10px; }
        .link { color:var(--primary); text-decoration:none; font-size:14px; }
        .link:hover { text-decoration:underline; }
        .alert {
            padding:10px 12px; border-radius:8px; font-size:14px; margin-bottom:10px;
        }
        .alert-error { background:#ffe6e6; color:#a30000; border:1px solid #ffcaca; }
        .alert-ok { background:#e8fff1; color:#0a7a3b; border:1px solid #bff0d1; }
        .helper {
            text-align:center; margin-top:14px; font-size:14px; color:#555;
        }
        .helper a { font-weight:bold; }
        footer { text-align:center; color:#999; padding:18px 10px; font-size:13px; }
    </style>
</head>
<body>

<header>
    <div style="font-size:20px; font-weight:bold;">Pizzas 505 ENMU 🍕</div>
    <nav style="margin-top:6px;">
        <a href="index.jsp">Home</a>
        <a href="index.jsp#menu">Menu</a>
        <a href="index.jsp#coupons">Coupons</a>
    </nav>
</header>

<div class="wrap">
    <div class="card">
        <h2>Sign in</h2>
        <p class="sub">Admins, staff, and customers use the same login.</p>

        <% if (message != null) { %>
        <div class="alert alert-ok"><%= message %></div>
        <% } %>
        <% if (error != null) { %>
        <div class="alert alert-error"><%= error %></div>
        <% } %>

        <!-- POST goes to your AuthController (/auth) -->
        <form action="auth" method="post">
            <!-- If you support redirect-after-login, keep a hidden 'redirect' param -->
            <input type="hidden" name="redirect" value="<%= request.getParameter("redirect") != null ? request.getParameter("redirect") : "" %>"/>

            <label for="email">Email</label>
            <input id="email" type="email" name="email" placeholder="you@example.com" required>

            <label for="password">Password</label>
            <input id="password" type="password" name="password" placeholder="Your password" required>

            <button type="submit">Login</button>

            <div class="row">
                <a class="link" href="forgotPassword.jsp">Forgot password?</a>
                <!-- You can keep a single portal link or shortcuts -->
                <a class="link" href="index.jsp">Back to Home</a>
            </div>
        </form>

        <div class="helper">
            New here?
            <a class="link" href="registerCustomer.jsp">Create a customer account</a>
        </div>
    </div>
</div>

<footer>© 2025 Pizzas 505 ENMU — All Rights Reserved.</footer>

</body>
</html>
