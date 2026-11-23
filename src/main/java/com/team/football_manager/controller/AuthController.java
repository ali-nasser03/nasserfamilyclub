package com.team.football_manager.controller;

import com.team.football_manager.dto.LoginRequest;
import com.team.football_manager.dto.MeResponse;
import com.team.football_manager.dto.RegisterRequest;
import com.team.football_manager.model.User;
import com.team.football_manager.service.AuthService;
import com.team.football_manager.service.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final PlayerService players;
    private final CurrentUser currentUser;

    public AuthController(AuthService auth, PlayerService players, CurrentUser currentUser) {
        this.auth = auth;
        this.players = players;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    public MeResponse login(@RequestBody LoginRequest body) {
        User user = auth.login(body);
        return toMe(user, currentUser.signIn(user));
    }

    @PostMapping("/register")
    public MeResponse register(@Valid @RequestBody RegisterRequest body) {
        User user = players.register(body);
        return toMe(user, currentUser.signIn(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        currentUser.signOut(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MeResponse me(HttpServletRequest request) {
        return toMe(currentUser.get(request), null);
    }

    private static MeResponse toMe(User user, String token) {
        return new MeResponse(user.getId(), user.getFullName(), user.isAdmin() ? "ADMIN" : "PLAYER", token);
    }
}
