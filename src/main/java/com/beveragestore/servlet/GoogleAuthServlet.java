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
 * servlet xử lý đăng nhập bằng google.
 * nhận firebase id token từ frontend (lấy qua firebase js sdk),
 * sử dụng firebase admin sdk để check token, sau đó tìm hoặc tạo user mới trên firestore.
 * nếu thành công thì tạo session đăng nhập và trả về chuỗi json chứa link chuyển hướng.
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
            // đọc body của request (dạng json chứa idtoken)
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

            // xác thực firebase id token bằng admin sdk
            FirebaseAuth firebaseAuth = FirebaseInitializer.getInstance().getFirebaseAuth();
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);

            // lấy thông tin user từ token đã được xác thực
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

            // tìm hoặc tạo user mới trên firestore
            User user = userDAO.findOrCreateGoogleUser(uid, email, name, picture);

            if (!user.isActive()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"success\": false, \"error\": \"Your account has been deactivated. Please contact support.\"}");
                return;
            }

            // đăng nhập thành công -> lưu thông tin user vào session
            HttpSession session = request.getSession(true);
            SessionUtil.setUserInSession(session, user);

            logger.info("Google user logged in successfully: {} (role: {})", user.getEmail(), user.getRole());

            // tạo link redirect dựa vào role của user
            String contextPath = request.getContextPath();
            String redirectUrl;
            if (user.isAdmin()) {
                redirectUrl = contextPath + "/admin/dashboard";
            } else {
                redirectUrl = contextPath + "/";
            }

            // trả về phản hồi thành công kèm link chuyển hướng
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
