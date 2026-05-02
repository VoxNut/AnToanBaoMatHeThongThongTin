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
 * Singleton class to initialize Firebase Admin SDK and provide access to Firestore.
 * 
 * This class ensures that Firebase is initialized only once when the application starts.
 * The serviceAccountKey.json file should be placed in src/main/resources/.
 * 
 * Usage:
 *   Firestore db = FirebaseInitializer.getInstance().getFirestore();
 *   FirebaseAuth auth = FirebaseInitializer.getInstance().getFirebaseAuth();
 */
public class FirebaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseInitializer.class);
    private static FirebaseInitializer instance;
    private Firestore firestore;

    /**
     * Private constructor to prevent instantiation
     */
    private FirebaseInitializer() {
        initializeFirebase();
    }

    /**
     * Get singleton instance
     */
    public static synchronized FirebaseInitializer getInstance() {
        if (instance == null) {
            instance = new FirebaseInitializer();
        }
        return instance;
    }

    /**
     * Initialize Firebase Admin SDK using serviceAccountKey.json from classpath
     */
    private void initializeFirebase() {
        try {
            // Load service account key from classpath (src/main/resources/)
            InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");
            
            if (serviceAccount == null) {
                throw new IOException("serviceAccountKey.json not found in classpath (src/main/resources/)");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Check if Firebase is already initialized (prevent re-initialization)
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully");
            } else {
                logger.info("Firebase is already initialized");
            }

            // Get Firestore instance
            this.firestore = FirestoreClient.getFirestore();
            logger.info("Firestore client initialized successfully");

        } catch (IOException e) {
            logger.error("Failed to initialize Firebase. Make sure serviceAccountKey.json is in src/main/resources/.", e);
            throw new RuntimeException("Firebase initialization failed. Ensure serviceAccountKey.json is in src/main/resources/.", e);
        }
    }

    /**
     * Get Firestore database instance
     */
    public Firestore getFirestore() {
        if (firestore == null) {
            throw new IllegalStateException("Firestore is not initialized. Check Firebase configuration.");
        }
        return firestore;
    }

    /**
     * Get FirebaseAuth instance for token verification
     */
    public FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}
