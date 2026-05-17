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

/**
 * servlet để xem danh sách sản phẩm (trang công khai).
 * hỗ trợ lọc theo danh mục và tìm kiếm theo tên.
 * các tham số get: category (tùy chọn), search (tùy chọn)
 */
public class ProductServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProductServlet.class);
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String category = request.getParameter("category");
            String search = request.getParameter("search");

            List<Product> products;
            String pageTitle = "All Beverages";

            if (search != null && !search.trim().isEmpty()) {
                // tìm kiếm theo tên sản phẩm
                products = productDAO.searchByName(search);
                pageTitle = "Search Results for: " + search;
                logger.info("Product search: {}", search);

            } else if (category != null && !category.trim().isEmpty()) {
                // lọc sản phẩm theo danh mục
                products = productDAO.getProductsByCategory(category);
                pageTitle = category + " Beverages";
                logger.info("Product filter by category: {}", category);

            } else {
                // lấy toàn bộ sản phẩm đang mở bán
                products = productDAO.getAllActiveProducts();
            }

            // lấy tất cả các danh mục để hiển thị vào thanh lọc
            List<String> categories = productDAO.getAllCategories();

            request.setAttribute("products", products);
            request.setAttribute("categories", categories);
            request.setAttribute("pageTitle", pageTitle);
            request.setAttribute("selectedCategory", category);
            request.setAttribute("searchTerm", search);

            request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request, response);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error retrieving products", e);
            request.setAttribute("error", "Error loading products. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
