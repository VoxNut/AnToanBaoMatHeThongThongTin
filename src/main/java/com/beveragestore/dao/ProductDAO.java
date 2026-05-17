package com.beveragestore.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.model.Product;
import com.beveragestore.util.FirebaseInitializer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

/**
 * dao cho thực thể sản phẩm (product).
 * xử lý mọi thao tác dữ liệu liên quan đến sản phẩm trên firestore.
 */
public class ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);
    private static final String COLLECTION_NAME = "products";
    private final Firestore db;

    public ProductDAO() {
        this.db = FirebaseInitializer.getInstance().getFirestore();
    }

    /**
     * tạo sản phẩm mới
     */
    public Product createProduct(String name, String category, String brand, String description,
                                 double price, int stock, String imageUrl) throws ExecutionException, InterruptedException {
        String productId = UUID.randomUUID().toString();
        Product product = Product.builder()
                .productId(productId)
                .name(name)
                .category(category)
                .brand(brand)
                .description(description)
                .price(price)
                .stock(stock)
                .imageUrl(imageUrl)
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        db.collection(COLLECTION_NAME)
                .document(productId)
                .set(product)
                .get();

        logger.info("Product created: {} ({})", name, productId);
        return product;
    }

    /**
     * tạo sản phẩm mới từ object product đầy đủ (dùng trong databaseseeder)
     */
    public void createProduct(Product product) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(product.getProductId())
                .set(product)
                .get();
        logger.info("Product created from object: {} ({})", product.getName(), product.getProductId());
    }

    /**
     * lấy thông tin sản phẩm theo id
     */
    public Product getProductById(String productId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection(COLLECTION_NAME)
                .document(productId)
                .get()
                .get();

        if (doc.exists()) {
            return doc.toObject(Product.class);
        }

        return null;
    }

    /**
     * lấy toàn bộ sản phẩm đang mở bán
     */
    public List<Product> getAllActiveProducts() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("active", true)
                .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .get()
                .get();

        List<Product> products = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            products.add(doc.toObject(Product.class));
        }

        return products;
    }

    /**
     * lấy tất cả sản phẩm (gồm cả sản phẩm ẩn - chỉ admin được xem)
     */
    public List<Product> getAllProducts() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .get()
                .get();

        List<Product> products = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            products.add(doc.toObject(Product.class));
        }

        return products;
    }

    /**
     * lấy danh sách sản phẩm theo danh mục
     */
    public List<Product> getProductsByCategory(String category) throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("category", category)
                .whereEqualTo("active", true)
                .orderBy("name", com.google.cloud.firestore.Query.Direction.ASCENDING)
                .get()
                .get();

        List<Product> products = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            products.add(doc.toObject(Product.class));
        }

        return products;
    }

    /**
     * tìm kiếm sản phẩm theo tên (so khớp chuỗi con)
     */
    public List<Product> searchByName(String searchTerm) throws ExecutionException, InterruptedException {
        List<Product> allProducts = getAllActiveProducts();
        List<Product> results = new ArrayList<>();

        String searchLower = searchTerm.toLowerCase();
        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(searchLower)) {
                results.add(product);
            }
        }

        return results;
    }

    /**
     * lấy các sản phẩm sắp hết hàng (số lượng tồn < 10)
     */
    public List<Product> getLowStockProducts() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = db.collection(COLLECTION_NAME)
                .whereLessThan("stock", 10)
                .whereEqualTo("active", true)
                .get()
                .get();

        List<Product> products = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            products.add(doc.toObject(Product.class));
        }

        return products;
    }

    /**
     * cập nhật thông tin chi tiết của sản phẩm
     */
    public void updateProduct(Product product) throws ExecutionException, InterruptedException {
        product.setUpdatedAt(new Date());
        db.collection(COLLECTION_NAME)
                .document(product.getProductId())
                .set(product)
                .get();

        logger.info("Product updated: {}", product.getProductId());
    }

    /**
     * xóa mềm sản phẩm bằng cách set isactive thành false
     */
    public void deactivateProduct(String productId) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(productId)
                .update("active", false, "updatedAt", new Date())
                .get();

        logger.info("Product deactivated: {}", productId);
    }

    /**
     * cập nhật số lượng tồn kho sản phẩm
     */
    public void updateStock(String productId, int newStock) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(productId)
                .update("stock", newStock, "updatedAt", new Date())
                .get();

        logger.debug("Product stock updated: {} -> {}", productId, newStock);
    }

    /**
     * lấy toàn bộ các danh mục (không trùng nhau từ các sản phẩm)
     */
    public List<String> getAllCategories() throws ExecutionException, InterruptedException {
        List<Product> products = getAllActiveProducts();
        List<String> categories = new ArrayList<>();

        for (Product product : products) {
            if (!categories.contains(product.getCategory())) {
                categories.add(product.getCategory());
            }
        }

        return categories;
    }
}
