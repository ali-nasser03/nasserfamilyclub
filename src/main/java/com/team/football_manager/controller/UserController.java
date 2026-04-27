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

    // 🔥 تسجيل لاعب
    @PostMapping("/register")
    public ResponseEntity<?> registerPlayer(@RequestBody User user) {
        try {
            String fullName = user.getFullName().trim().replaceAll("\\s+", " ");

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

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("فشل التسجيل");
        }
    }

    // 🔥 تحقق من وجود لاعب
    @GetMapping("/check")
    public String checkPlayer(@RequestParam String name) {
        return userRepository.findByFullName(name.trim())
                .isPresent() ? "FOUND" : "NOT_FOUND";
    }

    // 🔥 دخول الأدمن
    @PostMapping("/admin-login")
    public String adminLogin(@RequestBody Map<String, String> body) {

        String name = body.get("fullName");
        String password = body.get("password");

        User user = userRepository.findByFullName(name).orElse(null);

        if (user == null) return "FAIL";
        if (!"ADMIN".equals(user.getRole())) return "FAIL";

        return user.getPassword().equals(password) ? "SUCCESS" : "FAIL";
    }

    // 🔥 أهم API — يرجع فقط اللاعبين
    @GetMapping("/players")
    public List<User> getPlayersOnly() {
        return userRepository.findByRole("PLAYER");
    }

    @GetMapping("/players")
public List<User> getPlayersOnly() {
    return userRepository.findByRole("PLAYER");
}

@PutMapping("/update/{id}")
public ResponseEntity<?> updatePlayer(@PathVariable Long id, @RequestBody User updatedUser) {
    User user = userRepository.findById(id).orElse(null);

    if (user == null) {
        return ResponseEntity.badRequest().body("اللاعب غير موجود");
    }

    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
        return ResponseEntity.badRequest().body("لا يمكن تعديل الأدمن من هنا");
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
