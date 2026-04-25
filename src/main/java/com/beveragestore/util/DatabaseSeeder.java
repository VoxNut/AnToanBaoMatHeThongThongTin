package com.beveragestore.util;

import com.google.cloud.firestore.Firestore;

/**
 * DatabaseSeeder - khởi tạo dữ liệu mẫu
 * Bản nháp: chưa thêm đầy đủ sản phẩm
 */
public class DatabaseSeeder {
    private final Firestore db;

    public DatabaseSeeder(Firestore db) {
        this.db = db;
    }

    public void seedProducts() throws Exception {
        // TODO: thêm sản phẩm mẫu vào Firestore
        System.out.println("Seeding products...");
    }

    public void seedUsers() throws Exception {
        // TODO: tạo tài khoản admin mặc định
        System.out.println("Seeding users...");
    }
}
