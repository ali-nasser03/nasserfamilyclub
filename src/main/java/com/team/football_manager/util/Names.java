package com.team.football_manager.util;

public final class Names {

    private Names() {
    }

    /** Trims and collapses repeated spaces so "  محمد   ناصر " == "محمد ناصر". */
    public static String clean(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }
}
