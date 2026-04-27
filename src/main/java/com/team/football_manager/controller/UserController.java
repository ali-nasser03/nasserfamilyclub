package com.team.football_manager.controller;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        String inputName = creds.get("username") != null ? creds.get("username").trim() : "";
        String inputPass = creds.get("password");

        // 1. تشيك الأدمن
        if ("admin".equalsIgnoreCase(inputName)) {
            User admin = userRepository.findByUsernameIgnoreCase("admin").orElse(null);
            if (admin != null && admin.getPassword().equals(inputPass)) {
                return ResponseEntity.ok(admin);
            }
            return ResponseEntity.status(401).body("كلمة سر الأدمن خطأ");
        }

        // 2. تشيك اللاعبين
        Optional<User> player = userRepository.findByFullNameIgnoreCase(inputName);
        if (player.isPresent()) {
            return ResponseEntity.ok(player.get());
        }
        return ResponseEntity.status(404).body("هذا الاسم غير مسجل لدينا");
    }

    @PostMapping("/toggle-exempt/{id}")
    public ResponseEntity<?> toggleExempt(@PathVariable Long id) {
        User u = userRepository.findById(id).orElseThrow();
        u.setExempt(!u.isExempt());
        userRepository.save(u);
        return ResponseEntity.ok("Done");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        String name = user.getFullName() != null ? user.getFullName().trim() : "";
        if (name.isEmpty() || userRepository.findByFullNameIgnoreCase(name).isPresent()) 
            return ResponseEntity.badRequest().body("الاسم فارغ أو مسجل مسبقاً");
        
        User newUser = new User();
        newUser.setFullName(name); newUser.setUsername(name); newUser.setRole("PLAYER"); newUser.setPassword("");
        userRepository.save(newUser);
        return ResponseEntity.ok("Success");
    }
}
