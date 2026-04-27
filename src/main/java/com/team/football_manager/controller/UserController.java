package com.team.football_manager.controller;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // جلب اللاعبين فقط (بدون الأدمن)
    @GetMapping("/players")
    public List<User> getPlayers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole()))
                .toList();
    }

    // تعديل اللاعب
    @PutMapping("/update/{id}")
    public User updatePlayer(@PathVariable Long id, @RequestBody User updated) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return null;
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return null;
        }

        user.setFullName(updated.getFullName());
        user.setUsername(updated.getFullName());
        user.setAge(updated.getAge());
        user.setWorking(updated.isWorking());

        return userRepository.save(user);
    }
}
