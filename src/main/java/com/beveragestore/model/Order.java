package com.beveragestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Order model representing a customer's order.
 * Stored in the "orders" Firestore collection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    // Order status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String orderId;         // Firestore document ID
    private String userId;          // Reference to user
    private List<OrderItem> items;  // List of items in this order
    private double totalAmount;
    private String status;          // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    private String shippingAddress;
    private Date createdAt;
    private Date updatedAt;
    private String notes;           // Optional notes/instructions

    // Signature verification fields
    private String signature;
    private String signedHash;
    private String publicKeyId;
    private String signatureStatus; // "VALID", "INVALID", "REVOKED_KEY"

    public int getTotalItems() {

        if (items == null) return 0;
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    /**
     * Nested class representing a single item within an order.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private double unitPrice;
        private int quantity;
        private String imageUrl;

        public double getSubtotal() {
            return unitPrice * quantity;
        }
    }
}
