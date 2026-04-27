package com.team.football_manager.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    private String username;
    private int age;
    private String password;
    private String role; // ADMIN or PLAYER

    // الحقل الجديد
    private boolean exempt = false; 
}
