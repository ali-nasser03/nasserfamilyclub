package com.team.football_manager.model;

import jakarta.persistence.*;

import java.time.Instant;

/** A row means the player paid for that month (format yyyy-MM). No row = not paid. */
@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payment_player_month", columnNames = {"player_id", "pay_month"}))
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private User player;

    @Column(name = "pay_month", nullable = false, length = 7)
    private String month;

    @Column(name = "paid_at")
    private Instant paidAt = Instant.now();

    public Payment() {
    }

    public Payment(User player, String month) {
        this.player = player;
        this.month = month;
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

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }
}
