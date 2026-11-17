package com.team.football_manager.dto;

import com.team.football_manager.model.Vote;

import java.util.List;

/** What a player sees: the open match, who is coming, and their own status. */
public record BoardResponse(
        MatchDto match,
        String month,
        List<Entry> players,
        Me me
) {
    public record Entry(Long id, String name, Vote vote) {
    }

    public record Me(Long id, String name, Vote vote, PaymentStatus payment) {
    }
}
