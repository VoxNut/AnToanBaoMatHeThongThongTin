package com.beveragestore.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import java.io.IOException;
import java.util.Locale;

@WebFilter("/*")
public class LocaleFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();

        // lấy ngôn ngữ từ session, mặc định là tiếng anh "en"
        String lang = (String) session.getAttribute("lang");
        if (lang == null || lang.isEmpty()) {
            lang = "en";
        }

        Locale locale = new Locale(lang);

        // thiết lập locale trên cả session và request scope để đảm bảo chuẩn xác nhất
        Config.set(session, Config.FMT_LOCALE, locale);
        Config.set(request, Config.FMT_LOCALE, locale);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
