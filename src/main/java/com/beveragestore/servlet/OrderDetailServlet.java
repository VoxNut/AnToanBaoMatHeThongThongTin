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
import com.beveragestore.model.Order;
import com.beveragestore.util.SessionUtil;

/**
 * Servlet for viewing order details.
 * Shows full details of a specific order.
 */
public class OrderDetailServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailServlet.class);
    private OrderDAO orderDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        orderDAO = new OrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String userId = SessionUtil.getUserId(request.getSession());

            if (userId == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            String orderId = request.getParameter("id");

            if (orderId == null || orderId.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/customer/orders");
                return;
            }

            Order order = orderDAO.getOrderById(orderId);

            if (order == null || !order.getUserId().equals(userId)) {
                logger.warn("Unauthorized access to order: {} by user: {}", orderId, userId);
                response.sendRedirect(request.getContextPath() + "/customer/orders");
                return;
            }

            request.setAttribute("order", order);
            request.getRequestDispatcher("/WEB-INF/views/customer/order-detail.jsp").forward(request, response);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error retrieving order details", e);
            request.setAttribute("error", "Error loading order. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
