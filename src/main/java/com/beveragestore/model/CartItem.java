package com.beveragestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * model cartitem đại diện cho một sản phẩm trong giỏ hàng của user.
 * được lưu trong subcollection "cart" dưới document của từng user.
 * đường dẫn truy cập: db.collection("cart").document(userId).collection("items").document(productId)
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
    private long addedAt;           // thời gian thêm món vào giỏ hàng (timestamp)

    /**
     * tính tổng tiền món này (giá nhân với số lượng nha)
     */
    public double getSubtotal() {
        return price * quantity;
    }
}
