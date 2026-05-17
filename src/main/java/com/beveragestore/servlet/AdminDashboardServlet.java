package com.beveragestore.servlet;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.dao.OrderDAO;
import com.beveragestore.dao.ProductDAO;
import com.beveragestore.util.SessionUtil;

/**
 * servlet trang tổng quan của admin.
 * hiển thị mấy số liệu thống kê với tổng quan hệ thống.
 */
public class AdminDashboardServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardServlet.class);
    private OrderDAO orderDAO;
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        orderDAO = new OrderDAO();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // xác thực user có phải admin không
            if (!SessionUtil.isAdmin(request.getSession())) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            // lấy số liệu thống kê cho trang dashboard
            long totalOrders = orderDAO.getTotalOrdersCount();
            double totalRevenue = orderDAO.getTotalRevenue();
            
            com.beveragestore.dao.UserDAO userDAO = new com.beveragestore.dao.UserDAO();
            int totalUsers = userDAO.getAllUsers().size();
            int lowStockCount = productDAO.getLowStockProducts().size();

            request.setAttribute("totalOrders", totalOrders);
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("lowStockCount", lowStockCount);

            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error loading admin dashboard", e);
            request.setAttribute("error", "Error loading dashboard. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
