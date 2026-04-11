package com.beveragestore.servlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RegisterServlet - bản đầu chưa hash password
 * TODO: tích hợp BCrypt password hashing
 */
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email    = request.getParameter("email");
        String password = request.getParameter("password");
        String fullname = request.getParameter("fullname");
        // TODO: hash password, lưu Firestore, kiểm tra email tồn tại
        if (email == null || password == null || fullname == null) {
            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin");
            doGet(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
