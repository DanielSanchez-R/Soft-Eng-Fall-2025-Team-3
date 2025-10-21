<%@ page import="model.Reservation" %>
<%
    Reservation r = (Reservation) request.getAttribute("reservation");
%>
<html>
<head><title>Edit Reservation</title></head>
<body>
<h2><%= (r != null) ? "Edit Reservation" : "Add New Reservation" %></h2>
<form method="post" action="reservation">
    <input type="hidden" name="id" value="<%= r != null ? r.getId() : "" %>"/>
    Customer Name: <input type="text" name="customerName" value="<%= r != null ? r.getCustomerName() : "" %>"/><br>
    Contact: <input type="text" name="contact" value="<%= r != null ? r.getContact() : "" %>"/><br>
    Table ID: <input type="number" name="tableId" value="<%= r != null ? r.getTableId() : "" %>"/><br>
    Date/Time: <input type="datetime-local" name="dateTime" value="<%= r != null ? r.getDateTime().toString().replace(' ', 'T') : "" %>"/><br>
    Party Size: <input type="number" name="partySize" value="<%= r != null ? r.getPartySize() : "" %>"/><br>
    Status: <input type="text" name="status" value="<%= r != null ? r.getStatus() : "confirmed" %>"/><br>
    <input type="submit" value="Save"/>
</form>
<p style="color:red;"><%= request.getAttribute("error") == null ? "" : request.getAttribute("error") %></p>
<a href="reservation">Back to Reservation List</a>
</body>
</html>
