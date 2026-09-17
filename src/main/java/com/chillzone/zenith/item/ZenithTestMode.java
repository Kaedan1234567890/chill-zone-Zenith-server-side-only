package com.chillzone.zenith.item;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ZenithTestMode {
    private ZenithTestMode() {}
    private static final Set<UUID> TESTERS = new HashSet<>();

    public static boolean isTesting(UUID id) { return TESTERS.contains(id); }
    public static void enable(UUID id) { TESTERS.add(id); }
    public static void disable(UUID id) { TESTERS.remove(id); }
}
