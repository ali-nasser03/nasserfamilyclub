package com.team.football_manager.service;

import com.team.football_manager.model.User;
import com.team.football_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User login(String name, String password) {
        Optional<User> user = userRepository.findByFullName(name);

        if (user.isPresent()) {
            User u = user.get();
            // إذا كان مدير، يجب التأكد من كلمة السر
            if (u.getRole().equals("ADMIN")) {
                return u.getPassword().equals(password) ? u : null;
            }
            // إذا كان لاعب، يدخل بالاسم فقط
            return u;
        }
        return null;
    }
}