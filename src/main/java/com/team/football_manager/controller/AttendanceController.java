package com.team.football_manager.controller;

import com.team.football_manager.model.Attendance;
import com.team.football_manager.model.Match;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.AttendanceRepository;
import com.team.football_manager.repository.MatchRepository;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    // 🔥 القائمة الرئيسية
    @GetMapping("/active-list")
    public List<Map<String, Object>> getActiveAttendance() {

        List<User> users = userRepository.findAll();
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        Match currentMatch = activeMatches.isEmpty() ? null : activeMatches.get(0);

        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {

            Map<String, Object> map = new HashMap<>();

            map.put("id", user.getId());
            map.put("name", user.getFullName());
            map.put("role", user.getRole());
            map.put("working", user.isWorking());

            Attendance att = null;

            if (currentMatch != null) {
                att = attendanceRepository.findAll().stream()
                        .filter(a ->
                                a.getPlayer().getId().equals(user.getId()) &&
                                a.getMatch().getId().equals(currentMatch.getId())
                        )
                        .findFirst()
                        .orElse(null);
            }

            if (att != null) {
                map.put("paid", att.isPaid());
                map.put("status", att.getStatus());
            } else {
                map.put("paid", false);
                map.put("status", null);
            }

            result.add(map);
        }

        return result;
    }

    // الدفع
    @PostMapping("/toggle-payment/{id}")
    public void togglePayment(@PathVariable Long id) {
        Attendance a = attendanceRepository.findById(id).orElse(null);
        if (a != null) {
            a.setPaid(!a.isPaid());
            attendanceRepository.save(a);
        }
    }

    // التصويت
    @PostMapping("/register")
    public void vote(@RequestBody Map<String, String> body) {

        String name = body.get("playerName");
        String status = body.get("status");

        User user = userRepository.findAll()
                .stream()
                .filter(u -> u.getFullName().equals(name))
                .findFirst()
                .orElse(null);

        Match match = matchRepository.findByIsActiveTrue().stream().findFirst().orElse(null);

        if (user == null || match == null) return;

        Attendance att = attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer().getId().equals(user.getId())
                        && a.getMatch().getId().equals(match.getId()))
                .findFirst()
                .orElse(null);

        if (att == null) {
            att = new Attendance();
            att.setPlayer(user);
            att.setMatch(match);
        }

        att.setStatus(status);
        attendanceRepository.save(att);
    }

    // حذف التصويت
    @PostMapping("/remove")
    public void removeVote(@RequestBody Map<String, String> body) {

        String name = body.get("playerName");

        User user = userRepository.findAll()
                .stream()
                .filter(u -> u.getFullName().equals(name))
                .findFirst()
                .orElse(null);

        Match match = matchRepository.findByIsActiveTrue().stream().findFirst().orElse(null);

        if (user == null || match == null) return;

        attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer().getId().equals(user.getId())
                        && a.getMatch().getId().equals(match.getId()))
                .forEach(a -> {
                    a.setStatus(null);
                    attendanceRepository.save(a);
                });
    }
}
