package com.team.football_manager.dto;

import com.team.football_manager.model.Vote;

import java.util.List;

public record AdminOverview(
        MatchDto match,
        String month,
        List<Row> players
) {
    public record Row(Long id, String name, int age, boolean working, Vote vote, PaymentStatus payment) {
    }
}
