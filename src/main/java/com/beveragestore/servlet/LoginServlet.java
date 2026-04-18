package com.beveragestore.servlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LoginServlet - bản đầu chỉ hiển thị form
 * TODO: tích hợp xác thực Firebase và BCrypt
 */
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: xác thực email/password với Firestore
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        if (email == null || password == null) {
            request.setAttribute("error", "Thiếu thông tin đăng nhập");
            doGet(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/store");
    }
}
