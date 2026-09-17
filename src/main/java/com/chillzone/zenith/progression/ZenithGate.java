package com.chillzone.zenith.progression;

import net.minecraft.server.MinecraftServer;

/**
 * One place for every feature to ask:
 * "Is this branch live right now?"
 *
 * Drops, crafting stations and active sword abilities should all call this.
 */
public final class ZenithGate {
    private ZenithGate() {}

    public static boolean isEnabled(MinecraftServer server, ZenithCategory category) {
        return ZenithProgressionState.get(server).isEnabled(category);
    }

    public static boolean canDrop(MinecraftServer server, ZenithCategory category) {
        return isEnabled(server, category);
    }

    public static boolean canCraft(MinecraftServer server, ZenithCategory category) {
        return isEnabled(server, category);
    }

    public static boolean canUseAbility(MinecraftServer server, ZenithCategory category) {
        return isEnabled(server, category);
    }
}
