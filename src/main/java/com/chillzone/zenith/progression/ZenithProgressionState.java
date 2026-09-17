package com.chillzone.zenith.progression;

import com.chillzone.zenith.ZenithMod;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.EnumMap;
import java.util.Map;

public final class ZenithProgressionState extends SavedData {
    /*
     * One packed integer keeps this backwards-compatible with the Fix 3/6 save:
     * bits 0-5  = branch activation
     * bits 8-13 = unique boss blade has been legitimately crafted
     */
    private static final Codec<ZenithProgressionState> CODEC =
            Codec.INT.xmap(ZenithProgressionState::new, ZenithProgressionState::toPackedInt);

    private static final SavedDataType<ZenithProgressionState> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, "progression_state"),
                    ZenithProgressionState::new,
                    CODEC,
                    null
            );

    private final EnumMap<ZenithCategory, Boolean> enabled =
            new EnumMap<>(ZenithCategory.class);

    private final EnumMap<ZenithCategory, Boolean> bossCrafted =
            new EnumMap<>(ZenithCategory.class);

    // Bit 16 stores DISABLED instead of enabled so old worlds default to ON.
    private boolean joinMessageEnabled = true;

    public ZenithProgressionState() {
        for (ZenithCategory category : ZenithCategory.values()) {
            enabled.put(category, false);
            bossCrafted.put(category, false);
        }
    }

    private ZenithProgressionState(int packed) {
        this();
        ZenithCategory[] values = ZenithCategory.values();

        for (int i = 0; i < values.length; i++) {
            enabled.put(values[i], (packed & (1 << i)) != 0);
            bossCrafted.put(values[i], (packed & (1 << (i + 8))) != 0);
        }

        // Old saves have this bit unset, which correctly means enabled.
        joinMessageEnabled = (packed & (1 << 16)) == 0;
    }

    private int toPackedInt() {
        int packed = 0;
        ZenithCategory[] values = ZenithCategory.values();

        for (int i = 0; i < values.length; i++) {
            if (isEnabled(values[i])) packed |= (1 << i);
            if (isBossCrafted(values[i])) packed |= (1 << (i + 8));
        }

        if (!joinMessageEnabled) packed |= (1 << 16);

        return packed;
    }

    public boolean isEnabled(ZenithCategory category) {
        return enabled.getOrDefault(category, false);
    }

    public void setEnabled(ZenithCategory category, boolean value) {
        enabled.put(category, value);
        setDirty();
    }

    public void setAll(boolean value) {
        for (ZenithCategory category : ZenithCategory.values()) {
            enabled.put(category, value);
        }
        setDirty();
    }

    public boolean isBossCrafted(ZenithCategory category) {
        return bossCrafted.getOrDefault(category, false);
    }

    public void markBossCrafted(ZenithCategory category) {
        bossCrafted.put(category, true);
        setDirty();
    }

    public void resetBossCrafted(ZenithCategory category) {
        bossCrafted.put(category, false);
        setDirty();
    }

    public Map<ZenithCategory, Boolean> snapshot() {
        return Map.copyOf(enabled);
    }

    public boolean isJoinMessageEnabled() {
        return joinMessageEnabled;
    }

    public void setJoinMessageEnabled(boolean enabled) {
        this.joinMessageEnabled = enabled;
        setDirty();
    }

    public static ZenithProgressionState get(MinecraftServer server) {
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);

        if (level == null) {
            return new ZenithProgressionState();
        }

        return level.getDataStorage().computeIfAbsent(TYPE);
    }
}
