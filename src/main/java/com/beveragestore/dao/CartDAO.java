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
 * dao xử lý các thao tác giỏ hàng.
 * xử lý cart items được lưu trên firestore tại:
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
     * thêm món vào giỏ hàng của user
     * nếu món đó có sẵn rồi thì cộng dồn số lượng nha
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
     * lấy toàn bộ món trong giỏ hàng của user
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
     * lấy một món cụ thể trong giỏ hàng
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
     * cập nhật số lượng của một món trong giỏ hàng
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
     * xóa món khỏi giỏ hàng
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
     * xóa sạch giỏ hàng (xóa mọi món)
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
     * tính tổng tiền của cả giỏ hàng
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
     * lấy số lượng món trong giỏ hàng
     */
    public int getCartItemCount(String userId) throws ExecutionException, InterruptedException {
        List<CartItem> items = getCartItems(userId);
        return items.size();
    }
}
