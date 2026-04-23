package com.team.football_manager.service;

import com.team.football_manager.model.Attendance;
import com.team.football_manager.model.Match;
import com.team.football_manager.repository.AttendanceRepository;
import com.team.football_manager.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    public Match createMatch(Match match) {
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        for (Match m : activeMatches) {
            m.setActive(false);
        }
        matchRepository.saveAll(activeMatches);

        match.setActive(true);
        return matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Match getLatestActiveMatch() {
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        if (activeMatches.isEmpty()) {
            return null;
        }
        return activeMatches.get(0);
    }

    @Scheduled(fixedRate = 10000)
    public void deactivateExpiredMatches() {
        
        System.out.println("🔥 SCHEDULER RUNNING...");
        
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        LocalDateTime now = LocalDateTime.now();

        for (Match match : activeMatches) {
            if (match.getDateTime() != null && !match.getDateTime().isAfter(now)) {
                match.setActive(false);
                matchRepository.save(match);

                List<Attendance> attendanceList = attendanceRepository.findByMatchId(match.getId());
                for (Attendance attendance : attendanceList) {
                    attendance.setStatus(null); // تصفير التصويت فقط
                }
                attendanceRepository.saveAll(attendanceList);

                System.out.println("✅ Match expired and votes reset: " + match.getLocation());
            }
        }
    }
}
