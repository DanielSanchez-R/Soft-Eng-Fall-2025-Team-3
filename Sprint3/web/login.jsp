<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // If already logged in, bounce to the right dashboard
    Object userObj = session.getAttribute("userObj");
    if (userObj != null) {
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
            --brand:#c0392b;
        }

        body {
            font-family: "Segoe UI", Arial, sans-serif;
            margin: 0;
            padding: 0;
            background: url('https://images.unsplash.com/photo-1525610553991-2bede1a236e2?auto=format&fit=crop&w=1920&q=90')
            no-repeat center center fixed;
            background-size: cover;
            color: #fff;
        }

        /* dark overlay for readability */
        .overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.6);
            z-index: -1;
        }

        header {
            background: rgba(192, 57, 43, 0.9);
            color:#fff;
            text-align:center;
            padding:18px 10px;
        }

        header a {
            color:#fff;
            text-decoration:none;
            margin:0 10px;
            font-weight:bold;
        }

        .wrap {
            display:flex;
            justify-content:center;
            padding:60px 12px;
        }

        .card {
            width:100%;
            max-width:380px;
            background: rgba(255, 255, 255, 0.95);
            border-radius:12px;
            box-shadow:0 6px 20px rgba(0,0,0,.2);
            padding:24px;
            color:#222;
        }

        h2 {
            margin:0 0 8px;
            color:#c0392b;
            text-align:center;
        }

        p.sub {
            margin:0 0 18px;
            color:#555;
            text-align:center;
        }

        form { margin-top:12px; }

        label {
            display:block;
            font-size:14px;
            color:#444;
            margin-top:12px;
        }

        input[type="email"], input[type="password"] {
            width:100%;
            padding:10px 12px;
            margin-top:6px;
            border:1px solid #ddd;
            border-radius:8px;
            font-size:14px;
            box-sizing:border-box;
        }

        button {
            width:100%;
            margin-top:18px;
            padding:12px;
            background:var(--primary);
            color:#fff;
            border:none;
            border-radius:8px;
            font-weight:bold;
            cursor:pointer;
            font-size:15px;
        }

        button:hover { background:var(--primary-dark); }

        .row {
            display:flex;
            justify-content:space-between;
            align-items:center;
            margin-top:10px;
        }

        .link {
            color:var(--primary);
            text-decoration:none;
            font-size:14px;
        }

        .link:hover { text-decoration:underline; }

        .alert {
            padding:10px 12px;
            border-radius:8px;
            font-size:14px;
            margin-bottom:10px;
        }

        .alert-error { background:#ffe6e6; color:#a30000; border:1px solid #ffcaca; }
        .alert-ok { background:#e8fff1; color:#0a7a3b; border:1px solid #bff0d1; }

        .helper {
            text-align:center;
            margin-top:14px;
            font-size:14px;
            color:#555;
        }

        .helper a { font-weight:bold; }

        footer {
            text-align:center;
            color:#ddd;
            padding:18px 10px;
            font-size:13px;
        }
    </style>
</head>
<body>

<div class="overlay"></div>

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

        <form action="auth" method="post">
            <input type="hidden" name="redirect"
                   value="<%= request.getParameter("redirect") != null ? request.getParameter("redirect") : "" %>"/>

            <label for="email">Email</label>
            <input id="email" type="email" name="email" placeholder="you@example.com" required>

            <label for="password">Password</label>
            <input id="password" type="password" name="password" placeholder="Your password" required>

            <button type="submit">Login</button>

            <div class="row">
                <a class="link" href="forgotPassword.jsp">Forgot password?</a>
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

