package com.beveragestore.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.dao.UserDAO;
import com.beveragestore.model.User;
import com.beveragestore.util.CryptoUtil;
import com.beveragestore.util.SessionUtil;

public class KeyManagementServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(KeyManagementServlet.class);
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            User user = SessionUtil.getUserFromSession(request.getSession());
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // Reload user from DB to get the latest key info
            user = userDAO.findByUid(user.getUid());
            request.setAttribute("userKeys", user);

            request.getRequestDispatcher("/WEB-INF/views/customer/key-management.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Error displaying key management", e);
            request.setAttribute("error", "Không thể tải trang quản lý khóa.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            User user = SessionUtil.getUserFromSession(request.getSession());
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }


            // Reload user from DB
            user = userDAO.findByUid(user.getUid());

            String action = request.getParameter("action");
            if ("generate".equals(action)) {
                handleGenerateKeys(request, response, user);
            } else if ("revoke".equals(action)) {
                handleRevokeKeys(request, response, user);
            } else {
                response.sendRedirect(request.getContextPath() + "/customer/keys");
            }
        } catch (Exception e) {
            logger.error("Error processing key management action", e);
            request.setAttribute("error", "Lỗi xử lý khóa: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void handleGenerateKeys(HttpServletRequest request, HttpServletResponse response, User user) throws Exception {
        // Generate key pair
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        String pubKeyPem = CryptoUtil.publicKeyToPem(keyPair.getPublic());
        String privKeyPem = CryptoUtil.privateKeyToPem(keyPair.getPrivate());
        String keyId = UUID.randomUUID().toString();

        // Save public key details to user
        user.setActivePublicKey(pubKeyPem);
        user.setActivePublicKeyId(keyId);
        user.setKeyRevokedAt(null);

        List<User.PublicKeyRecord> history = user.getKeyHistory();
        if (history == null) {
            history = new ArrayList<>();
        }
        history.add(User.PublicKeyRecord.builder()
                .keyId(keyId)
                .publicKeyPem(pubKeyPem)
                .createdAt(new Date())
                .build());
        user.setKeyHistory(history);

        userDAO.updateUser(user);
        logger.info("Generated new key pair for user: {}, KeyID: {}", user.getEmail(), keyId);

        // Download private key
        sendPrivateKeyDownload(response, user.getEmail(), keyId, privKeyPem);
    }

    private void handleRevokeKeys(HttpServletRequest request, HttpServletResponse response, User user) throws Exception {
        String inputRevokeTimeStr = request.getParameter("revokeTime"); // User can input custom time of leak
        Date revokeDate = new Date(); // Default to now
        if (inputRevokeTimeStr != null && !inputRevokeTimeStr.trim().isEmpty()) {
            try {
                // simple ISO parsing or datetime-local parsing
                // input from HTML datetime-local is yyyy-MM-dd'T'HH:mm
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                revokeDate = sdf.parse(inputRevokeTimeStr);
            } catch (Exception e) {
                logger.warn("Could not parse revoke date: {}", inputRevokeTimeStr);
            }
        }

        // Revoke active key in history
        String activeKeyId = user.getActivePublicKeyId();
        List<User.PublicKeyRecord> history = user.getKeyHistory();
        if (history != null && activeKeyId != null) {
            for (User.PublicKeyRecord record : history) {
                if (activeKeyId.equals(record.getKeyId())) {
                    record.setRevokedAt(revokeDate);
                    break;
                }
            }
        }

        // Generate new key pair
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        String pubKeyPem = CryptoUtil.publicKeyToPem(keyPair.getPublic());
        String privKeyPem = CryptoUtil.privateKeyToPem(keyPair.getPrivate());
        String newKeyId = UUID.randomUUID().toString();

        // Update user
        user.setActivePublicKey(pubKeyPem);
        user.setActivePublicKeyId(newKeyId);
        user.setKeyRevokedAt(null); // new key is not revoked

        if (history == null) {
            history = new ArrayList<>();
        }
        history.add(User.PublicKeyRecord.builder()
                .keyId(newKeyId)
                .publicKeyPem(pubKeyPem)
                .createdAt(new Date())
                .build());
        user.setKeyHistory(history);

        userDAO.updateUser(user);
        logger.info("Revoked key {} and generated new key {} for user {}", activeKeyId, newKeyId, user.getEmail());

        // Download new private key
        sendPrivateKeyDownload(response, user.getEmail(), newKeyId, privKeyPem);
    }

    private void sendPrivateKeyDownload(HttpServletResponse response, String email, String keyId, String privKeyPem) throws IOException {
        response.setContentType("application/x-pem-file");
        String filename = "private_key_" + email.split("@")[0] + "_" + keyId.substring(0, 8) + ".pem";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(privKeyPem);
        }
    }
}
