<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <title>Thanh toán - Online Beverage Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/header.jsp" %>
<div class="container">
    <h2>Thanh toán đơn hàng</h2>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/checkout">
        <div class="form-group">
            <label for="fullname">Họ và tên người nhận:</label>
            <input type="text" id="fullname" name="fullname" class="form-control" required>
        </div>
        <div class="form-group">
            <label for="address">Địa chỉ giao hàng:</label>
            <input type="text" id="address" name="address" class="form-control" required>
        </div>
        <div class="form-group">
            <label for="phone">Số điện thoại:</label>
            <input type="text" id="phone" name="phone" class="form-control" required>
        </div>
        <!-- TODO: thêm form ký số sau khi tích hợp CryptoUtil -->
        <button type="submit" class="btn btn-primary">Đặt hàng</button>
    </form>
</div>
<%@ include file="/WEB-INF/views/partials/footer.jsp" %>
</body>
</html>
