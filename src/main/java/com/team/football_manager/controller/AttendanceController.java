package com.team.football_manager.controller;

import com.team.football_manager.model.Attendance;
import com.team.football_manager.model.Match;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.AttendanceRepository;
import com.team.football_manager.repository.MatchRepository;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MatchRepository matchRepository;

    // جلب القائمة مع ترتيب الحالات (حاضر، معتذر، لم يصوت)
    @GetMapping("/active-list")
    public List<Map<String, Object>> getActiveAttendance() {
        List<User> allUsers = userRepository.findAll();
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        Match currentMatch = activeMatches.isEmpty() ? null : activeMatches.get(0);

        return allUsers.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getFullName());

            if (currentMatch != null) {
                Attendance att = attendanceRepository.findAll().stream()
                        .filter(a -> a.getMatch().getId().equals(currentMatch.getId()) &&
                                a.getPlayer().getId().equals(user.getId()))
                        .findFirst().orElse(null);

                if (att == null || att.getStatus() == null || att.getStatus().isEmpty()) {
                    map.put("status", "لم يصوت");
                    map.put("paid", att != null && att.isPaid()); // الاحتفاظ بحالة الدفع حتى لو لم يصوت
                } else {
                    map.put("status", "YES".equals(att.getStatus()) ? "✅ سأحضر" : "❌ معتذر");
                    map.put("paid", att.isPaid());
                }
            } else {
                map.put("status", "لا توجد مباراة");
                map.put("paid", false);
            }
            return map;
        }).collect(Collectors.toList());
    }

    // ⭐ الدالة الجديدة: تصفير الحضور للمباراة الحالية (تُستدعى عند نشر مباراة جديدة)
    @PostMapping("/reset-all-votes")
    public ResponseEntity<String> resetAllVotes() {
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        if (activeMatches.isEmpty()) return ResponseEntity.ok("No active match to reset");

        Match currentMatch = activeMatches.get(0);
        List<Attendance> currentAttendance = attendanceRepository.findByMatchId(currentMatch.getId());

        // نمسح حالات الحضور فقط ونبقي على سجل الدفع إذا أردت
        for (Attendance att : currentAttendance) {
            att.setStatus(null); // تصفير التصويت
        }
        attendanceRepository.saveAll(currentAttendance);

        return ResponseEntity.ok("Success: Attendance Reset");
    }

    @PostMapping("/register")
    public String registerAttendance(@RequestBody Map<String, String> data) {
        String playerName = data.get("playerName");
        String status = data.get("status");

        User player = userRepository.findByFullName(playerName).orElse(null);
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();

        if (player == null || activeMatches.isEmpty()) return "Error: No Active Match";

        Match currentMatch = activeMatches.get(0);
        Attendance attendance = attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer().getId().equals(player.getId()) &&
                        a.getMatch().getId().equals(currentMatch.getId()))
                .findFirst().orElse(new Attendance());

        attendance.setPlayer(player);
        attendance.setMatch(currentMatch);
        attendance.setStatus(status);
        attendanceRepository.save(attendance);
        return "Success";
    }

    @PostMapping("/toggle-payment/{playerId}")
    public String togglePayment(@PathVariable Long playerId) {
        User player = userRepository.findById(playerId).orElse(null);
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        if (player == null || activeMatches.isEmpty()) return "Error";

        Attendance att = attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer().getId().equals(playerId) &&
                        a.getMatch().getId().equals(activeMatches.get(0).getId()))
                .findFirst().orElse(new Attendance());

        if (att.getId() == null) {
            att.setPlayer(player);
            att.setMatch(activeMatches.get(0));
        }
        att.setPaid(!att.isPaid());
        attendanceRepository.save(att);
        return "Updated";
    }

    @PostMapping("/reset-month")
    public String resetAllPayments() {
        List<Attendance> all = attendanceRepository.findAll();
        for (Attendance a : all) { a.setPaid(false); }
        attendanceRepository.saveAll(all);
        return "Done";
    }
    @PostMapping("/remove")
public void removeVote(@RequestBody Map<String, String> body) {
    String name = body.get("playerName");

    Attendance att = attendanceRepository.findByPlayerName(name);
    if (att != null) {
        att.setStatus("WAIT");
        attendanceRepository.save(att);
    }
}

    @GetMapping("/history")
    public Map<String, List<String>> getMatchHistory() {
        List<Attendance> all = attendanceRepository.findAll();
        Map<String, List<String>> history = new HashMap<>();

        for (Attendance att : all) {
            if ("YES".equals(att.getStatus())) {
                String matchKey = att.getMatch().getLocation() + " (" + att.getMatch().getDateTime().toString().replace("T", " ") + ")";
                history.computeIfAbsent(matchKey, k -> new ArrayList<>()).add(att.getPlayer().getFullName());
            }
        }
        return history;
    }
}
