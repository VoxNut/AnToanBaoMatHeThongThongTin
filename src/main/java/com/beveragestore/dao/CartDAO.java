package com.beveragestore.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.model.CartItem;
import com.beveragestore.util.FirebaseInitializer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

/**
 * DAO for Cart operations.
 * Handles cart items stored in Firestore under:
 * db.collection("cart").document(userId).collection("items")
 */
public class CartDAO {
    private static final Logger logger = LoggerFactory.getLogger(CartDAO.class);
    private static final String CART_COLLECTION = "cart";
    private static final String ITEMS_SUBCOLLECTION = "items";
    private final Firestore db;

    public CartDAO() {
        this.db = FirebaseInitializer.getInstance().getFirestore();
    }

    /**
     * Add item to user's cart
     * If item already exists, update the quantity
     */
    public void addOrUpdateCartItem(String userId, CartItem cartItem) throws ExecutionException, InterruptedException {
        db.collection(CART_COLLECTION)
                .document(userId)
                .collection(ITEMS_SUBCOLLECTION)
                .document(cartItem.getProductId())
                .set(cartItem)
                .get();

        logger.debug("Cart item added/updated: userId={}, productId={}", userId, cartItem.getProductId());
    }

    /**
     * Get all items in user's cart
     */
    public List<CartItem> getCartItems(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(CART_COLLECTION)
                .document(userId)
                .collection(ITEMS_SUBCOLLECTION)
                .get()
                .get();

        List<CartItem> items = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            items.add(doc.toObject(CartItem.class));
        }

        logger.debug("Retrieved {} items from cart for user: {}", items.size(), userId);
        return items;
    }

    /**
     * Get single item from cart
     */
    public CartItem getCartItem(String userId, String productId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection(CART_COLLECTION)
                .document(userId)
                .collection(ITEMS_SUBCOLLECTION)
                .document(productId)
                .get()
                .get();

        if (doc.exists()) {
            return doc.toObject(CartItem.class);
        }

        return null;
    }

    /**
     * Update quantity of an item in cart
     */
    public void updateCartItemQuantity(String userId, String productId, int newQuantity) throws ExecutionException, InterruptedException {
        if (newQuantity <= 0) {
            removeCartItem(userId, productId);
        } else {
            db.collection(CART_COLLECTION)
                    .document(userId)
                    .collection(ITEMS_SUBCOLLECTION)
                    .document(productId)
                    .update("quantity", newQuantity)
                    .get();

            logger.debug("Updated cart item quantity: userId={}, productId={}, quantity={}", userId, productId, newQuantity);
        }
    }

    /**
     * Remove item from cart
     */
    public void removeCartItem(String userId, String productId) throws ExecutionException, InterruptedException {
        db.collection(CART_COLLECTION)
                .document(userId)
                .collection(ITEMS_SUBCOLLECTION)
                .document(productId)
                .delete()
                .get();

        logger.debug("Cart item removed: userId={}, productId={}", userId, productId);
    }

    /**
     * Clear entire cart (all items)
     */
    public void clearCart(String userId) throws ExecutionException, InterruptedException {
        List<CartItem> items = getCartItems(userId);

        for (CartItem item : items) {
            db.collection(CART_COLLECTION)
                    .document(userId)
                    .collection(ITEMS_SUBCOLLECTION)
                    .document(item.getProductId())
                    .delete()
                    .get();
        }

        logger.info("Cart cleared for user: {}", userId);
    }

    /**
     * Calculate cart total
     */
    public double getCartTotal(String userId) throws ExecutionException, InterruptedException {
        List<CartItem> items = getCartItems(userId);
        double total = 0;

        for (CartItem item : items) {
            total += item.getSubtotal();
        }

        return total;
    }

    /**
     * Get cart item count
     */
    public int getCartItemCount(String userId) throws ExecutionException, InterruptedException {
        List<CartItem> items = getCartItems(userId);
        return items.size();
    }
}
