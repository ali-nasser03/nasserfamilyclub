package com.team.football_manager.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * A sign-in key given to a browser at login. The page keeps it and sends it with every request
 * (header X-Auth-Token). Stored in the database, so people stay signed in after a redeploy.
 */
@Entity
@Table(name = "auth_tokens")
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public AuthToken() {
    }

    public AuthToken(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
