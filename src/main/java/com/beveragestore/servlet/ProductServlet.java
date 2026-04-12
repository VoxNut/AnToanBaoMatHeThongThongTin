package com.beveragestore.servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.beveragestore.model.Product;

/**
 * ProductServlet - hiển thị danh sách sản phẩm - bản thử nghiệm
 * TODO: hoàn thiện logic sau khi thảo luận với nhóm
 */
public class ProductServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: thay bằng query Firestore
        List<Product> products = new ArrayList<>();
        products.add(new Product());
        request.setAttribute("products", products);
        request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request, response);
    }
}
