package com.beveragestore.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.dao.OrderDAO;
import com.beveragestore.model.Order;
import com.beveragestore.model.Product;
import com.beveragestore.util.FirebaseInitializer;
import com.beveragestore.util.SessionUtil;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

/**
 * servlet xem chi tiết đơn hàng.
 * hiển thị thông tin đầy đủ của một đơn hàng cụ thể.
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

            // Đọc và xóa flash messages từ session
            String success = (String) request.getSession().getAttribute("success");
            String error = (String) request.getSession().getAttribute("error");
            if (success != null) {
                request.setAttribute("success", success);
                request.getSession().removeAttribute("success");
            }
            if (error != null) {
                request.setAttribute("error", error);
                request.getSession().removeAttribute("error");
            }

            request.setAttribute("order", order);
            request.getRequestDispatcher("/WEB-INF/views/customer/order-detail.jsp").forward(request, response);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error retrieving order details", e);
            request.setAttribute("error", "Error loading order. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = SessionUtil.getUserId(request.getSession());

        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String orderId = request.getParameter("id");
        String action = request.getParameter("action");

        if (orderId == null || orderId.trim().isEmpty() || !"cancel".equals(action)) {
            response.sendRedirect(request.getContextPath() + "/customer/orders");
            return;
        }

        try {
            cancelOrderTransaction(userId, orderId);
            request.getSession().setAttribute("success", "Hủy đơn hàng thành công!");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.warn("Cancel order validation failed: {}", cause.getMessage());
                request.getSession().setAttribute("error", cause.getMessage());
            } else {
                logger.error("Error cancelling order", e);
                request.getSession().setAttribute("error", "Có lỗi xảy ra khi hủy đơn hàng. Vui lòng thử lại.");
            }
        } catch (InterruptedException e) {
            logger.error("Error cancelling order", e);
            Thread.currentThread().interrupt();
            request.getSession().setAttribute("error", "Yêu cầu bị gián đoạn. Vui lòng thử lại.");
        }

        response.sendRedirect(request.getContextPath() + "/customer/order-detail?id=" + orderId);
    }

    private void cancelOrderTransaction(String userId, String orderId) throws ExecutionException, InterruptedException {
        Firestore db = FirebaseInitializer.getInstance().getFirestore();
        db.runTransaction(transaction -> {
            DocumentSnapshot orderSnapshot = transaction.get(db.collection("orders").document(orderId)).get();
            Order order = orderSnapshot.toObject(Order.class);

            if (order == null) {
                throw new IllegalArgumentException("Đơn hàng không tồn tại.");
            }

            if (!order.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Bạn không có quyền hủy đơn hàng này.");
            }

            if (!Order.STATUS_PENDING.equals(order.getStatus())) {
                throw new IllegalArgumentException("Chỉ đơn hàng ở trạng thái chờ duyệt (PENDING) mới có thể hủy.");
            }

            // Hoàn lại số lượng sản phẩm vào kho
            if (order.getItems() != null) {
                for (Order.OrderItem item : order.getItems()) {
                    DocumentSnapshot productSnapshot = transaction.get(db.collection("products").document(item.getProductId())).get();
                    Product product = productSnapshot.toObject(Product.class);
                    if (product != null) {
                        int newStock = product.getStock() + item.getQuantity();
                        transaction.update(
                                db.collection("products").document(item.getProductId()),
                                "stock", newStock,
                                "updatedAt", new Date()
                        );
                    }
                }
            }

            // Cập nhật trạng thái đơn hàng thành CANCELLED
            transaction.update(
                    db.collection("orders").document(orderId),
                    "status", Order.STATUS_CANCELLED,
                    "updatedAt", new Date()
            );

            return null;
        }).get();
    }
}
