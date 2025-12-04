<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*, model.MenuItem" %>
<!DOCTYPE html>
<html>
<head>
    <title>Checkout - Pizzas 505 ENMU</title>
    <style>
        body { font-family: Segoe UI, Arial, sans-serif; background:#fff8f0; margin:0; padding:0; }
        header { background:#c0392b; color:white; text-align:center; padding:15px; font-size:20px; }
        table { width:80%; margin:auto; border-collapse:collapse; margin-top:20px; background:white; }
        th, td { padding:10px; border-bottom:1px solid #ddd; text-align:left; vertical-align:top; }
        th { background:#e74c3c; color:white; }
        .section { width:80%; margin:auto; background:white; padding:20px; margin-top:30px;
            border-radius:8px; box-shadow:0 2px 5px #ccc; }
        .actions { text-align:center; margin-top:25px; }
        .confirm-btn { background:#27ae60; color:white; padding:10px 25px; border:none; border-radius:6px; cursor:pointer; font-size:16px; transition:all 0.3s ease; }
        .confirm-btn:hover:not(:disabled) { background:#1e8449; }
        .confirm-btn:disabled { opacity:0.6; cursor:not-allowed; }
        .confirm-btn.shake { animation: shake 0.3s; box-shadow: 0 0 10px red; }
        @keyframes shake {
            0% { transform: translateX(0); }
            25% { transform: translateX(-5px); }
            50% { transform: translateX(5px); }
            75% { transform: translateX(-5px); }
            100% { transform: translateX(0); }
        }
        .cancel-btn { background:#e74c3c; color:white; padding:10px 25px; border:none; border-radius:6px; cursor:pointer; font-size:16px; margin-left:10px; }
        .cancel-btn:hover { background:#c0392b; }
        label { display:block; margin-top:10px; }
        input[type=text], input[type=number], input[type=email], input[type=time] {
            width:100%; padding:8px; border:1px solid #ccc; border-radius:4px;
        }
        .delivery-fields { display:none; }
        .discount-applied { color:#27ae60; font-weight:bold; margin-top:5px; }
        small.custom-info { color:#666; font-style:italic; display:block; margin-left:10px; }
    </style>

    <script>
        function recalcTotal() {
            const delivery = document.getElementById("delivery");
            const subtotal = parseFloat(document.getElementById("subtotalValue").value);
            const promo = document.getElementById("promoCode").value.trim().toUpperCase();
            const deliveryFeeRow = document.getElementById("deliveryFeeRow");
            const discountRow = document.getElementById("discountRow");
            const promoRow = document.getElementById("promoRow");
            const taxCell = document.getElementById("taxCell");
            const taxLabel = document.getElementById("taxLabel");
            const totalCell = document.getElementById("totalCell");

            const deliveryFee = delivery.checked ? 3.00 : 0.00;
            const taxRate = delivery.checked ? 0.08 : 0.06;

            let autoDiscount = subtotal >= 50 ? subtotal * 0.10 : 0.00;
            let promoDiscount = 0.00;

            if (promo === "D50") {
                promoDiscount = subtotal * 0.50;
                document.getElementById("promoStatus").innerHTML = "Promo applied: 50% off!";
            } else if (promo.length > 0) {
                document.getElementById("promoStatus").innerHTML = "Invalid promo code.";
            } else {
                document.getElementById("promoStatus").innerHTML = "";
            }

            let tax = subtotal * taxRate;
            let grand = subtotal + tax + deliveryFee - autoDiscount - promoDiscount;

            tax = Math.round(tax * 100) / 100;
            autoDiscount = Math.round(autoDiscount * 100) / 100;
            promoDiscount = Math.round(promoDiscount * 100) / 100;
            grand = Math.max(0, Math.round(grand * 100) / 100);

            taxLabel.innerHTML = "Tax (" + Math.round(taxRate * 100) + "%)";
            taxCell.innerHTML = "$" + tax.toFixed(2);
            document.getElementById("promoDiscountCell").innerHTML = "-$" + promoDiscount.toFixed(2);
            totalCell.innerHTML = "$" + grand.toFixed(2);
            discountRow.style.display = autoDiscount > 0 ? "table-row" : "none";
            promoRow.style.display = promoDiscount > 0 ? "table-row" : "none";
            deliveryFeeRow.style.display = delivery.checked ? "table-row" : "none";
            validateZip();
        }

        //  Validate ZIP radius dynamically
        function validateZip() {
            const delivery = document.getElementById("delivery");
            const zipInput = document.getElementById("zip");
            const deliveryStatus = document.getElementById("deliveryStatus");
            const placeOrderBtn = document.getElementById("placeOrderBtn");

            if (!delivery.checked) return; // Only validate ZIP when delivery selected

            const zip = parseInt(zipInput.value.trim());
            if (isNaN(zip)) {
                deliveryStatus.style.color = "red";
                deliveryStatus.innerHTML = "⚠ Please enter a valid ZIP code.";
                placeOrderBtn.disabled = true;
                placeOrderBtn.style.opacity = "0.6";
                return;
            }

            if (zip >= 88101 && zip <= 88103) {
                deliveryStatus.style.color = "#27ae60";
                deliveryStatus.innerHTML = "Delivery accepted in your area!";
                placeOrderBtn.disabled = false;
                placeOrderBtn.style.opacity = "1";
            } else {
                deliveryStatus.style.color = "red";
                deliveryStatus.innerHTML = "Delivery not available outside ZIP 88101–88103. Please select pickup.";
                placeOrderBtn.disabled = true;
                placeOrderBtn.style.opacity = "0.6";
            }
        }

        // Validate business hours & estimate time
        function validateTime() {
            const orderTime = document.getElementById("orderTime").value;
            const delivery = document.getElementById("delivery");
            const deliveryStatus = document.getElementById("deliveryStatus");
            const pickupStatus = document.getElementById("pickupStatus");
            const placeOrderBtn = document.getElementById("placeOrderBtn");

            deliveryStatus.innerHTML = "";
            pickupStatus.innerHTML = "";
            pickupStatus.style.display = "none";
            deliveryStatus.style.display = "none";

            placeOrderBtn.disabled = true;
            placeOrderBtn.style.opacity = "0.6";

            if (!orderTime) return;
            validateZip();
            const [hour, minute] = orderTime.split(":").map(Number);
            const totalMinutes = hour * 60 + minute;
            const open = 10 * 60;
            const close = 22 * 60;

            if (totalMinutes < open || totalMinutes >= close) {
                const msg = "⚠ Sorry, we only operate between 10:00 AM and 10:00 PM.";
                if (delivery.checked) {
                    deliveryStatus.style.color = "red";
                    deliveryStatus.innerHTML = msg;
                    deliveryStatus.style.display = "block";
                } else {
                    pickupStatus.style.color = "red";
                    pickupStatus.innerHTML = msg;
                    pickupStatus.style.display = "block";
                }
                placeOrderBtn.classList.add("shake");
                setTimeout(() => placeOrderBtn.classList.remove("shake"), 400);
                return;
            }

            const selected = new Date();
            selected.setHours(hour);
            selected.setMinutes(minute);
            const est = new Date(selected);
            if (delivery.checked) est.setMinutes(est.getMinutes() + 45);
            else est.setMinutes(est.getMinutes() + 15);

            const h = est.getHours() % 12 || 12;
            const m = est.getMinutes().toString().padStart(2, "0");
            const ampm = est.getHours() >= 12 ? "PM" : "AM";
            const formattedEst = `${h}:${m} ${ampm}`;

            placeOrderBtn.disabled = false;
            placeOrderBtn.style.opacity = "1";

            if (delivery.checked) {
                deliveryStatus.style.color = "#27ae60";
                deliveryStatus.style.display = "block";
                deliveryStatus.innerHTML = `Delivery time accepted! Estimated arrival in 45minutes!`;
            } else {
                pickupStatus.style.color = "#27ae60";
                pickupStatus.style.display = "block";
                pickupStatus.innerHTML = `Pickup time accepted! Estimated pickup ready in 15minutes!`;
            }
        }

        function toggleDeliveryFields() {
            const delivery = document.getElementById("delivery");
            const deliveryFields = document.getElementById("deliveryFields");
            const deliveryStatus = document.getElementById("deliveryStatus");
            const pickupStatus = document.getElementById("pickupStatus");

            deliveryFields.style.display = delivery.checked ? "block" : "none";
            deliveryStatus.innerHTML = "";
            pickupStatus.style.display = "none";

            recalcTotal();
            validateZip()
            localStorage.setItem("deliveryMethod", delivery.checked ? "delivery" : "pickup");
            validateTime();
        }

        function lockSubmit(form) {
            const btn = document.getElementById("placeOrderBtn");
            if (!btn) return true;

            // If already disabled, block extra submits
            if (btn.disabled && btn.innerText.indexOf("Processing") !== -1) {
                return false;
            }

            btn.disabled = true;
            btn.style.opacity = "0.6";
            btn.innerText = "Processing…";
            return true;
        }

        window.onload = function() {
            const savedMethod = localStorage.getItem("deliveryMethod");
            if (savedMethod === "delivery") document.getElementById("delivery").checked = true;
            else document.getElementById("pickup").checked = true;
            toggleDeliveryFields();
        };
    </script>
</head>
<body>
<header>💳 Confirm Your Order</header>
<%
    String error = request.getParameter("error");
    if (error != null) {
%>
<div style="background:#ffe6e6; border:1px solid #cc0000; color:#cc0000;
                padding:12px; margin:15px auto; width:80%; border-radius:6px;
                font-weight:bold; text-align:center;">

    <% if ("invalid".equals(error)) { %>
    ⚠️ Invalid payment information. Please enter a valid 16-digit card number.
    <% } else if ("declined".equals(error)) { %>
    ❌ Your payment was declined. Please try another card.
    <% } else if ("duplicate".equals(error)) { %>
    🔄 This payment was already submitted. Please do not resubmit.
    <% } else { %>
    ⚠️ An unknown error occurred. Please try again.
    <% } %>

</div>
<%
    }
%>

<%
    List<MenuItem> cart = (List<MenuItem>) session.getAttribute("cart");
    if (cart == null || cart.isEmpty()) {
%>
<p style="text-align:center; color:#999; margin-top:40px;">Your cart is empty.</p>
<%
} else {

    //  Group like cart.jsp, so we get quantity and customization displayed cleanly
    Map<String, Map<String, Object>> grouped = new LinkedHashMap<String, Map<String, Object>>();
    double subtotal = 0;

    for (MenuItem item : cart) {
        String key =
                item.getName() + "_" +
                        item.getSize() + "_" +
                        item.getToppings() + "_" +
                        String.format("%.2f", item.getPrice());

        if (!grouped.containsKey(key)) {
            Map<String, Object> info = new HashMap<String, Object>();
            info.put("item", item);
            info.put("qty", 1);
            grouped.put(key, info);
        } else {
            int qty = (Integer) grouped.get(key).get("qty");
            grouped.get(key).put("qty", qty + 1);
        }

        subtotal += item.getPrice();
    }

    double taxRate = 0.06;
    double tax = Math.round(subtotal * taxRate * 100.0) / 100.0;
    double deliveryFee = 3.00;
    double discount = (subtotal >= 50) ? Math.round(subtotal * 0.10 * 100.0) / 100.0 : 0.00;
    double grand = Math.round((subtotal + tax - discount) * 100.0) / 100.0;
%>

<!-- Order Summary -->
<div class="section">
    <h2>Order Summary</h2>
    <table id="orderTable">
        <tr><th>Item</th><th>Qty</th><th>Each</th><th>Subtotal</th></tr>

        <%
            for (Map.Entry<String, Map<String, Object>> entry : grouped.entrySet()) {
                MenuItem item = (MenuItem) entry.getValue().get("item");
                int qty = (Integer) entry.getValue().get("qty");
                double sub = item.getPrice() * qty;

                List<String> tps = item.getToppings();
                String toppingsStr = "";
                if (tps != null && !tps.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i2 = 0; i2 < tps.size(); i2++) {
                        if (i2 > 0) sb.append(", ");
                        sb.append(tps.get(i2));
                    }
                    toppingsStr = sb.toString();
                }
        %>
        <tr>
            <td>
                <strong><%= item.getName() %></strong>
                <% if (item.getSize() != null) { %>
                <small class="custom-info">Size: <%= item.getSize() %></small>
                <% } %>
                <% if (toppingsStr != null && !"".equals(toppingsStr)) { %>
                <small class="custom-info">Toppings: <%= toppingsStr %></small>
                <% } %>
                <small class="custom-info">Customized Price: $<%= String.format("%.2f", item.getPrice()) %></small>
            </td>
            <td><%= qty %></td>
            <td>$<%= String.format("%.2f", item.getPrice()) %></td>
            <td>$<%= String.format("%.2f", sub) %></td>
        </tr>
        <% } %>

        <tr><th>Subtotal</th><td colspan="3">$<%= String.format("%.2f", subtotal) %></td></tr>
        <input type="hidden" id="subtotalValue" value="<%= subtotal %>">

        <tr><th id="taxLabel">Tax (6%)</th><td colspan="3" id="taxCell">$<%= String.format("%.2f", tax) %></td></tr>
        <tr id="deliveryFeeRow" style="display:none;"><th>Delivery Fee</th><td colspan="3">$<%= String.format("%.2f", deliveryFee) %></td></tr>
        <tr id="discountRow" style="display:none;"><th>Auto Discount (10% off $50+)</th><td colspan="3">-$<%= String.format("%.2f", discount) %></td></tr>
        <tr id="promoRow" style="display:none;"><th>Promo Discount</th><td colspan="3" id="promoDiscountCell">-$0.00</td></tr>
        <tr><th>Total</th><td colspan="3" id="totalCell"><strong>$<%= String.format("%.2f", grand) %></strong></td></tr>
    </table>
</div>

<form action="checkout" method="post" onsubmit="return lockSubmit(this);">

<!-- Delivery or Pickup -->
<div class="section">
    <h2>Delivery or Pickup</h2>
    <label><input type="radio" name="method" id="pickup" value="pickup" onclick="toggleDeliveryFields()"> Pickup (6% tax, no fee)</label>
    <label><input type="radio" name="method" id="delivery" value="delivery" onclick="toggleDeliveryFields()"> Delivery (8% tax, $3 fee)</label>

    <div id="deliveryFields" class="delivery-fields">
        <label for="address">Delivery Address:</label>
        <input type="text" name="address" id="address" placeholder="123 Main St, Clovis, NM" />
        <label for="zip">ZIP Code:</label>
        <input type="text" name="zip" id="zip" placeholder="88101" onkeyup="validateZip()" onchange="validateZip()" />
    </div>

    <label for="orderTime">Preferred Pickup/Delivery Time:</label>
    <input type="time" id="orderTime" name="orderTime" onchange="validateTime()" style="margin-bottom:10px;" />

    <div id="deliveryStatus" style="font-weight:bold; margin-top:8px;"></div>
    <div id="pickupStatus" style="font-weight:bold; margin-top:8px; display:none;"></div>
</div>

<!-- Payment + Promo -->
<div class="section">
    <h2>Payment & Promotions</h2>
    <label for="promoCode">Promo Code:</label>
    <input type="text" id="promoCode" placeholder="Enter code (e.g., D50)" onkeyup="recalcTotal()">
    <div id="promoStatus" class="discount-applied"></div>
    <label for="cardName">Name on Card:</label>
    <input type="text" name="cardName" id="cardName" placeholder="John Doe" required />
    <label for="cardNumber">Card Number:</label>
    <input type="text" name="cardNumber" id="cardNumber" maxlength="16" placeholder="1111 2222 3333 4444" required />
    <label for="exp">Expiry Date:</label>
    <input type="text" name="exp" id="exp" maxlength="5" placeholder="MM/YY" required />
    <label for="cvv">CVV:</label>
    <input type="number" name="cvv" id="cvv" maxlength="3" placeholder="123" required />
</div>

    <!-- Confirm Buttons -->
    <div class="actions">

        <%
            // keep the token logic
            String paymentToken = session.getId() + "-" + System.currentTimeMillis();
            session.setAttribute("paymentToken", paymentToken);
        %>

        <input type="hidden" name="token" value="<%= paymentToken %>">

        <button type="submit" id="placeOrderBtn" class="confirm-btn" disabled>✅ Place Order</button>
        <a href="cart.jsp"><button type="button" class="cancel-btn">❌ Cancel</button></a>

    </div>

</form>

</div>

<% } %>
</body>
</html>

