<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.List" %>
<%@ page import="com.beveragestore.model.Order" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="order.list.title" /> - The Grindery</title>
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

<div class="page-header">
    <div class="container">
        <h1 style="display:flex; align-items:center; gap:10px;">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="16.5" y1="9.4" x2="7.5" y2="4.21"></line><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>
            <fmt:message key="order.list.title" />
        </h1>
    </div>
</div>

<div class="container">
    <%
        List<Order> orders = (List<Order>) request.getAttribute("orders");
        if (orders != null && !orders.isEmpty()) {
    %>
    <div class="orders-list">
        <% for (Order order : orders) { 
            String sigStatus = order.getSignatureStatus() != null ? order.getSignatureStatus() : "UNSIGNED";
            String badgeBg = "var(--bg-secondary)";
            String badgeColor = "var(--text-primary)";
            String badgeText = "Chưa ký";
            
            if ("VALID".equals(sigStatus)) {
                badgeBg = "var(--success-bg)";
                badgeColor = "var(--success-text)";
                badgeText = "Chữ ký Hợp lệ";
            } else if ("INVALID".equals(sigStatus)) {
                badgeBg = "var(--error-bg)";
                badgeColor = "var(--error-text)";
                badgeText = "Chữ ký Bị Sửa Đổi";
            } else if ("REVOKED_KEY".equals(sigStatus)) {
                badgeBg = "#fef3c7";
                badgeColor = "#b45309";
                badgeText = "Khóa Đã Báo Mất";
            } else if ("NO_KEY_FOUND".equals(sigStatus)) {
                badgeBg = "var(--error-bg)";
                badgeColor = "var(--error-text)";
                badgeText = "Thiếu Khóa";
            }
        %>
        <div class="order-card" onclick="window.location='${pageContext.request.contextPath}/customer/order-detail?id=<%= order.getOrderId() %>'" style="cursor: pointer; position: relative;">
            <div class="order-info">
                <h3>
                    <fmt:message key="order.id" /> #<%= order.getOrderId().substring(0, 8) %>...
                    
                    <!-- Signature Badge -->
                    <span style="display: inline-flex; align-items: center; padding: 4px 8px; border-radius: 20px; font-size: 11px; font-weight: 600; color: <%= badgeColor %>; background-color: <%= badgeBg %>; border: 1px solid <%= badgeColor %>22; margin-left: 10px; vertical-align: middle;">
                        <%= badgeText %>
                    </span>
                </h3>
                <div class="order-meta">
                    <span><fmt:message key="order.date_placed" />: <%= new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(order.getCreatedAt()) %></span>
                    <span><fmt:message key="order.list.item_count"><fmt:param value="<%= order.getTotalItems() %>" /></fmt:message></span>
                </div>
                <div class="order-amount"><%= String.format("%,.0f VNĐ", order.getTotalAmount()) %></div>
                <span class="order-status status-<%= order.getStatus().toLowerCase() %>"><%= order.getStatus() %></span>
                
                <% if (order.isResignRequired()) { %>
                <div style="margin-top: 15px; padding: 10px 14px; background-color: #fee2e2; border: 1px solid #fecaca; border-radius: 6px; color: #991b1b; font-size: 12.5px; font-weight: 500; display: flex; align-items: center; gap: 8px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                    <strong>Yêu cầu ký lại:</strong> Phát hiện dữ liệu đơn hàng bị thay đổi bất thường. Vui lòng click để ký lại!
                </div>
                <% } %>
            </div>
            <button class="btn btn-secondary btn-small" onclick="event.stopPropagation(); window.location='${pageContext.request.contextPath}/customer/order-detail?id=<%= order.getOrderId() %>'">
                <fmt:message key="order.list.view_details" />
            </button>
        </div>
        <% } %>
    </div>

    <%
        } else {
    %>
    <div class="empty-state">
        <h2><fmt:message key="order.list.empty_title" /></h2>
        <p style="color: var(--text-secondary); margin-bottom: var(--spacing-lg);"><fmt:message key="order.list.empty_desc" /></p>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary"><fmt:message key="order.list.browse" /></a>
    </div>
    <% } %>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>
