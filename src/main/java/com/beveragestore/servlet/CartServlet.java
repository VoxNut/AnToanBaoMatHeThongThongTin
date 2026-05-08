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

import com.beveragestore.dao.CartDAO;
import com.beveragestore.dao.ProductDAO;
import com.beveragestore.model.CartItem;
import com.beveragestore.model.Product;
import com.beveragestore.util.SessionUtil;

/**
 * Servlet for shopping cart operations (customer only).
 * Handles multiple actions: add, update, remove, view
 * Action parameter determines the operation.
 */
public class CartServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(CartServlet.class);
    private CartDAO cartDAO;
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        cartDAO = new CartDAO();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String userId = SessionUtil.getUserId(request.getSession());

            if (userId == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // Get cart items
            List<CartItem> cartItems = cartDAO.getCartItems(userId);
            double cartTotal = cartDAO.getCartTotal(userId);

            request.setAttribute("cartItems", cartItems);
            request.setAttribute("cartTotal", cartTotal);
            request.setAttribute("itemCount", cartItems.size());

            request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error retrieving cart", e);
            request.setAttribute("error", "Error loading cart. Please try again.");
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

            String action = request.getParameter("action");

            if ("add".equals(action)) {
                handleAddToCart(request, response, userId);
            } else if ("update".equals(action)) {
                handleUpdateCart(request, response, userId);
            } else if ("remove".equals(action)) {
                handleRemoveFromCart(request, response, userId);
            } else if ("clear".equals(action)) {
                handleClearCart(request, response, userId);
            } else {
                response.sendRedirect(request.getContextPath() + "/customer/cart");
            }

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error processing cart action", e);
            request.setAttribute("error", "Error processing cart. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response, String userId)
            throws ExecutionException, InterruptedException, IOException {
        String productId = request.getParameter("productId");
        String quantityStr = request.getParameter("quantity");

        if (productId == null || quantityStr == null) {
            response.sendRedirect(request.getContextPath() + "/customer/cart");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);

            if (quantity < 1) {
                quantity = 1;
            }

            // Get product details
            Product product = productDAO.getProductById(productId);

            if (product == null || !product.isActive()) {
                logger.warn("Attempt to add inactive/non-existent product to cart: {}", productId);
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }

            // Check stock
            if (product.getStock() < quantity) {
                logger.warn("Insufficient stock for product: {}", productId);
                response.sendRedirect(request.getContextPath() + "/product?id=" + productId + "&error=insufficient_stock");
                return;
            }

            // Get existing cart item or create new one
            CartItem existingItem = cartDAO.getCartItem(userId, productId);

            if (existingItem != null) {
                // Update quantity
                int newQuantity = existingItem.getQuantity() + quantity;
                if (newQuantity > product.getStock()) {
                    newQuantity = product.getStock();
                }
                cartDAO.updateCartItemQuantity(userId, productId, newQuantity);
            } else {
                // Add new item
                CartItem cartItem = CartItem.builder()
                        .productId(productId)
                        .name(product.getName())
                        .price(product.getPrice())
                        .quantity(quantity)
                        .imageUrl(product.getImageUrl())
                        .addedAt(System.currentTimeMillis())
                        .build();
                cartDAO.addOrUpdateCartItem(userId, cartItem);
            }

            logger.info("Product added to cart: userId={}, productId={}, quantity={}", userId, productId, quantity);
            response.sendRedirect(request.getContextPath() + "/customer/cart");

        } catch (NumberFormatException e) {
            logger.warn("Invalid quantity format: {}", quantityStr);
            response.sendRedirect(request.getContextPath() + "/customer/cart");
        }
    }

    private void handleUpdateCart(HttpServletRequest request, HttpServletResponse response, String userId)
            throws ExecutionException, InterruptedException, IOException {
        String productId = request.getParameter("productId");
        String quantityStr = request.getParameter("quantity");

        if (productId == null || quantityStr == null) {
            response.sendRedirect(request.getContextPath() + "/customer/cart");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);

            if (quantity < 1) {
                cartDAO.removeCartItem(userId, productId);
            } else {
                cartDAO.updateCartItemQuantity(userId, productId, quantity);
            }

            logger.info("Cart item updated: userId={}, productId={}, newQuantity={}", userId, productId, quantity);
            response.sendRedirect(request.getContextPath() + "/customer/cart");

        } catch (NumberFormatException e) {
            logger.warn("Invalid quantity format: {}", quantityStr);
            response.sendRedirect(request.getContextPath() + "/customer/cart");
        }
    }

    private void handleRemoveFromCart(HttpServletRequest request, HttpServletResponse response, String userId)
            throws ExecutionException, InterruptedException, IOException {
        String productId = request.getParameter("productId");

        if (productId == null) {
            response.sendRedirect(request.getContextPath() + "/customer/cart");
            return;
        }

        cartDAO.removeCartItem(userId, productId);
        logger.info("Item removed from cart: userId={}, productId={}", userId, productId);
        response.sendRedirect(request.getContextPath() + "/customer/cart");
    }

    private void handleClearCart(HttpServletRequest request, HttpServletResponse response, String userId)
            throws ExecutionException, InterruptedException, IOException {
        cartDAO.clearCart(userId);
        logger.info("Cart cleared for user: {}", userId);
        response.sendRedirect(request.getContextPath() + "/customer/cart");
    }
}
