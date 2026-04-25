<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <title>Quản lý đơn hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/header.jsp" %>
<div class="container">
    <h2>Danh sách đơn hàng</h2>
    <table class="table">
        <thead>
            <tr>
                <th>Mã đơn</th>
                <th>Khách hàng</th>
                <th>Ngày đặt</th>
                <th>Tổng tiền</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${orders}" var="o">
            <tr>
                <td>${o.orderId}</td>
                <td>${o.userId}</td>
                <td>${o.createdAt}</td>
                <td>${o.totalAmount}</td>
                <td>${o.status}</td>
                <td>
                    <a href="#" class="btn btn-sm btn-info">Xem</a>
                </td>
            </tr>
            </c:forEach>
        </tbody>
    </table>
    <!-- TODO: thêm cột chữ ký số sau khi tích hợp CryptoUtil -->
</div>
<%@ include file="/WEB-INF/views/partials/footer.jsp" %>
</body>
</html>
