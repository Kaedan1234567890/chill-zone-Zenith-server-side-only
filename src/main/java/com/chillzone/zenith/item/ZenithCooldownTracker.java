package com.chillzone.zenith.item;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side cooldown authority.
 *
 * Vanilla ItemCooldowns are player-session state and are cleared on reconnect.
 * This tracker stores expiry timestamps by player UUID + ability so disconnecting
 * and rejoining cannot bypass a Zenith cooldown.
 *
 * The vanilla ItemCooldowns API is still used as the visual overlay while the
 * player is online.
 */
public final class ZenithCooldownTracker {
    private ZenithCooldownTracker() {}

    private static final Map<UUID, EnumMap<ZenithAbility, Long>> EXPIRY =
            new HashMap<>();

    public static void start(UUID playerId, ZenithAbility ability) {
        long expiresAt = System.currentTimeMillis()
                + (ability.cooldownSeconds() * 1000L);

        EXPIRY.computeIfAbsent(
                playerId,
                ignored -> new EnumMap<>(ZenithAbility.class)
        ).put(ability, expiresAt);
    }

    public static int remainingTicks(UUID playerId, ZenithAbility ability) {
        EnumMap<ZenithAbility, Long> byAbility = EXPIRY.get(playerId);

        if (byAbility == null) return 0;

        Long expiresAt = byAbility.get(ability);
        if (expiresAt == null) return 0;

        long remainingMs = expiresAt - System.currentTimeMillis();

        if (remainingMs <= 0L) {
            byAbility.remove(ability);
            if (byAbility.isEmpty()) {
                EXPIRY.remove(playerId);
            }
            return 0;
        }

        return (int) Math.max(1L, (remainingMs + 49L) / 50L);
    }
}
