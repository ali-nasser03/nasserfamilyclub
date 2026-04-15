package com.team.football_manager.repository;

import com.team.football_manager.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByIsActiveTrue(); // لجلب المباراة القادمة فقط
}