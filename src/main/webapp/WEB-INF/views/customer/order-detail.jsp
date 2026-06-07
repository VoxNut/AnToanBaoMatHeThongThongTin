<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.beveragestore.model.Order" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="order.id" /> - The Grindery</title>
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
    <a href="${pageContext.request.contextPath}/customer/orders" class="back-link"><fmt:message key="order.detail.back" /></a>

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
        <h1 style="margin-bottom: var(--spacing-lg); font-family: var(--font-heading);"><fmt:message key="order.detail.title"><fmt:param value="<%= order.getOrderId() %>" /></fmt:message></h1>
        <div class="info-grid">
            <div class="info-item">
                <span class="info-label"><fmt:message key="order.status" /></span>
                <span class="order-status status-<%= order.getStatus().toLowerCase() %>"><%= order.getStatus() %></span>
            </div>
            <div class="info-item">
                <span class="info-label"><fmt:message key="order.date_placed" /></span>
                <span class="info-value"><%= new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(order.getCreatedAt()) %></span>
            </div>
            <div class="info-item">
                <span class="info-label"><fmt:message key="order.total_amount" /></span>
                <span class="info-value" style="color: var(--accent-primary); font-size: 18px;"><%= String.format("%,.0f VNĐ", order.getTotalAmount()) %></span>
            </div>
            <div class="info-item">
                <span class="info-label"><fmt:message key="admin.orders.table.signature" /></span>
                <%
                    String sigStatus = order.getSignatureStatus() != null ? order.getSignatureStatus() : "UNSIGNED";
                    String badgeBg = "var(--bg-secondary)";
                    String badgeColor = "var(--text-primary)";
                    String sigKey = "admin.orders.sig.unsigned";
                    
                    if ("VALID".equals(sigStatus)) {
                        badgeBg = "var(--success-bg)";
                        badgeColor = "var(--success-text)";
                        sigKey = "admin.orders.sig.valid";
                    } else if ("INVALID".equals(sigStatus)) {
                        badgeBg = "var(--error-bg)";
                        badgeColor = "var(--error-text)";
                        sigKey = "admin.orders.sig.invalid";
                    } else if ("REVOKED_KEY".equals(sigStatus)) {
                        badgeBg = "#fef3c7";
                        badgeColor = "#b45309";
                        sigKey = "admin.orders.sig.revoked";
                    }
                %>
                <span style="display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 20px; font-size: 13px; font-weight: 600; color: <%= badgeColor %>; background-color: <%= badgeBg %>; border: 1px solid <%= badgeColor %>22;">
                    <fmt:message key="<%= sigKey %>" />
                </span>
            </div>
        </div>
    </div>

    <% if (order.isResignRequired()) { %>
        <div style="background-color: #fee2e2; color: #991b1b; padding: 16px 20px; border-radius: var(--border-radius); margin-top: var(--spacing-md); border: 1px solid #fecaca; font-weight: 500; margin-bottom: var(--spacing-lg);">
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px; font-size: 16px; font-weight: 600;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                <fmt:message key="checkout.resign_title" />
            </div>
            <p style="font-size: 14px; margin-bottom: 8px; font-weight: normal; line-height: 1.5;">
                <fmt:message key="checkout.resign_desc" />
            </p>
            <% if (order.getResignMessage() != null && !order.getResignMessage().trim().isEmpty()) { %>
                <p style="font-size: 13.5px; font-weight: 600; margin: 0; padding: 8px 12px; background: rgba(153, 27, 27, 0.05); border-left: 3px solid #b91c1c; border-radius: 4px;">
                    <strong>Lý do từ Admin:</strong> <%= order.getResignMessage() %>
                </p>
            <% } %>
        </div>
    <% } %>

    <div class="order-detail-layout">
        <div class="main-column">
            <% if (order.isResignRequired()) { 
                StringBuilder rawSb = new StringBuilder();
                rawSb.append(order.getOrderId()).append("|");
                rawSb.append(order.getUserId()).append("|");
                rawSb.append(String.format(java.util.Locale.US, "%.2f", order.getTotalAmount())).append("|");
                rawSb.append(order.getShippingAddress() != null ? order.getShippingAddress().trim() : "").append("|");
                if (order.getItems() != null) {
                    for (Order.OrderItem item : order.getItems()) {
                        rawSb.append(item.getProductId()).append(":")
                             .append(item.getQuantity()).append(":")
                             .append(String.format(java.util.Locale.US, "%.2f", item.getUnitPrice())).append("|");
                    }
                }
                String rawOrderData = rawSb.toString();
            %>
                <div class="detail-section" style="margin-bottom: var(--spacing-lg); padding: 25px; border: 1px dashed var(--border-color);">
                    <h2 style="margin-bottom: var(--spacing-md); display: flex; align-items: center; gap: 8px;">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--accent-primary);"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"></path></svg>
                        <fmt:message key="checkout.resign_title" />
                    </h2>
                    
                    <form method="POST" action="${pageContext.request.contextPath}/customer/order-detail">
                        <input type="hidden" name="action" value="resign_order" />
                        <input type="hidden" name="id" value="<%= order.getOrderId() %>" />
                        <input type="hidden" id="signMethod" name="signMethod" value="online" />

                        <!-- Download Link and Tabs -->
                        <div style="margin-bottom: 20px; display: flex; flex-direction: column; gap: 15px;">
                            <div>
                                <a href="${pageContext.request.contextPath}/downloads/SignatureTool.exe" class="btn btn-secondary" style="font-size: 13px; padding: 8px 16px; display: inline-flex; align-items: center; gap: 8px; text-decoration: none;" download>
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                                    <fmt:message key="checkout.offline_tool_link" />
                                </a>
                            </div>
                            
                            <div>
                                <span class="form-label" style="font-size: 13px; display: block; margin-bottom: 8px; font-weight: 500;"><fmt:message key="checkout.signing_method" /></span>
                                <div style="display: flex; gap: 10px;">
                                    <button type="button" id="btnSignOnline" class="method-btn active" onclick="setSignMethod('online')"><fmt:message key="checkout.sign_online" /></button>
                                    <button type="button" id="btnSignOffline" class="method-btn" onclick="setSignMethod('offline')"><fmt:message key="checkout.sign_offline" /></button>
                                </div>
                            </div>
                        </div>

                        <!-- Online signing input -->
                        <div id="online-signing-container" style="margin-bottom: 20px;">
                            <div class="form-group" style="margin-bottom: 12px;">
                                <input type="file" id="privateKeyFile" accept=".pem" class="form-control" style="font-size: 13px; padding: 8px; background: var(--bg-white); border: 1px solid var(--border-color); color: var(--text-primary); width: 100%;">
                            </div>
                            <textarea id="privateKey" name="privateKey" class="form-control" required style="font-family: monospace; font-size: 12px; height: 150px; background: var(--bg-white); border: 1px solid var(--border-color); color: var(--text-primary); width: 100%; box-sizing: border-box; padding: 12px;" placeholder="-----BEGIN PRIVATE KEY-----&#10;...&#10;-----END PRIVATE KEY-----"></textarea>
                        </div>

                        <!-- Offline signing input -->
                        <div id="offline-signing-container" style="display: none; margin-bottom: 20px;">
                            <div style="margin-bottom: 15px;">
                                <label class="form-label" style="font-size: 13px; font-weight: 500; display: block; margin-bottom: 6px;"><fmt:message key="checkout.raw_order_title" /></label>
                                <div style="display: flex; gap: 5px; align-items: stretch;">
                                    <input type="text" id="rawOrderData" readonly class="form-control" style="font-family: monospace; font-size: 12px; background: var(--bg-white); color: var(--text-primary); flex-grow: 1; padding: 10px; border: 1px solid var(--border-color);" value="<%= rawOrderData %>">
                                    <button type="button" class="btn btn-secondary" style="padding: 0 15px;" onclick="copyRawOrder()"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg></button>
                                </div>
                            </div>
                            <div>
                                <label for="signature" class="form-label" style="font-size: 13px; font-weight: 500; display: block; margin-bottom: 6px;"><fmt:message key="checkout.paste_signature" /></label>
                                <textarea id="signature" name="signature" class="form-control" style="font-family: monospace; font-size: 12px; height: 120px; background: var(--bg-white); border: 1px solid var(--border-color); color: var(--text-primary); width: 100%; box-sizing: border-box; padding: 12px;" placeholder="Dán mã chữ ký Base64 tạo ra từ tool..."></textarea>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary" style="width: 100%;"><fmt:message key="checkout.btn_resign" /></button>
                    </form>
                </div>
                
                <style>
                    .method-btn {
                        padding: 8px 16px;
                        font-size: 13px;
                        font-weight: 500;
                        border-radius: var(--border-radius);
                        cursor: pointer;
                        transition: all 0.2s ease;
                        border: 1px solid var(--border-color);
                        background-color: var(--bg-white);
                        color: var(--text-primary);
                    }
                    .method-btn.active {
                        background-color: var(--accent-primary);
                        color: white;
                        border-color: var(--accent-primary);
                    }
                </style>
                
                <script>
                document.getElementById('privateKeyFile').addEventListener('change', function(e) {
                    var file = e.target.files[0];
                    if (file) {
                        var reader = new FileReader();
                        reader.onload = function(evt) {
                            document.getElementById('privateKey').value = evt.target.result;
                        };
                        reader.readAsText(file);
                    }
                });

                function setSignMethod(method) {
                    document.getElementById('signMethod').value = method;
                    
                    var btnOnline = document.getElementById('btnSignOnline');
                    var btnOffline = document.getElementById('btnSignOffline');
                    var onlineContainer = document.getElementById('online-signing-container');
                    var offlineContainer = document.getElementById('offline-signing-container');
                    
                    var privateKeyInput = document.getElementById('privateKey');
                    var signatureInput = document.getElementById('signature');
                    
                    if (method === 'online') {
                        btnOnline.classList.add('active');
                        btnOffline.classList.remove('active');
                        onlineContainer.style.display = 'block';
                        offlineContainer.style.display = 'none';
                        
                        privateKeyInput.setAttribute('required', 'required');
                        signatureInput.removeAttribute('required');
                    } else {
                        btnOnline.classList.remove('active');
                        btnOffline.classList.add('active');
                        onlineContainer.style.display = 'none';
                        offlineContainer.style.display = 'block';
                        
                        privateKeyInput.removeAttribute('required');
                        signatureInput.setAttribute('required', 'required');
                    }
                }

                function copyRawOrder() {
                    var copyText = document.getElementById("rawOrderData");
                    copyText.select();
                    copyText.setSelectionRange(0, 99999);
                    navigator.clipboard.writeText(copyText.value).then(function() {
                        alert("Đã copy chuỗi dữ liệu gốc để ký!");
                    });
                }
                </script>
            <% } %>

            <!-- tiến trình trạng thái đơn hàng -->
            <div class="detail-section">
                <h2><fmt:message key="order.detail.timeline" /></h2>
                <div class="timeline">
                    <div class="timeline-item completed">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <h4><fmt:message key="order.detail.placed" /></h4>
                            <p><%= new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(order.getCreatedAt()) %></p>
                        </div>
                    </div>
                    <div class="timeline-item <%= order.getStatus().equals("PROCESSING") || order.getStatus().equals("SHIPPED") || order.getStatus().equals("DELIVERED") ? "completed" : "" %>">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <h4><fmt:message key="order.detail.processing" /></h4>
                            <p><fmt:message key="order.detail.processing_desc" /></p>
                        </div>
                    </div>
                    <div class="timeline-item <%= order.getStatus().equals("SHIPPED") || order.getStatus().equals("DELIVERED") ? "completed" : "" %>">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <h4><fmt:message key="order.detail.shipped" /></h4>
                            <p><fmt:message key="order.detail.shipped_desc" /></p>
                        </div>
                    </div>
                    <div class="timeline-item <%= order.getStatus().equals("DELIVERED") ? "completed" : "" %>">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <h4><fmt:message key="order.detail.delivered" /></h4>
                            <p><fmt:message key="order.detail.delivered_desc" /></p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- các món hàng trong đơn -->
            <div class="detail-section">
                <h2><fmt:message key="order.items_ordered" /></h2>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th><fmt:message key="order.detail.product" /></th>
                                <th><fmt:message key="order.detail.unit_price" /></th>
                                <th><fmt:message key="order.detail.quantity" /></th>
                                <th><fmt:message key="order.detail.subtotal" /></th>
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
                <h3><fmt:message key="cart.order_summary" /></h3>
                <div class="summary-row">
                    <span><fmt:message key="order.detail.subtotal" />:</span>
                    <span><%= String.format("%,.0f VNĐ", order.getTotalAmount()) %></span>
                </div>
                <div class="summary-row">
                    <span><fmt:message key="order.detail.shipping" />:</span>
                    <span><fmt:message key="checkout.free" /></span>
                </div>
                <div class="summary-row">
                    <span><fmt:message key="order.detail.tax" />:</span>
                    <span><%= String.format("%,.0f VNĐ", (order.getTotalAmount() * 0.08)) %></span>
                </div>
                <div class="summary-total">
                    <span><fmt:message key="order.detail.total_paid" />:</span>
                    <span><%= String.format("%,.0f VNĐ", (order.getTotalAmount() * 1.08)) %></span>
                </div>

                <div style="margin-top: var(--spacing-xl); padding-top: var(--spacing-md); border-top: 1px solid var(--border-color);">
                    <h3 style="margin-bottom: var(--spacing-sm); font-size: 14px;"><fmt:message key="order.detail.shipping_address" /></h3>
                    <p style="white-space: pre-wrap; color: var(--text-secondary); font-size: 13px;"><%= order.getShippingAddress() %></p>
                </div>

                <% if (order.getNotes() != null && !order.getNotes().isEmpty()) { %>
                <div style="margin-top: var(--spacing-md);">
                    <h3 style="margin-bottom: var(--spacing-sm); font-size: 14px;"><fmt:message key="order.detail.delivery_notes" /></h3>
                    <p style="color: var(--text-secondary); font-size: 13px;"><%= order.getNotes() %></p>
                </div>
                <% } %>

                <% if (Order.STATUS_PENDING.equals(order.getStatus())) { %>
                <div style="margin-top: var(--spacing-xl); padding-top: var(--spacing-md); border-top: 1px solid var(--border-color);">
                    <form method="POST" action="${pageContext.request.contextPath}/customer/order-detail" onsubmit="return confirmCancel();">
                        <input type="hidden" name="action" value="cancel" />
                        <input type="hidden" name="id" value="<%= order.getOrderId() %>" />
                        <button type="submit" class="btn btn-danger" style="width: 100%;">
                            <fmt:message key="order.detail.cancel_btn" />
                        </button>
                    </form>
                </div>
                <script>
                    function confirmCancel() {
                        return confirm('<fmt:message key="order.detail.cancel_confirm" />');
                    }
                </script>
                <% } %>
            </div>
        </div>
    </div>

    <% } else { %>
    <div class="empty-state">
        <h2><fmt:message key="order.not_found" /></h2>
        <a href="${pageContext.request.contextPath}/customer/orders" class="btn btn-primary" style="margin-top: var(--spacing-md);"><fmt:message key="order.view_orders" /></a>
    </div>
    <% } %>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>
