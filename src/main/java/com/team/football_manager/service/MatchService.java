package com.team.football_manager.service;

import com.team.football_manager.model.Attendance;
import com.team.football_manager.model.Match;
import com.team.football_manager.repository.AttendanceRepository;
import com.team.football_manager.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private static final ZoneId PALESTINE_ZONE = ZoneId.of("Asia/Jerusalem");

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
        LocalDateTime now = LocalDateTime.now(PALESTINE_ZONE);

        System.out.println("🔥 SCHEDULER RUNNING...");
        System.out.println("🕒 Palestine time now: " + now);

        List<Match> activeMatches = matchRepository.findByIsActiveTrue();

        for (Match match : activeMatches) {
            System.out.println("📌 Checking match: " + match.getLocation() + " at " + match.getDateTime());

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
