<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.beveragestore.model.User" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="key.title" /> - The Grindery</title>
    <!-- nạp font chữ từ google -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    
    <!-- file css dùng chung cho cả web -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/store.css?v=1.0">
    
    <style>
        .key-container {
            max-width: 800px;
            margin: 2rem auto;
            background: var(--bg-white);
            padding: 2.5rem;
            border-radius: var(--border-radius);
            border: 1px solid var(--border-color);
        }
        .key-badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: 600;
            margin-bottom: 10px;
        }
        .key-badge.active { background: var(--success-bg); color: var(--success-text); }
        .key-badge.revoked { background: var(--error-bg); color: var(--error-text); }
        .key-box {
            font-family: monospace;
            background: var(--bg-secondary);
            border: 1px solid var(--border-color);
            padding: 15px;
            border-radius: var(--border-radius);
            overflow-x: auto;
            white-space: pre-wrap;
            font-size: 13px;
            color: var(--text-primary);
            max-height: 150px;
            margin: 10px 0;
        }
        .key-history-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 1.5rem;
        }
        .key-history-table th, .key-history-table td {
            padding: 10px 15px;
            border-bottom: 1px solid var(--border-color);
            text-align: left;
            font-size: 14px;
            color: var(--text-primary);
        }
        .key-history-table th {
            background-color: var(--bg-secondary);
            font-weight: 600;
        }
        .revoke-section {
            background-color: var(--error-bg);
            border-left: 5px solid var(--error-text);
            padding: 15px;
            border-radius: var(--border-radius);
            margin-top: 2rem;
            color: var(--text-primary);
        }
    </style>
    <script>
        function validateKeyForm(form) {
            // kiểm tra phương thức nhận khóa
            var checkboxes = form.querySelectorAll('input[name="receiveMethod"]');
            var checkedOne = Array.prototype.slice.call(checkboxes).some(x => x.checked);
            if (!checkedOne) {
                alert('<fmt:message key="key.validation.select_method" />');
                return false;
            }

            // kiểm tra thời gian lộ khóa không được ở tương lai
            var revokeTimeInput = form.querySelector('input[name="revokeTime"]');
            if (revokeTimeInput && revokeTimeInput.value) {
                var selectedDate = new Date(revokeTimeInput.value);
                var now = new Date();
                if (selectedDate > now) {
                    alert('<fmt:message key="key.validation.future_date" />');
                    return false;
                }
            }
            return true;
        }

        document.addEventListener("DOMContentLoaded", function() {
            var revokeTimeInput = document.getElementById("revokeTime");
            if (revokeTimeInput) {
                // thiết lập max datetime-local là thời gian hiện tại
                var now = new Date();
                var year = now.getFullYear();
                var month = String(now.getMonth() + 1).padStart(2, '0');
                var day = String(now.getDate()).padStart(2, '0');
                var hours = String(now.getHours()).padStart(2, '0');
                var minutes = String(now.getMinutes()).padStart(2, '0');
                
                var formattedDateTime = year + '-' + month + '-' + day + 'T' + hours + ':' + minutes;
                revokeTimeInput.setAttribute("max", formattedDateTime);
            }
        });
    </script>
</head>
<body>

<jsp:include page="/WEB-INF/views/partials/header.jsp" />

<div class="page-header">
    <div class="container">
        <h1 style="display: flex; align-items: center; justify-content: center; gap: 10px;">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--accent-primary);"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"></path></svg>
            <fmt:message key="key.title" />
        </h1>
        <p style="color: var(--text-light); margin-top: 5px;"><fmt:message key="key.subtitle" /></p>
    </div>
</div>

<div class="container">
    <div class="key-container">
        <c:if test="${not empty error}">
            <div class="alert alert-danger" style="margin-bottom: 20px; padding: 10px; background: #f8d7da; color: #721c24; border-radius: 4px;">
                ${error}
            </div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success" style="margin-bottom: 20px; padding: 10px; background: #d4edda; color: #155724; border-radius: 4px; border: 1px solid #c3e6cb;">
                ${success}
            </div>
        </c:if>
        
        <%
            User userKeys = (User) request.getAttribute("userKeys");
            if (userKeys != null && userKeys.getActivePublicKey() != null) {
        %>
            <div class="card" style="margin-bottom: 2rem; border: 1px solid #ced4da; padding: 20px; border-radius: 8px;">
                <span class="key-badge active"><fmt:message key="key.active_badge" /></span>
                <p><strong><fmt:message key="key.id" /></strong> <%= userKeys.getActivePublicKeyId() %></p>
                <p><strong><fmt:message key="key.public_pem" /></strong></p>
                <div class="key-box"><%= userKeys.getActivePublicKey() %></div>
                <p style="font-size: 13px; color: #6c757d; margin-top: 5px;">
                    <fmt:message key="key.notice" />
                </p>
            </div>

            <div class="revoke-section">
                <h3 style="display: flex; align-items: center; gap: 8px; color: var(--error-text);">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                    <fmt:message key="key.revoke_title" />
                </h3>
                <p style="font-size: 14px; margin-bottom: 15px;">
                    <fmt:message key="key.revoke_desc" />
                </p>
                <form action="${pageContext.request.contextPath}/customer/keys" method="post" class="form-inline" onsubmit="return validateKeyForm(this);">
                    <input type="hidden" name="action" value="revoke">
                    <div style="margin-bottom: 15px;">
                        <label for="revokeTime" style="font-weight: 500; font-size: 14px; display: block; margin-bottom: 5px;"><fmt:message key="key.revoke_time" /></label>
                        <input type="datetime-local" id="revokeTime" name="revokeTime" class="form-control" style="padding: 8px; width: 100%; max-width: 300px; border: 1px solid #ced4da; border-radius: 4px;">
                    </div>
                    <div style="margin: 15px 0; padding: 12px; background-color: var(--bg-secondary); border-radius: var(--border-radius); border: 1px solid var(--border-color); text-align: left;">
                        <p style="font-weight: 600; margin-bottom: 8px; font-size: 14px; color: var(--text-primary);"><fmt:message key="key.receive_options" /></p>
                        <div style="display: flex; flex-direction: column; gap: 8px;">
                            <label style="display: flex; align-items: center; gap: 8px; font-size: 14px; cursor: pointer; color: var(--text-primary);">
                                <input type="checkbox" name="receiveMethod" value="download" checked style="width: 16px; height: 16px; accent-color: var(--accent-primary);">
                                <fmt:message key="key.option_download" />
                            </label>
                            <label style="display: flex; align-items: center; gap: 8px; font-size: 14px; cursor: pointer; color: var(--text-primary);">
                                <input type="checkbox" name="receiveMethod" value="email" checked style="width: 16px; height: 16px; accent-color: var(--accent-primary);">
                                <fmt:message key="key.option_email"><fmt:param value="<%= userKeys.getEmail() %>" /></fmt:message>
                            </label>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary" style="background-color: #dc3545; border-color: #dc3545;" onclick="return confirm('<fmt:message key="key.confirm_revoke" />');">
                        <fmt:message key="key.btn_revoke" />
                    </button>
                </form>
            </div>
        <%
            } else {
        %>
            <div class="empty-state" style="text-align: center; padding: 2rem 0;">
                <h2><fmt:message key="key.empty_title" /></h2>
                <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">
                    <fmt:message key="key.empty_desc" />
                </p>
                <form action="${pageContext.request.contextPath}/customer/keys" method="post" onsubmit="return validateKeyForm(this);">
                    <input type="hidden" name="action" value="generate">
                    <div style="margin: 15px auto 20px; padding: 12px; background-color: var(--bg-secondary); border-radius: var(--border-radius); border: 1px solid var(--border-color); text-align: left; max-width: 400px;">
                        <p style="font-weight: 600; margin-bottom: 8px; font-size: 14px; color: var(--text-primary); text-align: center;"><fmt:message key="key.receive_options_new" /></p>
                        <div style="display: flex; flex-direction: column; gap: 8px; max-width: 300px; margin: 0 auto;">
                            <label style="display: flex; align-items: center; gap: 8px; font-size: 14px; cursor: pointer; color: var(--text-primary);">
                                <input type="checkbox" name="receiveMethod" value="download" checked style="width: 16px; height: 16px; accent-color: var(--accent-primary);">
                                <fmt:message key="key.option_download" />
                            </label>
                            <label style="display: flex; align-items: center; gap: 8px; font-size: 14px; cursor: pointer; color: var(--text-primary);">
                                <input type="checkbox" name="receiveMethod" value="email" checked style="width: 16px; height: 16px; accent-color: var(--accent-primary);">
                                <fmt:message key="key.option_email"><fmt:param value="<%= userKeys != null ? userKeys.getEmail() : \"\" %>" /></fmt:message>
                            </label>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary"><fmt:message key="key.btn_generate" /></button>
                </form>
            </div>
        <%
            }
        %>

        <%
            if (userKeys != null && userKeys.getKeyHistory() != null && !userKeys.getKeyHistory().isEmpty()) {
        %>
            <h3 style="margin-top: 3rem;"><fmt:message key="key.history_title" /></h3>
            <table class="key-history-table">
                <thead>
                    <tr>
                        <th>Key ID</th>
                        <th><fmt:message key="key.history.date" /></th>
                        <th><fmt:message key="key.history.status" /></th>
                        <th><fmt:message key="key.history.revoked_date" /></th>
                    </tr>
                </thead>
                <tbody>
                    <% for (User.PublicKeyRecord record : userKeys.getKeyHistory()) { 
                        boolean isRevoked = record.getRevokedAt() != null;
                    %>
                        <tr>
                            <td><%= record.getKeyId().substring(0, 8) %>...</td>
                            <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(record.getCreatedAt()) %></td>
                            <td>
                                <span class="key-badge <%= isRevoked ? "revoked" : "active" %>">
                                    <fmt:message key="<%= isRevoked ? \"key.status.revoked\" : \"key.status.active\" %>" />
                                </span>
                            </td>
                            <td>
                                <%= isRevoked ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(record.getRevokedAt()) : "-" %>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <%
            }
        %>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

</body>
</html>
