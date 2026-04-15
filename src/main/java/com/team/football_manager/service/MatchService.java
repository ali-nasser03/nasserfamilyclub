package com.team.football_manager.service;

import com.team.football_manager.model.Match;
import com.team.football_manager.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;

    public Match createMatch(Match match) {
        match.setActive(true);
        return matchRepository.save(match);
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }
}