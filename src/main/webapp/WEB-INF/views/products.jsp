<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="product.page_title" /> - The Grindery</title>
    
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
        <h1><fmt:message key="product.page_title" /></h1>
        <p style="color: var(--text-secondary);"><fmt:message key="product.page_subtitle" /></p>
    </div>
</div>

<div class="container">
    <div class="filters">
        <form method="GET" action="${pageContext.request.contextPath}/products" style="display: contents;">
            <div class="filter-group">
                <div class="filter-item">
                    <label for="category" class="form-label"><fmt:message key="product.category" /></label>
                    <select id="category" name="category" class="form-control" onchange="this.form.submit();">
                        <option value=""><fmt:message key="product.all_categories" /></option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat}" <c:if test="${cat == selectedCategory}">selected</c:if>>${cat}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="filter-item" style="flex: 2;">
                    <label for="search" class="form-label"><fmt:message key="product.search" /></label>
                    <input type="text" id="search" name="search" class="form-control" placeholder="<fmt:message key="product.search_placeholder" />" value="${searchTerm}">
                </div>
                <div class="filter-item" style="flex: 0 0 auto;">
                    <button type="submit" class="btn btn-primary" style="padding: 14px 28px;"><fmt:message key="product.search_btn" /></button>
                </div>
            </div>
        </form>
    </div>

    <c:choose>
        <c:when test="${not empty products}">
            <div class="products-grid">
                <c:forEach var="product" items="${products}">
                    <div class="product-card">
                        <div class="product-image">
                            <c:choose>
                                <c:when test="${not empty product.imageUrl}">
                                    <img src="${product.imageUrl}" alt="${product.name}">
                                </c:when>
                                <c:otherwise>
                                    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--border-color)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8h1a4 4 0 0 1 0 8h-1"></path><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"></path><line x1="6" y1="1" x2="6" y2="4"></line><line x1="10" y1="1" x2="10" y2="4"></line><line x1="14" y1="1" x2="14" y2="4"></line></svg>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="product-info">
                            <div class="product-category">${product.category}</div>
                            <h3 class="product-name">${product.name}</h3>
                            <div class="product-brand">${product.brand}</div>
                            <div class="product-description">${product.description}</div>
                            <div class="product-footer">
                                <div class="product-price">$${product.price}</div>
                                <div class="product-stock">
                                    <c:choose>
                                        <c:when test="${product.stock > 10}">
                                            <span class="stock-available"><fmt:message key="product.in_stock" /></span>
                                        </c:when>
                                        <c:when test="${product.stock > 0}">
                                            <span class="stock-low"><fmt:message key="product.low_stock" /></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="stock-low"><fmt:message key="product.out_of_stock" /></span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="product-actions">
                                <button class="btn btn-secondary btn-small" onclick="window.location='${pageContext.request.contextPath}/product?id=${product.productId}'"><fmt:message key="product.view" /></button>
                                
                                <%@ page import="com.beveragestore.model.User" %>
                                <%@ page import="com.beveragestore.util.SessionUtil" %>
                                <% User pUser = SessionUtil.getUserFromSession(request.getSession(false)); %>
                                
                                <% if (pUser != null && pUser.isCustomer()) { %>
                                    <button class="btn btn-primary btn-small" onclick="addToCart('${product.productId}')" <c:if test="${product.stock == 0}">disabled</c:if>><fmt:message key="product.add_to_cart" /></button>
                                <% } else if (pUser == null) { %>
                                    <button class="btn btn-primary btn-small" onclick="window.location='${pageContext.request.contextPath}/login'"><fmt:message key="product.log_in" /></button>
                                <% } %>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="no-products">
                <h2><fmt:message key="product.no_products" /></h2>
                <p style="color: var(--text-secondary); margin-bottom: var(--spacing-lg);"><fmt:message key="product.no_products_hint" /></p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary"><fmt:message key="product.clear_filters" /></a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

<script>
    function addToCart(productId) {
        fetch('${pageContext.request.contextPath}/customer/cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'action=add&productId=' + productId + '&quantity=1'
        }).then(response => {
            if (response.ok) {
                showAlert('<fmt:message key="cart.added" />', 'success');
            } else {
                showAlert('<fmt:message key="cart.add_failed" />', 'error');
            }
        }).catch(err => {
            showAlert('<fmt:message key="cart.error" />', 'error');
        });
    }
</script>
