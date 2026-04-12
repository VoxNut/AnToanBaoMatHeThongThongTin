package com.beveragestore.dao;
import java.util.concurrent.ExecutionException;
import com.beveragestore.model.User;
import com.beveragestore.util.FirebaseInitializer;
import com.google.cloud.firestore.Firestore;

/**
 * UserDAO - bản nháp ban đầu
 * TODO: bổ sung các phương thức truy vấn nâng cao
 */
public class UserDAO {
    private final Firestore db;

    public UserDAO() {
        this.db = FirebaseInitializer.getInstance().getFirestore();
    }

    public void createUser(User user) throws ExecutionException, InterruptedException {
        db.collection("users").document(user.getUid()).set(user).get();
    }

    public User findByEmail(String email) throws ExecutionException, InterruptedException {
        // TODO: implement proper query
        return null;
    }
}
