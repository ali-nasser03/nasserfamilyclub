package com.team.football_manager.util;

import java.util.regex.Pattern;

/**
 * Normalizes phone numbers to international (E.164) format so Twilio accepts them.
 *
 * Local numbers are accepted too:
 *  - 059xxxxxxx / 056xxxxxxx  -> Palestinian mobile (+970)
 *  - other 05xxxxxxxx          -> Israeli mobile (+972)
 */
public final class PhoneNumbers {

    private static final Pattern E164 = Pattern.compile("\\+[1-9]\\d{7,14}");

    private PhoneNumbers() {
    }

    /** Returns the number in +XXXXXXXX format, or throws IllegalArgumentException if it is not a valid number. */
    public static String normalize(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("رقم الجوال مطلوب");
        }

        String digits = toAsciiDigits(raw).replaceAll("[\\s\\-().]", "");

        if (digits.startsWith("00")) {
            digits = "+" + digits.substring(2);
        }

        String result;
        if (digits.startsWith("+")) {
            result = digits;
        } else if ((digits.startsWith("970") || digits.startsWith("972")) && digits.length() == 12) {
            result = "+" + digits;
        } else if (digits.matches("05\\d{8}")) {
            String country = digits.startsWith("059") || digits.startsWith("056") ? "+970" : "+972";
            result = country + digits.substring(1);
        } else {
            throw new IllegalArgumentException("رقم الجوال غير صحيح");
        }

        if (!E164.matcher(result).matches()) {
            throw new IllegalArgumentException("رقم الجوال غير صحيح");
        }
        return result;
    }

    /** Like {@link #normalize} but returns null for blank input. */
    public static String normalizeOptional(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return normalize(raw);
    }

    /** Converts Arabic-Indic digits (٠١٢...) typed on Arabic keyboards to 0-9. */
    private static String toAsciiDigits(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.trim().toCharArray()) {
            if (c >= '٠' && c <= '٩') {
                sb.append((char) ('0' + (c - '٠')));
            } else if (c >= '۰' && c <= '۹') {
                sb.append((char) ('0' + (c - '۰')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
