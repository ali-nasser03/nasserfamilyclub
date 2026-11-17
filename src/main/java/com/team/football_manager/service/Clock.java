package com.team.football_manager.service;

import com.team.football_manager.config.AppProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/** Local time of the club (Asia/Jerusalem), independent of the server's time zone. */
@Component
public class Clock {

    private final AppProperties props;

    public Clock(AppProperties props) {
        this.props = props;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(props.zoneId());
    }

    public String currentMonth() {
        return YearMonth.now(props.zoneId()).toString();
    }

    /** Validates a yyyy-MM string; blank means the current month. */
    public String monthOrCurrent(String month) {
        if (month == null || month.isBlank()) {
            return currentMonth();
        }
        try {
            return YearMonth.parse(month.trim()).toString();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("الشهر غير صحيح");
        }
    }
}
