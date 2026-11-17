package com.team.football_manager.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumbersTest {

    @Test
    void palestinianLocalNumbersGet970() {
        assertEquals("+970591234567", PhoneNumbers.normalize("0591234567"));
        assertEquals("+970561234567", PhoneNumbers.normalize("056-123-4567"));
    }

    @Test
    void israeliLocalNumbersGet972() {
        assertEquals("+972521234567", PhoneNumbers.normalize("052 123 4567"));
        assertEquals("+972541234567", PhoneNumbers.normalize("0541234567"));
    }

    @Test
    void internationalFormatsAreKept() {
        assertEquals("+970591234567", PhoneNumbers.normalize("+970591234567"));
        assertEquals("+970591234567", PhoneNumbers.normalize("00970591234567"));
        assertEquals("+972521234567", PhoneNumbers.normalize("972521234567"));
    }

    @Test
    void arabicDigitsAreAccepted() {
        assertEquals("+970591234567", PhoneNumbers.normalize("٠٥٩١٢٣٤٥٦٧"));
    }

    @Test
    void invalidNumbersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PhoneNumbers.normalize("12345"));
        assertThrows(IllegalArgumentException.class, () -> PhoneNumbers.normalize("abc"));
        assertThrows(IllegalArgumentException.class, () -> PhoneNumbers.normalize(""));
        assertThrows(IllegalArgumentException.class, () -> PhoneNumbers.normalize(null));
    }

    @Test
    void optionalAllowsBlank() {
        assertNull(PhoneNumbers.normalizeOptional("  "));
        assertEquals("+970591234567", PhoneNumbers.normalizeOptional("0591234567"));
    }
}
