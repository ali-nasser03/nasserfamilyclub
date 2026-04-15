package com.team.football_manager.controller;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public User registerPlayer(@RequestBody User user) {
        user.setRole("PLAYER"); // أي شخص يسجل من الخارج هو لاعب تلقائياً
        return userRepository.save(user);
    }

    @GetMapping("/check")
    public String checkPlayer(@RequestParam String name) {
        // تنظيف الاسم من أي مسافات زائدة قد تأتي من المتصفح
        String cleanedName = name.trim();

        System.out.println("Searching for player: [" + cleanedName + "]"); // سيظهر لك في IntelliJ ماذا يبحث

        boolean exists = userRepository.findByFullName(cleanedName).isPresent();

        System.out.println("Result found: " + exists);
        return exists ? "FOUND" : "NOT_FOUND";
    }
}