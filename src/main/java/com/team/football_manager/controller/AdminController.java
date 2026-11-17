package com.team.football_manager.controller;

import com.team.football_manager.dto.*;
import com.team.football_manager.service.Clock;
import com.team.football_manager.service.MatchService;
import com.team.football_manager.service.PaymentService;
import com.team.football_manager.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Admin-only endpoints. Access is checked by AuthInterceptor for everything under /api/admin. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MatchService matches;
    private final PlayerService players;
    private final PaymentService payments;
    private final Clock clock;

    public AdminController(MatchService matches, PlayerService players, PaymentService payments, Clock clock) {
        this.matches = matches;
        this.players = players;
        this.payments = payments;
        this.clock = clock;
    }

    @GetMapping("/overview")
    public AdminOverview overview(@RequestParam(required = false) String month) {
        return matches.adminOverview(clock.monthOrCurrent(month));
    }

    // ---------- matches

    @PostMapping("/matches")
    public MatchDto createMatch(@Valid @RequestBody CreateMatchRequest body) {
        return matches.create(body);
    }

    @DeleteMapping("/matches/current")
    public ResponseEntity<Void> closeCurrent() {
        matches.closeCurrent();
        return ResponseEntity.noContent().build();
    }

    // ---------- players

    @PutMapping("/players/{id}")
    public ResponseEntity<Void> updatePlayer(@PathVariable Long id, @Valid @RequestBody UpdatePlayerRequest body) {
        players.update(id, body);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        players.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- payments

    @PutMapping("/players/{id}/payment")
    public ResponseEntity<Void> setPayment(@PathVariable Long id, @RequestBody PaymentRequest body) {
        payments.setPaid(id, clock.monthOrCurrent(body.month()), body.paid());
        return ResponseEntity.noContent().build();
    }
}
