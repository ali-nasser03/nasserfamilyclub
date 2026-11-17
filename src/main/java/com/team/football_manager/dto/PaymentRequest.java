package com.team.football_manager.dto;

/** @param month yyyy-MM, or empty for the current month */
public record PaymentRequest(String month, boolean paid) {
}
