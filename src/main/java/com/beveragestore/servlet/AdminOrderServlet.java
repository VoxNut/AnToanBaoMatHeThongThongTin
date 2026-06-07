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

import com.beveragestore.dao.OrderDAO;
import com.beveragestore.model.Order;
import com.beveragestore.model.User;
import com.beveragestore.dao.UserDAO;
import com.beveragestore.util.SessionUtil;


public class AdminOrderServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminOrderServlet.class);
    private OrderDAO orderDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        orderDAO = new OrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!SessionUtil.isAdmin(request.getSession())) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            // lấy toàn bộ danh sách đơn hàng
            List<Order> orders = orderDAO.getAllOrders();
            UserDAO userDAO = new UserDAO();

            // xác thực động cho từng đơn hàng
            for (Order order : orders) {
                if (order.getSignature() == null) {
                    order.setSignatureStatus("UNSIGNED");
                    continue;
                }

                try {
                    User buyer = userDAO.findByUid(order.getUserId());
                    if (buyer == null) {
                        order.setSignatureStatus("NO_USER");
                        continue;
                    }

                    // tìm khóa trong lịch sử đã dùng để ký đơn hàng này
                    User.PublicKeyRecord keyRecord = null;
                    if (buyer.getKeyHistory() != null) {
                        for (User.PublicKeyRecord rec : buyer.getKeyHistory()) {
                            if (order.getPublicKeyId() != null && order.getPublicKeyId().equals(rec.getKeyId())) {
                                keyRecord = rec;
                                break;
                            }
                        }
                    }

                    if (keyRecord == null) {
                        order.setSignatureStatus("NO_KEY_FOUND");
                    } else {
                        // check xem khóa này có bị hủy trước lúc tạo đơn hàng không nha
                        if (keyRecord.getRevokedAt() != null && keyRecord.getRevokedAt().before(order.getCreatedAt())) {
                            order.setSignatureStatus("REVOKED_KEY");
                        } else {
                            // tính mã băm hiện tại của đơn hàng để kiểm tra xem có bị sửa đổi gì không
                            String currentHash = com.beveragestore.util.CryptoUtil.calculateOrderHash(order);
                            java.security.PublicKey pubKey = com.beveragestore.util.CryptoUtil.pemToPublicKey(keyRecord.getPublicKeyPem());
                            boolean isValid = com.beveragestore.util.CryptoUtil.verify(currentHash, order.getSignature(), pubKey);
                            
                            if (isValid) {
                                order.setSignatureStatus("VALID");
                            } else {
                                order.setSignatureStatus("INVALID"); // đơn hàng đã bị thay đổi trái phép (tampered)!
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error verifying signature for order: " + order.getOrderId(), e);
                    order.setSignatureStatus("ERROR");
                }
            }

            // 1. Lấy các tham số lọc từ request
            String statusFilter = request.getParameter("statusFilter");
            String sigFilter = request.getParameter("sigFilter");
            String searchId = request.getParameter("searchId");

            if (statusFilter == null) statusFilter = "";
            if (sigFilter == null) sigFilter = "";
            if (searchId == null) searchId = "";

            statusFilter = statusFilter.trim();
            sigFilter = sigFilter.trim();
            searchId = searchId.trim();

            // 2. Lọc in-memory
            List<Order> filteredOrders = new java.util.ArrayList<>();
            for (Order order : orders) {
                boolean match = true;

                // Lọc theo status
                if (!statusFilter.isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                    if (!statusFilter.equalsIgnoreCase(order.getStatus())) {
                        match = false;
                    }
                }

                // Lọc theo chữ ký
                if (match && !sigFilter.isEmpty() && !"ALL".equalsIgnoreCase(sigFilter)) {
                    String status = order.getSignatureStatus();
                    if ("UNSIGNED".equalsIgnoreCase(sigFilter)) {
                        if (status != null && !"UNSIGNED".equalsIgnoreCase(status) 
                                && !"NO_KEY_FOUND".equalsIgnoreCase(status) 
                                && !"NO_USER".equalsIgnoreCase(status) 
                                && !"ERROR".equalsIgnoreCase(status)) {
                            match = false;
                        }
                    } else {
                        if (status == null || !sigFilter.equalsIgnoreCase(status)) {
                            match = false;
                        }
                    }
                }

                // Lọc theo searchId
                if (match && !searchId.isEmpty()) {
                    if (order.getOrderId() == null || !order.getOrderId().toLowerCase().contains(searchId.toLowerCase())) {
                        match = false;
                    }
                }

                if (match) {
                    filteredOrders.add(order);
                }
            }
            
            request.setAttribute("orders", filteredOrders);
            request.setAttribute("statusFilter", statusFilter);
            request.setAttribute("sigFilter", sigFilter);
            request.setAttribute("searchId", searchId);
            request.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(request, response);

        } catch (Exception e) {
            logger.error("Error loading orders", e);
            request.setAttribute("error", "Error loading orders. Please try again.");
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
            String orderId = request.getParameter("orderId");

            if ("update_status".equals(action) && orderId != null) {
                String newStatus = request.getParameter("status");
                if (newStatus != null && !newStatus.trim().isEmpty()) {
                    orderDAO.updateOrderStatus(orderId, newStatus);
                    logger.info("Admin updated order {} status to {}", orderId, newStatus);
                    response.sendRedirect(request.getContextPath() + "/admin/orders?success=Order status updated successfully");
                    return;
                }
            }

            response.sendRedirect(request.getContextPath() + "/admin/orders?error=Invalid action");

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error updating order", e);
            response.sendRedirect(request.getContextPath() + "/admin/orders?error=Failed to update order");
        }
    }
}
