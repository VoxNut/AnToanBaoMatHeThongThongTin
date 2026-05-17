<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.beveragestore.model.User" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Khóa Chữ Ký - Online Beverage Store</title>
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
</head>
<body>

<jsp:include page="/WEB-INF/views/partials/header.jsp" />

<div class="page-header">
    <div class="container">
        <h1 style="display: flex; align-items: center; justify-content: center; gap: 10px;">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--accent-primary);"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"></path></svg>
            Quản Lý Khóa Ký Đơn Hàng
        </h1>
        <p style="color: var(--text-light); margin-top: 5px;">Hệ thống xác thực và đảm bảo tính toàn vẹn của đơn hàng</p>
    </div>
</div>

<div class="container">
    <div class="key-container">
        <c:if test="${not empty error}">
            <div class="alert alert-danger" style="margin-bottom: 20px; padding: 10px; background: #f8d7da; color: #721c24; border-radius: 4px;">
                ${error}
            </div>
        </c:if>
        
        <%
            User userKeys = (User) request.getAttribute("userKeys");
            if (userKeys != null && userKeys.getActivePublicKey() != null) {
        %>
            <div class="card" style="margin-bottom: 2rem; border: 1px solid #ced4da; padding: 20px; border-radius: 8px;">
                <span class="key-badge active">Khóa Đang Hoạt Động</span>
                <p><strong>Mã định danh khóa (Key ID):</strong> <%= userKeys.getActivePublicKeyId() %></p>
                <p><strong>Khóa Công Khai (Public Key PEM):</strong></p>
                <div class="key-box"><%= userKeys.getActivePublicKey() %></div>
                <p style="font-size: 13px; color: #6c757d; margin-top: 5px;">
                    * Lưu ý: Khóa bí mật (Private Key) đã được tải về máy của bạn khi khởi tạo và không lưu trữ trên máy chủ của chúng tôi. Hãy cất giữ nó cẩn thận để ký đơn hàng.
                </p>
            </div>

            <div class="revoke-section">
                <h3 style="display: flex; align-items: center; gap: 8px; color: var(--error-text);">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                    Báo Mất Khóa / Lộ Khóa
                </h3>
                <p style="font-size: 14px; margin-bottom: 15px;">
                    Nếu bạn nghi ngờ khóa bí mật (Private Key) đã bị lộ hoặc bị mất, vui lòng báo mất ngay lập tức. Hệ thống sẽ vô hiệu hóa khóa này từ thời điểm bạn chọn và tự động tạo khóa mới cho bạn.
                </p>
                <form action="${pageContext.request.contextPath}/customer/keys" method="post" class="form-inline">
                    <input type="hidden" name="action" value="revoke">
                    <div style="margin-bottom: 15px;">
                        <label for="revokeTime" style="font-weight: 500; font-size: 14px; display: block; margin-bottom: 5px;">Thời gian lộ khóa (Nếu để trống sẽ mặc định là hiện tại):</label>
                        <input type="datetime-local" id="revokeTime" name="revokeTime" class="form-control" style="padding: 8px; width: 100%; max-width: 300px; border: 1px solid #ced4da; border-radius: 4px;">
                    </div>
                    <button type="submit" class="btn btn-primary" style="background-color: #dc3545; border-color: #dc3545;" onclick="return confirm('Bạn có chắc chắn muốn vô hiệu hóa khóa này không? Khóa mới sẽ tự động được tải về.');">
                        Báo mất & Tạo khóa mới
                    </button>
                </form>
            </div>
        <%
            } else {
        %>
            <div class="empty-state" style="text-align: center; padding: 2rem 0;">
                <h2>Bạn chưa tạo khóa chữ ký</h2>
                <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">
                    Để đặt hàng, bạn cần tạo một cặp khóa. Khóa công khai sẽ lưu trên web và khóa bí mật sẽ do bạn tự quản lý để ký giao dịch.
                </p>
                <form action="${pageContext.request.contextPath}/customer/keys" method="post">
                    <input type="hidden" name="action" value="generate">
                    <button type="submit" class="btn btn-primary">Tạo cặp khóa mới</button>
                </form>
            </div>
        <%
            }
        %>

        <%
            if (userKeys != null && userKeys.getKeyHistory() != null && !userKeys.getKeyHistory().isEmpty()) {
        %>
            <h3 style="margin-top: 3rem;">Lịch sử khóa của bạn</h3>
            <table class="key-history-table">
                <thead>
                    <tr>
                        <th>Key ID</th>
                        <th>Ngày tạo</th>
                        <th>Trạng thái</th>
                        <th>Ngày thu hồi</th>
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
                                    <%= isRevoked ? "Bị thu hồi" : "Hoạt động" %>
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
