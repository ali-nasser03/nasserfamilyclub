package com.team.football_manager.sms;

import com.team.football_manager.model.Match;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * SMS texts. Kept short on purpose: Arabic SMS are billed per 70 characters.
 */
public final class SmsTemplates {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE d/M", Locale.forLanguageTag("ar"));
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private SmsTemplates() {
    }

    public static String newMatch(Match match, String siteUrl) {
        return "مباراة جديدة - نادي عائلة ناصر\n"
                + when(match) + "\n"
                + "المكان: " + match.getLocation() + "\n"
                + (siteUrl.isEmpty() ? "صوّت على الموقع" : "صوّت: " + siteUrl);
    }

    public static String reminder(Match match, String siteUrl) {
        return "تذكير: صوّت على مباراة " + match.getDateTime().format(DAY) + " " + match.getDateTime().format(TIME) + "\n"
                + (siteUrl.isEmpty() ? "صوّت على الموقع" : siteUrl);
    }

    private static String when(Match match) {
        return match.getDateTime().format(DAY) + " الساعة " + match.getDateTime().format(TIME);
    }
}
