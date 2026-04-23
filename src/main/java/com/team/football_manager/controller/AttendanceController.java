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

    // 🟢 تسجيل التصويت
    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> body) {

        String playerName = body.get("playerName");
        String status = body.get("status"); // YES / NO

        Match match = matchRepository.findByIsActiveTrue()
                .stream().findFirst().orElse(null);

        if (match == null) return "NO_MATCH";

        User user = userRepository.findByFullName(playerName).orElse(null);
        if (user == null) return "NO_USER";

        // ❌ منع الأدمن
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "ADMIN_BLOCKED";
        }

        Attendance attendance = attendanceRepository
                .findByPlayerNameAndMatchId(playerName, match.getId())
                .orElse(new Attendance());

        attendance.setPlayerName(playerName);
        attendance.setMatch(match);

        // 🟢 التصويت
        if ("YES".equals(status)) {
            attendance.setStatus("✅");
        } else if ("NO".equals(status)) {
            attendance.setStatus("❌");
        }

        // 🔥 الدفع حسب working
        if (!user.isWorking()) {
            attendance.setPaid(false);
            attendance.setStatus("🟡 معفى");
        }

        attendanceRepository.save(attendance);

        return "OK";
    }

    // 🟢 حذف التصويت
    @PostMapping("/remove")
    public String remove(@RequestBody Map<String, String> body) {

        String playerName = body.get("playerName");

        Match match = matchRepository.findByIsActiveTrue()
                .stream().findFirst().orElse(null);

        if (match == null) return "NO_MATCH";

        Attendance attendance = attendanceRepository
                .findByPlayerNameAndMatchId(playerName, match.getId())
                .orElse(null);

        if (attendance != null) {
            attendance.setStatus(null);
            attendanceRepository.save(attendance);
        }

        return "OK";
    }

    // 🟢 قائمة الحضور الحالية
    @GetMapping("/active-list")
    public List<Map<String, Object>> getActiveList() {

        Match match = matchRepository.findByIsActiveTrue()
                .stream().findFirst().orElse(null);

        if (match == null) return new ArrayList<>();

        List<Attendance> list = attendanceRepository.findByMatchId(match.getId());

        List<Map<String, Object>> result = new ArrayList<>();

        for (Attendance a : list) {

            User user = userRepository.findByFullName(a.getPlayerName()).orElse(null);

            if (user == null) continue;

            // ❌ فلترة الأدمن
            if ("ADMIN".equalsIgnoreCase(user.getRole())) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("name", a.getPlayerName());
            map.put("status", a.getStatus());
            map.put("paid", a.isPaid());
            map.put("role", user.getRole());

            result.add(map);
        }

        return result;
    }

    // 🟢 سجل المباريات (للاعبين فقط)
    @GetMapping("/history")
    public Map<String, List<String>> getHistory() {

        List<Attendance> list = attendanceRepository.findAll();

        Map<String, List<String>> result = new HashMap<>();

        for (Attendance a : list) {

            User user = userRepository.findByFullName(a.getPlayerName()).orElse(null);

            if (user == null) continue;

            // ❌ فلترة الأدمن
            if ("ADMIN".equalsIgnoreCase(user.getRole())) continue;

            if (a.getStatus() != null && a.getStatus().contains("✅")) {

                String key = a.getMatch().getLocation() + " - " + a.getMatch().getDateTime();

                result.putIfAbsent(key, new ArrayList<>());
                result.get(key).add(a.getPlayerName());
            }
        }

        return result;
    }
}
