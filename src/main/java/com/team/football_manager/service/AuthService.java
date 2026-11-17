package com.team.football_manager.service;

import com.team.football_manager.dto.LoginRequest;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import com.team.football_manager.util.ApiException;
import com.team.football_manager.util.Names;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_TIME = Duration.ofMinutes(10);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    /** Failed admin password attempts per account (in memory, resets on restart). */
    private final Map<Long, FailedAttempts> failedAttempts = new ConcurrentHashMap<>();

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Players sign in with their name only. The admin account also needs its password:
     * the first call without a password answers PASSWORD_REQUIRED so the page can ask for it.
     */
    @Transactional
    public User login(LoginRequest request) {
        String name = Names.clean(request.name());
        if (name.isEmpty()) {
            throw ApiException.badRequest("اكتب اسمك");
        }

        User user = users.findByFullName(name)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                        "الاسم غير مسجل. سجّل كلاعب جديد أولاً"));

        if (!user.isAdmin()) {
            return user;
        }

        String password = request.password();
        if (password == null || password.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "PASSWORD_REQUIRED", "أدخل كلمة السر");
        }

        checkNotLocked(user.getId());

        if (!passwordMatches(user, password)) {
            registerFailure(user.getId());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "WRONG_PASSWORD", "كلمة السر غير صحيحة");
        }

        failedAttempts.remove(user.getId());
        return user;
    }

    private boolean passwordMatches(User admin, String password) {
        String stored = admin.getPasswordHash();
        if (stored == null || stored.isEmpty()) {
            return false;
        }

        if (isBcrypt(stored)) {
            return passwordEncoder.matches(password, stored);
        }

        // Old database rows keep the password as plain text: accept it once, then store it hashed.
        if (stored.equals(password)) {
            admin.setPasswordHash(passwordEncoder.encode(password));
            users.save(admin);
            return true;
        }
        return false;
    }

    static boolean isBcrypt(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private void checkNotLocked(Long userId) {
        FailedAttempts attempts = failedAttempts.get(userId);
        if (attempts != null && attempts.lockedUntil != null && Instant.now().isBefore(attempts.lockedUntil)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "LOCKED",
                    "محاولات كثيرة غلط. جرّب بعد " + LOCK_TIME.toMinutes() + " دقائق");
        }
    }

    private void registerFailure(Long userId) {
        failedAttempts.compute(userId, (id, current) -> {
            FailedAttempts next = current == null || current.lockedUntil != null ? new FailedAttempts() : current;
            next.count++;
            if (next.count >= MAX_FAILED_ATTEMPTS) {
                next.lockedUntil = Instant.now().plus(LOCK_TIME);
            }
            return next;
        });
    }

    private static final class FailedAttempts {
        int count;
        Instant lockedUntil;
    }
}
