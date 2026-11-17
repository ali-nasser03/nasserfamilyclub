package com.team.football_manager.repository;

import com.team.football_manager.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByMatch_Id(Long matchId);

    Optional<Attendance> findByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    void deleteByPlayer_Id(Long playerId);
}
