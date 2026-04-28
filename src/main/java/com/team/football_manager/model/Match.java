package com.team.football_manager.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Data
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private LocalDateTime dateTime;
    private boolean isActive;

    @Column(length = 1000)
    private String note;
}
