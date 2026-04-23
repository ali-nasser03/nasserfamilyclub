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
        // قبل إنشاء مباراة جديدة، اغلق أي مباراة مفعلة قديمة
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

    // كل دقيقة افحص إذا المباراة انتهى وقتها
    @Scheduled(fixedRate = 60000)
    public void deactivateExpiredMatches() {
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        LocalDateTime now = LocalDateTime.now();

        for (Match match : activeMatches) {
            if (match.getDateTime() != null && !match.getDateTime().isAfter(now)) {
                // 1) إلغاء تفعيل المباراة
                match.setActive(false);
                matchRepository.save(match);

                // 2) تصفير التصويتات الخاصة بها
                List<Attendance> attendanceList = attendanceRepository.findByMatchId(match.getId());
                for (Attendance attendance : attendanceList) {
                    attendance.setStatus(null);
                }
                attendanceRepository.saveAll(attendanceList);
            }
        }
    }
}
