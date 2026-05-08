package com.beveragestore.util;

import com.beveragestore.dao.OrderDAO;
import com.beveragestore.dao.ProductDAO;
import com.beveragestore.dao.UserDAO;
import com.beveragestore.model.Order;
import com.beveragestore.model.Product;
import com.beveragestore.model.User;
import com.google.cloud.firestore.Firestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    public static void main(String[] args) {
        logger.info("Starting Database Seeder...");

        try {
            // Force initialization of Firebase
            Firestore db = FirebaseInitializer.getInstance().getFirestore();
            logger.info("Firebase initialized successfully. Seeding data...");

            UserDAO userDAO = new UserDAO();
            ProductDAO productDAO = new ProductDAO();
            OrderDAO orderDAO = new OrderDAO();

            // 1. Seed Users
            logger.info("Seeding Users...");
            List<User> users = seedUsers(userDAO);

            // 2. Seed Products
            logger.info("Seeding Products...");
            List<Product> products = seedProducts(productDAO);

            // 3. Seed Orders
            logger.info("Seeding Orders...");
            seedOrders(orderDAO, users, products);

            logger.info("Database Seeder finished successfully!");

            // Allow background threads to finish sending
            Thread.sleep(3000);
            System.exit(0);
            
        } catch (Exception e) {
            logger.error("Error during database seeding", e);
            System.exit(1);
        }
    }

    private static List<User> seedUsers(UserDAO userDAO) throws Exception {
        List<User> seededUsers = new ArrayList<>();

        // Create default users for each role
        User admin = User.builder()
                .uid(UUID.randomUUID().toString())
                .fullName("System Admin")
                .email("admin@grindery.com")
                .passwordHash(org.mindrot.jbcrypt.BCrypt.hashpw("admin123", org.mindrot.jbcrypt.BCrypt.gensalt()))
                .role(User.ROLE_ADMIN)
                .authProvider("local")
                .createdAt(new Date())
                .active(true)
                .build();
        
        User shopOwner = User.builder()
                .uid(UUID.randomUUID().toString())
                .fullName("Shop Owner")
                .email("owner@grindery.com")
                .passwordHash(org.mindrot.jbcrypt.BCrypt.hashpw("owner123", org.mindrot.jbcrypt.BCrypt.gensalt()))
                .role(User.ROLE_SHOP_OWNER)
                .authProvider("local")
                .createdAt(new Date())
                .active(true)
                .build();
        
        User shipper = User.builder()
                .uid(UUID.randomUUID().toString())
                .fullName("Fast Shipper")
                .email("shipper@grindery.com")
                .passwordHash(org.mindrot.jbcrypt.BCrypt.hashpw("shipper123", org.mindrot.jbcrypt.BCrypt.gensalt()))
                .role(User.ROLE_SHIPPER)
                .authProvider("local")
                .createdAt(new Date())
                .active(true)
                .build();
        
        User customer1 = User.builder()
                .uid(UUID.randomUUID().toString())
                .fullName("Alice Customer")
                .email("alice@customer.com")
                .passwordHash(org.mindrot.jbcrypt.BCrypt.hashpw("customer123", org.mindrot.jbcrypt.BCrypt.gensalt()))
                .role(User.ROLE_CUSTOMER)
                .authProvider("local")
                .createdAt(new Date())
                .active(true)
                .build();

        User customer2 = User.builder()
                .uid(UUID.randomUUID().toString())
                .fullName("Bob Customer")
                .email("bob@customer.com")
                .passwordHash(org.mindrot.jbcrypt.BCrypt.hashpw("customer123", org.mindrot.jbcrypt.BCrypt.gensalt()))
                .role(User.ROLE_CUSTOMER)
                .authProvider("local")
                .createdAt(new Date())
                .active(true)
                .build();

        List<User> usersToCreate = Arrays.asList(admin, shopOwner, shipper, customer1, customer2);
        
        for (User u : usersToCreate) {
            // Check if exists
            User existing = userDAO.findByEmail(u.getEmail());
            if (existing == null) {
                userDAO.createUser(u);
                seededUsers.add(u);
                logger.info("Created user: {}", u.getEmail());
            } else {
                seededUsers.add(existing);
                logger.info("User already exists: {}", existing.getEmail());
            }
        }
        
        return seededUsers;
    }

    private static List<Product> seedProducts(ProductDAO productDAO) throws ExecutionException, InterruptedException {
        List<Product> productsToCreate = Arrays.asList(
            Product.builder()
                .productId(UUID.randomUUID().toString())
                .name("Ethiopia Yirgacheffe Light Roast")
                .category("Coffee")
                .brand("Grindery Reserve")
                .description("A bright, floral light roast with notes of jasmine and bergamot. Ethically sourced from Yirgacheffe.")
                .price(24.00)
                .stock(50)
                .imageUrl("https://res.cloudinary.com/dbpl94opl/image/upload/v1714000000/coffee_bag_1.jpg")
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build(),
            
            Product.builder()
                .productId(UUID.randomUUID().toString())
                .name("Colombia Supremo Medium Roast")
                .category("Coffee")
                .brand("Grindery Reserve")
                .description("A balanced medium roast featuring notes of chocolate, caramel, and a hint of cherry.")
                .price(22.00)
                .stock(30)
                .imageUrl("https://res.cloudinary.com/dbpl94opl/image/upload/v1714000000/coffee_bag_2.jpg")
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build(),
            
            Product.builder()
                .productId(UUID.randomUUID().toString())
                .name("Sumatra Mandheling Dark Roast")
                .category("Coffee")
                .brand("Grindery Reserve")
                .description("Earthy and full-bodied dark roast with low acidity and notes of dark chocolate and spice.")
                .price(23.50)
                .stock(45)
                .imageUrl("https://res.cloudinary.com/dbpl94opl/image/upload/v1714000000/coffee_bag_3.jpg")
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build(),
            
            Product.builder()
                .productId(UUID.randomUUID().toString())
                .name("Matcha Ceremonial Grade")
                .category("Tea")
                .brand("Grindery Reserve")
                .description("Premium stone-ground ceremonial matcha from Uji, Japan. Vibrant green color and smooth umami flavor.")
                .price(35.00)
                .stock(20)
                .imageUrl("https://res.cloudinary.com/dbpl94opl/image/upload/v1714000000/matcha.jpg")
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build(),
            
            Product.builder()
                .productId(UUID.randomUUID().toString())
                .name("Earl Grey Reserve")
                .category("Tea")
                .brand("Grindery Reserve")
                .description("Classic black tea infused with premium Italian bergamot oil.")
                .price(18.00)
                .stock(60)
                .imageUrl("https://res.cloudinary.com/dbpl94opl/image/upload/v1714000000/earl_grey.jpg")
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build(),

            Product.builder()
                .productId(UUID.randomUUID().toString())
                .name("Chemex Pour-Over Glass Coffeemaker")
                .category("Goods")
                .brand("Chemex")
                .description("The classic 8-cup Chemex pour-over glass coffeemaker. Made of non-porous Borosilicate glass.")
                .price(48.00)
                .stock(15)
                .imageUrl("https://res.cloudinary.com/dbpl94opl/image/upload/v1714000000/chemex.jpg")
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build(),

            Product.builder()
                .productId(UUID.randomUUID().toString())
                .name("Hario V60 Ceramic Dripper")
                .category("Goods")
                .brand("Hario")
                .description("Size 02 ceramic coffee dripper in white. Retains heat to ensure a constant temperature throughout the brewing cycle.")
                .price(25.00)
                .stock(25)
                .imageUrl("https://res.cloudinary.com/dbpl94opl/image/upload/v1714000000/v60.jpg")
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build()
        );

        List<Product> seededProducts = new ArrayList<>();
        
        // Let's just retrieve existing products to not duplicate endlessly, or just add them if it's empty
        List<Product> existingProducts = productDAO.getAllProducts();
        if (existingProducts.isEmpty()) {
            for (Product p : productsToCreate) {
                productDAO.createProduct(p);
                seededProducts.add(p);
                logger.info("Created product: {}", p.getName());
            }
        } else {
            seededProducts.addAll(existingProducts);
            logger.info("Products already exist. Skipping product seed.");
        }
        
        return seededProducts;
    }

    private static void seedOrders(OrderDAO orderDAO, List<User> users, List<Product> products) throws Exception {
        long existingOrders = orderDAO.getTotalOrdersCount();
        if (existingOrders > 0) {
            logger.info("Orders already exist. Skipping order seed.");
            return;
        }

        // Find customer users
        User customer1 = users.stream().filter(u -> u.getEmail().equals("alice@customer.com")).findFirst().orElse(users.get(0));
        User customer2 = users.stream().filter(u -> u.getEmail().equals("bob@customer.com")).findFirst().orElse(users.get(0));

        if (products.size() < 2) return; // safety

        // Order 1: Alice, PENDING
        List<Order.OrderItem> items1 = new ArrayList<>();
        items1.add(Order.OrderItem.builder()
                .productId(products.get(0).getProductId())
                .productName(products.get(0).getName())
                .unitPrice(products.get(0).getPrice())
                .quantity(2)
                .imageUrl(products.get(0).getImageUrl())
                .build());
        items1.add(Order.OrderItem.builder()
                .productId(products.get(3).getProductId())
                .productName(products.get(3).getName())
                .unitPrice(products.get(3).getPrice())
                .quantity(1)
                .imageUrl(products.get(3).getImageUrl())
                .build());
        
        double total1 = items1.stream().mapToDouble(Order.OrderItem::getSubtotal).sum();
        Order order1 = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(customer1.getUid())
                .items(items1)
                .totalAmount(total1)
                .shippingAddress("123 Alice St, Wonderland")
                .status(Order.STATUS_PENDING)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        orderDAO.createOrder(order1);
        logger.info("Created Order 1 (PENDING) for {}", customer1.getEmail());

        // Order 2: Bob, PROCESSING
        List<Order.OrderItem> items2 = new ArrayList<>();
        items2.add(Order.OrderItem.builder()
                .productId(products.get(5).getProductId())
                .productName(products.get(5).getName())
                .unitPrice(products.get(5).getPrice())
                .quantity(1)
                .imageUrl(products.get(5).getImageUrl())
                .build());
        
        double total2 = items2.stream().mapToDouble(Order.OrderItem::getSubtotal).sum();
        Order order2 = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(customer2.getUid())
                .items(items2)
                .totalAmount(total2)
                .shippingAddress("456 Bob Ave, Builder City")
                .status(Order.STATUS_PROCESSING)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        orderDAO.createOrder(order2);
        logger.info("Created Order 2 (PROCESSING) for {}", customer2.getEmail());

        // Order 3: Alice, DELIVERED
        List<Order.OrderItem> items3 = new ArrayList<>();
        items3.add(Order.OrderItem.builder()
                .productId(products.get(1).getProductId())
                .productName(products.get(1).getName())
                .unitPrice(products.get(1).getPrice())
                .quantity(1)
                .imageUrl(products.get(1).getImageUrl())
                .build());
        
        double total3 = items3.stream().mapToDouble(Order.OrderItem::getSubtotal).sum();
        Order order3 = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(customer1.getUid())
                .items(items3)
                .totalAmount(total3)
                .shippingAddress("123 Alice St, Wonderland")
                .status(Order.STATUS_DELIVERED)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        orderDAO.createOrder(order3);
        logger.info("Created Order 3 (DELIVERED) for {}", customer1.getEmail());
    }
}
