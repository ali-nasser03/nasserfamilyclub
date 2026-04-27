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
        List<User> allUsers = userRepository.findAll()
                .stream()
                .filter(user -> !"ADMIN".equalsIgnoreCase(user.getRole()))
                .collect(Collectors.toList());

        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        Match currentMatch = activeMatches.isEmpty() ? null : activeMatches.get(0);

        return allUsers.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getFullName());
            map.put("role", user.getRole());

            if (currentMatch != null) {
                Attendance att = attendanceRepository.findAll().stream()
                        .filter(a -> a.getMatch() != null
                                && a.getPlayer() != null
                                && a.getMatch().getId().equals(currentMatch.getId())
                                && a.getPlayer().getId().equals(user.getId()))
                        .findFirst()
                        .orElse(null);

                if (att == null || att.getStatus() == null || att.getStatus().isEmpty()) {
                    map.put("status", "لم يصوت");
                    map.put("paid", att != null && att.isPaid());
                } else {
                    map.put("status", "YES".equals(att.getStatus()) ? "✅ سأحضر" : "❌ معتذر");
                    map.put("paid", att.isPaid());
                }
            } else {
                Attendance latestAttendance = attendanceRepository.findAll().stream()
                        .filter(a -> a.getPlayer() != null
                                && a.getPlayer().getId().equals(user.getId()))
                        .reduce((first, second) -> second)
                        .orElse(null);

                map.put("status", "لا توجد مباراة");
                map.put("paid", latestAttendance != null && latestAttendance.isPaid());
            }

            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/reset-all-votes")
    public ResponseEntity<String> resetAllVotes() {
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        if (activeMatches.isEmpty()) {
            return ResponseEntity.ok("No active match to reset");
        }

        Match currentMatch = activeMatches.get(0);
        List<Attendance> currentAttendance = attendanceRepository.findByMatchId(currentMatch.getId());

        for (Attendance att : currentAttendance) {
            att.setStatus(null);
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

        if (player == null || activeMatches.isEmpty()) {
            return "Error: No Active Match";
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
            return "Error: No Active Match";
        }

        Match currentMatch = activeMatches.get(0);

        Attendance attendance = attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer() != null
                        && a.getMatch() != null
                        && a.getPlayer().getId().equals(player.getId())
                        && a.getMatch().getId().equals(currentMatch.getId()))
                .findFirst()
                .orElse(null);

        if (attendance == null) {
            return "No Vote Found";
        }

        attendance.setStatus(null);
        attendanceRepository.save(attendance);

        return "Vote Removed";
    }

    @PostMapping("/toggle-payment/{playerId}")
    public String togglePayment(@PathVariable Long playerId) {
        User player = userRepository.findById(playerId).orElse(null);
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();

        if (player == null) {
            return "Error";
        }

        Attendance att;

        if (!activeMatches.isEmpty()) {
            Match currentMatch = activeMatches.get(0);

            att = attendanceRepository.findAll().stream()
                    .filter(a -> a.getPlayer() != null
                            && a.getMatch() != null
                            && a.getPlayer().getId().equals(playerId)
                            && a.getMatch().getId().equals(currentMatch.getId()))
                    .findFirst()
                    .orElse(new Attendance());

            if (att.getId() == null) {
                att.setPlayer(player);
                att.setMatch(currentMatch);
            }
        } else {
            att = attendanceRepository.findAll().stream()
                    .filter(a -> a.getPlayer() != null
                            && a.getPlayer().getId().equals(playerId))
                    .reduce((first, second) -> second)
                    .orElse(new Attendance());

            if (att.getId() == null) {
                att.setPlayer(player);
            }
        }

        att.setPaid(!att.isPaid());
        attendanceRepository.save(att);

        return "Updated";
    }

    @PostMapping("/reset-month")
    public String resetAllPayments() {
        List<Attendance> all = attendanceRepository.findAll();
        for (Attendance a : all) {
            a.setPaid(false);
        }
        attendanceRepository.saveAll(all);
        return "Done";
    }

    @GetMapping("/history")
    public Map<String, List<String>> getMatchHistory() {
        List<Attendance> all = attendanceRepository.findAll();
        Map<String, List<String>> history = new HashMap<>();

        for (Attendance att : all) {
            if (att.getMatch() != null && "YES".equals(att.getStatus())) {
                String matchKey = att.getMatch().getLocation()
                        + " (" + att.getMatch().getDateTime().toString().replace("T", " ") + ")";
                history.computeIfAbsent(matchKey, k -> new ArrayList<>())
                        .add(att.getPlayer().getFullName());
            }
        }
        return history;
    }
}
