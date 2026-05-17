<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="app.name" /> - The Grindery</title>
    
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

<main>
    <section class="hero-section">
        <div class="hero-content">
            <h1 class="hero-title"><fmt:message key="home.welcome" /></h1>
            <p class="hero-subtitle"><fmt:message key="home.subtitle" /></p>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary"><fmt:message key="home.shop_now" /></a>
        </div>
        <div class="hero-image"></div>
    </section>

    <section class="featured-section container">
        <div class="section-header">
            <h2 class="section-title"><fmt:message key="home.featured" /></h2>
            <a href="${pageContext.request.contextPath}/products" style="font-size: 14px; text-decoration: underline;"><fmt:message key="home.view_all" /></a>
        </div>
        
        <div class="product-grid">
            <%
                try {
                    com.beveragestore.dao.ProductDAO productDAO = new com.beveragestore.dao.ProductDAO();
                    java.util.List<com.beveragestore.model.Product> products = productDAO.getAllActiveProducts();
                    
                    // chỉ hiển thị tối đa 3 sản phẩm nổi bật thôi nha
                    int count = 0;
                    for (com.beveragestore.model.Product p : products) {
                        if (count >= 3) break;
            %>
            <div class="product-card-small">
                <div class="product-card-img-wrap" style="cursor: pointer;" onclick="window.location='${pageContext.request.contextPath}/product?id=<%= p.getProductId() %>'">
                    <% if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) { %>
                        <img src="<%= p.getImageUrl() %>" alt="<%= p.getName() %>" style="max-height:100%; max-width:100%; object-fit:contain;">
                    <% } else { %>
                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--border-color)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8h1a4 4 0 0 1 0 8h-1"></path><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"></path><line x1="6" y1="1" x2="6" y2="4"></line><line x1="10" y1="1" x2="10" y2="4"></line><line x1="14" y1="1" x2="14" y2="4"></line></svg>
                    <% } %>
                </div>
                <div class="product-card-info">
                    <h3 class="product-card-title"><%= p.getName() %></h3>
                    <p class="product-card-desc"><%= p.getDescription() != null && p.getDescription().length() > 50 ? p.getDescription().substring(0, 50) + "..." : p.getDescription() %></p>
                    <form method="POST" action="${pageContext.request.contextPath}/customer/cart" style="margin-top: auto;">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="productId" value="<%= p.getProductId() %>">
                        <input type="hidden" name="quantity" value="1">
                        <button type="submit" class="btn-add"><fmt:message key="home.add_to_cart_price" /> - $<%= String.format("%.2f", p.getPrice()) %></button>
                    </form>
                </div>
            </div>
            <%
                        count++;
                    }
                } catch (Exception e) {
            %>
                <p>Error loading featured products.</p>
            <%
                }
            %>
        </div>
    </section>
</main>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />
