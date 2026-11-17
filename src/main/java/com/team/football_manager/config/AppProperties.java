package com.team.football_manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String zone, String publicUrl) {

    public ZoneId zoneId() {
        return ZoneId.of(zone == null || zone.isBlank() ? "Asia/Jerusalem" : zone);
    }

    public String publicUrlOrEmpty() {
        return publicUrl == null ? "" : publicUrl.trim();
    }
}
