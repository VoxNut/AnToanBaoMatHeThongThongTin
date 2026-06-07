<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.util.List" %>
<%@ page import="com.beveragestore.model.Order" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Orders - The Grindery Admin</title>
    
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
        <h1>Manage Orders</h1>
    </div>
</div>

<div class="container admin-container">
    <div class="admin-sidebar">
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="active">Manage Orders</a>
            <a href="${pageContext.request.contextPath}/admin/products">Manage Products</a>
            <a href="${pageContext.request.contextPath}/admin/users">Manage Users</a>
        </nav>
    </div>

    <div class="admin-content">
        <% String success = request.getParameter("success");
           String error = request.getParameter("error");
           if (success != null) { %>
            <div id="server-success-msg" style="display:none;"><%= success %></div>
        <% } if (error != null) { %>
            <div id="server-error-msg" style="display:none;"><%= error %></div>
        <% } %>

        <h2 style="font-family: var(--font-body); margin-bottom: var(--spacing-lg);">All Orders</h2>

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Date</th>
                        <th>Total</th>
                        <th>Chữ ký</th>
                        <th>Status</th>
                        <th>Update Status</th>
                    </tr>
                </thead>
                <tbody>
                    <% List<Order> orders = (List<Order>) request.getAttribute("orders");
                       if (orders != null && !orders.isEmpty()) {
                           for (Order o : orders) {
                               String sigStatus = o.getSignatureStatus() != null ? o.getSignatureStatus() : "UNSIGNED";
                               String badgeBg = "var(--bg-secondary)";
                               String badgeColor = "var(--text-primary)";
                               String badgeText = "Chưa ký";
                               String badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><circle cx='12' cy='12' r='10'></circle><line x1='12' y1='8' x2='12' y2='12'></line><line x1='12' y1='16' x2='12.01' y2='16'></line></svg>";
                               
                               if ("VALID".equals(sigStatus)) {
                                   badgeBg = "var(--success-bg)";
                                   badgeColor = "var(--success-text)";
                                   badgeText = "Hợp lệ";
                                   badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><polyline points='20 6 9 17 4 12'></polyline></svg>";
                               } else if ("INVALID".equals(sigStatus)) {
                                   badgeBg = "var(--error-bg)";
                                   badgeColor = "var(--error-text)";
                                   badgeText = "ĐÃ BỊ SỬA ĐỔI";
                                   badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><line x1='18' y1='6' x2='6' y2='18'></line><line x1='6' y1='6' x2='18' y2='18'></line></svg>";
                               } else if ("REVOKED_KEY".equals(sigStatus)) {
                                   badgeBg = "#fef3c7";
                                   badgeColor = "#b45309";
                                   badgeText = "Khóa đã báo mất";
                                   badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><path d='M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z'></path><line x1='12' y1='9' x2='12' y2='13'></line><line x1='12' y1='17' x2='12.01' y2='17'></line></svg>";
                               } else if ("NO_KEY_FOUND".equals(sigStatus)) {
                                   badgeBg = "var(--error-bg)";
                                   badgeColor = "var(--error-text)";
                                   badgeText = "Mất khóa liên kết";
                                   badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><circle cx='12' cy='12' r='10'></circle><line x1='4.93' y1='4.93' x2='19.07' y2='19.07'></line></svg>";
                               }
                    %>
                    <tr>
                        <td>
                            <span style="font-family: monospace; color: var(--text-secondary);" title="<%= o.getOrderId() %>">
                                <%= o.getOrderId().substring(0, 8) %>...
                            </span>
                        </td>
                        <td><%= new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(o.getCreatedAt()) %></td>
                        <td style="font-weight: 500;"><%= String.format("%,.0f VNĐ", o.getTotalAmount()) %></td>
                        <td>
                            <span style="display: inline-flex; align-items: center; padding: 4px 8px; border-radius: var(--border-radius); font-size: 11px; font-weight: 600; color: <%= badgeColor %>; background-color: <%= badgeBg %>; border: 1px solid <%= badgeColor %>22;">
                                <%= badgeIcon %>
                                <%= badgeText %>
                            </span>
                        </td>
                        <td>
                            <span class="status-badge status-<%= o.getStatus() %>"><%= o.getStatus() %></span>
                        </td>
                        <td>
                            <form method="POST" action="${pageContext.request.contextPath}/admin/orders" class="action-form">
                                <input type="hidden" name="action" value="update_status">
                                <input type="hidden" name="orderId" value="<%= o.getOrderId() %>">
                                <select name="status" class="form-control">
                                    <option value="PENDING" <%= "PENDING".equals(o.getStatus()) ? "selected" : "" %>>Pending</option>
                                    <option value="PROCESSING" <%= "PROCESSING".equals(o.getStatus()) ? "selected" : "" %>>Processing</option>
                                    <option value="SHIPPED" <%= "SHIPPED".equals(o.getStatus()) ? "selected" : "" %>>Shipped</option>
                                    <option value="DELIVERED" <%= "DELIVERED".equals(o.getStatus()) ? "selected" : "" %>>Delivered</option>
                                    <option value="CANCELLED" <%= "CANCELLED".equals(o.getStatus()) ? "selected" : "" %>>Cancelled</option>
                                </select>
                                <button type="submit" class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;">Update</button>
                            </form>
                        </td>
                    </tr>
                    <%      }
                       } else { %>
                    <tr>
                        <td colspan="6" style="text-align: center; padding: 40px;">No orders found.</td>
                    </tr>

                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />
</body>
</html>
