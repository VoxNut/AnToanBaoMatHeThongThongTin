package com.beveragestore.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * KeyManagementServlet - trang quản lý khóa chữ ký số
 * Bản nháp: chưa tích hợp CryptoUtil đầy đủ
 */
public class KeyManagementServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        // TODO: lấy thông tin khóa hiện tại của user từ Firestore
        request.getRequestDispatcher("/WEB-INF/views/customer/key-management.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        // TODO: xử lý tạo khóa mới hoặc báo mất khóa
        if ("generate".equals(action)) {
            // TODO: call CryptoUtil.generateKeyPair()
        } else if ("revoke".equals(action)) {
            // TODO: cập nhật keyRevokedAt
        }
        response.sendRedirect(request.getContextPath() + "/customer/keys");
    }
}
