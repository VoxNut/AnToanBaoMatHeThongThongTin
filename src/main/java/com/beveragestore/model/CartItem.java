package com.beveragestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * CartItem model representing a product in the user's cart.
 * Stored in the "cart" subcollection under each user's document.
 * Accessed via: db.collection("cart").document(userId).collection("items").document(productId)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;
    private long addedAt;           // Timestamp when added to cart

    /**
     * Calculate subtotal for this item (price * quantity)
     */
    public double getSubtotal() {
        return price * quantity;
    }
}
