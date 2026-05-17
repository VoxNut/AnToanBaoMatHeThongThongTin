<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="com.beveragestore.model.Order" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="order.confirmed" /> - The Grindery</title>
    <!-- nạp font chữ từ google -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap" rel="stylesheet">
    
    <!-- file css dùng chung cho cả web -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/store.css?v=1.0">
</head>
<body>

<jsp:include page="/WEB-INF/views/partials/header.jsp" />

<div class="container">
    <%
        Order order = (Order) request.getAttribute("order");
        if (order != null) {
    %>
    <div class="confirmation-container">
        <div class="success-icon" style="margin-bottom: 20px;">
            <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="#27ae60" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
        </div>
        <h1><fmt:message key="order.confirmed" /></h1>
        <p style="color: var(--text-secondary); margin-bottom: var(--spacing-xl);"><fmt:message key="order.thank_you" /></p>

        <div class="order-id-box">
            <fmt:message key="order.id" />: <%= order.getOrderId() %>
        </div>

        <div class="confirmation-details">
            <div class="summary-row">
                <span style="font-weight: 500; color: var(--text-primary);"><fmt:message key="order.date_placed" />:</span>
                <span><%= new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(order.getCreatedAt()) %></span>
            </div>
            <div class="summary-row">
                <span style="font-weight: 500; color: var(--text-primary);"><fmt:message key="order.status" />:</span>
                <span style="color: #f57c00; font-weight: 600;"><%= order.getStatus() %></span>
            </div>
            <div class="summary-row">
                <span style="font-weight: 500; color: var(--text-primary);"><fmt:message key="order.shipping_address" />:</span>
                <span style="text-align: right;"><%= order.getShippingAddress() %></span>
            </div>
            <div class="summary-row" style="margin-top: var(--spacing-md);">
                <span style="font-weight: 500; color: var(--text-primary);"><fmt:message key="order.total_amount" />:</span>
                <span style="color: var(--accent-primary); font-weight: 600; font-size: 18px;">$<%= String.format("%.2f", order.getTotalAmount()) %></span>
            </div>
        </div>

        <div class="confirmation-items">
            <h3><fmt:message key="order.items_ordered" /></h3>
            <% for (Order.OrderItem item : order.getItems()) { %>
            <div class="summary-row">
                <span><%= item.getProductName() %> (x<%= item.getQuantity() %>)</span>
                <span style="font-weight: 500;">$<%= String.format("%.2f", item.getSubtotal()) %></span>
            </div>
            <% } %>
        </div>

        <p style="color: var(--text-light); margin: var(--spacing-xxl) 0;"><fmt:message key="order.track_hint" /></p>

        <div class="action-buttons" style="justify-content: center; gap: var(--spacing-lg);">
            <a href="${pageContext.request.contextPath}/customer/orders" class="btn btn-primary" style="padding: 12px 30px; min-width: auto;"><fmt:message key="order.view_orders" /></a>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary" style="padding: 12px 30px; min-width: auto;"><fmt:message key="cart.continue_shopping" /></a>
        </div>
    </div>
    <% } else { %>
    <div class="confirmation-container">
        <h1 style="color: var(--error-text);"><fmt:message key="order.not_found" /></h1>
        <p style="color: var(--text-secondary); margin-bottom: var(--spacing-xl);"><fmt:message key="order.not_found_hint" /></p>
        <div class="action-buttons" style="justify-content: center;">
            <a href="${pageContext.request.contextPath}/customer/orders" class="btn btn-primary" style="padding: 12px 30px; min-width: auto;"><fmt:message key="order.view_orders" /></a>
        </div>
    </div>
    <% } %>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>
