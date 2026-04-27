package com.team.football_manager.repository;

import com.team.football_manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // تم تصحيح الباراميتر ليتوافق مع fullName في موديل User
    Optional<User> findByFullName(String fullName);
}
