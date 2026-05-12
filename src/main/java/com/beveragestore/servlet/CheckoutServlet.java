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
 * Servlet for checkout and order placement.
 * Uses Firestore Transactions to atomically:
 * 1. Decrement product stock
 * 2. Create order
 * 3. Clear cart
 * 
 * GET: Show checkout form
 * POST: Process order with transaction
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

            // Get cart
            List<CartItem> cartItems = cartDAO.getCartItems(userId);
            double cartTotal = cartDAO.getCartTotal(userId);

            if (cartItems.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/customer/cart");
                return;
            }

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
            String privateKeyPem = request.getParameter("privateKey");

            if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
                request.setAttribute("error", "Shipping address is required");
                doGet(request, response);
                return;
            }

            if (privateKeyPem == null || privateKeyPem.trim().isEmpty()) {
                request.setAttribute("error", "Private Key is required to sign the order");
                doGet(request, response);
                return;
            }

            // Get cart items
            List<CartItem> cartItems = cartDAO.getCartItems(userId);

            if (cartItems.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/customer/cart");
                return;
            }

            double cartTotal = cartDAO.getCartTotal(userId);

            // Use Firestore Transaction to atomically:
            // 1. Check stock availability
            // 2. Decrement stock for each product
            // 3. Create signed order
            // 4. Clear cart
            String orderId = UUID.randomUUID().toString();

            db.runTransaction(transaction -> {
                // Step 0: Get User Key Info
                DocumentSnapshot userSnapshot = transaction.get(db.collection("users").document(userId)).get();
                User dbUser = userSnapshot.toObject(User.class);
                if (dbUser == null || dbUser.getActivePublicKey() == null) {
                    throw new IllegalArgumentException("Bạn chưa tạo khóa chữ ký. Vui lòng vào trang Quản lý Khóa để tạo trước.");
                }

                // Step 1: Verify stock for all items (perform all reads first)
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

                // Step 2: Decrement stock for all items (perform all writes after)
                for (CartItem item : cartItems) {
                    Product product = productMap.get(item.getProductId());
                    int newStock = product.getStock() - item.getQuantity();

                    transaction.update(
                            db.collection("products").document(item.getProductId()),
                            "stock", newStock,
                            "updatedAt", new Date()
                    );
                }

                // Step 3: Create order with order items
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

                // Cryptographic Hashing and Signing
                try {
                    String hash = com.beveragestore.util.CryptoUtil.calculateOrderHash(order);
                    java.security.PrivateKey privateKey = com.beveragestore.util.CryptoUtil.pemToPrivateKey(privateKeyPem);
                    String signature = com.beveragestore.util.CryptoUtil.sign(hash, privateKey);

                    // Verify against active public key
                    java.security.PublicKey publicKey = com.beveragestore.util.CryptoUtil.pemToPublicKey(dbUser.getActivePublicKey());
                    boolean isValid = com.beveragestore.util.CryptoUtil.verify(hash, signature, publicKey);
                    if (!isValid) {
                        throw new IllegalArgumentException("Khóa bí mật không khớp với khóa công khai đã đăng ký trên hệ thống.");
                    }

                    order.setSignature(signature);
                    order.setSignedHash(hash);
                    order.setPublicKeyId(dbUser.getActivePublicKeyId());
                    order.setSignatureStatus("VALID");

                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (Exception e) {
                    logger.error("Lỗi ký đơn hàng", e);
                    throw new IllegalArgumentException("Không thể ký đơn hàng. Vui lòng kiểm tra lại khóa bí mật.");
                }

                transaction.set(db.collection("orders").document(orderId), order);

                // Step 4: Clear cart items
                for (CartItem item : cartItems) {
                    transaction.delete(
                            db.collection("cart")
                                     .document(userId)
                                     .collection("items")
                                     .document(item.getProductId())
                    );
                }

                return null;

            }).get(); // Wait for transaction to complete

            logger.info("Order placed successfully: {} (userId={}, total={})", orderId, userId, cartTotal);

            // Redirect to order confirmation
            response.sendRedirect(request.getContextPath() + "/customer/order-confirmation?orderId=" + orderId);


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
