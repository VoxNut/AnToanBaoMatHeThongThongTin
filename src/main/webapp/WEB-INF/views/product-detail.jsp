<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="com.beveragestore.model.Product" %>
<%@ page import="com.beveragestore.util.SessionUtil" %>
<%@ page import="com.beveragestore.model.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= product.getName() %> - The Grindery</title>
    
    <!-- nạp font chữ từ google -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap" rel="stylesheet">
    
    <!-- file css dùng chung cho cả web -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/store.css?v=1.0">
</head>
<body>

<%
    Product product = (Product) request.getAttribute("product");
%>

<jsp:include page="/WEB-INF/views/partials/header.jsp" />

<div class="container">
    <div class="breadcrumb">
        <a href="${pageContext.request.contextPath}/"><fmt:message key="product.detail.home" /></a> /
        <a href="${pageContext.request.contextPath}/products"><fmt:message key="product.detail.shop" /></a> /
        <a href="${pageContext.request.contextPath}/products?category=<%= product.getCategory() %>"><%= product.getCategory() %></a> /
        <span style="color: var(--text-primary);"><%= product.getName() %></span>
    </div>

    <div class="product-detail-section">
        <div class="detail-grid">
            <div class="detail-image-col">
                <% if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) { %>
                    <img src="<%= product.getImageUrl() %>" alt="<%= product.getName() %>">
                <% } else { %>
                    <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="var(--border-color)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8h1a4 4 0 0 1 0 8h-1"></path><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"></path><line x1="6" y1="1" x2="6" y2="4"></line><line x1="10" y1="1" x2="10" y2="4"></line><line x1="14" y1="1" x2="14" y2="4"></line></svg>
                <% } %>
            </div>

            <div class="detail-info-col">
                <div class="detail-category"><%= product.getCategory() %></div>
                <h1 class="detail-title"><%= product.getName() %></h1>
                <div class="detail-brand"><fmt:message key="product.detail.by" /> <%= product.getBrand() %></div>
                
                <div class="detail-price">$<%= String.format("%.2f", product.getPrice()) %></div>

                <div class="detail-description">
                    <%= product.getDescription() %>
                </div>

                <div class="detail-specs">
                    <div class="spec-row">
                        <span class="spec-label"><fmt:message key="product.detail.availability" /></span>
                        <span class="spec-value">
                            <% if (product.getStock() > 10) { %>
                                <span style="color: var(--success-text);"><fmt:message key="product.in_stock" /></span>
                            <% } else if (product.getStock() > 0) { %>
                                <span style="color: var(--error-text);"><fmt:message key="product.detail.only_left"><fmt:param value="<%= product.getStock() %>" /></fmt:message></span>
                            <% } else { %>
                                <span style="color: var(--text-light);"><fmt:message key="product.out_of_stock" /></span>
                            <% } %>
                        </span>
                    </div>
                </div>

                <% User currentUser = SessionUtil.getUserFromSession(session); %>
                <% if (currentUser != null && currentUser.isCustomer()) { %>
                    <form method="POST" action="${pageContext.request.contextPath}/customer/cart" id="addToCartForm">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="productId" value="<%= product.getProductId() %>">
                        
                        <div class="quantity-wrap">
                            <label for="quantity"><fmt:message key="product.detail.quantity" /></label>
                            <input type="number" id="quantity" name="quantity" class="form-control quantity-input" value="1" min="1" max="<%= product.getStock() %>" <% if (product.getStock() == 0) { %>disabled<% } %>>
                        </div>

                        <div class="action-buttons">
                            <button type="button" class="btn btn-primary" onclick="addToCartAjax()" <% if (product.getStock() == 0) { %>disabled<% } %>><fmt:message key="product.add_to_cart" /></button>
                            <button type="button" class="btn btn-secondary" onclick="window.location='${pageContext.request.contextPath}/customer/cart'"><fmt:message key="product.detail.view_cart" /></button>
                        </div>
                    </form>
                <% } else if (currentUser == null) { %>
                    <div class="action-buttons">
                        <button type="button" class="btn btn-primary" onclick="window.location='${pageContext.request.contextPath}/login'"><fmt:message key="product.detail.sign_in_to_purchase" /></button>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

<script>
    function addToCartAjax() {
        const form = document.getElementById('addToCartForm');
        const formData = new URLSearchParams(new FormData(form));

        fetch('${pageContext.request.contextPath}/customer/cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData.toString()
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
</body>
</html>
