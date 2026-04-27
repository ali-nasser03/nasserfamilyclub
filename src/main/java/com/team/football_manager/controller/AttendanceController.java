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
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @GetMapping("/active-list")
    public List<Map<String, Object>> getActiveList() {
        List<User> users = userRepository.findAll();
        List<Match> activeMatches = matchRepository.findByIsActiveTrue();
        Match activeMatch = activeMatches.isEmpty() ? null : activeMatches.get(0);

        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            if (user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN")) {
                continue;
            }

            Attendance attendance = null;

            if (activeMatch != null) {
                Long userId = user.getId();
                Long matchId = activeMatch.getId();

                attendance = attendanceRepository.findAll().stream()
                        .filter(a -> a.getPlayer() != null
                                && a.getMatch() != null
                                && a.getPlayer().getId().equals(userId)
                                && a.getMatch().getId().equals(matchId))
                        .findFirst()
                        .orElse(null);
            }

            boolean paid = false;
            String status = "لم يصوت";

            if (attendance != null) {
                paid = attendance.isPaid();

                if ("YES".equalsIgnoreCase(attendance.getStatus())) {
                    status = "✅";
                } else if ("NO".equalsIgnoreCase(attendance.getStatus())) {
                    status = "❌";
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getFullName());
            map.put("role", user.getRole());
            map.put("working", user.isWorking());
            map.put("paid", paid);
            map.put("status", status);

            result.add(map);
        }

        return result;
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> body) {
        String playerName = body.get("playerName");
        String status = body.get("status");

        User player = userRepository.findByFullName(playerName).orElse(null);
        Match activeMatch = matchRepository.findByIsActiveTrue().stream().findFirst().orElse(null);

        if (player == null || activeMatch == null) {
            return "ERROR";
        }

        Attendance attendance = attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer() != null
                        && a.getMatch() != null
                        && a.getPlayer().getId().equals(player.getId())
                        && a.getMatch().getId().equals(activeMatch.getId()))
                .findFirst()
                .orElse(new Attendance());

        attendance.setPlayer(player);
        attendance.setMatch(activeMatch);
        attendance.setStatus(status);

        attendanceRepository.save(attendance);
        return "OK";
    }

    @PostMapping("/remove")
    public String remove(@RequestBody Map<String, String> body) {
        String playerName = body.get("playerName");

        User player = userRepository.findByFullName(playerName).orElse(null);
        Match activeMatch = matchRepository.findByIsActiveTrue().stream().findFirst().orElse(null);

        if (player == null || activeMatch == null) {
            return "ERROR";
        }

        attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer() != null
                        && a.getMatch() != null
                        && a.getPlayer().getId().equals(player.getId())
                        && a.getMatch().getId().equals(activeMatch.getId()))
                .findFirst()
                .ifPresent(a -> {
                    a.setStatus(null);
                    attendanceRepository.save(a);
                });

        return "OK";
    }

    @PostMapping("/toggle-payment/{playerId}")
    public String togglePayment(@PathVariable Long playerId) {
        User player = userRepository.findById(playerId).orElse(null);

        if (player == null) {
            return "ERROR";
        }

        if (!player.isWorking()) {
            return "EXEMPT";
        }

        Match activeMatch = matchRepository.findByIsActiveTrue().stream().findFirst().orElse(null);

        Attendance attendance = null;

        if (activeMatch != null) {
            attendance = attendanceRepository.findAll().stream()
                    .filter(a -> a.getPlayer() != null
                            && a.getMatch() != null
                            && a.getPlayer().getId().equals(player.getId())
                            && a.getMatch().getId().equals(activeMatch.getId()))
                    .findFirst()
                    .orElse(null);
        }

        if (attendance == null) {
            attendance = new Attendance();
            attendance.setPlayer(player);
            if (activeMatch != null) {
                attendance.setMatch(activeMatch);
            }
            attendance.setStatus(null);
            attendance.setPaid(false);
        }

        attendance.setPaid(!attendance.isPaid());
        attendanceRepository.save(attendance);

        return "OK";
    }

    @PostMapping("/reset-month")
    public String resetMonth() {
        List<Attendance> all = attendanceRepository.findAll();

        for (Attendance a : all) {
            if (a.getPlayer() != null && a.getPlayer().isWorking()) {
                a.setPaid(false);
            }
        }

        attendanceRepository.saveAll(all);
        return "OK";
    }

    @PostMapping("/reset-all-votes")
    public String resetAllVotes() {
        Match activeMatch = matchRepository.findByIsActiveTrue().stream().findFirst().orElse(null);

        if (activeMatch == null) {
            return "NO_MATCH";
        }

        List<Attendance> list = attendanceRepository.findByMatchId(activeMatch.getId());

        for (Attendance a : list) {
            a.setStatus(null);
        }

        attendanceRepository.saveAll(list);
        return "OK";
    }

    @GetMapping("/history")
    public Map<String, List<String>> history() {
        Map<String, List<String>> result = new LinkedHashMap<>();

        for (Attendance a : attendanceRepository.findAll()) {
            if (a.getPlayer() == null || a.getMatch() == null) continue;
            if (a.getPlayer().getRole() != null && a.getPlayer().getRole().equalsIgnoreCase("ADMIN")) continue;

            if ("YES".equalsIgnoreCase(a.getStatus())) {
                String key = a.getMatch().getLocation() + " - " + a.getMatch().getDateTime();
                result.putIfAbsent(key, new ArrayList<>());
                result.get(key).add(a.getPlayer().getFullName());
            }
        }

        return result;
    }
}
