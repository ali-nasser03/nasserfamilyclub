package com.team.football_manager.config;

import com.team.football_manager.model.Role;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import com.team.football_manager.service.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once on every startup (all steps are safe to repeat):
 *  1. Moves data from the old database layout to the new one, so nothing is lost after the update.
 *  2. Creates the admin account from ADMIN_NAME / ADMIN_PASSWORD if there is no admin yet.
 */
@Component
public class StartupTasks implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupTasks.class);

    private final JdbcTemplate jdbc;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Value("${ADMIN_NAME:Admin}")
    private String adminName;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public StartupTasks(JdbcTemplate jdbc, UserRepository users, PasswordEncoder passwordEncoder, Clock clock) {
        this.jdbc = jdbc;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrateOldSchema();
        createAdminIfMissing();
    }

    private void migrateOldSchema() {
        // Roles were free text before; make them match the enum (ADMIN / PLAYER).
        step("normalize roles", "UPDATE app_users SET role = UPPER(TRIM(role)) WHERE role IS NOT NULL AND role <> UPPER(TRIM(role))");
        step("default role", "UPDATE app_users SET role = 'PLAYER' WHERE role IS NULL OR role = ''");

        // Votes used to be "reset" by setting status to NULL; now no row means "not voted".
        step("drop empty votes", "DELETE FROM attendance WHERE status IS NULL OR status NOT IN ('YES', 'NO') OR match_id IS NULL OR player_id IS NULL");

        // Keep only the newest vote per player and match (needed for the unique constraint).
        step("dedupe votes", "DELETE FROM attendance a USING attendance b "
                + "WHERE a.player_id = b.player_id AND a.match_id = b.match_id AND a.id < b.id");

        // Payments used to live on the attendance rows (is_paid). Move them to the payments table for this month.
        if (columnExists("attendance", "is_paid")) {
            String month = clock.currentMonth();
            step("move old payments", "INSERT INTO payments (player_id, pay_month, paid_at) "
                    + "SELECT DISTINCT a.player_id, '" + month + "', NOW() FROM attendance a "
                    + "WHERE a.is_paid = TRUE AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.player_id = a.player_id AND p.pay_month = '" + month + "')");
            step("drop attendance.is_paid", "ALTER TABLE attendance DROP COLUMN is_paid");
        }

        // "username" was always a copy of full_name.
        if (columnExists("app_users", "username")) {
            step("drop app_users.username", "ALTER TABLE app_users DROP COLUMN username");
        }
    }

    private void createAdminIfMissing() {
        boolean hasAdmin = users.findAll().stream().anyMatch(User::isAdmin);
        if (hasAdmin) {
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("No admin account exists. Set ADMIN_PASSWORD (and optionally ADMIN_NAME) to create one.");
            return;
        }
        User admin = new User();
        admin.setFullName(adminName.trim());
        admin.setRole(Role.ADMIN);
        admin.setWorking(false);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        users.save(admin);
        log.info("Admin account '{}' created", admin.getFullName());
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                Integer.class, table, column);
        return count != null && count > 0;
    }

    private void step(String name, String sql) {
        try {
            int rows = jdbc.update(sql);
            if (rows > 0) {
                log.info("Migration '{}': {} row(s)", name, rows);
            }
        } catch (Exception e) {
            log.warn("Migration '{}' skipped: {}", name, e.getMessage());
        }
    }
}
