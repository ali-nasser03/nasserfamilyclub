package com.team.football_manager.sms;

import com.team.football_manager.model.Match;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmsTemplatesTest {

    private Match match() {
        Match m = new Match();
        m.setLocation("ملعب سلوان");
        m.setDateTime(LocalDateTime.of(2026, 11, 5, 19, 30));
        return m;
    }

    @Test
    void newMatchMessageHasPlaceTimeAndLink() {
        String text = SmsTemplates.newMatch(match(), "https://example.com");
        assertTrue(text.contains("ملعب سلوان"));
        assertTrue(text.contains("19:30"));
        assertTrue(text.contains("5/11"));
        assertTrue(text.contains("الخميس"));
        assertTrue(text.contains("https://example.com"));
    }

    @Test
    void worksWithoutLink() {
        String text = SmsTemplates.reminder(match(), "");
        assertTrue(text.contains("تذكير"));
        assertFalse(text.contains("http"));
    }
}
