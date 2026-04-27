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

    @PostMapping("/toggle-exempt/{id}")
    public ResponseEntity<?> toggleExempt(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setExempt(!user.isExempt());
        userRepository.save(user);
        return ResponseEntity.ok("Updated");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        String name = user.getFullName() != null ? user.getFullName().trim() : "";
        if (name.isEmpty() || userRepository.findByFullName(name).isPresent()) return ResponseEntity.badRequest().body("Error");
        User newUser = new User();
        newUser.setFullName(name); newUser.setUsername(name); newUser.setRole("PLAYER"); newUser.setPassword("");
        userRepository.save(newUser);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/check")
    public String check(@RequestParam String name) {
        return userRepository.findByFullName(name).isPresent() ? "FOUND" : "NOT_FOUND";
    }
}
