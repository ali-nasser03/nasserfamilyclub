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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/attendance")
@CrossOrigin
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list")
    public List<Map<String, Object>> getAttendanceList() {

        Optional<Match> currentMatchOpt = matchRepository.findAll()
                .stream()
                .filter(Match::isActive)
                .findFirst();

        if (currentMatchOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Match currentMatch = currentMatchOpt.get();

        List<Attendance> attendances = attendanceRepository.findAll();

        // 🔥 هون التعديل: فلترة الأدمن
        List<User> players = userRepository.findAll()
                .stream()
                .filter(user -> !"ADMIN".equals(user.getRole()))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : players) {

            Optional<Attendance> att = attendances.stream()
                    .filter(a ->
                            a.getPlayer().getId().equals(user.getId()) &&
                            a.getMatch().getId().equals(currentMatch.getId())
                    )
                    .findFirst();

            Map<String, Object> map = new HashMap<>();
            map.put("name", user.getFullName());

            if (att.isPresent()) {
                map.put("status", att.get().getStatus());
            } else {
                map.put("status", "لم يصوت");
            }

            result.add(map);
        }

        return result;
    }
}
