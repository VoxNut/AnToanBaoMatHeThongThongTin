package com.beveragestore.servlet;

import java.io.IOException;
import java.util.List;
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

public class AdminUserServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserServlet.class);
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!SessionUtil.isAdmin(request.getSession())) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            List<User> users = userDAO.getAllUsers();
            request.setAttribute("users", users);

            request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error loading users", e);
            request.setAttribute("error", "Error loading users. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!SessionUtil.isAdmin(request.getSession())) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            String action = request.getParameter("action");
            String uid = request.getParameter("uid");

            if (uid == null || uid.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/admin/users?error=invalid_user");
                return;
            }

            User user = userDAO.findByUid(uid);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/admin/users?error=user_not_found");
                return;
            }

            if ("update_role".equals(action)) {
                String newRole = request.getParameter("role");
                if (newRole != null && !newRole.trim().isEmpty()) {
                    user.setRole(newRole);
                    userDAO.updateUser(user);
                    logger.info("Admin updated role for user {} to {}", user.getEmail(), newRole);
                }
            } else if ("toggle_status".equals(action)) {
                user.setActive(!user.isActive());
                userDAO.updateUser(user);
                logger.info("Admin toggled active status for user {} to {}", user.getEmail(), user.isActive());
            }

            response.sendRedirect(request.getContextPath() + "/admin/users?success=User updated successfully");

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error updating user", e);
            response.sendRedirect(request.getContextPath() + "/admin/users?error=Failed to update user");
        }
    }
}
