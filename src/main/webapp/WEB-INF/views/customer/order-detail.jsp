<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.beveragestore.model.Order" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Details - Online Beverage Store</title>
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

<div class="container" style="padding-top: var(--spacing-xl);">
    <a href="${pageContext.request.contextPath}/customer/orders" class="back-link">← Back to Orders</a>

    <%
        Order order = (Order) request.getAttribute("order");
        if (order != null) {
    %>
    <%
        String success = (String) request.getAttribute("success");
        String errorMsg = (String) request.getAttribute("error");
        if (success != null) {
    %>
        <div style="background-color: var(--success-bg); color: var(--success-text); padding: 12px 16px; border-radius: var(--border-radius); margin-top: var(--spacing-md); border: 1px solid var(--success-text); font-weight: 500;">
            <%= success %>
        </div>
    <% } %>
    <% if (errorMsg != null) { %>
        <div style="background-color: var(--error-bg); color: var(--error-text); padding: 12px 16px; border-radius: var(--border-radius); margin-top: var(--spacing-md); border: 1px solid var(--error-text); font-weight: 500;">
            <%= errorMsg %>
        </div>
    <% } %>

    <div class="detail-section" style="margin-top: var(--spacing-lg);">
        <h1 style="margin-bottom: var(--spacing-lg); font-family: var(--font-heading);">Order #<%= order.getOrderId() %></h1>
        <div class="info-grid">
            <div class="info-item">
                <span class="info-label">Status</span>
                <span class="order-status status-<%= order.getStatus().toLowerCase() %>"><%= order.getStatus() %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Order Date</span>
                <span class="info-value"><%= new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(order.getCreatedAt()) %></span>
            </div>
            <div class="info-item">
                <span class="info-label">Total Amount</span>
                <span class="info-value" style="color: var(--accent-primary); font-size: 18px;"><%= String.format("%,.0f VNĐ", order.getTotalAmount()) %></span>
            </div>
        </div>
    </div>

    <div class="order-detail-layout">
        <div class="main-column">
            <!-- tiến trình trạng thái đơn hàng -->
            <div class="detail-section">
                <h2>Order Timeline</h2>
                <div class="timeline">
                    <div class="timeline-item completed">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <h4>Order Placed</h4>
                            <p><%= new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(order.getCreatedAt()) %></p>
                        </div>
                    </div>
                    <div class="timeline-item <%= order.getStatus().equals("PROCESSING") || order.getStatus().equals("SHIPPED") || order.getStatus().equals("DELIVERED") ? "completed" : "" %>">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <h4>Processing</h4>
                            <p>We're preparing your order</p>
                        </div>
                    </div>
                    <div class="timeline-item <%= order.getStatus().equals("SHIPPED") || order.getStatus().equals("DELIVERED") ? "completed" : "" %>">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <h4>Shipped</h4>
                            <p>Your package is on the way</p>
                        </div>
                    </div>
                    <div class="timeline-item <%= order.getStatus().equals("DELIVERED") ? "completed" : "" %>">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <h4>Delivered</h4>
                            <p>Order delivered</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- các món hàng trong đơn -->
            <div class="detail-section">
                <h2>Items Ordered</h2>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Product</th>
                                <th>Unit Price</th>
                                <th>Quantity</th>
                                <th>Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Order.OrderItem item : order.getItems()) { %>
                            <tr>
                                <td><%= item.getProductName() %></td>
                                <td><%= String.format("%,.0f VNĐ", item.getUnitPrice()) %></td>
                                <td><%= item.getQuantity() %></td>
                                <td><%= String.format("%,.0f VNĐ", item.getSubtotal()) %></td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="side-column">
            <!-- tóm tắt đơn hàng -->
            <div class="checkout-summary">
                <h3>Order Summary</h3>
                <div class="summary-row">
                    <span>Subtotal:</span>
                    <span><%= String.format("%,.0f VNĐ", order.getTotalAmount()) %></span>
                </div>
                <div class="summary-row">
                    <span>Shipping:</span>
                    <span>Free</span>
                </div>
                <div class="summary-row">
                    <span>Tax (8%):</span>
                    <span><%= String.format("%,.0f VNĐ", (order.getTotalAmount() * 0.08)) %></span>
                </div>
                <div class="summary-total">
                    <span>Total Paid:</span>
                    <span><%= String.format("%,.0f VNĐ", (order.getTotalAmount() * 1.08)) %></span>
                </div>

                <div style="margin-top: var(--spacing-xl); padding-top: var(--spacing-md); border-top: 1px solid var(--border-color);">
                    <h3 style="margin-bottom: var(--spacing-sm); font-size: 14px;">Shipping Address</h3>
                    <p style="white-space: pre-wrap; color: var(--text-secondary); font-size: 13px;"><%= order.getShippingAddress() %></p>
                </div>

                <% if (order.getNotes() != null && !order.getNotes().isEmpty()) { %>
                <div style="margin-top: var(--spacing-md);">
                    <h3 style="margin-bottom: var(--spacing-sm); font-size: 14px;">Delivery Notes</h3>
                    <p style="color: var(--text-secondary); font-size: 13px;"><%= order.getNotes() %></p>
                </div>
                <% } %>

                <% if (Order.STATUS_PENDING.equals(order.getStatus())) { %>
                <div style="margin-top: var(--spacing-xl); padding-top: var(--spacing-md); border-top: 1px solid var(--border-color);">
                    <form method="POST" action="${pageContext.request.contextPath}/customer/order-detail" onsubmit="return confirmCancel();">
                        <input type="hidden" name="action" value="cancel" />
                        <input type="hidden" name="id" value="<%= order.getOrderId() %>" />
                        <button type="submit" class="btn btn-danger" style="width: 100%;">
                            Hủy đơn hàng
                        </button>
                    </form>
                </div>
                <script>
                    function confirmCancel() {
                        return confirm("Bạn có chắc chắn muốn hủy đơn hàng này? Số lượng sản phẩm sẽ được tự động hoàn lại vào kho.");
                    }
                </script>
                <% } %>
            </div>
        </div>
    </div>

    <% } else { %>
    <div class="empty-state">
        <h2>Order Not Found</h2>
        <a href="${pageContext.request.contextPath}/customer/orders" class="btn btn-primary" style="margin-top: var(--spacing-md);">Back to Orders</a>
    </div>
    <% } %>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>
