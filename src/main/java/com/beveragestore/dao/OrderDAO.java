package com.beveragestore.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.model.Order;
import com.beveragestore.util.FirebaseInitializer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

/**
 * DAO for Order entity.
 * Handles all database operations for orders in Firestore.
 */
public class OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAO.class);
    private static final String COLLECTION_NAME = "orders";
    private final Firestore db;
    
    public OrderDAO() {
        this.db = FirebaseInitializer.getInstance().getFirestore();
    }

    /**
     * Create a new order
     */
    public void createOrder(Order order) throws ExecutionException, InterruptedException {
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        db.collection(COLLECTION_NAME)
                .document(order.getOrderId())
                .set(order)
                .get();

        logger.info("Order created: {} (userId={})", order.getOrderId(), order.getUserId());
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(String orderId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection(COLLECTION_NAME)
                .document(orderId)
                .get()
                .get();

        if (doc.exists()) {
            return doc.toObject(Order.class);
        }

        return null;
    }

    /**
     * Get all orders for a specific user
     */
    public List<Order> getOrdersByUserId(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .get()
                .get();

        List<Order> orders = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            orders.add(doc.toObject(Order.class));
        }

        return orders;
    }

    /**
     * Get all orders (admin view)
     */
    public List<Order> getAllOrders() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .get()
                .get();

        List<Order> orders = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            orders.add(doc.toObject(Order.class));
        }

        return orders;
    }

    /**
     * Get orders filtered by status
     */
    public List<Order> getOrdersByStatus(String status) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", status)
                .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .get()
                .get();

        List<Order> orders = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            orders.add(doc.toObject(Order.class));
        }

        return orders;
    }

    /**
     * Get orders by user and status
     */
    public List<Order> getOrdersByUserIdAndStatus(String userId, String status) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", status)
                .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .get()
                .get();

        List<Order> orders = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            orders.add(doc.toObject(Order.class));
        }

        return orders;
    }

    /**
     * Update order status
     */
    public void updateOrderStatus(String orderId, String newStatus) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(orderId)
                .update("status", newStatus, "updatedAt", new Date())
                .get();

        logger.info("Order status updated: {} -> {}", orderId, newStatus);
    }

    /**
     * Update entire order
     */
    public void updateOrder(Order order) throws ExecutionException, InterruptedException {
        order.setUpdatedAt(new Date());

        db.collection(COLLECTION_NAME)
                .document(order.getOrderId())
                .set(order)
                .get();

        logger.info("Order updated: {}", order.getOrderId());
    }

    /**
     * Get total orders count
     */
    public long getTotalOrdersCount() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME).get().get();
        return querySnapshot.size();
    }

    /**
     * Get total revenue
     */
    public double getTotalRevenue() throws ExecutionException, InterruptedException {
        List<Order> orders = getAllOrders();
        double total = 0;

        for (Order order : orders) {
            if (!order.getStatus().equals(Order.STATUS_CANCELLED)) {
                total += order.getTotalAmount();
            }
        }

        return total;
    }

    /**
     * Get orders created today
     */
    public List<Order> getOrdersCreatedToday() throws ExecutionException, InterruptedException {
        Date today = new Date();
        long todayStart = (today.getTime() / (1000 * 60 * 60 * 24)) * (1000 * 60 * 60 * 24);
        long todayEnd = todayStart + (1000 * 60 * 60 * 24);

        Date startDate = new Date(todayStart);
        Date endDate = new Date(todayEnd);

        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThan("createdAt", endDate)
                .get()
                .get();

        List<Order> orders = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            orders.add(doc.toObject(Order.class));
        }

        return orders;
    }
}
