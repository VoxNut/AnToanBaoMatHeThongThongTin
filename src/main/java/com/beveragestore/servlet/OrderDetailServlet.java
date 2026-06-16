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
import com.beveragestore.model.User;
import com.beveragestore.dao.UserDAO;


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

            UserDAO userDAO = new UserDAO();
            User buyer = userDAO.findByUid(userId);
            if (buyer != null) {
                com.beveragestore.util.CryptoUtil.verifyOrderSignature(order, buyer);
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

        if (orderId == null || orderId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/customer/orders");
            return;
        }

        if ("resign_order".equals(action)) {
            String privateKeyPem = request.getParameter("privateKey");
            String offlineSignature = request.getParameter("signature");

            try {
                Order order = orderDAO.getOrderById(orderId);
                if (order == null || !order.getUserId().equals(userId)) {
                    response.sendRedirect(request.getContextPath() + "/customer/orders");
                    return;
                }

                UserDAO userDAO = new UserDAO();
                User buyer = userDAO.findByUid(userId);

                if (buyer == null || buyer.getActivePublicKey() == null) {
                    request.getSession().setAttribute("error", "Bạn chưa tạo khóa chữ ký. Vui lòng vào trang Quản lý Khóa để tạo trước.");
                    response.sendRedirect(request.getContextPath() + "/customer/order-detail?id=" + orderId);
                    return;
                }

                String hash = com.beveragestore.util.CryptoUtil.calculateOrderHash(order);
                String signature = "";

                if (privateKeyPem != null && !privateKeyPem.trim().isEmpty()) {
                    java.security.PrivateKey privateKey = com.beveragestore.util.CryptoUtil.pemToPrivateKey(privateKeyPem);
                    signature = com.beveragestore.util.CryptoUtil.sign(hash, privateKey);
                } else if (offlineSignature != null && !offlineSignature.trim().isEmpty()) {
                    signature = offlineSignature.strip().replace("\r", "").replace("\n", "");
                } else {
                    request.getSession().setAttribute("error", "Vui lòng nhập khóa bí mật hoặc chữ ký ngoại tuyến.");
                    response.sendRedirect(request.getContextPath() + "/customer/order-detail?id=" + orderId);
                    return;
                }

                java.security.PublicKey publicKey = com.beveragestore.util.CryptoUtil.pemToPublicKey(buyer.getActivePublicKey());
                boolean isValid = com.beveragestore.util.CryptoUtil.verify(hash, signature, publicKey);
                if (!isValid) {
                    String rawOrderData = com.beveragestore.util.CryptoUtil.buildRawOrderString(order);
                    isValid = com.beveragestore.util.CryptoUtil.verifyPlain(rawOrderData, signature, publicKey);
                }

                if (!isValid) {
                    request.getSession().setAttribute("error", "Chữ ký không khớp với khóa công khai đã đăng ký trên hệ thống.");
                    response.sendRedirect(request.getContextPath() + "/customer/order-detail?id=" + orderId);
                    return;
                }

                order.setSignature(signature);
                order.setSignedHash(hash);
                order.setPublicKeyId(buyer.getActivePublicKeyId());
                order.setResignRequired(false);
                order.setResignMessage(null);
                
                orderDAO.updateOrder(order);

                request.getSession().setAttribute("success", "Ký lại đơn hàng thành công!");
            } catch (Exception e) {
                logger.error("Lỗi ký lại đơn hàng", e);
                request.getSession().setAttribute("error", "Không thể ký lại đơn hàng: " + e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/customer/order-detail?id=" + orderId);
            return;
        }

        if ("cancel_and_reorder".equals(action)) {
            try {
                Order order = orderDAO.getOrderById(orderId);
                if (order == null || !order.getUserId().equals(userId)) {
                    response.sendRedirect(request.getContextPath() + "/customer/orders");
                    return;
                }

                // 1. Thực hiện hủy đơn hàng (hoàn lại số lượng sản phẩm vào kho)
                cancelOrderTransaction(userId, orderId);

                // 2. Đưa các món trong đơn hàng bị hủy vào giỏ hàng
                com.beveragestore.dao.CartDAO cartDAO = new com.beveragestore.dao.CartDAO();
                if (order.getItems() != null) {
                    for (Order.OrderItem item : order.getItems()) {
                        com.beveragestore.model.CartItem cartItem = com.beveragestore.model.CartItem.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .addedAt(System.currentTimeMillis())
                                .build();
                        cartDAO.addOrUpdateCartItem(userId, cartItem);
                    }
                }

                request.getSession().setAttribute("success", "Đã hủy đơn hàng bị sửa đổi thành công và đưa các sản phẩm vào giỏ hàng. Vui lòng kiểm tra và sửa lại số lượng chính xác trước khi đặt hàng mới.");
                response.sendRedirect(request.getContextPath() + "/customer/cart");
                return;
            } catch (Exception e) {
                logger.error("Lỗi khi hủy và đặt lại đơn hàng", e);
                request.getSession().setAttribute("error", "Không thể hủy và đặt lại đơn hàng: " + e.getMessage());
                response.sendRedirect(request.getContextPath() + "/customer/order-detail?id=" + orderId);
                return;
            }
        }

        if (!"cancel".equals(action)) {
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

            // 1. Thực hiện toàn bộ lượt ĐỌC (Reads) trước
            java.util.Map<String, Integer> productNewStockMap = new java.util.HashMap<>();
            if (order.getItems() != null) {
                for (Order.OrderItem item : order.getItems()) {
                    DocumentSnapshot productSnapshot = transaction.get(db.collection("products").document(item.getProductId())).get();
                    Product product = productSnapshot.toObject(Product.class);
                    if (product != null) {
                        int newStock = product.getStock() + item.getQuantity();
                        productNewStockMap.put(item.getProductId(), newStock);
                    }
                }
            }

            // 2. Thực hiện toàn bộ lượt GHI (Writes) sau
            for (java.util.Map.Entry<String, Integer> entry : productNewStockMap.entrySet()) {
                transaction.update(
                        db.collection("products").document(entry.getKey()),
                        "stock", entry.getValue(),
                        "updatedAt", new Date()
                );
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
