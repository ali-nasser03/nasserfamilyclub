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

    private String username;
    private int age;
    private String password;

    @Column(name = "full_name")
    private String fullName;

    private String role; // ADMIN or PLAYER

    @Column(name = "is_employed", nullable = false, columnDefinition = "boolean default true")
    private boolean isEmployed = true; // true = بيشتغل, false = ما بيشتغل (معفي من الدفع)
}
