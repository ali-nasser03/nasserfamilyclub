package com.team.football_manager.controller;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

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
            newUser.setWorking(user.isWorking());

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

    @PostMapping("/admin-login")
    public String adminLogin(@RequestBody Map<String, String> body) {
        String fullName = body.get("fullName");
        String password = body.get("password");

        if (fullName == null || password == null) {
            return "FAIL";
        }

        User user = userRepository.findByFullName(fullName.trim()).orElse(null);

        if (user == null) {
            return "FAIL";
        }

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "FAIL";
        }

        String savedPassword = user.getPassword() == null ? "" : user.getPassword();
        return savedPassword.equals(password) ? "SUCCESS" : "FAIL";
    }
}
