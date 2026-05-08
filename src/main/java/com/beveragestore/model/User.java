package com.beveragestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * User model representing a customer or admin in the system.
 * This object is stored in the "users" Firestore collection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uid;              // Firestore document ID
    private String fullName;
    private String email;
    private String passwordHash;    // BCrypt hashed password
    private String role;            // "customer" or "admin"
    private String authProvider;    // "local" or "google"
    private String photoUrl;        // Google profile picture URL
    private Date createdAt;
    private boolean active;

    // Cryptographic key management fields
    private String activePublicKey;
    private String activePublicKeyId;
    private Date keyRevokedAt;
    private List<PublicKeyRecord> keyHistory;

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_SHIPPER = "shipper";
    public static final String ROLE_SHOP_OWNER = "shop_owner";

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(this.role);
    }

    public boolean isCustomer() {
        return ROLE_CUSTOMER.equals(this.role);
    }

    public boolean isShipper() {
        return ROLE_SHIPPER.equals(this.role);
    }

    public boolean isShopOwner() {
        return ROLE_SHOP_OWNER.equals(this.role);
    }

    public boolean isGoogleUser() {
        return "google".equals(this.authProvider);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublicKeyRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        private String keyId;
        private String publicKeyPem;
        private Date createdAt;
        private Date revokedAt;
    }
}

