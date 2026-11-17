package com.team.football_manager.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "twilio")
public record TwilioProperties(String accountSid, String authToken, String from, String messagingServiceSid) {

    public boolean isConfigured() {
        return notBlank(accountSid) && notBlank(authToken) && (notBlank(from) || notBlank(messagingServiceSid));
    }

    static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
