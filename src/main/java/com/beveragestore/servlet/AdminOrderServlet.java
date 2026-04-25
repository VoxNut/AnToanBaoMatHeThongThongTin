package com.beveragestore.servlet;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AdminOrderServlet - Draft version - chưa hoàn chỉnh
 * TODO: hoàn thiện logic sau khi thảo luận với nhóm
 */
public class AdminOrderServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO: query Firestore lấy danh sách đơn hàng
        request.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String orderId = request.getParameter("orderId");
        // TODO: cập nhật trạng thái đơn hàng
        response.sendRedirect(request.getContextPath() + "/admin/orders");
    }
}
