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

            // tải lại thông tin user từ db để cập nhật thông tin khóa mới nhất
            user = userDAO.findByUid(user.getUid());
            request.setAttribute("userKeys", user);

            String success = (String) request.getSession().getAttribute("success");
            if (success != null) {
                request.setAttribute("success", success);
                request.getSession().removeAttribute("success");
            }

            request.getRequestDispatcher("/WEB-INF/views/customer/key-management.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Error displaying key management", e);
            request.setAttribute("error", "Không thể tải trang quản lý khóa.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = null;
        try {
            user = SessionUtil.getUserFromSession(request.getSession());
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }


            // tải lại thông tin user từ cơ sở dữ liệu
            user = userDAO.findByUid(user.getUid());

            String action = request.getParameter("action");
            if ("generate".equals(action)) {
                handleGenerateKeys(request, response, user);
            } else if ("revoke".equals(action)) {
                handleRevokeKeys(request, response, user);
            } else if ("register_public_key".equals(action)) {
                handleRegisterPublicKey(request, response, user);
            } else {
                response.sendRedirect(request.getContextPath() + "/customer/keys");
            }
        } catch (Exception e) {
            logger.error("Error processing key management action", e);
            try {
                if (user == null) {
                    user = SessionUtil.getUserFromSession(request.getSession());
                }
                if (user != null) {
                    user = userDAO.findByUid(user.getUid());
                }
                request.setAttribute("userKeys", user);
                request.setAttribute("error", e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/customer/key-management.jsp").forward(request, response);
            } catch (Exception ex) {
                request.setAttribute("error", "Lỗi xử lý khóa: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            }
        }
    }

    private void handleGenerateKeys(HttpServletRequest request, HttpServletResponse response, User user) throws Exception {
        // tạo cặp khóa mới
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        String pubKeyPem = CryptoUtil.publicKeyToPem(keyPair.getPublic());
        String privKeyPem = CryptoUtil.privateKeyToPem(keyPair.getPrivate());
        String keyId = UUID.randomUUID().toString();

        // lưu thông tin public key vào thông tin user
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

        String[] receiveMethods = request.getParameterValues("receiveMethod");
        boolean downloadKey = false;
        boolean emailKey = false;
        if (receiveMethods != null) {
            for (String method : receiveMethods) {
                if ("download".equals(method)) {
                    downloadKey = true;
                } else if ("email".equals(method)) {
                    emailKey = true;
                }
            }
        } else {
            downloadKey = true; // mặc định nếu không chọn gì
        }

        // gửi khóa qua email nếu khách hàng yêu cầu
        if (emailKey) {
            com.beveragestore.util.EmailUtil.sendPrivateKeyEmailAsync(user.getEmail(), keyId, privKeyPem);
        }

        // xử lý phản hồi
        if (downloadKey) {
            sendPrivateKeyDownload(response, user.getEmail(), keyId, privKeyPem);
        } else {
            request.getSession().setAttribute("success", "tạo khóa thành công! khóa bí mật đã được gửi tới email " + user.getEmail());
            response.sendRedirect(request.getContextPath() + "/customer/keys");
        }
    }

    private void handleRevokeKeys(HttpServletRequest request, HttpServletResponse response, User user) throws Exception {
        String inputRevokeTimeStr = request.getParameter("revokeTime"); // user có thể nhập thời điểm rò rỉ khóa tùy chọn
        Date revokeDate = new Date(); // mặc định lấy thời gian hiện tại
        if (inputRevokeTimeStr != null && !inputRevokeTimeStr.trim().isEmpty()) {
            try {
                // phân tích định dạng iso hoặc datetime-local đơn giản
                // dữ liệu đầu vào từ html datetime-local có dạng yyyy-MM-dd'T'HH:mm
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                revokeDate = sdf.parse(inputRevokeTimeStr);
                if (revokeDate.after(new Date())) {
                    throw new IllegalArgumentException("Thời gian lộ khóa không được ở trong tương lai.");
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                logger.warn("Could not parse revoke date: {}", inputRevokeTimeStr);
            }
        }

        // hủy kích hoạt khóa đang sử dụng trong lịch sử khóa
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

        // tạo cặp khóa mới tinh
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        String pubKeyPem = CryptoUtil.publicKeyToPem(keyPair.getPublic());
        String privKeyPem = CryptoUtil.privateKeyToPem(keyPair.getPrivate());
        String newKeyId = UUID.randomUUID().toString();

        // cập nhật user
        user.setActivePublicKey(pubKeyPem);
        user.setActivePublicKeyId(newKeyId);
        user.setKeyRevokedAt(null); // khóa mới tạo mặc định chưa bị thu hồi

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

        String[] receiveMethods = request.getParameterValues("receiveMethod");
        boolean downloadKey = false;
        boolean emailKey = false;
        if (receiveMethods != null) {
            for (String method : receiveMethods) {
                if ("download".equals(method)) {
                    downloadKey = true;
                } else if ("email".equals(method)) {
                    emailKey = true;
                }
            }
        } else {
            downloadKey = true; // mặc định nếu không chọn gì
        }

        // gửi khóa mới qua email nếu khách hàng yêu cầu
        if (emailKey) {
            com.beveragestore.util.EmailUtil.sendPrivateKeyEmailAsync(user.getEmail(), newKeyId, privKeyPem);
        }

        // xử lý phản hồi cho khách hàng
        if (downloadKey) {
            sendPrivateKeyDownload(response, user.getEmail(), newKeyId, privKeyPem);
        } else {
            request.getSession().setAttribute("success", "làm lại khóa thành công! khóa bí mật mới đã được gửi tới email " + user.getEmail());
            response.sendRedirect(request.getContextPath() + "/customer/keys");
        }
    }

    private void sendPrivateKeyDownload(HttpServletResponse response, String email, String keyId, String privKeyPem) throws IOException {
        response.setContentType("application/x-pem-file");
        String filename = "private_key_" + email.split("@")[0] + "_" + keyId.substring(0, 8) + ".pem";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(privKeyPem);
        }
    }

    private void handleRegisterPublicKey(HttpServletRequest request, HttpServletResponse response, User user) throws Exception {
        String pubKeyPem = request.getParameter("publicKey");
        if (pubKeyPem == null || pubKeyPem.trim().isEmpty()) {
            throw new IllegalArgumentException("Khóa công khai không được để trống.");
        }

        // Validate public key PEM
        try {
            CryptoUtil.pemToPublicKey(pubKeyPem);
        } catch (Exception e) {
            throw new IllegalArgumentException("Khóa công khai không hợp lệ. Vui lòng kiểm tra lại định dạng PEM.");
        }

        // Revoke active key if exists
        String activeKeyId = user.getActivePublicKeyId();
        List<User.PublicKeyRecord> history = user.getKeyHistory();
        if (history == null) {
            history = new ArrayList<>();
        }
        if (activeKeyId != null) {
            for (User.PublicKeyRecord record : history) {
                if (activeKeyId.equals(record.getKeyId())) {
                    if (record.getRevokedAt() == null) {
                        record.setRevokedAt(new Date());
                    }
                    break;
                }
            }
        }

        // Register new public key
        String newKeyId = UUID.randomUUID().toString();
        user.setActivePublicKey(pubKeyPem.trim());
        user.setActivePublicKeyId(newKeyId);
        user.setKeyRevokedAt(null);

        history.add(User.PublicKeyRecord.builder()
                .keyId(newKeyId)
                .publicKeyPem(pubKeyPem.trim())
                .createdAt(new Date())
                .build());
        user.setKeyHistory(history);

        userDAO.updateUser(user);
        logger.info("Registered offline public key for user: {}, KeyID: {}", user.getEmail(), newKeyId);

        request.getSession().setAttribute("success", "Đăng ký khóa công khai thành công!");
        response.sendRedirect(request.getContextPath() + "/customer/keys");
    }
}
