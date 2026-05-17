package com.beveragestore.servlet;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.dao.UserDAO;
import com.beveragestore.model.User;
import com.beveragestore.util.SessionUtil;

/**
 * servlet đăng ký tài khoản user mới.
 * tạo tài khoản mới với mật khẩu đã băm.
 * kiểm tra tính hợp lệ của dữ liệu đầu vào và check trùng email.
 */
public class RegisterServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RegisterServlet.class);
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // nếu user đã đăng nhập rồi thì chuyển về trang chủ luôn nha
        User loggedInUser = SessionUtil.getUserFromSession(request.getSession(false));
        if (loggedInUser != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // hiển thị form đăng ký
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // kiểm tra tính hợp lệ của dữ liệu đầu vào (validate)
        if (fullName == null || fullName.trim().isEmpty()) {
            request.setAttribute("error", "Full name is required");
            logger.warn("Registration attempt with missing full name");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Email is required");
            logger.warn("Registration attempt with missing email");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Password is required");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("error", "Password must be at least 6 characters long");
            logger.warn("Registration attempt with short password for email: {}", email);
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            logger.warn("Registration attempt with mismatched passwords for email: {}", email);
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        try {
            // đăng ký user mới
            User newUser = userDAO.registerUser(fullName, email, password);

            logger.info("New user registered successfully: {} ({})", email, fullName);

            // thiết lập thông báo thành công rồi đẩy về trang login
            request.setAttribute("success", "Registration successful! Please log in with your credentials.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            // email này đã tồn tại trong hệ thống rồi
            request.setAttribute("error", e.getMessage());
            logger.warn("Registration failed: {}", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Database error during registration", e);
            request.setAttribute("error", "An error occurred during registration. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}
