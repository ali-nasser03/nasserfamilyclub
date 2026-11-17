package com.team.football_manager.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * Protects the API:
 *  - /api/admin/**  -> only a signed-in admin
 *  - other /api/**  -> any signed-in user (except /api/auth/**, which is open)
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.startsWith("/api/auth/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(SessionKeys.USER_ID);

        if (userId == null) {
            return reject(response, 401, "سجل دخول أولاً");
        }

        if (path.startsWith("/api/admin/") && !Boolean.TRUE.equals(session.getAttribute(SessionKeys.IS_ADMIN))) {
            return reject(response, 403, "هذه الصفحة للإدارة فقط");
        }

        return true;
    }

    private boolean reject(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"error\":\"" + message + "\"}");
        return false;
    }
}
