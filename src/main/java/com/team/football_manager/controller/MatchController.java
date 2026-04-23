package com.team.football_manager.controller;

import com.team.football_manager.model.Match;
import com.team.football_manager.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping("/add")
    public Match addMatch(@RequestBody Match match) {
        return matchService.createMatch(match);
    }

    @GetMapping("/all")
    public List<Match> getAll() {
        return matchService.getAllMatches();
    }

    @GetMapping("/latest")
    public Match getLatestMatch() {
        return matchService.getLatestActiveMatch();
    }
}
