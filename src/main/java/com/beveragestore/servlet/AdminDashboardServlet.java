package com.beveragestore.servlet;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * admindashboardservlet - bản nháp trang tổng quan nè
 * TODO: cần tích hợp câu truy vấn doanh thu từ firestore
 */
public class AdminDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: cần đếm số đơn hàng, sản phẩm và user nữa
        request.setAttribute("totalOrders", 0);
        request.setAttribute("totalRevenue", 0.0);
        request.setAttribute("totalProducts", 0);
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }
}
