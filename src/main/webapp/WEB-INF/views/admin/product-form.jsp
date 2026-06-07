<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.beveragestore.model.Product" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>
        <c:choose>
            <c:when test="${mode == 'edit'}"><fmt:message key="admin.portal.edit_product_title" /></c:when>
            <c:otherwise><fmt:message key="admin.portal.add_product_title" /></c:otherwise>
        </c:choose>
    </title>
    
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
        <h1>
            <c:choose>
                <c:when test="${mode == 'edit'}"><fmt:message key="admin.product_form.edit_title" /></c:when>
                <c:otherwise><fmt:message key="admin.product_form.add_title" /></c:otherwise>
            </c:choose>
        </h1>
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
        <div class="form-container">
            <% Product p = (Product) request.getAttribute("product"); %>
            
            <form method="POST" action="${pageContext.request.contextPath}/admin/products">
                <input type="hidden" name="action" value="${mode == 'edit' ? 'update' : 'create'}">
                <% if (p != null) { %><input type="hidden" name="productId" value="<%= p.getProductId() %>"><% } %>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label"><fmt:message key="admin.product_form.field.name" /></label>
                        <input type="text" name="name" class="form-control" required value="<%= p != null ? p.getName() : "" %>">
                    </div>
                    <div class="form-group">
                        <label class="form-label"><fmt:message key="admin.product_form.field.category" /></label>
                        <select name="category" class="form-control" required>
                            <option value="Coffee" <%= p != null && "Coffee".equals(p.getCategory()) ? "selected" : "" %>>Coffee</option>
                            <option value="Tea" <%= p != null && "Tea".equals(p.getCategory()) ? "selected" : "" %>>Tea</option>
                            <option value="Goods" <%= p != null && "Goods".equals(p.getCategory()) ? "selected" : "" %>>Goods</option>
                            <option value="Water" <%= p != null && "Water".equals(p.getCategory()) ? "selected" : "" %>>Water</option>
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label"><fmt:message key="admin.product_form.field.brand" /></label>
                        <input type="text" name="brand" class="form-control" value="<%= p != null ? p.getBrand() : "" %>">
                    </div>
                    <div class="form-group">
                        <label class="form-label"><fmt:message key="admin.product_form.field.price" /></label>
                        <input type="number" name="price" step="1000" min="0" class="form-control" required value="<%= p != null ? String.format("%.0f", p.getPrice()) : "" %>">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label"><fmt:message key="admin.product_form.field.stock" /></label>
                        <input type="number" name="stock" min="0" class="form-control" required value="<%= p != null ? p.getStock() : "0" %>">
                    </div>
                    <div class="form-group">
                        <label class="form-label"><fmt:message key="admin.product_form.field.image" /></label>
                        <input type="url" name="imageUrl" class="form-control" placeholder="https://res.cloudinary.com/..." value="<%= p != null && p.getImageUrl() != null ? p.getImageUrl() : "" %>">
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label"><fmt:message key="admin.product_form.field.description" /></label>
                    <textarea name="description" class="form-control"><%= p != null ? p.getDescription() : "" %></textarea>
                </div>

                <% if (p != null) { %>
                <div class="form-group checkbox-group">
                    <input type="checkbox" id="isActive" name="isActive" <%= p.isActive() ? "checked" : "" %>>
                    <label for="isActive"><fmt:message key="admin.product_form.field.active" /></label>
                </div>
                <% } %>

                <div style="margin-top: var(--spacing-xl); display: flex; gap: var(--spacing-md);">
                    <button type="submit" class="btn btn-primary">
                        <c:choose>
                            <c:when test="${mode == 'edit'}"><fmt:message key="admin.product_form.btn_save" /></c:when>
                            <c:otherwise><fmt:message key="admin.product_form.btn_create" /></c:otherwise>
                        </c:choose>
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-secondary"><fmt:message key="admin.product_form.btn_cancel" /></a>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />
</body>
</html>
