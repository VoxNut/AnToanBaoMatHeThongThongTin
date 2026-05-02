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
}
