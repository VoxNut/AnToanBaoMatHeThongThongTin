package com.beveragestore.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.dao.CartDAO;
import com.beveragestore.dao.OrderDAO;
import com.beveragestore.dao.ProductDAO;
import com.beveragestore.model.CartItem;
import com.beveragestore.model.Order;
import com.beveragestore.model.Product;
import com.beveragestore.model.User;

import com.beveragestore.util.FirebaseInitializer;
import com.beveragestore.util.SessionUtil;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

/**
 * servlet xử lý thanh toán và đặt đơn hàng.
 * dùng firestore transaction để chạy đồng thời các bước:
 * 1. trừ số lượng sản phẩm trong kho
 * 2. tạo đơn hàng mới
 * 3. xóa sạch giỏ hàng
 * 
 * GET: hiển thị form thanh toán
 * POST: xử lý đặt hàng bằng transaction
 */
public class CheckoutServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutServlet.class);
    private CartDAO cartDAO;
    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private Firestore db;

    @Override
    public void init() throws ServletException {
        super.init();
        cartDAO = new CartDAO();
        orderDAO = new OrderDAO();
        productDAO = new ProductDAO();
        db = FirebaseInitializer.getInstance().getFirestore();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String userId = SessionUtil.getUserId(request.getSession());

            if (userId == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // lấy thông tin giỏ hàng
            List<CartItem> cartItems = cartDAO.getCartItems(userId);
            double cartTotal = cartDAO.getCartTotal(userId);

            if (cartItems.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/customer/cart");
                return;
            }

            String orderId = UUID.randomUUID().toString();
            request.setAttribute("orderId", orderId);
            request.setAttribute("cartItems", cartItems);
            request.setAttribute("cartTotal", cartTotal);

            request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);


        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error loading checkout page", e);
            request.setAttribute("error", "Error loading checkout. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String userId = SessionUtil.getUserId(request.getSession());

            if (userId == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            String shippingAddress = request.getParameter("shippingAddress");
            String notes = request.getParameter("notes");
            String signMethod = request.getParameter("signMethod");
            String privateKeyPem = request.getParameter("privateKey");
            String offlineSignature = request.getParameter("signature");
            String orderId = request.getParameter("orderId");

            if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
                request.setAttribute("error", "Shipping address is required");
                doGet(request, response);
                return;
            }

            if (orderId == null || orderId.trim().isEmpty()) {
                request.setAttribute("error", "Order ID is invalid");
                doGet(request, response);
                return;
            }

            if ("offline".equals(signMethod)) {
                if (offlineSignature == null || offlineSignature.trim().isEmpty()) {
                    request.setAttribute("error", "Signature is required for offline signing");
                    doGet(request, response);
                    return;
                }
            } else {
                if (privateKeyPem == null || privateKeyPem.trim().isEmpty()) {
                    request.setAttribute("error", "Private Key is required to sign the order");
                    doGet(request, response);
                    return;
                }
            }


            // lấy các món trong giỏ hàng
            List<CartItem> cartItems = cartDAO.getCartItems(userId);

            if (cartItems.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/customer/cart");
                return;
            }

            double cartTotal = cartDAO.getCartTotal(userId);

            // sử dụng firestore transaction để thực hiện các thao tác nguyên tử (atomic):
            // bước 1: kiểm tra xem trong kho có đủ hàng không
            // bước 2: giảm số lượng trong kho của mỗi sản phẩm
            // bước 3: tạo đơn hàng mới có chữ ký số xác thực
            // bước 4: xóa sạch giỏ hàng của user


            db.runTransaction(transaction -> {
                // bước 0: lấy thông tin khóa của user
                DocumentSnapshot userSnapshot = transaction.get(db.collection("users").document(userId)).get();
                User dbUser = userSnapshot.toObject(User.class);
                if (dbUser == null || dbUser.getActivePublicKey() == null) {
                    throw new IllegalArgumentException("Bạn chưa tạo khóa chữ ký. Vui lòng vào trang Quản lý Khóa để tạo trước.");
                }

                // bước 1: check hàng tồn kho cho tất cả các món (phải chạy hết các lệnh đọc trước nha)
                java.util.Map<String, Product> productMap = new java.util.HashMap<>();
                for (CartItem item : cartItems) {
                    DocumentSnapshot docSnapshot = transaction.get(db.collection("products").document(item.getProductId())).get();
                    Product product = docSnapshot.toObject(Product.class);

                    if (product == null || !product.isActive()) {
                        throw new IllegalArgumentException("Product no longer available: " + item.getProductId());
                    }

                    if (product.getStock() < item.getQuantity()) {
                        throw new IllegalArgumentException("Insufficient stock for: " + product.getName());
                    }
                    productMap.put(item.getProductId(), product);
                }

                // bước 2: trừ số lượng tồn kho của các món (chạy các lệnh ghi dữ liệu sau)
                for (CartItem item : cartItems) {
                    Product product = productMap.get(item.getProductId());
                    int newStock = product.getStock() - item.getQuantity();

                    transaction.update(
                            db.collection("products").document(item.getProductId()),
                            "stock", newStock,
                            "updatedAt", new Date()
                    );
                }

                // bước 3: tạo đơn hàng mới cùng chi tiết sản phẩm
                List<Order.OrderItem> orderItems = new ArrayList<>();
                for (CartItem item : cartItems) {
                    orderItems.add(Order.OrderItem.builder()
                            .productId(item.getProductId())
                            .productName(item.getName())
                            .unitPrice(item.getPrice())
                            .quantity(item.getQuantity())
                            .imageUrl(item.getImageUrl())
                            .build());
                }

                Order order = Order.builder()
                        .orderId(orderId)
                        .userId(userId)
                        .items(orderItems)
                        .totalAmount(cartTotal)
                        .shippingAddress(shippingAddress)
                        .notes(notes)
                        .status(Order.STATUS_PENDING)
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build();

                // băm dữ liệu mật mã và tạo chữ ký số
                try {
                    String hash = com.beveragestore.util.CryptoUtil.calculateOrderHash(order);
                    String signature = "";

                    if ("offline".equals(signMethod)) {
                        signature = offlineSignature.strip().replace("\r", "").replace("\n", "");
                    } else {
                        java.security.PrivateKey privateKey = com.beveragestore.util.CryptoUtil.pemToPrivateKey(privateKeyPem);
                        signature = com.beveragestore.util.CryptoUtil.sign(hash, privateKey);
                    }

                    // xác thực dựa trên public key đang kích hoạt
                    java.security.PublicKey publicKey = com.beveragestore.util.CryptoUtil.pemToPublicKey(dbUser.getActivePublicKey());
                    String rawOrderData = com.beveragestore.util.CryptoUtil.buildRawOrderString(order);
                    boolean isValid = com.beveragestore.util.CryptoUtil.verify(hash, signature, publicKey);
                    boolean isValidPlain = com.beveragestore.util.CryptoUtil.verifyPlain(rawOrderData, signature, publicKey);
                    
                    if (!isValid && !isValidPlain) {
                        throw new IllegalArgumentException("Khóa hoặc chữ ký không khớp với khóa công khai đã đăng ký trên hệ thống.");
                    }
                    
                    // Nếu pass một trong hai
                    isValid = true;

                    order.setSignature(signature);
                    order.setSignedHash(hash);
                    order.setPublicKeyId(dbUser.getActivePublicKeyId());
                    order.setSignatureStatus("VALID");

                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (Exception e) {
                    logger.error("Lỗi ký đơn hàng", e);
                    throw new IllegalArgumentException("Không thể ký đơn hàng. Vui lòng kiểm tra lại khóa hoặc chữ ký.");
                }


                transaction.set(db.collection("orders").document(orderId), order);

                // bước 4: xóa sạch các món trong giỏ hàng
                for (CartItem item : cartItems) {
                    transaction.delete(
                            db.collection("cart")
                                     .document(userId)
                                     .collection("items")
                                     .document(item.getProductId())
                    );
                }

                return null;

            }).get(); // chờ cho transaction thực hiện xong

            logger.info("Order placed successfully: {} (userId={}, total={})", orderId, userId, cartTotal);

            // chuyển hướng sang trang xác nhận đơn hàng
            response.sendRedirect(request.getContextPath() + "/customer/order-confirmation?orderId=" + orderId);


        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.warn("Order validation failed: {}", cause.getMessage());
                request.setAttribute("error", cause.getMessage());
                doGet(request, response);
            } else {
                logger.error("Error processing order in transaction", e);
                request.setAttribute("error", "Error processing order. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Order validation failed: {}", e.getMessage());
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (Exception e) {
            logger.error("Error processing order", e);
            request.setAttribute("error", "Error processing order. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
