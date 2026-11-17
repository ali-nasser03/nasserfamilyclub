package com.team.football_manager.sms;

/** @param to phone in +XXXXXXXX format */
public record SmsMessage(String to, String recipientName, String body) {
}
