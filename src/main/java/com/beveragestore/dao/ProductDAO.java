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
 * DAO for Product entity.
 * Handles all database operations for products in Firestore.
 */
public class ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);
    private static final String COLLECTION_NAME = "products";
    private final Firestore db;

    public ProductDAO() {
        this.db = FirebaseInitializer.getInstance().getFirestore();
    }

    /**
     * Create a new product
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
     * Create a new product from a fully constructed Product object (used by DatabaseSeeder)
     */
    public void createProduct(Product product) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(product.getProductId())
                .set(product)
                .get();
        logger.info("Product created from object: {} ({})", product.getName(), product.getProductId());
    }

    /**
     * Get product by ID
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
     * Get all active products
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
     * Get all products (including inactive ones - admin only)
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
     * Get products by category
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
     * Search products by name (substring match)
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
     * Get low-stock products (stock < 10)
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
     * Update product information
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
     * Soft delete product by setting isActive to false
     */
    public void deactivateProduct(String productId) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(productId)
                .update("active", false, "updatedAt", new Date())
                .get();

        logger.info("Product deactivated: {}", productId);
    }

    /**
     * Update product stock
     */
    public void updateStock(String productId, int newStock) throws ExecutionException, InterruptedException {
        db.collection(COLLECTION_NAME)
                .document(productId)
                .update("stock", newStock, "updatedAt", new Date())
                .get();

        logger.debug("Product stock updated: {} -> {}", productId, newStock);
    }

    /**
     * Get all categories (unique values from all products)
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
