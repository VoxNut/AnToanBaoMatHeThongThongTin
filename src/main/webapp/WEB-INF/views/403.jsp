<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>403 - Access Denied</title>
    
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=1.1">
</head>
<body>

<jsp:include page="/WEB-INF/views/partials/header.jsp" />

<div class="container error-page">
    <div class="error-content">
        <h1>403</h1>
        <h2>Access Denied</h2>
        <p>You don't have permission to access this resource. Please log in with appropriate credentials.</p>
        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">Sign In</a>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />
</body>
</html>
