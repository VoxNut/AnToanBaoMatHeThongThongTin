package com.beveragestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * model order đại diện cho đơn hàng của khách.
 * được lưu trong collection "orders" trên firestore.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IgnoreExtraProperties
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    // các hằng số định nghĩa trạng thái đơn hàng
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String orderId;         // id document trên firestore
    private String userId;          // liên kết (reference) tới user
    private List<OrderItem> items;  // danh sách các món trong đơn hàng này
    private double totalAmount;
    private String status;          // các trạng thái đơn hàng: pending, processing, shipped, delivered, cancelled
    private String shippingAddress;
    private Date createdAt;
    private Date updatedAt;
    private String notes;           // ghi chú tùy chọn của khách hàng

    // các trường phục vụ cho việc xác minh chữ ký số
    private String signature;
    private String signedHash;
    private String publicKeyId;
    private String signatureStatus; // "VALID", "INVALID", "REVOKED_KEY" (các trạng thái xác thực của chữ ký)

    @Exclude
    public int getTotalItems() {

        if (items == null) return 0;
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    /**
     * class lồng đại diện cho một chi tiết món hàng trong đơn.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @IgnoreExtraProperties
    public static class OrderItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private double unitPrice;
        private int quantity;
        private String imageUrl;

        @Exclude
        public double getSubtotal() {
            return unitPrice * quantity;
        }
    }
}
