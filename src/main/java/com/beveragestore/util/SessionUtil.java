package com.beveragestore.util;

import javax.servlet.http.HttpSession;

import com.beveragestore.model.User;

/**
 * class tiện ích để quản lý session (phiên làm việc).
 * cung cấp các hàm helper để xử lý session của user.
 */
public class SessionUtil {
    private static final String SESSION_KEY_USER = "loggedInUser";

    /**
     * lưu user vào session sau khi đăng nhập thành công
     */
    public static void setUserInSession(HttpSession session, User user) {
        session.setAttribute(SESSION_KEY_USER, user);
    }

    /**
     * lấy thông tin user đã đăng nhập từ session
     */
    public static User getUserFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(SESSION_KEY_USER);
    }

    /**
     * check xem người dùng đã đăng nhập chưa
     */
    public static boolean isUserLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(SESSION_KEY_USER) != null;
    }

    /**
     * check xem user đang đăng nhập có phải admin không
     */
    public static boolean isAdmin(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null && user.isAdmin();
    }

    /**
     * check xem user đang đăng nhập có phải khách hàng (customer) không
     */
    public static boolean isCustomer(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null && user.isCustomer();
    }

    /**
     * check xem user đang đăng nhập có phải shipper không
     */
    public static boolean isShipper(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null && user.isShipper();
    }

    /**
     * check xem user đang đăng nhập có phải chủ shop không
     */
    public static boolean isShopOwner(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null && user.isShopOwner();
    }

    /**
     * lấy id của user đang lưu trong session
     */
    public static String getUserId(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null ? user.getUid() : null;
    }

    /**
     * xóa sạch session của user (đăng xuất)
     */
    public static void clearUserSession(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_KEY_USER);
            session.invalidate();
        }
    }
}
