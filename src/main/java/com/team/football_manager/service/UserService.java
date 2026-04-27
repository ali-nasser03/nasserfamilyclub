package com.team.football_manager.service;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User register(User user) {
        return userRepository.save(user);
    }

    public boolean checkAdminLogin(String fullName, String password) {
        User user = userRepository.findByFullName(fullName).orElse(null);

        if (user == null) return false;
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) return false;

        String savedPassword = user.getPassword() == null ? "" : user.getPassword();

        return savedPassword.equals(password);
    }
}
