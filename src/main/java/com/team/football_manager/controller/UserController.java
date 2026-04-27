package com.team.football_manager.controller;

import com.team.football_manager.model.Attendance;
import com.team.football_manager.model.User;
import com.team.football_manager.repository.AttendanceRepository;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerPlayer(@RequestBody User user) {
        try {
            String fullName = user.getFullName() != null
                    ? user.getFullName().trim().replaceAll("\\s+", " ")
                    : "";

            if (fullName.isEmpty()) {
                return ResponseEntity.badRequest().body("الاسم مطلوب");
            }

            if (userRepository.findByFullName(fullName).isPresent()) {
                return ResponseEntity.badRequest().body("اللاعب مسجل مسبقًا");
            }

            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setUsername(fullName);
            newUser.setAge(user.getAge());
            newUser.setRole("PLAYER");
            newUser.setPassword("");
            newUser.setWorking(true);

            User saved = userRepository.save(newUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "تم التسجيل بنجاح");
            response.put("id", saved.getId());
            response.put("fullName", saved.getFullName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("فشل التسجيل: " + e.getMessage());
        }
    }

    @GetMapping("/check")
    public String checkPlayer(@RequestParam String name) {
        String cleanedName = name == null ? "" : name.trim().replaceAll("\\s+", " ");

        if (cleanedName.isEmpty()) {
            return "NOT_FOUND";
        }

        boolean exists = userRepository.findByFullName(cleanedName).isPresent();
        return exists ? "FOUND" : "NOT_FOUND";
    }

    @GetMapping("/players")
    public List<Map<String, Object>> getPlayers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> !"ADMIN".equalsIgnoreCase(user.getRole()))
                .sorted(Comparator.comparing(User::getFullName, Comparator.nullsLast(String::compareTo)))
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("fullName", user.getFullName());
                    map.put("age", user.getAge());
                    map.put("working", user.isWorking());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePlayer(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("اللاعب غير موجود");
        }

        User user = optionalUser.get();

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.badRequest().body("لا يمكن تعديل حساب الأدمن من هنا");
        }

        String fullName = updatedUser.getFullName() != null
                ? updatedUser.getFullName().trim().replaceAll("\\s+", " ")
                : "";

        if (fullName.isEmpty()) {
            return ResponseEntity.badRequest().body("الاسم مطلوب");
        }

        Optional<User> sameNameUser = userRepository.findByFullName(fullName);
        if (sameNameUser.isPresent() && !sameNameUser.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body("هذا الاسم مستخدم مسبقًا");
        }

        user.setFullName(fullName);
        user.setUsername(fullName);
        user.setAge(updatedUser.getAge());
        user.setWorking(updatedUser.isWorking());

        userRepository.save(user);

        return ResponseEntity.ok("تم تعديل بيانات اللاعب");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable Long id) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("اللاعب غير موجود");
        }

        User user = optionalUser.get();

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.badRequest().body("لا يمكن حذف حساب الأدمن");
        }

        List<Attendance> playerAttendance = attendanceRepository.findByPlayerId(id);
        attendanceRepository.deleteAll(playerAttendance);

        userRepository.delete(user);

        return ResponseEntity.ok("تم حذف اللاعب");
    }
}
