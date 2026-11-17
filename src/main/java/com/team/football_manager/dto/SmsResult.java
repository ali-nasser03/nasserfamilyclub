package com.team.football_manager.dto;

/**
 * @param recipients how many players will get a message
 * @param missingPhone how many players were skipped because they have no phone number
 */
public record SmsResult(boolean smsEnabled, int recipients, int missingPhone) {
}
