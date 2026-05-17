package com.beveragestore.servlet;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.dao.UserDAO;
import com.beveragestore.model.User;
import com.beveragestore.util.SessionUtil;

/**
 * servlet đăng nhập hệ thống.
 * xác thực thông tin tài khoản user với firestore.
 * nếu thành công thì lưu user vào session rồi chuyển hướng dựa trên role.
 * nếu thất bại thì quay lại trang login kèm thông báo lỗi.
 */
public class LoginServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // nếu user đã đăng nhập rồi thì chuyển hướng về trang dashboard tương ứng luôn
        User loggedInUser = SessionUtil.getUserFromSession(request.getSession(false));
        if (loggedInUser != null) {
            if (loggedInUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }
            return;
        }

        // hiển thị form đăng nhập
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // kiểm tra tính hợp lệ của dữ liệu đầu vào (validate)
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Email and password are required");
            logger.warn("Login attempt with missing credentials");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        try {
            // xác thực tài khoản người dùng
            User user = userDAO.findByEmailAndPassword(email, password);

            if (user == null) {
                request.setAttribute("error", "Invalid email or password");
                logger.warn("Failed login attempt for email: {}", email);
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }

            if (!user.isActive()) {
                request.setAttribute("error", "Your account has been deactivated. Please contact support.");
                logger.warn("Login attempt on inactive account: {}", email);
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }

            // đăng nhập thành công -> lưu thông tin user vào session
            HttpSession session = request.getSession(true);
            SessionUtil.setUserInSession(session, user);

            logger.info("User logged in successfully: {} (role: {})", user.getEmail(), user.getRole());

            // chuyển hướng trang tương ứng với quyền (role)
            if (user.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard?msg=login");
            } else {
                response.sendRedirect(request.getContextPath() + "/?msg=login");
            }

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Database error during login", e);
            request.setAttribute("error", "An error occurred during login. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}
