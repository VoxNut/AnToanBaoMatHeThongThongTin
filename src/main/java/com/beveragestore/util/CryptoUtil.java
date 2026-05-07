package com.beveragestore.util;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import com.beveragestore.model.Order;

public class CryptoUtil {

    // Generate RSA 2048-bit Key Pair
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    // Convert PublicKey to PEM string
    public static String publicKeyToPem(PublicKey publicKey) {
        String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" +
                insertNewLines(base64) +
                "\n-----END PUBLIC KEY-----";
    }

    // Convert PrivateKey to PEM string
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

    // Load PublicKey from PEM string
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

    // Load PrivateKey from PEM string
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

    // Hash text data using SHA-256
    public static String sha256(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    // Calculate deterministic hash for Order data
    public static String calculateOrderHash(Order order) throws NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        sb.append(order.getOrderId()).append("|");
        sb.append(order.getUserId()).append("|");
        sb.append(String.format("%.2f", order.getTotalAmount())).append("|");
        sb.append(order.getShippingAddress() != null ? order.getShippingAddress().trim() : "").append("|");
        
        if (order.getItems() != null) {
            for (Order.OrderItem item : order.getItems()) {
                sb.append(item.getProductId()).append(":")
                  .append(item.getQuantity()).append(":")
                  .append(String.format("%.2f", item.getUnitPrice())).append("|");
            }
        }
        return sha256(sb.toString());
    }

    // Sign a hash using a PrivateKey
    public static String sign(String hash, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(Base64.getDecoder().decode(hash));
        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // Verify a signature using a PublicKey
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
}
