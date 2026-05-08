<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.beveragestore.util.SessionUtil" %>
<%@ page import="com.beveragestore.model.User" %>

<%
    User currentUser = SessionUtil.getUserFromSession(request.getSession(false));
%>

<header class="site-header">
    <div class="container">
        <div class="brand-logo">
            <a href="${pageContext.request.contextPath}/">The Grindery</a>
        </div>
        
        <nav class="main-nav">
            <a href="${pageContext.request.contextPath}/"><fmt:message key="nav.home" /></a>
            <a href="${pageContext.request.contextPath}/products?category=Coffee"><fmt:message key="nav.coffee" /></a>
            <a href="${pageContext.request.contextPath}/products?category=Tea"><fmt:message key="nav.tea" /></a>
            <a href="${pageContext.request.contextPath}/products"><fmt:message key="nav.products" /></a>
        </nav>

        <div class="nav-actions">
            <div class="language-selector">
                <a href="${pageContext.request.contextPath}/language?lang=en" class="${sessionScope.lang == 'en' || sessionScope.lang == null ? 'active' : ''}">EN</a>
                <a href="${pageContext.request.contextPath}/language?lang=vi" class="${sessionScope.lang == 'vi' ? 'active' : ''}">VI</a>
                <a href="${pageContext.request.contextPath}/language?lang=ja" class="${sessionScope.lang == 'ja' ? 'active' : ''}">JA</a>
            </div>
            <a href="${pageContext.request.contextPath}/products" title="<fmt:message key="product.search" />">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
            </a>
            
            <a href="${pageContext.request.contextPath}/customer/cart" title="<fmt:message key="nav.cart" />">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>
            </a>

            <% if (currentUser != null) { %>
                <div class="user-menu">
                    <div class="avatar-container" onclick="toggleDropdown()">
                        <% if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) { %>
                            <img src="<%= currentUser.getPhotoUrl() %>" alt="User Avatar" class="user-avatar">
                        <% } else { %>
                            <img src="https://res.cloudinary.com/dbpl94opl/image/upload/v1777211644/ina_drinks_hievr6.jpg" alt="User Avatar Placeholder" class="user-avatar">
                        <% } %>
                    </div>
                    <div class="dropdown-menu" id="userDropdown">
                        <div class="dropdown-header">
                            <strong style="display:block; font-size: 14px;"><%= currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getEmail() %></strong>
                            <span style="display:block; font-size:12px; color:var(--text-secondary); text-transform:uppercase; margin-top:4px;"><%= currentUser.getRole() %></span>
                        </div>
                        <hr>
                        <% if (currentUser.isAdmin() || currentUser.isShopOwner() || currentUser.isShipper()) { %>
                            <a href="${pageContext.request.contextPath}/admin/dashboard">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 6px; vertical-align: text-bottom;"><rect x="3" y="3" width="7" height="9"></rect><rect x="14" y="3" width="7" height="5"></rect><rect x="14" y="12" width="7" height="9"></rect><rect x="3" y="16" width="7" height="5"></rect></svg>
                                <fmt:message key="nav.dashboard" />
                            </a>
                        <% } %>
                        <a href="${pageContext.request.contextPath}/customer/orders">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 6px; vertical-align: text-bottom;"><line x1="16.5" y1="9.4" x2="7.5" y2="4.21"></line><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>
                            <fmt:message key="nav.my_orders" />
                        </a>
                        <hr>
                        <a href="${pageContext.request.contextPath}/logout" style="color: var(--error-text);">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 6px; vertical-align: text-bottom;"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                            <fmt:message key="nav.logout" />
                        </a>
                    </div>
                </div>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/login" title="<fmt:message key="nav.login" />" style="margin-left: 10px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                </a>
            <% } %>
        </div>
    </div>
</header>

    <!-- Hidden elements to pass server messages to JS -->
<% if (request.getAttribute("error") != null) { %>
    <div id="server-error-msg" style="display:none;"><%= request.getAttribute("error") %></div>
<% } %>
<% if (request.getAttribute("success") != null) { %>
    <div id="server-success-msg" style="display:none;"><%= request.getAttribute("success") %></div>
<% } %>

<script>
function toggleDropdown() {
    document.getElementById("userDropdown").classList.toggle("show");
}

// Close the dropdown if the user clicks outside of it
window.onclick = function(event) {
    if (!event.target.matches('.user-avatar') && !event.target.matches('.default-avatar') && !event.target.closest('.avatar-container')) {
        var dropdowns = document.getElementsByClassName("dropdown-menu");
        for (var i = 0; i < dropdowns.length; i++) {
            var openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
    }
}
</script>
