<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="java.util.List" %>
<%@ page import="com.beveragestore.model.CartItem" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="checkout.title" /> - The Grindery</title>
    
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
        <h1><fmt:message key="checkout.title" /></h1>
    </div>
</div>

<div class="container">
    <% String error = (String) request.getAttribute("error");
       if (error != null) { %>
    <div id="server-error-msg" style="display:none;"><%= error %></div>
    <% } %>

    <div class="checkout-layout">
        <!-- phần form nhập liệu -->
        <div class="checkout-form-container">
            <h2><fmt:message key="checkout.shipping_delivery" /></h2>

            <form method="POST" action="${pageContext.request.contextPath}/customer/checkout">
                <div class="form-group">
                    <label for="shippingAddress" class="form-label"><fmt:message key="checkout.shipping_address" /></label>
                    <textarea id="shippingAddress" name="shippingAddress" class="form-control" required placeholder="<fmt:message key="checkout.shipping_placeholder" />"></textarea>
                </div>

                <div class="form-group">
                    <label for="notes" class="form-label"><fmt:message key="checkout.delivery_notes" /></label>
                    <textarea id="notes" name="notes" class="form-control" placeholder="<fmt:message key="checkout.delivery_placeholder" />"></textarea>
                </div>

                <div class="form-group" style="margin-top: 20px; padding: 15px; background-color: #f8f9fa; border: 1px solid #dee2e6; border-radius: 6px;">
                    <label for="privateKey" class="form-label" style="font-weight: 600; color: #495057; display: block; margin-bottom: 5px;">🔑 Ký số đơn hàng (Private Key PEM):</label>
                    <p style="font-size: 12px; color: #6c757d; margin-bottom: 10px;">
                        Vui lòng dán nội dung file Private Key (`.pem`) đã tải về để ký đơn hàng. Khóa này chỉ dùng để ký trong phiên giao dịch hiện tại và sẽ không được lưu trên web.
                    </p>
                    <textarea id="privateKey" name="privateKey" class="form-control" required style="font-family: monospace; font-size: 12px; height: 120px;" placeholder="-----BEGIN PRIVATE KEY-----&#10;...&#10;-----END PRIVATE KEY-----"></textarea>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: var(--spacing-md);"><fmt:message key="checkout.place_order" /></button>
            </form>


            <a href="${pageContext.request.contextPath}/customer/cart" class="back-link"><fmt:message key="checkout.return_to_cart" /></a>
        </div>

        <!-- phần tóm tắt nha -->
        <div class="checkout-summary">
            <h3><fmt:message key="cart.order_summary" /></h3>

            <div class="summary-items">
                <%
                    List<CartItem> cartItems = (List<CartItem>) request.getAttribute("cartItems");
                    if (cartItems != null) {
                        for (CartItem item : cartItems) {
                %>
                <div class="summary-item">
                    <span class="item-name"><%= item.getName() %> (x<%= item.getQuantity() %>)</span>
                    <span class="item-price">$<%= String.format("%.2f", item.getSubtotal()) %></span>
                </div>
                <%
                        }
                    }
                %>
            </div>

            <div class="summary-row">
                <span><fmt:message key="cart.subtotal" /></span>
                <span>$<%= String.format("%.2f", request.getAttribute("cartTotal")) %></span>
            </div>

            <div class="summary-row">
                <span><fmt:message key="cart.shipping" /></span>
                <span><fmt:message key="checkout.free" /></span>
            </div>

            <div class="summary-row">
                <span><fmt:message key="cart.tax" /></span>
                <span>$<%= String.format("%.2f", ((double) request.getAttribute("cartTotal") * 0.08)) %></span>
            </div>

            <div class="summary-total">
                <span><fmt:message key="checkout.total_to_pay" /></span>
                <span>$<%= String.format("%.2f", ((double) request.getAttribute("cartTotal") * 1.08)) %></span>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />
</body>
</html>
