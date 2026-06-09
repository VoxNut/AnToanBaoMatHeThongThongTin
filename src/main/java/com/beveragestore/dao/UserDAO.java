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
 * dao cho thực thể người dùng (user).
 * xử lý mọi thao tác dữ liệu liên quan đến user trên firestore.
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private static final String COLLECTION_NAME = "users";
    private final Firestore db;

    public UserDAO() {
        this.db = FirebaseInitializer.getInstance().getFirestore();
    }

    /**
     * đăng ký user mới
     * tự động sinh uuid làm id luôn
     */
    public User registerUser(String fullName, String email, String plainPassword) throws ExecutionException, InterruptedException {
        // check xem email này đã có ai đăng ký chưa nha
        User existingUser = findByEmail(email);
        if (existingUser != null) {
            throw new IllegalArgumentException("Email already registered");
        }

        // băm mật khẩu bằng bcrypt
        String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        // tự sinh id cho user
        String uid = UUID.randomUUID().toString();

        // tạo đối tượng user mới với phương thức xác thực local
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

        // lưu vào firestore
        db.collection(COLLECTION_NAME)
                .document(uid)
                .set(user)
                .get();

        logger.info("User registered successfully: {}", email);
        return user;
    }

    /**
     * tạo user mới từ object user đầy đủ (dùng trong databaseseeder)
     */
    public void createUser(User user) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(user.getUid())
                .set(user)
                .get();
        logger.info("User created from object: {}", user.getEmail());
    }

    /**
     * tìm hoặc tạo user mới từ google sign-in.
     * nếu đã có user trùng email thì liên kết với tài khoản google luôn.
     * nếu chưa có thì tạo user mới sử dụng google làm nhà cung cấp.
     */
    public User findOrCreateGoogleUser(String firebaseUid, String email, String fullName, String photoUrl) throws ExecutionException, InterruptedException {
        // đầu tiên là check xem user đã tồn tại qua email chưa nha
        User existingUser = findByEmail(email);

        if (existingUser != null) {
            // user đã tồn tại -> cập nhật thêm thông tin từ google nếu cần
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
            // cập nhật thông tin user trên firestore
            updateUser(existingUser);
            logger.info("Existing user linked with Google: {}", email);
            return existingUser;
        }

        // tạo user google mới lấy uid firebase làm id document
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

        // lưu vào firestore
        db.collection(COLLECTION_NAME)
                .document(firebaseUid)
                .set(newUser)
                .get();

        logger.info("New Google user created: {}", email);
        return newUser;
    }

    /**
     * tìm user theo email và check xem mật khẩu có đúng không
     */
    public User findByEmailAndPassword(String email, String plainPassword) throws ExecutionException, InterruptedException {
        User user = findByEmail(email);

        if (user != null && user.getPasswordHash() != null && !user.getPasswordHash().trim().isEmpty() && BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
            return user;
        }

        return null;
    }

    /**
     * tìm user theo email
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
     * tìm user theo uid
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
     * lấy toàn bộ danh sách user (chỉ dành cho admin)
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
     * cập nhật thông tin của user
     */
    public void updateUser(User user) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(user.getUid())
                .set(user)
                .get();

        logger.info("User updated: {}", user.getUid());
    }

    /**
     * xóa user (dùng xóa mềm bằng cách set active thành false)
     */
    public void deactivateUser(String uid) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(uid)
                .update("active", false)
                .get();

        logger.info("User deactivated: {}", uid);
    }
}
