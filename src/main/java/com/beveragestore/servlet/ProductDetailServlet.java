package com.beveragestore.servlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.beveragestore.model.Product;

/**
 * ProductDetailServlet - Draft version - chưa hoàn chỉnh
 * TODO: hoàn thiện logic sau khi thảo luận với nhóm
 */
public class ProductDetailServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        // TODO: query Firestore theo id
        Product p = new Product();
        p.setProductId(id);
        request.setAttribute("product", p);
        request.getRequestDispatcher("/WEB-INF/views/product-detail.jsp").forward(request, response);
    }
}
