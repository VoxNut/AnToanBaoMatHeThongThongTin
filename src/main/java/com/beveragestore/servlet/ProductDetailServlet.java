package com.beveragestore.servlet;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beveragestore.dao.ProductDAO;
import com.beveragestore.model.Product;

/**
 * Servlet for viewing product details (public page).
 * GET param: id (product ID)
 */
public class ProductDetailServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProductDetailServlet.class);
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String productId = request.getParameter("id");

            if (productId == null || productId.trim().isEmpty()) {
                logger.warn("Product detail request without product ID");
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }

            Product product = productDAO.getProductById(productId);

            if (product == null || !product.isActive()) {
                logger.warn("Requested product not found: {}", productId);
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }

            request.setAttribute("product", product);
            request.getRequestDispatcher("/WEB-INF/views/product-detail.jsp").forward(request, response);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error retrieving product details", e);
            request.setAttribute("error", "Error loading product. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
