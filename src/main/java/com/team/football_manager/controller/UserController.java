package com.team.football_manager.controller;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerPlayer(@RequestBody User user) {
        String fullName = user.getFullName() != null ? user.getFullName().trim() : "";

        if (fullName.isEmpty()) {
            return ResponseEntity.badRequest().body("الاسم مطلوب");
        }

        if (userRepository.findByFullName(fullName).isPresent()) {
            return ResponseEntity.badRequest().body("اللاعب مسجل مسبقًا");
        }

        user.setFullName(fullName);
        user.setUsername(fullName);
        user.setRole("PLAYER");

        if (user.getAge() < 0) {
            user.setAge(0);
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/check")
    public String checkPlayer(@RequestParam String name) {
        String cleanedName = name == null ? "" : name.trim();

        if (cleanedName.isEmpty()) {
            return "NOT_FOUND";
        }

        boolean exists = userRepository.findByFullName(cleanedName).isPresent();

        return exists ? "FOUND" : "NOT_FOUND";
    }
}
