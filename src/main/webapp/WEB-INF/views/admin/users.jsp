<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.List" %>
<%@ page import="com.beveragestore.model.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="admin.portal.manage_users_title" /></title>
    
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
        <h1><fmt:message key="admin.nav.manage_users" /></h1>
    </div>
</div>

<div class="container admin-container">
    <div class="admin-sidebar">
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin/dashboard"><fmt:message key="admin.nav.dashboard" /></a>
            <a href="${pageContext.request.contextPath}/admin/orders"><fmt:message key="admin.nav.manage_orders" /></a>
            <a href="${pageContext.request.contextPath}/admin/products"><fmt:message key="admin.nav.manage_products" /></a>
            <a href="${pageContext.request.contextPath}/admin/users" class="active"><fmt:message key="admin.nav.manage_users" /></a>
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

        <h2 style="font-family: var(--font-body); margin-bottom: var(--spacing-lg);"><fmt:message key="admin.users.all" /></h2>

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th><fmt:message key="admin.users.table.name" /></th>
                        <th><fmt:message key="admin.users.table.email" /></th>
                        <th><fmt:message key="admin.users.table.role" /></th>
                        <th><fmt:message key="admin.users.table.status" /></th>
                        <th><fmt:message key="admin.users.table.actions" /></th>
                    </tr>
                </thead>
                <tbody>
                    <% List<User> users = (List<User>) request.getAttribute("users");
                       if (users != null && !users.isEmpty()) {
                           for (User u : users) {
                     %>
                     <tr>
                         <td><%= u.getFullName() != null ? u.getFullName() : "N/A" %></td>
                         <td><%= u.getEmail() %></td>
                         <td>
                             <span class="role-badge role-<%= u.getRole() %>">
                                 <fmt:message key="<%= \"admin.users.role.\" + u.getRole() %>" />
                             </span>
                         </td>
                         <td>
                             <% if (u.isActive()) { %>
                                 <span class="status-badge status-active"><fmt:message key="admin.users.status.active" /></span>
                             <% } else { %>
                                 <span class="status-badge status-inactive"><fmt:message key="admin.users.status.inactive" /></span>
                             <% } %>
                         </td>
                         <td>
                             <form method="POST" action="${pageContext.request.contextPath}/admin/users" class="action-form" style="margin-bottom: 8px;">
                                 <input type="hidden" name="action" value="update_role">
                                 <input type="hidden" name="uid" value="<%= u.getUid() %>">
                                 <select name="role" class="form-control">
                                     <option value="customer" <%= "customer".equals(u.getRole()) ? "selected" : "" %>><fmt:message key="admin.users.role.customer" /></option>
                                     <option value="admin" <%= "admin".equals(u.getRole()) ? "selected" : "" %>><fmt:message key="admin.users.role.admin" /></option>
                                     <option value="shipper" <%= "shipper".equals(u.getRole()) ? "selected" : "" %>><fmt:message key="admin.users.role.shipper" /></option>
                                     <option value="shop_owner" <%= "shop_owner".equals(u.getRole()) ? "selected" : "" %>><fmt:message key="admin.users.role.shop_owner" /></option>
                                 </select>
                                 <button type="submit" class="btn btn-primary" style="padding: 6px 12px; font-size: 12px;"><fmt:message key="admin.users.action.update" /></button>
                             </form>
                             <form method="POST" action="${pageContext.request.contextPath}/admin/users" class="action-form">
                                 <input type="hidden" name="action" value="toggle_status">
                                 <input type="hidden" name="uid" value="<%= u.getUid() %>">
                                 <button type="submit" class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;">
                                     <% if (u.isActive()) { %>
                                         <fmt:message key="admin.users.action.deactivate" />
                                     <% } else { %>
                                         <fmt:message key="admin.users.action.activate" />
                                     <% } %>
                                 </button>
                             </form>
                         </td>
                     </tr>
                     <%      }
                        } else { %>
                     <tr>
                         <td colspan="5" style="text-align: center; padding: 40px;"><fmt:message key="admin.users.table.no_users" /></td>
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
