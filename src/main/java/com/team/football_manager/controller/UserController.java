package com.team.football_manager.controller;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 🔥 جلب اللاعبين فقط (بدون الأدمن)
    @GetMapping("/players")
    public List<User> getPlayers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == null || !user.getRole().equalsIgnoreCase("ADMIN"))
                .toList();
    }

    // 🔥 تعديل اللاعب
    @PutMapping("/update/{id}")
    public User updatePlayer(@PathVariable Long id, @RequestBody User updatedUser) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return null;
        }

        // ❌ منع تعديل الأدمن
        if (user.getRole() != null && user.getRole().equalsIgnoreCase("ADMIN")) {
            return null;
        }

        // ✔ تعديل البيانات
        user.setFullName(updatedUser.getFullName());
        user.setUsername(updatedUser.getFullName());
        user.setAge(updatedUser.getAge());
        user.setWorking(updatedUser.isWorking());

        return userRepository.save(user);
    }

    // 🔥 تسجيل لاعب جديد
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userRepository.save(user);
    }

    // 🔥 جلب كل المستخدمين (إذا احتجته)
    @GetMapping("/all")
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
