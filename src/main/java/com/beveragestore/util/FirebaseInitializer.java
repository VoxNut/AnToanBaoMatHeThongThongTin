package com.beveragestore.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;

/**
 * class singleton để khởi tạo firebase admin sdk và cấp quyền truy cập firestore.
 * đảm bảo firebase chỉ khởi tạo đúng một lần khi app bắt đầu chạy.
 * file serviceaccountkey.json cần được đặt trong thư mục src/main/resources/.
 * cách dùng:
 * Firestore db = FirebaseInitializer.getInstance().getFirestore();
 * FirebaseAuth auth = FirebaseInitializer.getInstance().getFirebaseAuth();
 */
public class FirebaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseInitializer.class);
    private static FirebaseInitializer instance;
    private Firestore firestore;

    /**
     * constructor private để chặn việc khởi tạo đối tượng trực tiếp bên ngoài class
     */
    private FirebaseInitializer() {
        initializeFirebase();
    }

    /**
     * lấy instance duy nhất (singleton)
     */
    public static synchronized FirebaseInitializer getInstance() {
        if (instance == null) {
            instance = new FirebaseInitializer();
        }
        return instance;
    }

    /**
     * khởi tạo firebase admin sdk sử dụng file serviceaccountkey.json từ classpath
     */
    private void initializeFirebase() {
        try {
            // nạp file serviceaccountkey.json từ classpath (src/main/resources/)
            InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");
            
            if (serviceAccount == null) {
                throw new IOException("serviceAccountKey.json not found in classpath (src/main/resources/)");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // check xem firebase đã khởi tạo chưa để đỡ bị chạy lại
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully");
            } else {
                logger.info("Firebase is already initialized");
            }

            // lấy instance của firestore
            this.firestore = FirestoreClient.getFirestore();
            logger.info("Firestore client initialized successfully");

        } catch (IOException e) {
            logger.error("Failed to initialize Firebase. Make sure serviceAccountKey.json is in src/main/resources/.", e);
            throw new RuntimeException("Firebase initialization failed. Ensure serviceAccountKey.json is in src/main/resources/.", e);
        }
    }

    /**
     * lấy instance của cơ sở dữ liệu firestore
     */
    public Firestore getFirestore() {
        if (firestore == null) {
            throw new IllegalStateException("Firestore is not initialized. Check Firebase configuration.");
        }
        return firestore;
    }

    /**
     * lấy instance của firebaseauth để check token
     */
    public FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}
