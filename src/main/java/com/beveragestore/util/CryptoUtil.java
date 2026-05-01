package com.beveragestore.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * CryptoUtil - tiện ích mã hóa RSA
 * Bản nháp: chưa có hash và sign hoàn chỉnh
 */
public class CryptoUtil {

    /**
     * Sinh cặp khóa RSA 2048-bit
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    /**
     * Chuyển PublicKey sang chuỗi PEM Base64
     * TODO: implement encode/decode hoàn chỉnh
     */
    public static String publicKeyToPem(PublicKey key) {
        // TODO
        return null;
    }

    /**
     * Chuyển PrivateKey sang chuỗi PEM Base64
     * TODO: implement
     */
    public static String privateKeyToPem(PrivateKey key) {
        // TODO
        return null;
    }
}
