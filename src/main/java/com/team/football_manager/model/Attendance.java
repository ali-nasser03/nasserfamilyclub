package com.team.football_manager.model;

import jakarta.persistence.*;

/** One player's vote for one match. No row = the player has not voted. */
@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(name = "uk_attendance_player_match", columnNames = {"player_id", "match_id"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private User player;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Vote status;

    public Attendance() {
    }

    public Attendance(User player, Match match, Vote status) {
        this.player = player;
        this.match = match;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Vote getStatus() {
        return status;
    }

    public void setStatus(Vote status) {
        this.status = status;
    }
}
