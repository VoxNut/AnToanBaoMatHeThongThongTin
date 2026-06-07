<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.List" %>
<%@ page import="com.beveragestore.model.Order" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="admin.portal.manage_orders_title" /></title>
    
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css?v=1.0">
    <style>
        .filter-bar {
            background: #f8f9fa;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 18px 24px;
            margin-bottom: var(--spacing-xl);
        }
        .filter-form {
            width: 100%;
        }
        .filter-fields {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            align-items: flex-end;
        }
        .filter-item {
            flex: 1 1 200px;
            display: flex;
            flex-direction: column;
            gap: 6px;
        }
        .filter-label {
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            color: var(--text-secondary);
            letter-spacing: 0.5px;
        }
        .filter-input, .filter-select {
            height: 40px;
            padding: 8px 12px;
            border: 1px solid var(--border-color);
            border-radius: 6px;
            width: 100%;
            background-color: var(--bg-white);
            color: var(--text-primary);
            font-size: 13.5px;
        }
        .filter-select {
            cursor: pointer;
        }
        .filter-actions {
            display: flex;
            gap: 10px;
            margin-top: 10px;
        }
        .btn-filter, .btn-clear {
            height: 40px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 0 20px;
            font-size: 13.5px;
            font-weight: 500;
            border-radius: 6px;
        }
        .btn-clear {
            background: transparent;
            border: 1px solid var(--border-color);
            color: var(--text-primary);
            text-decoration: none;
        }
        .btn-clear:hover {
            background: var(--bg-secondary);
        }
        
        /* Table Styling Improvements */
        .table-container {
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.02);
            border: 1px solid var(--border-color);
        }
        table {
            border-collapse: separate;
            border-spacing: 0;
        }
        th {
            padding: 16px 20px;
            background: #f8f9fa;
            font-size: 12px;
            letter-spacing: 0.5px;
            border-bottom: 2px solid var(--border-color);
        }
        td {
            padding: 16px 20px;
            border-bottom: 1px solid var(--border-color);
            vertical-align: middle;
        }
        tr:last-child td {
            border-bottom: none;
        }
        tr:hover td {
            background-color: rgba(248, 249, 250, 0.5);
        }
        
        /* Dropdown status coloring badge */
        .status-select {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            cursor: pointer;
            border: 1px solid transparent;
            outline: none;
            transition: all 0.2s ease;
            appearance: none;
            -webkit-appearance: none;
            background-image: url("data:image/svg+xml;utf8,<svg fill='currentColor' height='24' viewBox='0 0 24 24' width='24' xmlns='http://www.w3.org/2000/svg'><path d='M7 10l5 5 5-5z'/></svg>");
            background-repeat: no-repeat;
            background-position: right 8px center;
            padding-right: 28px;
            text-align: center;
        }
        .status-select.status-DELIVERED {
            background-color: var(--success-bg);
            color: var(--success-text);
            border-color: #bbf7d0;
        }
        .status-select.status-CANCELLED {
            background-color: var(--error-bg);
            color: var(--error-text);
            border-color: #fecaca;
        }
        .status-select.status-PENDING {
            background-color: #fff7ed;
            color: #c2410c;
            border-color: #ffedd5;
        }
        .status-select.status-PROCESSING {
            background-color: #f0f9ff;
            color: #0369a1;
            border-color: #e0f2fe;
        }
        .status-select.status-SHIPPED {
            background-color: #faf5ff;
            color: #6b21a8;
            border-color: #f3e8ff;
        }

        /* Toast Popup */
        .toast-container {
            position: fixed;
            bottom: 30px;
            right: 30px;
            background: #27272a;
            color: #ffffff;
            padding: 12px 24px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            display: none;
            z-index: 10000;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
            border: 1px solid #3f3f46;
        }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/partials/header.jsp" />

<div class="page-header" style="text-align:center; padding: var(--spacing-xl) 0 var(--spacing-md);">
    <div class="container">
        <h1><fmt:message key="admin.nav.manage_orders" /></h1>
    </div>
</div>

<div class="container admin-container">
    <div class="admin-sidebar">
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin/dashboard"><fmt:message key="admin.nav.dashboard" /></a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="active"><fmt:message key="admin.nav.manage_orders" /></a>
            <a href="${pageContext.request.contextPath}/admin/products"><fmt:message key="admin.nav.manage_products" /></a>
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

        <h2 style="font-family: var(--font-body); margin-bottom: var(--spacing-lg);"><fmt:message key="admin.orders.all" /></h2>

        <!-- Filter Bar -->
        <div class="filter-bar">
            <form method="GET" action="${pageContext.request.contextPath}/admin/orders" class="filter-form">
                <div class="filter-fields">
                    <div class="filter-item">
                        <label for="searchId" class="filter-label"><fmt:message key="admin.orders.filter.order_id" /></label>
                        <fmt:message key="admin.orders.filter.placeholder" var="searchPlaceholder" />
                        <input type="text" id="searchId" name="searchId" value="<%= request.getAttribute("searchId") != null ? request.getAttribute("searchId") : "" %>" placeholder="${searchPlaceholder}" class="filter-input">
                    </div>
                    <div class="filter-item">
                        <label for="statusFilter" class="filter-label"><fmt:message key="admin.orders.filter.status" /></label>
                        <select id="statusFilter" name="statusFilter" class="filter-select">
                            <% String statusVal = (String) request.getAttribute("statusFilter"); %>
                            <option value="ALL" <%= "ALL".equals(statusVal) || statusVal == null || statusVal.isEmpty() ? "selected" : "" %>><fmt:message key="admin.orders.filter.status.all" /></option>
                            <option value="PENDING" <%= "PENDING".equals(statusVal) ? "selected" : "" %>><fmt:message key="status.PENDING" /></option>
                            <option value="PROCESSING" <%= "PROCESSING".equals(statusVal) ? "selected" : "" %>><fmt:message key="status.PROCESSING" /></option>
                            <option value="SHIPPED" <%= "SHIPPED".equals(statusVal) ? "selected" : "" %>><fmt:message key="status.SHIPPED" /></option>
                            <option value="DELIVERED" <%= "DELIVERED".equals(statusVal) ? "selected" : "" %>><fmt:message key="status.DELIVERED" /></option>
                            <option value="CANCELLED" <%= "CANCELLED".equals(statusVal) ? "selected" : "" %>><fmt:message key="status.CANCELLED" /></option>
                        </select>
                    </div>
                    <div class="filter-item">
                        <label for="sigFilter" class="filter-label"><fmt:message key="admin.orders.filter.sig" /></label>
                        <select id="sigFilter" name="sigFilter" class="filter-select">
                            <% String sigVal = (String) request.getAttribute("sigFilter"); %>
                            <option value="ALL" <%= "ALL".equals(sigVal) || sigVal == null || sigVal.isEmpty() ? "selected" : "" %>><fmt:message key="admin.orders.filter.sig.all" /></option>
                            <option value="VALID" <%= "VALID".equals(sigVal) ? "selected" : "" %>><fmt:message key="admin.orders.sig.valid" /></option>
                            <option value="INVALID" <%= "INVALID".equals(sigVal) ? "selected" : "" %>><fmt:message key="admin.orders.sig.invalid" /></option>
                            <option value="REVOKED_KEY" <%= "REVOKED_KEY".equals(sigVal) ? "selected" : "" %>><fmt:message key="admin.orders.sig.revoked" /></option>
                            <option value="UNSIGNED" <%= "UNSIGNED".equals(sigVal) ? "selected" : "" %>><fmt:message key="admin.orders.sig.unsigned" /></option>
                        </select>
                    </div>
                    <div class="filter-actions">
                        <button type="submit" class="btn btn-primary btn-filter"><fmt:message key="admin.orders.filter.btn_filter" /></button>
                        <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-secondary btn-clear"><fmt:message key="admin.orders.filter.btn_clear" /></a>
                    </div>
                </div>
            </form>
        </div>

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th><fmt:message key="admin.orders.table.id" /></th>
                        <th><fmt:message key="admin.orders.table.date" /></th>
                        <th><fmt:message key="admin.orders.table.total" /></th>
                        <th><fmt:message key="admin.orders.table.signature" /></th>
                        <th><fmt:message key="admin.orders.table.status" /></th>
                    </tr>
                </thead>
                <tbody>
                    <% List<Order> orders = (List<Order>) request.getAttribute("orders");
                       if (orders != null && !orders.isEmpty()) {
                           for (Order o : orders) {
                               String sigStatus = o.getSignatureStatus() != null ? o.getSignatureStatus() : "UNSIGNED";
                               String badgeBg = "var(--bg-secondary)";
                               String badgeColor = "var(--text-primary)";
                               String badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><circle cx='12' cy='12' r='10'></circle><line x1='12' y1='8' x2='12' y2='12'></line><line x1='12' y1='16' x2='12.01' y2='16'></line></svg>";
                               
                               if ("VALID".equals(sigStatus)) {
                                   badgeBg = "var(--success-bg)";
                                   badgeColor = "var(--success-text)";
                                   badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><polyline points='20 6 9 17 4 12'></polyline></svg>";
                               } else if ("INVALID".equals(sigStatus)) {
                                   badgeBg = "var(--error-bg)";
                                   badgeColor = "var(--error-text)";
                                   badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><line x1='18' y1='6' x2='6' y2='18'></line><line x1='6' y1='6' x2='18' y2='18'></line></svg>";
                               } else if ("REVOKED_KEY".equals(sigStatus)) {
                                   badgeBg = "#fef3c7";
                                   badgeColor = "#b45309";
                                   badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><path d='M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z'></path><line x1='12' y1='9' x2='12' y2='13'></line><line x1='12' y1='17' x2='12.01' y2='17'></line></svg>";
                               } else if ("NO_KEY_FOUND".equals(sigStatus)) {
                                   badgeBg = "var(--error-bg)";
                                   badgeColor = "var(--error-text)";
                                   badgeIcon = "<svg width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' style='margin-right: 4px; vertical-align: text-bottom;'><circle cx='12' cy='12' r='10'></circle><line x1='4.93' y1='4.93' x2='19.07' y2='19.07'></line></svg>";
                               }
                     %>
                     <tr>
                         <td>
                             <div style="display: flex; align-items: center; gap: 4px;">
                                 <span style="font-family: monospace; color: var(--text-secondary);" title="<%= o.getOrderId() %>">
                                     <%= o.getOrderId().substring(0, 8) %>...
                                 </span>
                                 <fmt:message key="admin.orders.table.copy_title" var="copyTitle" />
                                 <button onclick="copyToClipboard('<%= o.getOrderId() %>')" style="background: none; border: none; padding: 2px; cursor: pointer; color: var(--text-light); display: inline-flex; align-items: center;" title="${copyTitle}">
                                     <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>
                                 </button>
                             </div>
                         </td>
                         <td><%= new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(o.getCreatedAt()) %></td>
                         <td style="font-weight: 500;"><%= String.format("%,.0f VNĐ", o.getTotalAmount()) %></td>
                         <td>
                              <div style="display: flex; flex-direction: column; gap: 6px; align-items: flex-start;">
                                  <span style="display: inline-flex; align-items: center; padding: 4px 8px; border-radius: var(--border-radius); font-size: 11px; font-weight: 600; color: <%= badgeColor %>; background-color: <%= badgeBg %>; border: 1px solid <%= badgeColor %>22;">
                                      <%= badgeIcon %>
                                      <% if ("VALID".equals(sigStatus)) { %>
                                          <fmt:message key="admin.orders.sig.valid" />
                                      <% } else if ("INVALID".equals(sigStatus)) { %>
                                          <fmt:message key="admin.orders.sig.invalid" />
                                      <% } else if ("REVOKED_KEY".equals(sigStatus)) { %>
                                          <fmt:message key="admin.orders.sig.revoked" />
                                      <% } else { %>
                                          <fmt:message key="admin.orders.sig.unsigned" />
                                      <% } %>
                                  </span>
                                  
                                  <% if (o.isResignRequired()) { %>
                                      <span style="display: inline-flex; align-items: center; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: 600; color: #b45309; background-color: #fef3c7; border: 1px solid #fde68a;">
                                          <fmt:message key="admin.orders.resign_requested" />
                                      </span>
                                  <% } else if ("INVALID".equals(sigStatus) && Order.STATUS_PENDING.equals(o.getStatus())) { %>
                                      <form method="POST" action="${pageContext.request.contextPath}/admin/orders" style="margin: 0;">
                                          <input type="hidden" name="action" value="request_resign" />
                                          <input type="hidden" name="orderId" value="<%= o.getOrderId() %>" />
                                          <button type="submit" class="btn btn-secondary" style="font-size: 10px; padding: 4px 8px; line-height: 1; min-height: auto; border-radius: 4px; cursor: pointer;">
                                              <fmt:message key="admin.orders.action.request_resign" />
                                          </button>
                                      </form>
                                  <% } %>
                              </div>
                          </td>
                         <td>
                             <form method="POST" action="${pageContext.request.contextPath}/admin/orders" class="action-form" style="margin: 0;">
                                 <input type="hidden" name="action" value="update_status">
                                 <input type="hidden" name="orderId" value="<%= o.getOrderId() %>">
                                 <select name="status" class="status-select status-<%= o.getStatus() %>" onchange="this.form.submit();">
                                     <option value="PENDING" <%= "PENDING".equals(o.getStatus()) ? "selected" : "" %>><fmt:message key="status.PENDING" /></option>
                                     <option value="PROCESSING" <%= "PROCESSING".equals(o.getStatus()) ? "selected" : "" %>><fmt:message key="status.PROCESSING" /></option>
                                     <option value="SHIPPED" <%= "SHIPPED".equals(o.getStatus()) ? "selected" : "" %>><fmt:message key="status.SHIPPED" /></option>
                                     <option value="DELIVERED" <%= "DELIVERED".equals(o.getStatus()) ? "selected" : "" %>><fmt:message key="status.DELIVERED" /></option>
                                     <option value="CANCELLED" <%= "CANCELLED".equals(o.getStatus()) ? "selected" : "" %>><fmt:message key="status.CANCELLED" /></option>
                                 </select>
                             </form>
                         </td>
                     </tr>
                     <%      }
                        } else { %>
                     <tr>
                         <td colspan="5" style="text-align: center; padding: 40px;"><fmt:message key="admin.orders.table.no_orders" /></td>
                     </tr>

                     <% } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Toast Popup -->
<div id="toast-msg" class="toast-container"></div>

<script>
function showToast(message) {
    var toast = document.getElementById("toast-msg");
    toast.innerText = message;
    toast.style.display = "block";
    setTimeout(function() {
        toast.style.display = "none";
    }, 2500);
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(function() {
        var successTemplate = '<fmt:message key="admin.orders.table.copied" />';
        var formatted = successTemplate.replace('{0}', text.substring(0, 8) + '...');
        showToast(formatted);
    }, function(err) {
        console.error('Could not copy text: ', err);
    });
}
</script>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />
</body>
</html>
