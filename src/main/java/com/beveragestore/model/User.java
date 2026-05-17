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
 * model user đại diện cho khách hàng hoặc admin.
 * đối tượng này được lưu ở collection "users" trên firestore.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IgnoreExtraProperties
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uid;              // id document trên firestore
    private String fullName;
    private String email;
    private String passwordHash;    // mật khẩu đã được băm bằng bcrypt
    private String role;            // role của user, chỉ nhận "customer" hoặc "admin" nha
    private String authProvider;    // phương thức đăng nhập: "local" (tài khoản thường) hoặc "google" (qua bên thứ ba)
    private String photoUrl;        // url ảnh đại diện lấy từ tài khoản google
    private Date createdAt;
    private boolean active;

    // các trường thông tin dùng để quản lý khóa mật mã
    private String activePublicKey;
    private String activePublicKeyId;
    private Date keyRevokedAt;
    private List<PublicKeyRecord> keyHistory;

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_SHIPPER = "shipper";
    public static final String ROLE_SHOP_OWNER = "shop_owner";

    @Exclude
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(this.role);
    }

    @Exclude
    public boolean isCustomer() {
        return ROLE_CUSTOMER.equals(this.role);
    }

    @Exclude
    public boolean isShipper() {
        return ROLE_SHIPPER.equals(this.role);
    }

    @Exclude
    public boolean isShopOwner() {
        return ROLE_SHOP_OWNER.equals(this.role);
    }

    @Exclude
    public boolean isGoogleUser() {
        return "google".equals(this.authProvider);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @IgnoreExtraProperties
    public static class PublicKeyRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        private String keyId;
        private String publicKeyPem;
        private Date createdAt;
        private Date revokedAt;
    }
}

