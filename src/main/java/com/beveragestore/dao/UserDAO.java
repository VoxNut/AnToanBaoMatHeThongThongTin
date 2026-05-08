package com.beveragestore.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.model.User;
import com.beveragestore.util.FirebaseInitializer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

/**
 * DAO for User entity.
 * Handles all database operations for users in Firestore.
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private static final String COLLECTION_NAME = "users";
    private final Firestore db;

    public UserDAO() {
        this.db = FirebaseInitializer.getInstance().getFirestore();
    }

    /**
     * Register a new user
     * Generates a UUID as the user ID
     */
    public User registerUser(String fullName, String email, String plainPassword) throws ExecutionException, InterruptedException {
        // Check if email already exists
        User existingUser = findByEmail(email);
        if (existingUser != null) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Hash password using BCrypt
        String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        // Generate user ID
        String uid = UUID.randomUUID().toString();

        // Create user object with local auth provider
        User user = User.builder()
                .uid(uid)
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordHash)
                .role("customer")
                .authProvider("local")
                .createdAt(new Date())
                .active(true)
                .build();

        // Save to Firestore
        db.collection(COLLECTION_NAME)
                .document(uid)
                .set(user)
                .get();

        logger.info("User registered successfully: {}", email);
        return user;
    }

    /**
     * Create a user from a fully constructed User object (used by DatabaseSeeder)
     */
    public void createUser(User user) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(user.getUid())
                .set(user)
                .get();
        logger.info("User created from object: {}", user.getEmail());
    }

    /**
     * Find or create a user from Google Sign-In.
     * If a user with the same email exists, link the Google account.
     * If no user exists, create a new one with Google provider.
     */
    public User findOrCreateGoogleUser(String firebaseUid, String email, String fullName, String photoUrl) throws ExecutionException, InterruptedException {
        // First, check if user already exists by email
        User existingUser = findByEmail(email);

        if (existingUser != null) {
            // User exists - update with Google info if needed
            if (!"google".equals(existingUser.getAuthProvider())) {
                existingUser.setAuthProvider("google");
            }
            if (photoUrl != null && !photoUrl.isEmpty()) {
                existingUser.setPhotoUrl(photoUrl);
            }
            if (fullName != null && !fullName.isEmpty() && 
                (existingUser.getFullName() == null || existingUser.getFullName().isEmpty())) {
                existingUser.setFullName(fullName);
            }
            // Update the user in Firestore
            updateUser(existingUser);
            logger.info("Existing user linked with Google: {}", email);
            return existingUser;
        }

        // Create new Google user using Firebase UID as document ID
        User newUser = User.builder()
                .uid(firebaseUid)
                .fullName(fullName)
                .email(email)
                .role("customer")
                .authProvider("google")
                .photoUrl(photoUrl)
                .createdAt(new Date())
                .active(true)
                .build();

        // Save to Firestore
        db.collection(COLLECTION_NAME)
                .document(firebaseUid)
                .set(newUser)
                .get();

        logger.info("New Google user created: {}", email);
        return newUser;
    }

    /**
     * Find user by email and verify password
     */
    public User findByEmailAndPassword(String email, String plainPassword) throws ExecutionException, InterruptedException {
        User user = findByEmail(email);

        if (user != null && BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
            return user;
        }

        return null;
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .get()
                .get();

        if (querySnapshot.isEmpty()) {
            return null;
        }

        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
        return doc.toObject(User.class);
    }

    /**
     * Find user by UID
     */
    public User findByUid(String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection(COLLECTION_NAME)
                .document(uid)
                .get()
                .get();

        if (doc.exists()) {
            return doc.toObject(User.class);
        }

        return null;
    }

    /**
     * Get all users (admin only)
     */
    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME).get().get();
        List<User> users = new ArrayList<>();

        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            users.add(doc.toObject(User.class));
        }

        return users;
    }

    /**
     * Update user information
     */
    public void updateUser(User user) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(user.getUid())
                .set(user)
                .get();

        logger.info("User updated: {}", user.getUid());
    }

    /**
     * Delete user (soft delete by setting active to false)
     */
    public void deactivateUser(String uid) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(uid)
                .update("active", false)
                .get();

        logger.info("User deactivated: {}", uid);
    }
}
