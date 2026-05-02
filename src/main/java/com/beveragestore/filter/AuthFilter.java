package com.beveragestore.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.model.User;
import com.beveragestore.util.SessionUtil;

/**
 * Authentication Filter that protects /customer/* and /admin/* URL patterns.
 * 
 * - Checks if user is logged in
 * - Redirects unauthenticated users to login page
 * - Validates role-based access (admins can't access customer pages and vice versa)
 * - Returns 403 Forbidden for unauthorized access
 */
public class AuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        logger.debug("Auth filter processing: {}", path);

        // Check if user is logged in
        User loggedInUser = SessionUtil.getUserFromSession(session);

        if (loggedInUser == null) {
            // User not logged in - redirect to login
            logger.warn("Unauthenticated request to protected resource: {}", path);
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        // User is logged in - check role-based access
        if (path.startsWith("/admin/")) {
            // Admin-only area
            if (!loggedInUser.isAdmin()) {
                logger.warn("Non-admin user attempted to access admin area: {} (user: {})", path, loggedInUser.getEmail());
                // Return 403 Forbidden
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpRequest.getRequestDispatcher("/WEB-INF/views/403.jsp").forward(request, response);
                return;
            }
        } else if (path.startsWith("/customer/")) {
            // Customer area
            // Accessible by all authenticated users (including Admins, Shippers, Shop Owners) so they can make purchases
            // We already verified the user is authenticated above, so no further role check is needed here.
        }

        // User is authenticated and authorized - proceed
        logger.debug("Auth filter passed for user: {} at path: {}", loggedInUser.getEmail(), path);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed");
    }
}
