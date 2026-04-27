package com.team.football_manager.repository;

import com.team.football_manager.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByMatchId(Long matchId);
    List<Attendance> findByPlayerId(Long playerId);
}
