package com.team.football_manager.controller;

import com.team.football_manager.dto.BoardResponse;
import com.team.football_manager.dto.VoteRequest;
import com.team.football_manager.service.Clock;
import com.team.football_manager.service.MatchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Endpoints for any signed-in player. The player is taken from the sign-in token, never from the request body. */
@RestController
@RequestMapping("/api")
public class PlayerController {

    private final MatchService matches;
    private final CurrentUser currentUser;
    private final Clock clock;

    public PlayerController(MatchService matches, CurrentUser currentUser, Clock clock) {
        this.matches = matches;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @GetMapping("/board")
    public BoardResponse board(HttpServletRequest request) {
        return matches.board(currentUser.get(request), clock.currentMonth());
    }

    @PutMapping("/vote")
    public BoardResponse vote(@Valid @RequestBody VoteRequest body, HttpServletRequest request) {
        var me = currentUser.get(request);
        matches.vote(me, body.vote());
        return matches.board(me, clock.currentMonth());
    }

    @DeleteMapping("/vote")
    public BoardResponse clearVote(HttpServletRequest request) {
        var me = currentUser.get(request);
        matches.clearVote(me);
        return matches.board(me, clock.currentMonth());
    }
}
