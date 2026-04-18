package com.beveragestore.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import java.io.InputStream;

/**
 * FirebaseInitializer - khởi tạo kết nối Firebase
 * Bản nháp: chưa xử lý singleton hoàn chỉnh
 */
public class FirebaseInitializer {
    private static FirebaseInitializer instance;
    private Firestore firestore;

    private FirebaseInitializer() {
        // TODO: init từ service account key
    }

    public static synchronized FirebaseInitializer getInstance() {
        if (instance == null) {
            instance = new FirebaseInitializer();
        }
        return instance;
    }

    public Firestore getFirestore() {
        return firestore;
    }
}
