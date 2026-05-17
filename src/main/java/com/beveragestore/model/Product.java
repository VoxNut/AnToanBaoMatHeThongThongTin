package com.beveragestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.cloud.firestore.annotation.Exclude;
import java.io.Serializable;
import java.util.Date;

/**
 * model product đại diện cho sản phẩm đồ uống.
 * được lưu trong collection "products" trên firestore.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;       // id document trên firestore
    private String name;
    private String category;        // ví dụ: "Water", "Soft Drinks", "Coffee", "Juice", "Tea", "Alcohol", "Energy Drinks"
    private String brand;
    private String description;
    private double price;
    private int stock;              // số lượng sản phẩm còn lại trong kho
    private String imageUrl;        // đường dẫn (url) ảnh sản phẩm
    private boolean isActive;       // xóa mềm (soft delete)
    private Date createdAt;
    private Date updatedAt;

    @Exclude
    public boolean isLowStock() {
        return stock < 10;
    }
}
