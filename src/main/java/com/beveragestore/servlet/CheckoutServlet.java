package com.beveragestore.servlet;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * CheckoutServlet - Draft version - chưa hoàn chỉnh
 * TODO: hoàn thiện logic sau khi thảo luận với nhóm
 */
public class CheckoutServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        List cartItems = (List) session.getAttribute("cartItems");
        request.setAttribute("cartItems", cartItems);
        request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: tạo đơn hàng và lưu Firestore
        String address = request.getParameter("address");
        if (address == null || address.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập địa chỉ");
            doGet(request, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/order-confirmation");
    }
}
