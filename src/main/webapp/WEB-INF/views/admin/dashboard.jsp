<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="admin.portal.title" /> - The Grindery</title>
    
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css?v=1.0">
</head>
<body>

<jsp:include page="/WEB-INF/views/partials/header.jsp" />

<div class="page-header" style="text-align:center; padding: var(--spacing-xl) 0 var(--spacing-md);">
    <div class="container">
        <h1><fmt:message key="admin.portal.title" /></h1>
    </div>
</div>

<div class="container admin-container">
    <div class="admin-sidebar">
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="active"><fmt:message key="admin.nav.dashboard" /></a>
            <a href="${pageContext.request.contextPath}/admin/orders"><fmt:message key="admin.nav.manage_orders" /></a>
            <a href="${pageContext.request.contextPath}/admin/products"><fmt:message key="admin.nav.manage_products" /></a>
            <a href="${pageContext.request.contextPath}/admin/users"><fmt:message key="admin.nav.manage_users" /></a>
        </nav>
    </div>

    <div class="admin-content">
        <h2 style="font-family: var(--font-body); margin-bottom: var(--spacing-lg);"><fmt:message key="admin.dashboard.overview" /></h2>
        
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-label"><fmt:message key="admin.dashboard.total_orders" /></div>
                <div class="stat-value"><%= request.getAttribute("totalOrders") %></div>
            </div>
            <div class="stat-card revenue">
                <div class="stat-label"><fmt:message key="admin.dashboard.total_revenue" /></div>
                <div class="stat-value"><%= String.format("%,.0f VNĐ", (double) request.getAttribute("totalRevenue")) %></div>
            </div>
            <div class="stat-card">
                <div class="stat-label"><fmt:message key="admin.dashboard.registered_users" /></div>
                <div class="stat-value"><%= request.getAttribute("totalUsers") %></div>
            </div>
            <div class="stat-card <% if ((int)request.getAttribute("lowStockCount") > 0) { %>warning<% } %>">
                <div class="stat-label"><fmt:message key="admin.dashboard.low_stock_items" /></div>
                <div class="stat-value"><%= request.getAttribute("lowStockCount") %></div>
            </div>
        </div>

        <div class="quick-actions">
            <h3><fmt:message key="admin.dashboard.quick_actions" /></h3>
            <div class="action-grid">
                <a href="${pageContext.request.contextPath}/admin/products?action=create" class="btn btn-primary" style="text-align:center;"><fmt:message key="admin.dashboard.btn_add_product" /></a>
                <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-secondary" style="text-align:center;"><fmt:message key="admin.dashboard.btn_view_pending" /></a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />
</body>
</html>
