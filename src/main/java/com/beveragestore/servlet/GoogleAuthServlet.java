package com.beveragestore.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.dao.UserDAO;
import com.beveragestore.model.User;
import com.beveragestore.util.FirebaseInitializer;
import com.beveragestore.util.SessionUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servlet for handling Google Sign-In authentication.
 * 
 * Receives a Firebase ID token from the frontend (obtained via Firebase JS SDK),
 * verifies it using Firebase Admin SDK, and creates or finds the user in Firestore.
 * On success, establishes a session and returns a JSON response with the redirect URL.
 */
public class GoogleAuthServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthServlet.class);
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Read the request body (JSON with idToken)
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            String requestBody = sb.toString();
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();

            if (!jsonRequest.has("idToken")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"error\": \"Missing idToken\"}");
                return;
            }

            String idToken = jsonRequest.get("idToken").getAsString();

            // Verify the Firebase ID token using Admin SDK
            FirebaseAuth firebaseAuth = FirebaseInitializer.getInstance().getFirebaseAuth();
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);

            // Extract user info from the verified token
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();
            String picture = decodedToken.getPicture();

            if (email == null || email.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"error\": \"Google account does not have an email address\"}");
                return;
            }

            logger.info("Google Sign-In token verified for: {} (uid: {})", email, uid);

            // Find or create user in Firestore
            User user = userDAO.findOrCreateGoogleUser(uid, email, name, picture);

            if (!user.isActive()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"success\": false, \"error\": \"Your account has been deactivated. Please contact support.\"}");
                return;
            }

            // Login successful - store user in session
            HttpSession session = request.getSession(true);
            SessionUtil.setUserInSession(session, user);

            logger.info("Google user logged in successfully: {} (role: {})", user.getEmail(), user.getRole());

            // Build redirect URL based on role
            String contextPath = request.getContextPath();
            String redirectUrl;
            if (user.isAdmin()) {
                redirectUrl = contextPath + "/admin/dashboard";
            } else {
                redirectUrl = contextPath + "/";
            }

            // Return success response with redirect URL
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("redirectUrl", redirectUrl);
            jsonResponse.addProperty("userName", user.getFullName());
            out.print(jsonResponse.toString());

        } catch (FirebaseAuthException e) {
            logger.error("Firebase token verification failed", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\": false, \"error\": \"Invalid authentication token. Please try again.\"}");

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Database error during Google authentication", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"error\": \"An error occurred. Please try again.\"}");

        } catch (Exception e) {
            logger.error("Unexpected error during Google authentication", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"error\": \"An unexpected error occurred. Please try again.\"}");
        }
    }
}
