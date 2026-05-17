<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<footer style="margin-top: var(--spacing-xxl); padding: var(--spacing-xl) 0; border-top: 1px solid var(--border-color); text-align: center;">
    <div class="container">
        <p style="font-family: var(--font-heading); font-size: 24px; margin-bottom: 16px;">The Grindery</p>
        <p style="color: var(--text-secondary); font-size: 14px;"><fmt:message key="footer.tagline" /></p>
        <p style="color: var(--text-light); font-size: 12px; margin-top: 24px;">&copy; 2026 The Grindery. <fmt:message key="footer.rights" /></p>
    </div>
</footer>

<!-- file js script dùng chung cho cả web -->
<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
