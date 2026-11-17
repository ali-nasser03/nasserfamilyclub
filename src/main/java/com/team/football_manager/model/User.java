package com.team.football_manager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, unique = true)
    private String fullName;

    /** International format, e.g. +970591234567. Used for SMS. */
    @Column(length = 20)
    private String phone;

    private int age;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role = Role.PLAYER;

    /** Players who work pay the monthly fee; the rest are exempt. */
    private boolean working = true;

    /** BCrypt hash. Only used for the admin account. */
    @Column(name = "password")
    private String passwordHash;

    public User() {
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isExemptFromPayment() {
        return !working;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
