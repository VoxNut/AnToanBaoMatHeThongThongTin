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
    
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/store.css?v=1.0">
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
</head>
<body>

<jsp:include page="/WEB-INF/views/partials/header.jsp" />

<div class="page-header">
    <div class="container">
        <h1><fmt:message key="checkout.title" /></h1>
    </div>
</div>

<div class="container">
    <div class="checkout-grid" style="display: grid; grid-template-columns: 1.5fr 1fr; gap: var(--spacing-xl); margin-top: var(--spacing-xl);">
        <div class="checkout-form">
            <form method="POST" action="${pageContext.request.contextPath}/customer/checkout">
                <input type="hidden" name="orderId" value="${orderId}">
                <input type="hidden" id="signMethod" name="signMethod" value="online">
                
                <h2><fmt:message key="checkout.shipping_delivery" /></h2>
                
                <div class="form-group">
                    <label for="shippingAddress" class="form-label"><fmt:message key="checkout.shipping_address" /></label>
                    <textarea id="shippingAddress" name="shippingAddress" class="form-control" required placeholder="<fmt:message key="checkout.shipping_placeholder" />" oninput="updateRawOrderString()"></textarea>
                </div>

                <div class="form-group">
                    <label for="notes" class="form-label"><fmt:message key="checkout.delivery_notes" /></label>
                    <textarea id="notes" name="notes" class="form-control" placeholder="<fmt:message key="checkout.delivery_placeholder" />"></textarea>
                </div>

                <!-- Signature Section -->
                <div class="form-group" style="margin-top: 20px; padding: 20px; background-color: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: var(--border-radius);">
                    <label class="form-label" style="font-weight: 600; color: var(--text-primary); display: flex; align-items: center; margin-bottom: 5px;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--accent-primary); margin-right: 8px;"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"></path></svg>
                        <fmt:message key="checkout.sign_order" />
                    </label>

                    <!-- Method Selection -->
                    <div style="margin-bottom: 15px;">
                        <span class="form-label" style="font-size: 12px; display: block; margin-bottom: 6px;"><fmt:message key="checkout.signing_method" /></span>
                        <div style="display: flex; gap: 10px;">
                            <button type="button" id="btnSignOnline" class="method-btn active" onclick="setSignMethod('online')"><fmt:message key="checkout.sign_online" /></button>
                            <button type="button" id="btnSignOffline" class="method-btn" onclick="setSignMethod('offline')"><fmt:message key="checkout.sign_offline" /></button>
                        </div>
                    </div>

                    <!-- Option 1: Online container -->
                    <div id="online-signing-container">
                        <p style="font-size: 12px; color: var(--text-secondary); margin-bottom: 10px;">
                            <fmt:message key="checkout.sign_instructions" />
                        </p>
                        <div style="margin-bottom: 10px;">
                            <input type="file" id="privateKeyFile" accept=".pem,.txt,.key" class="form-control" onchange="handlePrivateKeySelect(event)" style="font-size: 13px; padding: 5px; background: var(--bg-white); border: 1px solid var(--border-color); color: var(--text-primary);">
                        </div>
                        <textarea id="privateKey" name="privateKey" class="form-control" required style="font-family: monospace; font-size: 12px; height: 120px; background: var(--bg-white); border: 1px solid var(--border-color); color: var(--text-primary);" placeholder="-----BEGIN PRIVATE KEY-----&#10;...&#10;-----END PRIVATE KEY-----"></textarea>
                    </div>

                    <!-- Option 2: Offline container -->
                    <div id="offline-signing-container" style="display: none;">
                        <div style="margin-bottom: 10px;">
                            <label class="form-label" style="font-size: 12px;"><fmt:message key="checkout.raw_order_title" /></label>
                            <div style="display: flex; gap: 5px; align-items: stretch;">
                                <input type="text" id="rawOrderData" readonly class="form-control" style="font-family: monospace; font-size: 11px; background: var(--bg-white); color: var(--text-primary);" value="">
                                <button type="button" class="btn btn-secondary" style="padding: 0 10px;" onclick="copyRawOrder()"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg></button>
                            </div>
                        </div>
                        <div>
                            <label for="signature" class="form-label" style="font-size: 12px;"><fmt:message key="checkout.paste_signature" /></label>
                            <textarea id="signature" name="signature" class="form-control" style="font-family: monospace; font-size: 12px; height: 100px; background: var(--bg-white); border: 1px solid var(--border-color); color: var(--text-primary);" placeholder="Dán mã chữ ký Base64 tạo ra từ tool..."></textarea>
                        </div>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: var(--spacing-md);"><fmt:message key="checkout.place_order" /></button>
            </form>

            <a href="${pageContext.request.contextPath}/customer/cart" class="back-link"><fmt:message key="checkout.return_to_cart" /></a>
        </div>

        <!-- Order Summary panel -->
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
                    <span class="item-price"><%= String.format("%,.0f VNĐ", item.getSubtotal()) %></span>
                </div>
                <%
                        }
                    }
                %>
            </div>

            <div class="summary-row">
                <span><fmt:message key="cart.subtotal" /></span>
                <span><%= String.format("%,.0f VNĐ", request.getAttribute("cartTotal")) %></span>
            </div>

            <div class="summary-row">
                <span><fmt:message key="cart.shipping" /></span>
                <span><fmt:message key="checkout.free" /></span>
            </div>

            <div class="summary-row">
                <span><fmt:message key="cart.tax" /></span>
                <span><%= String.format("%,.0f VNĐ", ((double) request.getAttribute("cartTotal") * 0.08)) %></span>
            </div>

            <div class="summary-total">
                <span><fmt:message key="checkout.total_to_pay" /></span>
                <span><%= String.format("%,.0f VNĐ", ((double) request.getAttribute("cartTotal") * 1.08)) %></span>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

<script>
function handlePrivateKeySelect(e) {
    var file = e.target.files[0];
    if (file) {
        var reader = new FileReader();
        reader.onload = function(evt) {
            document.getElementById('privateKey').value = evt.target.result;
        };
        reader.readAsText(file);
    }
}

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
        
        updateRawOrderString();
    }
}

function updateRawOrderString() {
    var orderId = "${orderId}";
    var userId = "${sessionScope.loggedInUser.uid}";
    var totalAmount = parseFloat("${cartTotal}").toFixed(2);
    var address = document.getElementById("shippingAddress").value.trim();
    
    var itemsStr = "";
    <%
        if (cartItems != null) {
            for (CartItem item : cartItems) {
    %>
    itemsStr += "<%= item.getProductId() %>:<%= item.getQuantity() %>:<%= String.format(java.util.Locale.US, "%.2f", item.getPrice()) %>|";
    <%
            }
        }
    %>
    
    var raw = orderId + "|" + userId + "|" + totalAmount + "|" + address + "|" + itemsStr;
    document.getElementById("rawOrderData").value = raw;
}

function copyRawOrder() {
    var copyText = document.getElementById("rawOrderData");
    copyText.select();
    copyText.setSelectionRange(0, 99999);
    navigator.clipboard.writeText(copyText.value).then(function() {
        showAlert("Đã copy chuỗi dữ liệu gốc để ký!", "success");
    });
}
</script>
</body>
</html>
