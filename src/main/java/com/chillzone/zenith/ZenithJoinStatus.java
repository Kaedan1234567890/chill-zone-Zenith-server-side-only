package com.chillzone.zenith;

import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ZenithJoinStatus {
    private ZenithJoinStatus() {}

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ZenithProgressionState state = ZenithProgressionState.get(server);
            List<Component> branches = new ArrayList<>();

            for (ZenithCategory category : ZenithCategory.values()) {
                if (category == ZenithCategory.ZENITH || !state.isEnabled(category)) {
                    continue;
                }

                boolean crafted = state.isBossCrafted(category);

                Component branch = Component.literal(prettyName(category.id()))
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(" (Boss Blade: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(crafted ? "CRAFTED" : "NOT CRAFTED")
                                .withStyle(crafted ? ChatFormatting.GREEN : ChatFormatting.YELLOW))
                        .append(Component.literal(")").withStyle(ChatFormatting.GRAY));

                branches.add(branch);
            }

            Component message = Component.literal("Active Branches: ")
                    .withStyle(ChatFormatting.GOLD);

            if (branches.isEmpty()) {
                message = message.copy().append(
                        Component.literal("None").withStyle(ChatFormatting.GRAY)
                );
            } else {
                for (int i = 0; i < branches.size(); i++) {
                    if (i > 0) {
                        message = message.copy().append(
                                Component.literal(", ").withStyle(ChatFormatting.GRAY)
                        );
                    }
                    message = message.copy().append(branches.get(i));
                }
            }

            handler.player.sendSystemMessage(message);
        });
    }

    private static String prettyName(String id) {
        if (id == null || id.isBlank()) return "Unknown";
        String cleaned = id.replace('_', ' ').replace('-', ' ');
        StringBuilder out = new StringBuilder();
        boolean upper = true;
        for (char c : cleaned.toCharArray()) {
            if (Character.isWhitespace(c)) {
                out.append(c);
                upper = true;
            } else if (upper) {
                out.append(Character.toUpperCase(c));
                upper = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
