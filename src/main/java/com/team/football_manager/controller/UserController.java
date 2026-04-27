package com.team.football_manager.controller;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerPlayer(@RequestBody Map<String, Object> body) {
        try {
            String fullName = body.get("fullName") != null
                    ? body.get("fullName").toString().trim().replaceAll("\\s+", " ")
                    : "";

            if (fullName.isEmpty()) {
                return ResponseEntity.badRequest().body("الاسم مطلوب");
            }

            if (userRepository.findByFullName(fullName).isPresent()) {
                return ResponseEntity.badRequest().body("اللاعب مسجل مسبقًا");
            }

            int age = 0;
            if (body.get("age") != null) {
                try { age = Integer.parseInt(body.get("age").toString()); } catch (Exception ignored) {}
            }

            boolean isEmployed = true;
            if (body.get("isEmployed") != null) {
                isEmployed = Boolean.parseBoolean(body.get("isEmployed").toString());
            }

            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setUsername(fullName);
            newUser.setAge(age);
            newUser.setRole("PLAYER");
            newUser.setPassword("");
            newUser.setEmployed(isEmployed);

            User saved = userRepository.save(newUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "تم التسجيل بنجاح");
            response.put("id", saved.getId());
            response.put("fullName", saved.getFullName());
            response.put("isEmployed", saved.isEmployed());

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
        if (cleanedName.isEmpty()) return "NOT_FOUND";
        boolean exists = userRepository.findByFullName(cleanedName).isPresent();
        return exists ? "FOUND" : "NOT_FOUND";
    }

    // جلب كل اللاعبين (للأدمن)
    @GetMapping("/all")
    public List<User> getAllPlayers() {
        return userRepository.findAll().stream()
                .filter(u -> !"ADMIN".equals(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
    }

    // تعديل بيانات لاعب
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlayer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        if (body.get("fullName") != null) {
            String newName = body.get("fullName").toString().trim().replaceAll("\\s+", " ");
            if (!newName.isEmpty()) {
                user.setFullName(newName);
                user.setUsername(newName);
            }
        }
        if (body.get("age") != null) {
            try { user.setAge(Integer.parseInt(body.get("age").toString())); } catch (Exception ignored) {}
        }
        if (body.get("isEmployed") != null) {
            user.setEmployed(Boolean.parseBoolean(body.get("isEmployed").toString()));
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "تم التحديث"));
    }

    // حذف لاعب
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "تم الحذف"));
    }
}
