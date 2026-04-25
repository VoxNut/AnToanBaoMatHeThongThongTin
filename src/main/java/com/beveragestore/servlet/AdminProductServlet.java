package com.beveragestore.servlet;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.dao.ProductDAO;
import com.beveragestore.model.Product;
import com.beveragestore.util.SessionUtil;

public class AdminProductServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminProductServlet.class);
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!SessionUtil.isAdmin(request.getSession())) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            String action = request.getParameter("action");

            if ("create".equals(action)) {
                // Show create form
                request.setAttribute("mode", "create");
                request.getRequestDispatcher("/WEB-INF/views/admin/product-form.jsp").forward(request, response);
                return;
            } else if ("edit".equals(action)) {
                String productId = request.getParameter("id");
                if (productId != null) {
                    Product product = productDAO.getProductById(productId);
                    if (product != null) {
                        request.setAttribute("product", product);
                        request.setAttribute("mode", "edit");
                        request.getRequestDispatcher("/WEB-INF/views/admin/product-form.jsp").forward(request, response);
                        return;
                    }
                }
            }

            // Default: List all products
            List<Product> products = productDAO.getAllProducts();
            request.setAttribute("products", products);
            request.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(request, response);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error loading products", e);
            request.setAttribute("error", "Error loading products. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!SessionUtil.isAdmin(request.getSession())) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            String action = request.getParameter("action");

            if ("create".equals(action)) {
                String name = request.getParameter("name");
                String category = request.getParameter("category");
                String brand = request.getParameter("brand");
                String description = request.getParameter("description");
                double price = Double.parseDouble(request.getParameter("price"));
                int stock = Integer.parseInt(request.getParameter("stock"));
                String imageUrl = request.getParameter("imageUrl");

                productDAO.createProduct(name, category, brand, description, price, stock, imageUrl);
                response.sendRedirect(request.getContextPath() + "/admin/products?success=Product created successfully");

            } else if ("update".equals(action)) {
                String productId = request.getParameter("productId");
                Product product = productDAO.getProductById(productId);
                
                if (product != null) {
                    product.setName(request.getParameter("name"));
                    product.setCategory(request.getParameter("category"));
                    product.setBrand(request.getParameter("brand"));
                    product.setDescription(request.getParameter("description"));
                    product.setPrice(Double.parseDouble(request.getParameter("price")));
                    product.setStock(Integer.parseInt(request.getParameter("stock")));
                    product.setImageUrl(request.getParameter("imageUrl"));
                    product.setActive("on".equals(request.getParameter("isActive")));

                    productDAO.updateProduct(product);
                    response.sendRedirect(request.getContextPath() + "/admin/products?success=Product updated successfully");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/products?error=Product not found");
                }

            } else if ("toggle_status".equals(action)) {
                String productId = request.getParameter("productId");
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    product.setActive(!product.isActive());
                    productDAO.updateProduct(product);
                    response.sendRedirect(request.getContextPath() + "/admin/products?success=Product status updated");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/products?error=Product not found");
                }
            }

        } catch (Exception e) {
            logger.error("Error saving product", e);
            response.sendRedirect(request.getContextPath() + "/admin/products?error=Failed to save product. Please check the inputs.");
        }
    }
}
