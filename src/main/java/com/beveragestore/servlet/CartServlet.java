package com.beveragestore.servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * CartServlet - Draft version - chưa hoàn chỉnh
 * TODO: hoàn thiện logic sau khi thảo luận với nhóm
 */
public class CartServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        List cart = (List) session.getAttribute("cartItems");
        if (cart == null) cart = new ArrayList<>();
        request.setAttribute("cartItems", cart);
        request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: xử lý thêm/xóa sản phẩm khỏi giỏ
        response.sendRedirect(request.getContextPath() + "/cart");
    }
}
