package com.beveragestore.util;

import javax.servlet.http.HttpSession;

import com.beveragestore.model.User;

/**
 * Utility class for session management.
 * Provides helper methods for working with user sessions.
 */
public class SessionUtil {
    private static final String SESSION_KEY_USER = "loggedInUser";

    /**
     * Store user in session after successful login
     */
    public static void setUserInSession(HttpSession session, User user) {
        session.setAttribute(SESSION_KEY_USER, user);
    }

    /**
     * Retrieve logged-in user from session
     */
    public static User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute(SESSION_KEY_USER);
    }

    /**
     * Check if user is logged in
     */
    public static boolean isUserLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(SESSION_KEY_USER) != null;
    }

    /**
     * Check if logged-in user is an admin
     */
    public static boolean isAdmin(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null && user.isAdmin();
    }

    /**
     * Check if logged-in user is a customer
     */
    public static boolean isCustomer(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null && user.isCustomer();
    }

    /**
     * Check if logged-in user is a shipper
     */
    public static boolean isShipper(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null && user.isShipper();
    }

    /**
     * Check if logged-in user is a shop owner
     */
    public static boolean isShopOwner(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null && user.isShopOwner();
    }

    /**
     * Get user ID from session
     */
    public static String getUserId(HttpSession session) {
        User user = getUserFromSession(session);
        return user != null ? user.getUid() : null;
    }

    /**
     * Clear user session (logout)
     */
    public static void clearUserSession(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_KEY_USER);
            session.invalidate();
        }
    }
}
