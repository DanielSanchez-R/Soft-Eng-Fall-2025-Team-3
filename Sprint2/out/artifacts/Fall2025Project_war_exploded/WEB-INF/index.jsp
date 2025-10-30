<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Welcome to Pizzas 505 ENMU</title>
    <style>
        body {
            font-family: "Segoe UI", Arial, sans-serif;
            margin: 0;
            padding: 0;
            background: url('https://images.unsplash.com/photo-1600891964599-f61ba0e24092?auto=format&fit=crop&w=1920&q=90')
            no-repeat center center fixed;
            background-size: cover;
            color: #fff;
        }
        header {
            background-color: rgba(192, 57, 43, 0.9);
            color: white;
            padding: 20px 0;
            text-align: center;
        }
        h1 { margin: 0; font-size: 2em; }
        nav {
            margin-top: 10px;
        }
        nav a {
            color: white;
            text-decoration: none;
            margin: 0 15px;
            font-weight: bold;
        }
        nav a:hover { text-decoration: underline; }

        .content {
            text-align: center;
            margin: 40px auto;
            width: 80%;
        }

        .pizza-types {
            display: flex;
            justify-content: center;
            flex-wrap: wrap;
            gap: 20px;
            margin-top: 30px;
        }
        .pizza-card {
            background: white;
            padding: 20px;
            border-radius: 10px;
            width: 250px;
            box-shadow: 0 3px 8px rgba(0,0,0,0.1);
        }
        .pizza-card img {
            width: 100%;
            border-radius: 10px;
        }
        .pizza-card h3 {
            margin-top: 10px;
            color: #c0392b;
        }

        .coupons {
            margin-top: 50px;
            padding: 20px;
            background: #fbeee0;
            border-top: 3px solid #c0392b;
        }
        .coupon {
            font-size: 18px;
            font-weight: bold;
            color: #444;
            margin: 10px 0;
        }
        .coupons h2 {
            color: #222; /* darker text color */
            font-weight: 800;
        }

        .login-section {
            margin-top: 50px;
        }
        .login-section a {
            background-color: #007bff;
            color: white;
            padding: 10px 18px;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
        }
        .login-section a:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>

<!-- Background overlay -->
<div style="
    position: fixed;
    top: 0; left: 0;
    width: 100%; height: 100%;
    background: rgba(0, 0, 0, 0.6);
    z-index: -1;
"></div>

<header>
    <h1>Welcome to Pizzas 505 ENMU 🍕</h1>
    <nav>
        <a href="#menu">Menu</a>
        <a href="#coupons">Coupons</a>
        <a href="login.jsp">Login</a>
    </nav>
</header>

<div class="content">
    <h2 id="menu">Our Signature Pizzas</h2>
    <div class="pizza-types">
        <div class="pizza-card">
            <img src="https://upload.wikimedia.org/wikipedia/commons/d/d3/Supreme_pizza.jpg" alt="Hand Tossed">
            <h3>Hand-Tossed Classic</h3>
            <p>Soft, fluffy, and perfectly baked with our signature red sauce.</p>
        </div>
        <div class="pizza-card">
            <img src="https://upload.wikimedia.org/wikipedia/commons/d/d3/Supreme_pizza.jpg" alt="Thin Crust">
            <h3>Thin Crust Delight</h3>
            <p>Light, crispy, and full of flavor — ideal for a quick bite!</p>
        </div>
        <div class="pizza-card">
            <img src="https://upload.wikimedia.org/wikipedia/commons/d/d3/Supreme_pizza.jpg" alt="Deep Dish">
            <h3>Deep-Dish Supreme</h3>
            <p>Layered with cheese and toppings, baked to gooey perfection.</p>
        </div>
    </div>

    <div class="coupons" id="coupons">
        <h2>🔥 Weekly Specials</h2>
        <div class="coupon">🍕 3 for 2 on Carry-Out Orders!</div>
        <div class="coupon">🥤 Free 2-Liter Drink with Every Large Pizza Order!</div>
        <div class="coupon">🎓 ENMU Students get 10% off with ID Card!</div>
    </div>

    <div class="login-section">
        <h2>Ready to Order?</h2>
        <a href="login.jsp">Login / Staff / Admin Portal</a>
    </div>

    <div style="text-align:center; margin-top:40px; font-size:20px; color:#2c3e50;">
        🥰 <b>Thanks for shopping with Pizzas 505 ENMU!</b> 🍕
    </div>

</div>

<footer style="text-align:center; padding:20px; margin-top:40px; background:#c0392b; color:white;">
    © 2025 Pizzas 505 ENMU — All Rights Reserved.
</footer>

</body>
</html>
