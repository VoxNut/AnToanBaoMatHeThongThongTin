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
 * Servlet for user login.
 * Authenticates user credentials against Firestore.
 * On success, stores user in session and redirects based on role.
 * On failure, redirects to login with error message.
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
        // If user is already logged in, redirect to appropriate dashboard
        User loggedInUser = SessionUtil.getUserFromSession(request.getSession(false));
        if (loggedInUser != null) {
            if (loggedInUser.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }
            return;
        }

        // Show login form
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Validate input
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Email and password are required");
            logger.warn("Login attempt with missing credentials");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        try {
            // Authenticate user
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

            // Login successful - store user in session
            HttpSession session = request.getSession(true);
            SessionUtil.setUserInSession(session, user);

            logger.info("User logged in successfully: {} (role: {})", user.getEmail(), user.getRole());

            // Redirect based on role
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
