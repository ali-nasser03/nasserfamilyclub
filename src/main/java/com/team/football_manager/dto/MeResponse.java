package com.team.football_manager.dto;

/** @param token only set right after login/register; the page keeps it and sends it back as X-Auth-Token */
public record MeResponse(Long id, String name, String role, String token) {
}
