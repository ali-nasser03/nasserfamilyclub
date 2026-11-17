package com.team.football_manager.controller;

import com.team.football_manager.config.SessionKeys;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import com.team.football_manager.util.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UserRepository users;

    public CurrentUser(UserRepository users) {
        this.users = users;
    }

    public User get(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute(SessionKeys.USER_ID);
        if (!(id instanceof Long userId)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "سجل دخول أولاً");
        }
        return users.findById(userId).orElseThrow(() -> {
            // The player was deleted while signed in
            session.invalidate();
            return new ApiException(HttpStatus.UNAUTHORIZED, "حسابك غير موجود، سجل دخول مرة ثانية");
        });
    }

    /** Starts a fresh session (new id) for the user. */
    public void signIn(HttpServletRequest request, User user) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        session.setAttribute(SessionKeys.IS_ADMIN, user.isAdmin());
    }

    public void signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
