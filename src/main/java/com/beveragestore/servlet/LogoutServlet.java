package com.beveragestore.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.util.SessionUtil;

/**
 * servlet đăng xuất.
 * hủy session hiện tại rồi quay về trang chủ.
 */
public class LogoutServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            String email = SessionUtil.getUserFromSession(session) != null ? 
                    SessionUtil.getUserFromSession(session).getEmail() : "unknown";
            
            SessionUtil.clearUserSession(session);
            logger.info("User logged out: {}", email);
        }

        // chuyển hướng về trang chủ kèm theo thông báo đăng xuất
        response.sendRedirect(request.getContextPath() + "/?msg=logout");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
