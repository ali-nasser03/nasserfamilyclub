package com.team.football_manager.controller;

import com.team.football_manager.model.*;
import com.team.football_manager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MatchRepository matchRepository;

    @GetMapping("/active-list")
    public List<Map<String, Object>> getActiveAttendance() {
        List<User> users = userRepository.findAll();
        Match match = matchRepository.findByIsActiveTrue().stream().findFirst().orElse(null);

        return users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getFullName());
            map.put("isExempt", u.isExempt());
            if (match != null) {
                Attendance att = attendanceRepository.findAll().stream()
                    .filter(a -> a.getPlayer().getId().equals(u.getId()) && a.getMatch().getId().equals(match.getId()))
                    .findFirst().orElse(null);
                map.put("status", (att == null || att.getStatus() == null) ? "لم يصوت" : 
                       ("YES".equals(att.getStatus()) ? "✅ حاضر" : "❌ معتذر"));
                map.put("paid", att != null && att.isPaid());
            }
            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> data) {
        String name = data.get("playerName").trim();
        User player = userRepository.findByFullNameIgnoreCase(name).orElse(null);
        Match match = matchRepository.findByIsActiveTrue().stream().findFirst().orElse(null);
        if (player == null || match == null) return "Error";

        Attendance a = attendanceRepository.findAll().stream()
                .filter(att -> att.getPlayer().getId().equals(player.getId()) && att.getMatch().getId().equals(match.getId()))
                .findFirst().orElse(new Attendance());
        a.setPlayer(player); a.setMatch(match); a.setStatus(data.get("status"));
        attendanceRepository.save(a);
        return "Success";
    }
}
