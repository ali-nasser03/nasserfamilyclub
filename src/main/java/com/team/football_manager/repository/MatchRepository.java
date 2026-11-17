package com.team.football_manager.repository;

import com.team.football_manager.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findFirstByActiveTrueOrderByDateTimeDesc();

    List<Match> findByActiveTrue();

    List<Match> findByActiveTrueAndDateTimeLessThanEqual(LocalDateTime time);
}
