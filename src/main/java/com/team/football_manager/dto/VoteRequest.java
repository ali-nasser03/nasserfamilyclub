package com.team.football_manager.dto;

import com.team.football_manager.model.Vote;
import jakarta.validation.constraints.NotNull;

public record VoteRequest(@NotNull(message = "اختار سأحضر أو معتذر") Vote vote) {
}
