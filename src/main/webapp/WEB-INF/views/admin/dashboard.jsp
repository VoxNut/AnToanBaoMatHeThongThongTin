<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - The Grindery</title>
    
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
        <h1>Admin Portal</h1>
    </div>
</div>

<div class="container admin-container">
    <div class="admin-sidebar">
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="active">Dashboard</a>
            <a href="${pageContext.request.contextPath}/admin/orders">Manage Orders</a>
            <a href="${pageContext.request.contextPath}/admin/products">Manage Products</a>
            <a href="${pageContext.request.contextPath}/admin/users">Manage Users</a>
        </nav>
    </div>

    <div class="admin-content">
        <h2 style="font-family: var(--font-body); margin-bottom: var(--spacing-lg);">Overview</h2>
        
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-label">Total Orders</div>
                <div class="stat-value"><%= request.getAttribute("totalOrders") %></div>
            </div>
            <div class="stat-card revenue">
                <div class="stat-label">Total Revenue</div>
                <div class="stat-value">$<%= String.format("%.2f", (double) request.getAttribute("totalRevenue")) %></div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Registered Users</div>
                <div class="stat-value"><%= request.getAttribute("totalUsers") %></div>
            </div>
            <div class="stat-card <% if ((int)request.getAttribute("lowStockCount") > 0) { %>warning<% } %>">
                <div class="stat-label">Low Stock Items</div>
                <div class="stat-value"><%= request.getAttribute("lowStockCount") %></div>
            </div>
        </div>

        <div class="quick-actions">
            <h3>Quick Actions</h3>
            <div class="action-grid">
                <a href="${pageContext.request.contextPath}/admin/products?action=create" class="btn btn-primary" style="text-align:center;">Add New Product</a>
                <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-secondary" style="text-align:center;">View Pending Orders</a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />
</body>
</html>
