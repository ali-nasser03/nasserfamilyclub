package com.team.football_manager.dto;

import com.team.football_manager.model.Match;

import java.time.LocalDateTime;

public record MatchDto(Long id, String location, LocalDateTime dateTime, String note) {

    public static MatchDto from(Match m) {
        return m == null ? null : new MatchDto(m.getId(), m.getLocation(), m.getDateTime(), m.getNote());
    }
}
