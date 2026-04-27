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

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @GetMapping("/active-list")
    public List<Map<String, Object>> getActiveAttendance() {
        List<User> allUsers = userRepository.findAll();
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        Match currentMatch = activeMatches.isEmpty() ? null : activeMatches.get(0);

        return allUsers.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getFullName());
            map.put("isExempt", user.isExempt()); // إرسال حالة الإعفاء

            if (currentMatch != null) {
                Attendance att = attendanceRepository.findAll().stream()
                        .filter(a -> a.getMatch() != null && a.getPlayer() != null
                                && a.getMatch().getId().equals(currentMatch.getId())
                                && a.getPlayer().getId().equals(user.getId()))
                        .findFirst().orElse(null);

                map.put("status", (att == null || att.getStatus() == null) ? "لم يصوت" : 
                       ("YES".equals(att.getStatus()) ? "✅ سأحضر" : "❌ معتذر"));
                map.put("paid", att != null && att.isPaid());
            } else {
                map.put("status", "لا توجد مباراة");
                map.put("paid", false);
            }
            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/toggle-payment/{playerId}")
    public String togglePayment(@PathVariable Long playerId) {
        User user = userRepository.findById(playerId).orElse(null);
        if (user == null || user.isExempt()) return "Exempt";

        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        Attendance att;

        if (!activeMatches.isEmpty()) {
            Match currentMatch = activeMatches.get(0);
            att = attendanceRepository.findAll().stream()
                    .filter(a -> a.getPlayer() != null && a.getMatch() != null
                            && a.getPlayer().getId().equals(playerId)
                            && a.getMatch().getId().equals(currentMatch.getId()))
                    .findFirst().orElse(new Attendance());
            if (att.getId() == null) { att.setPlayer(user); att.setMatch(currentMatch); }
        } else {
            att = attendanceRepository.findAll().stream()
                    .filter(a -> a.getPlayer() != null && a.getPlayer().getId().equals(playerId))
                    .reduce((f, s) -> s).orElse(new Attendance());
            if (att.getId() == null) att.setPlayer(user);
        }

        att.setPaid(!att.isPaid());
        attendanceRepository.save(att);
        return "Updated";
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> data) {
        User player = userRepository.findByFullName(data.get("playerName")).orElse(null);
        List<Match> active = matchRepository.findByIsActiveTrue();
        if (player == null || active.isEmpty()) return "Error";
        
        Attendance a = attendanceRepository.findAll().stream()
                .filter(att -> att.getPlayer().getId().equals(player.getId()) && att.getMatch().getId().equals(active.get(0).getId()))
                .findFirst().orElse(new Attendance());
        
        a.setPlayer(player); a.setMatch(active.get(0)); a.setStatus(data.get("status"));
        attendanceRepository.save(a);
        return "Success";
    }

    @PostMapping("/remove")
    public String removeVote(@RequestBody Map<String, String> data) {
        User player = userRepository.findByFullName(data.get("playerName")).orElse(null);
        List<Match> active = matchRepository.findByIsActiveTrue();
        if (player == null || active.isEmpty()) return "Error";

        Attendance a = attendanceRepository.findAll().stream()
                .filter(att -> att.getPlayer().getId().equals(player.getId()) && att.getMatch().getId().equals(active.get(0).getId()))
                .findFirst().orElse(null);

        if (a != null) { a.setStatus(null); attendanceRepository.save(a); }
        return "Removed";
    }

    @PostMapping("/reset-month")
    public String resetPayments() {
        List<Attendance> all = attendanceRepository.findAll();
        all.forEach(a -> a.setPaid(false));
        attendanceRepository.saveAll(all);
        return "Done";
    }

    @PostMapping("/reset-all-votes")
    public ResponseEntity<String> resetVotes() {
        List<Match> active = matchRepository.findByIsActiveTrue();
        if (active.isEmpty()) return ResponseEntity.ok("No Match");
        List<Attendance> current = attendanceRepository.findByMatchId(active.get(0).getId());
        current.forEach(a -> a.setStatus(null));
        attendanceRepository.saveAll(current);
        return ResponseEntity.ok("Reset Done");
    }

    @GetMapping("/history")
    public Map<String, List<String>> getHistory() {
        Map<String, List<String>> history = new HashMap<>();
        attendanceRepository.findAll().stream().filter(a -> a.getMatch() != null && "YES".equals(a.getStatus())).forEach(a -> {
            String key = a.getMatch().getLocation() + " (" + a.getMatch().getDateTime().toString().replace("T", " ") + ")";
            history.computeIfAbsent(key, k -> new ArrayList<>()).add(a.getPlayer().getFullName());
        });
        return history;
    }
}
