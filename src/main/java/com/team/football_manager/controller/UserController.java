package com.team.football_manager.controller;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerPlayer(@RequestBody User user) {
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

        userRepository.save(newUser);

        return ResponseEntity.ok("تم التسجيل");
    }

    @GetMapping("/check")
    public String checkPlayer(@RequestParam String name) {
        String cleanedName = name == null ? "" : name.trim().replaceAll("\\s+", " ");

        if (cleanedName.isEmpty()) {
            return "NOT_FOUND";
        }

        return userRepository.findByFullName(cleanedName).isPresent()
                ? "FOUND"
                : "NOT_FOUND";
    }

    @PostMapping("/admin-login")
    public String adminLogin(@RequestBody Map<String, String> body) {
        String fullName = body.get("fullName");
        String password = body.get("password");

        if (fullName == null || password == null) {
            return "FAIL";
        }

        String cleanedName = fullName.trim().replaceAll("\\s+", " ");
        String cleanedPassword = password.trim();

        User user = userRepository.findByFullName(cleanedName).orElse(null);

        if (user == null) {
            return "FAIL";
        }

        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "FAIL";
        }

        String savedPassword = user.getPassword() == null ? "" : user.getPassword().trim();

        return savedPassword.equals(cleanedPassword) ? "SUCCESS" : "FAIL";
    }

    @GetMapping("/players")
    public List<User> getPlayersOnly() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole()))
                .toList();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePlayer(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("اللاعب غير موجود");
        }

        if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.badRequest().body("لا يمكن تعديل الأدمن");
        }

        String fullName = updatedUser.getFullName() != null
                ? updatedUser.getFullName().trim().replaceAll("\\s+", " ")
                : "";

        if (fullName.isEmpty()) {
            return ResponseEntity.badRequest().body("الاسم مطلوب");
        }

        user.setFullName(fullName);
        user.setUsername(fullName);
        user.setAge(updatedUser.getAge());
        user.setWorking(updatedUser.isWorking());

        userRepository.save(user);

        return ResponseEntity.ok("تم التعديل");
    }
}
