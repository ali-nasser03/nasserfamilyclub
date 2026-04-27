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

        List<User> players = userRepository.findAll()
                .stream()
                .filter(user -> !"ADMIN".equalsIgnoreCase(user.getRole()))
                .collect(Collectors.toList());

        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        Match currentMatch = activeMatches.isEmpty() ? null : activeMatches.get(0);

        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : players) {

            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getFullName());

            if (currentMatch != null) {
                Attendance att = attendanceRepository.findAll().stream()
                        .filter(a -> a.getMatch() != null
                                && a.getPlayer() != null
                                && a.getMatch().getId().equals(currentMatch.getId())
                                && a.getPlayer().getId().equals(user.getId()))
                        .findFirst()
                        .orElse(null);

                map.put("status", (att == null || att.getStatus() == null) ? "لم يصوت" : att.getStatus());
                map.put("paid", att != null && att.isPaid());
            } else {
                map.put("status", "لا توجد مباراة");
                map.put("paid", false);
            }

            result.add(map);
        }

        return result;
    }

    @PostMapping("/register")
    public String registerAttendance(@RequestBody Map<String, String> data) {
        String playerName = data.get("playerName");
        String status = data.get("status");

        User player = userRepository.findByFullName(playerName).orElse(null);
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();

        if (player == null || activeMatches.isEmpty()) {
            return "Error";
        }

        Match currentMatch = activeMatches.get(0);

        Attendance attendance = attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer() != null
                        && a.getMatch() != null
                        && a.getPlayer().getId().equals(player.getId())
                        && a.getMatch().getId().equals(currentMatch.getId()))
                .findFirst()
                .orElse(new Attendance());

        attendance.setPlayer(player);
        attendance.setMatch(currentMatch);
        attendance.setStatus(status);
        attendanceRepository.save(attendance);

        return "Success";
    }

    @PostMapping("/remove")
    public String removeVote(@RequestBody Map<String, String> data) {
        String playerName = data.get("playerName");

        User player = userRepository.findByFullName(playerName).orElse(null);
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();

        if (player == null || activeMatches.isEmpty()) {
            return "Error";
        }

        Match currentMatch = activeMatches.get(0);

        Attendance attendance = attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer() != null
                        && a.getMatch() != null
                        && a.getPlayer().getId().equals(player.getId())
                        && a.getMatch().getId().equals(currentMatch.getId()))
                .findFirst()
                .orElse(null);

        if (attendance == null) return "No Vote";

        attendance.setStatus(null);
        attendanceRepository.save(attendance);

        return "Removed";
    }

    @PostMapping("/toggle-payment/{playerId}")
    public String togglePayment(@PathVariable Long playerId) {

        User player = userRepository.findById(playerId).orElse(null);
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();

        if (player == null) return "Error";

        Match currentMatch = activeMatches.isEmpty() ? null : activeMatches.get(0);

        Attendance att = attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer() != null
                        && a.getMatch() != null
                        && currentMatch != null
                        && a.getPlayer().getId().equals(playerId)
                        && a.getMatch().getId().equals(currentMatch.getId()))
                .findFirst()
                .orElse(new Attendance());

        if (att.getId() == null) {
            att.setPlayer(player);
            if (currentMatch != null) {
                att.setMatch(currentMatch);
            }
        }

        att.setPaid(!att.isPaid());
        attendanceRepository.save(att);

        return "Updated";
    }
}
