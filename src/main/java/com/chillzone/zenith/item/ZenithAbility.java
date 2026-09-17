package com.chillzone.zenith.item;

public enum ZenithAbility {
    ENDER_STEP(20),
    SHULKER_SHOT(20),
    DRAGON_WARP(40),

    LAST_STAND(20),
    VEX_CALL(20),
    RAVAGER_CHARGE(40),

    GUARDIAN_RAY(20),
    TIDAL_BURST(20),
    ELDER_CURSE(20),
    WRATH_OF_MONUMENT(40),

    ECHO_SENSE(20),
    SONIC_BOOM(20),
    SONIC_DEVASTATION(40),

    INFERNO(20),
    GOLDEN_RUSH(20),
    GHAST_FIREBALL(20),
    WITHERING_BARRAGE(40),

    ZENITH_STORM(300);

    private final int cooldownSeconds;

    ZenithAbility(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int cooldownTicks() {
        return cooldownSeconds * 20;
    }

    public int cooldownSeconds() {
        return cooldownSeconds;
    }
}
