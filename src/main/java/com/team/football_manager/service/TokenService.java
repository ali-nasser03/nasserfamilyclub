package com.team.football_manager.service;

import com.team.football_manager.model.AuthToken;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.AuthTokenRepository;
import com.team.football_manager.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class TokenService {

    /** How long a sign-in lasts before the player has to type their name again. */
    private static final Duration LIFETIME = Duration.ofDays(90);

    private final SecureRandom random = new SecureRandom();
    private final AuthTokenRepository tokens;
    private final UserRepository users;

    public TokenService(AuthTokenRepository tokens, UserRepository users) {
        this.tokens = tokens;
        this.users = users;
    }

    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        tokens.save(new AuthToken(token, users.getReferenceById(user.getId())));
        return token;
    }

    /** The user behind a token, if the token exists and hasn't expired. */
    @Transactional(readOnly = true)
    public Optional<User> resolve(String token) {
        if (token == null || token.length() != 64) {
            return Optional.empty();
        }
        Instant oldestValid = Instant.now().minus(LIFETIME);
        return tokens.findByToken(token)
                .filter(t -> t.getCreatedAt().isAfter(oldestValid))
                .flatMap(t -> users.findById(t.getUser().getId()));
    }

    @Transactional
    public void revoke(String token) {
        if (token != null) {
            tokens.deleteByToken(token);
        }
    }

    @Transactional
    public void revokeAllFor(Long userId) {
        tokens.deleteByUser_Id(userId);
    }

    /** Daily cleanup of expired tokens. */
    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000L, initialDelay = 60_000)
    @Transactional
    public void deleteExpired() {
        tokens.deleteByCreatedAtBefore(Instant.now().minus(LIFETIME));
    }
}
