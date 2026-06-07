<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.List" %>
<%@ page import="com.beveragestore.model.Product" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="admin.portal.manage_products_title" /></title>
    
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
        <h1><fmt:message key="admin.nav.manage_products" /></h1>
    </div>
</div>

<div class="container admin-container">
    <div class="admin-sidebar">
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin/dashboard"><fmt:message key="admin.nav.dashboard" /></a>
            <a href="${pageContext.request.contextPath}/admin/orders"><fmt:message key="admin.nav.manage_orders" /></a>
            <a href="${pageContext.request.contextPath}/admin/products" class="active"><fmt:message key="admin.nav.manage_products" /></a>
            <a href="${pageContext.request.contextPath}/admin/users"><fmt:message key="admin.nav.manage_users" /></a>
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

        <div class="header-actions">
            <h2 style="font-family: var(--font-body);"><fmt:message key="admin.products.all" /></h2>
            <a href="${pageContext.request.contextPath}/admin/products?action=create" class="btn btn-primary"><fmt:message key="admin.products.btn_add" /></a>
        </div>

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th><fmt:message key="admin.products.table.image" /></th>
                        <th><fmt:message key="admin.products.table.name" /></th>
                        <th><fmt:message key="admin.products.table.category" /></th>
                        <th><fmt:message key="admin.products.table.price" /></th>
                        <th><fmt:message key="admin.products.table.stock" /></th>
                        <th><fmt:message key="admin.products.table.status" /></th>
                        <th><fmt:message key="admin.products.table.actions" /></th>
                    </tr>
                </thead>
                <tbody>
                    <% List<Product> products = (List<Product>) request.getAttribute("products");
                       if (products != null && !products.isEmpty()) {
                           for (Product p : products) {
                     %>
                     <tr>
                         <td>
                             <% if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) { %>
                                 <img src="<%= p.getImageUrl() %>" class="product-image" alt="Product">
                             <% } else { %>
                                 <div class="product-image" style="background:var(--bg-secondary); color: var(--text-light); display:flex; align-items:center; justify-content:center;">
                                     <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8h1a4 4 0 0 1 0 8h-1"></path><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"></path><line x1="6" y1="1" x2="6" y2="4"></line><line x1="10" y1="1" x2="10" y2="4"></line><line x1="14" y1="1" x2="14" y2="4"></line></svg>
                                 </div>
                             <% } %>
                         </td>
                         <td style="font-weight: 500;"><%= p.getName() %></td>
                         <td><%= p.getCategory() %></td>
                         <td><%= String.format("%,.0f VNĐ", p.getPrice()) %></td>
                         <td>
                             <span class="<%= p.getStock() < 10 ? "stock-low" : "" %>"><%= p.getStock() %></span>
                         </td>
                         <td>
                             <% if (p.isActive()) { %>
                                 <span class="status-badge status-active"><fmt:message key="admin.products.status.active" /></span>
                             <% } else { %>
                                 <span class="status-badge status-inactive"><fmt:message key="admin.products.status.inactive" /></span>
                             <% } %>
                         </td>
                         <td class="action-links">
                             <a href="${pageContext.request.contextPath}/admin/products?action=edit&id=<%= p.getProductId() %>" style="color:var(--accent-primary);"><fmt:message key="admin.products.action.edit" /></a>
                             <form method="POST" action="${pageContext.request.contextPath}/admin/products" style="display:inline;">
                                 <input type="hidden" name="action" value="toggle_status">
                                 <input type="hidden" name="productId" value="<%= p.getProductId() %>">
                                 <button type="submit" style="background:none; border:none; color:var(--text-secondary); cursor:pointer; text-decoration:underline;">
                                     <% if (p.isActive()) { %>
                                         <fmt:message key="admin.products.action.deactivate" />
                                     <% } else { %>
                                         <fmt:message key="admin.products.action.activate" />
                                     <% } %>
                                 </button>
                             </form>
                         </td>
                     </tr>
                     <%      }
                        } else { %>
                     <tr>
                         <td colspan="7" style="text-align: center; padding: 40px;"><fmt:message key="admin.products.table.no_products" /></td>
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
