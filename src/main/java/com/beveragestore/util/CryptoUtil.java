package com.beveragestore.util;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import com.beveragestore.model.Order;
import com.beveragestore.model.User;


public class CryptoUtil {

    // tạo cặp khóa rsa 2048-bit
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    // chuyển publickey thành chuỗi pem
    public static String publicKeyToPem(PublicKey publicKey) {
        String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" +
                insertNewLines(base64) +
                "\n-----END PUBLIC KEY-----";
    }

    // chuyển privatekey thành chuỗi pem
    public static String privateKeyToPem(PrivateKey privateKey) {
        String base64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" +
                insertNewLines(base64) +
                "\n-----END PRIVATE KEY-----";
    }

    private static String insertNewLines(String base64) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < base64.length()) {
            sb.append(base64, index, Math.min(index + 64, base64.length()));
            if (index + 64 < base64.length()) {
                sb.append("\n");
            }
            index += 64;
        }
        return sb.toString();
    }

    // nạp publickey từ chuỗi pem
    public static PublicKey pemToPublicKey(String pem) throws Exception {
        String cleanPem = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(cleanPem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    // nạp privatekey từ chuỗi pem
    public static PrivateKey pemToPrivateKey(String pem) throws Exception {
        String cleanPem = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(cleanPem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    // băm dữ liệu dạng text bằng sha-256
    public static String sha256(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
    // dựng chuỗi dữ liệu thô đặc trưng của đơn hàng
    public static String buildRawOrderString(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append(order.getOrderId()).append("|");
        sb.append(order.getUserId()).append("|");
        sb.append(String.format(java.util.Locale.US, "%.2f", order.getTotalAmount())).append("|");
        sb.append(order.getShippingAddress() != null ? order.getShippingAddress().trim() : "").append("|");
        
        if (order.getItems() != null) {
            for (Order.OrderItem item : order.getItems()) {
                sb.append(item.getProductId()).append(":")
                  .append(item.getQuantity()).append(":")
                  .append(String.format(java.util.Locale.US, "%.2f", item.getUnitPrice())).append("|");
            }
        }
        return sb.toString();
    }

    // tính mã băm (hash) đặc trưng cho dữ liệu đơn hàng (Double Hash cho Raw Order / Order Hash)
    public static String calculateOrderHash(Order order) throws NoSuchAlgorithmException {
        return sha256(buildRawOrderString(order));
    }

    // ký lên mã băm bằng privatekey
    public static String sign(String hash, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(Base64.getDecoder().decode(hash));
        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // xác thực chữ ký số bằng publickey (cho Double Hash)
    public static boolean verify(String hash, String signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(Base64.getDecoder().decode(hash));
            byte[] sigBytes = Base64.getDecoder().decode(signature);
            return sig.verify(sigBytes);
        } catch (Exception e) {
            return false;
        }
    }

    // xác thực chữ ký số dạng văn bản thường (cho Single Hash - Plain Text)
    public static boolean verifyPlain(String rawData, String signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(rawData.getBytes(StandardCharsets.UTF_8));
            byte[] sigBytes = Base64.getDecoder().decode(signature);
            return sig.verify(sigBytes);
        } catch (Exception e) {
            return false;
        }
    }

    // xác thực động trạng thái chữ ký số của đơn hàng từ lịch sử khóa của user
    public static void verifyOrderSignature(Order order, User buyer) {
        if (order.getSignature() == null) {
            order.setSignatureStatus("UNSIGNED");
            return;
        }
        if (buyer == null) {
            order.setSignatureStatus("NO_USER");
            return;
        }
        try {
            User.PublicKeyRecord keyRecord = null;
            if (buyer.getKeyHistory() != null) {
                for (User.PublicKeyRecord rec : buyer.getKeyHistory()) {
                    if (order.getPublicKeyId() != null && order.getPublicKeyId().equals(rec.getKeyId())) {
                        keyRecord = rec;
                        break;
                    }
                }
            }

            if (keyRecord == null) {
                order.setSignatureStatus("NO_KEY_FOUND");
            } else {
                // kiểm tra xem khóa này có bị hủy trước thời điểm đặt hàng không
                if (keyRecord.getRevokedAt() != null && keyRecord.getRevokedAt().before(order.getCreatedAt())) {
                    order.setSignatureStatus("REVOKED_KEY");
                } else {
                    // kiểm tra xem dữ liệu đơn hàng có bị sửa đổi không
                    PublicKey pubKey = pemToPublicKey(keyRecord.getPublicKeyPem());
                    
                    // Cách 1: Double Hash (Mặc định cho Raw Order và Order Hash)
                    String currentHash = calculateOrderHash(order);
                    boolean isValid = verify(currentHash, order.getSignature(), pubKey);
                    
                    // Cách 2: Single Hash (Cho Plain Text)
                    if (!isValid) {
                        String rawOrderData = buildRawOrderString(order);
                        isValid = verifyPlain(rawOrderData, order.getSignature(), pubKey);
                    }
                    
                    if (isValid) {
                        order.setSignatureStatus("VALID");
                    } else {
                        order.setSignatureStatus("INVALID");
                    }
                }
            }
        } catch (Exception e) {
            order.setSignatureStatus("ERROR");
        }
    }
}

