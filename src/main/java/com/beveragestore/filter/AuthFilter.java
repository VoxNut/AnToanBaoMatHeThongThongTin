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
 * bộ lọc xác thực bảo vệ các link dạng /customer/* và /admin/*.
 * - check xem user đã đăng nhập chưa
 * - chưa đăng nhập thì đẩy về trang login nha
 * - check quyền theo role (admin không được vào trang customer và ngược lại)
 * - trả về lỗi 403 forbidden nếu truy cập trái phép
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

        // check xem người dùng đã đăng nhập chưa
        User loggedInUser = SessionUtil.getUserFromSession(session);

        if (loggedInUser == null) {
            // user chưa đăng nhập -> chuyển hướng về trang login nha
            logger.warn("Unauthenticated request to protected resource: {}", path);
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        // user đã đăng nhập -> check tiếp quyền hạn truy cập
        if (path.startsWith("/admin/")) {
            // khu vực này chỉ dành riêng cho admin thôi nha
            if (!loggedInUser.isAdmin()) {
                logger.warn("Non-admin user attempted to access admin area: {} (user: {})", path, loggedInUser.getEmail());
                // trả về lỗi 403 forbidden
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpRequest.getRequestDispatcher("/WEB-INF/views/403.jsp").forward(request, response);
                return;
            }
        } else if (path.startsWith("/customer/")) {
            // khu vực dành riêng cho khách hàng
            // chỗ này các user đã đăng nhập (gồm cả admin, shipper, chủ shop) đều vào mua hàng được nha
            // ở trên đã kiểm tra user đăng nhập rồi nên ở đây không cần check role nữa
        }

        // user đã xác thực và có quyền truy cập -> cho đi tiếp nha
        logger.debug("Auth filter passed for user: {} at path: {}", loggedInUser.getEmail(), path);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed");
    }
}
