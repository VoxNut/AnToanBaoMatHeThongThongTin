package com.beveragestore.servlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AdminDashboardServlet - bản nháp trang tổng quan
 * TODO: tích hợp query thống kê doanh thu từ Firestore
 */
public class AdminDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: fetch orders, products, users count
        request.setAttribute("totalOrders", 0);
        request.setAttribute("totalRevenue", 0.0);
        request.setAttribute("totalProducts", 0);
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }
}
