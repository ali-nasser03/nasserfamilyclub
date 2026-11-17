package com.team.football_manager.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NamesTest {

    @Test
    void collapsesSpaces() {
        assertEquals("محمد ناصر", Names.clean("  محمد    ناصر "));
        assertEquals("", Names.clean(null));
    }
}
