package com.team.football_manager.controller;

import com.team.football_manager.model.User;
import com.team.football_manager.service.TokenService;
import com.team.football_manager.util.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Who is calling: read from the X-Auth-Token header the page sends.
 * (We don't use cookies: Safari on iPhone didn't keep the sign-in cookie.)
 */
@Component
public class CurrentUser {

    public static final String HEADER = "X-Auth-Token";
    private static final String ATTRIBUTE = CurrentUser.class.getName();

    private final TokenService tokens;

    public CurrentUser(TokenService tokens) {
        this.tokens = tokens;
    }

    /** The signed-in user, looked up once per request. */
    public Optional<User> find(HttpServletRequest request) {
        Object cached = request.getAttribute(ATTRIBUTE);
        if (cached instanceof User user) {
            return Optional.of(user);
        }
        Optional<User> user = tokens.resolve(request.getHeader(HEADER));
        user.ifPresent(u -> request.setAttribute(ATTRIBUTE, u));
        return user;
    }

    public User get(HttpServletRequest request) {
        return find(request).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "سجل دخول أولاً"));
    }

    /** Returns the new token the page must keep. */
    public String signIn(User user) {
        return tokens.issue(user);
    }

    public void signOut(HttpServletRequest request) {
        tokens.revoke(request.getHeader(HEADER));
    }
}
