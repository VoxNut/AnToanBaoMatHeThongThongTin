package com.beveragestore.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * SessionUtil - tiện ích quản lý phiên đăng nhập
 * Bản nháp: chưa xử lý role admin
 */
public class SessionUtil {
    public static String getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (String) session.getAttribute("userId") : null;
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getUserId(request) != null;
    }
}
