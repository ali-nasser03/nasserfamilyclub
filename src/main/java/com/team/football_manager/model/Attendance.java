package com.team.football_manager.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "attendance")
@Data
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User player;

    @ManyToOne
    private Match match;

    private String status; // "YES" (سأحضر), "NO" (أعتذر), "PENDING"
    private boolean isPaid; // هل دفع نقدًا؟ (يتحكم بها Admin فقط)
}