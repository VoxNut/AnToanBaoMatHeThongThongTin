package com.beveragestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Product model representing a beverage product.
 * Stored in the "products" Firestore collection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;       // Firestore document ID
    private String name;
    private String category;        // e.g., "Water", "Soft Drinks", "Coffee", "Juice", "Tea", "Alcohol", "Energy Drinks"
    private String brand;
    private String description;
    private double price;
    private int stock;              // Available quantity
    private String imageUrl;        // URL to product image
    private boolean isActive;       // Soft delete
    private Date createdAt;
    private Date updatedAt;

    public boolean isLowStock() {
        return stock < 10;
    }
}
