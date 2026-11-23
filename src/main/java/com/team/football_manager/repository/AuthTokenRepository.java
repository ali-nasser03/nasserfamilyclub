package com.team.football_manager.repository;

import com.team.football_manager.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUser_Id(Long userId);

    void deleteByCreatedAtBefore(Instant time);
}
