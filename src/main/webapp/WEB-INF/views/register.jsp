<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="register.title" /> - The Grindery</title>
    
    <!-- nạp font chữ từ google -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&family=Playfair+Display:ital,wght@0,400;0,500;0,600;1,400&display=swap" rel="stylesheet">
    
    <!-- file css dùng chung cho cả web -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=1.1">
</head>
<body>

<jsp:include page="/WEB-INF/views/partials/header.jsp" />

<div class="container">
    <div class="auth-container">
        <div class="auth-header">
            <h1><fmt:message key="register.title" /></h1>
            <p style="color: var(--text-secondary);"><fmt:message key="register.subtitle" /></p>
        </div>

        <!-- nút đăng ký bằng tài khoản google -->
        <button type="button" id="googleSignUpBtn" class="btn-google" onclick="signUpWithGoogle()">
            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
            </svg>
            <span id="googleBtnText"><fmt:message key="register.google" /></span>
        </button>

        <div class="divider">
            <span><fmt:message key="register.divider" /></span>
        </div>

        <form method="POST" action="${pageContext.request.contextPath}/register">
            <div class="form-group">
                <label for="fullName" class="form-label"><fmt:message key="register.full_name" /></label>
                <input type="text" id="fullName" name="fullName" class="form-control" required placeholder="John Doe">
            </div>

            <div class="form-group">
                <label for="email" class="form-label"><fmt:message key="register.email" /></label>
                <input type="email" id="email" name="email" class="form-control" required placeholder="you@example.com">
            </div>

            <div class="form-group">
                <label for="password" class="form-label"><fmt:message key="register.password" /></label>
                <input type="password" id="password" name="password" class="form-control" required placeholder="••••••••" minlength="6">
                <small style="color: var(--text-light); font-size: 12px; margin-top: 4px; display: block;"><fmt:message key="register.password_hint" /></small>
            </div>

            <div class="form-group">
                <label for="confirmPassword" class="form-label"><fmt:message key="register.confirm_password" /></label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" required placeholder="••••••••">
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;"><fmt:message key="register.submit" /></button>
        </form>

        <div class="links">
            <fmt:message key="register.has_account" /> <a href="${pageContext.request.contextPath}/login"><fmt:message key="register.login_link" /></a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/partials/footer.jsp" />

<!-- firebase js sdk -->
<script src="https://www.gstatic.com/firebasejs/10.12.0/firebase-app-compat.js"></script>
<script src="https://www.gstatic.com/firebasejs/10.12.0/firebase-auth-compat.js"></script>

<script>
    const firebaseConfig = {
        apiKey: "AIzaSyDTnkGdaUarDWFLt6De6KkuSuxc9zjOJHk",
        authDomain: "online-beverage-store.firebaseapp.com",
        projectId: "online-beverage-store"
    };

    if (!firebase.apps.length) {
        firebase.initializeApp(firebaseConfig);
    }

    function signUpWithGoogle() {
        const btn = document.getElementById('googleSignUpBtn');
        const btnText = document.getElementById('googleBtnText');
        
        btn.disabled = true;
        btnText.textContent = 'Signing up...';

        const provider = new firebase.auth.GoogleAuthProvider();

        firebase.auth().signInWithPopup(provider)
            .then(result => result.user.getIdToken())
            .then(idToken => fetch('${pageContext.request.contextPath}/google-auth', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ idToken: idToken })
            }))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    window.location.href = data.redirectUrl;
                } else {
                    throw new Error(data.error || 'Registration failed');
                }
            })
            .catch(error => {
                btn.disabled = false;
                btnText.textContent = '<fmt:message key="register.google" />';
                if (error.code !== 'auth/popup-closed-by-user') {
                    showAlert(error.message || 'Google Sign-Up failed.', 'error');
                }
            });
    }
</script>
