package com.team.football_manager.config;

import com.team.football_manager.controller.CurrentUser;
import com.team.football_manager.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * Protects the API:
 *  - /api/admin/**  -> only a signed-in admin
 *  - other /api/**  -> any signed-in user (except /api/auth/**, which is open)
 */
public class AuthInterceptor implements HandlerInterceptor {

    private final CurrentUser currentUser;

    public AuthInterceptor(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Never let a browser cache API answers. Safari on iPhone kept an old "signed in" answer
        // after the server restarted, which bounced the login page back and forth.
        response.setHeader("Cache-Control", "no-store");

        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (path.startsWith("/api/auth/")) {
            return true;
        }

        User user = currentUser.find(request).orElse(null);

        if (user == null) {
            return reject(response, 401, "سجل دخول أولاً");
        }

        if (path.startsWith("/api/admin/") && !user.isAdmin()) {
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
