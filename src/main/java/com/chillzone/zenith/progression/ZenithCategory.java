package com.chillzone.zenith.progression;

import java.util.Locale;
import java.util.Optional;

public enum ZenithCategory {
    ENDER("ender"),
    RAVAGER("ravager"),
    GUARDIAN("guardian"),
    WARDEN("warden"),
    WITHER("wither"),
    ZENITH("zenith");

    private final String id;

    ZenithCategory(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<ZenithCategory> fromId(String value) {
        if (value == null) return Optional.empty();
        String normalized = value.toLowerCase(Locale.ROOT);

        for (ZenithCategory category : values()) {
            if (category.id.equals(normalized)) {
                return Optional.of(category);
            }
        }

        // Friendly aliases for commands.
        return switch (normalized) {
            case "enderdragon", "dragon", "ender_dragon" -> Optional.of(ENDER);
            case "elder", "elderguardian", "elder_guardian" -> Optional.of(GUARDIAN);
            default -> Optional.empty();
        };
    }
}
