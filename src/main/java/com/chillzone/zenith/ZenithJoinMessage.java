package com.chillzone.zenith;

import com.chillzone.zenith.progression.ZenithProgressionState;
import com.chillzone.zenith.progression.ZenithCategory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ZenithJoinMessage {
    private ZenithJoinMessage() {}

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ZenithProgressionState state = ZenithProgressionState.get(server);

            if (!state.isJoinMessageEnabled()) {
                return;
            }

            var player = handler.getPlayer();

            player.sendSystemMessage(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.DARK_PURPLE));

            player.sendSystemMessage(Component.literal("✦ ZENITH PROGRESSION ✦")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

            player.sendSystemMessage(Component.literal(
                    "The Zenith progression mod is installed on this server.")
                    .withStyle(ChatFormatting.GRAY));

            List<String> activeBranches = new ArrayList<>();

            for (ZenithCategory category : ZenithCategory.values()) {
                if (state.isEnabled(category)) {
                    String name = category.id().substring(0, 1).toUpperCase()
                            + category.id().substring(1);
                    activeBranches.add(name);
                }
            }

            if (activeBranches.isEmpty()) {
                player.sendSystemMessage(
                        Component.literal("Active branches: None right now.")
                                .withStyle(ChatFormatting.YELLOW)
                );
            } else {
                player.sendSystemMessage(
                        Component.literal("Active branches: ")
                                .withStyle(ChatFormatting.WHITE)
                                .append(Component.literal(String.join(", ", activeBranches))
                                        .withStyle(ChatFormatting.GREEN))
                );
            }

            player.sendSystemMessage(
                    Component.literal("Recipes: ")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(
                                    "/zenith craftingrecipe <category> <weapon>")
                                    .withStyle(ChatFormatting.AQUA))
            );

            player.sendSystemMessage(
                    Component.literal("Crafting tables: ")
                            .withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(
                                    "/zenith craftingtable <category>")
                                    .withStyle(ChatFormatting.AQUA))
            );

            player.sendSystemMessage(
                    Component.literal("Each branch has multiple weapons, but only ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("ONE Boss Blade")
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(
                                    " can be legitimately crafted in the world.")
                                    .withStyle(ChatFormatting.GRAY))
            );

            player.sendSystemMessage(Component.literal(
                    "The five Boss Blades are needed to eventually craft the Zenith Blade.")
                    .withStyle(ChatFormatting.GRAY));

            player.sendSystemMessage(
                    Component.literal("Need help? Use ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("/discord")
                                    .withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(
                                    " to get the server Discord link.")
                                    .withStyle(ChatFormatting.GRAY))
            );

            player.sendSystemMessage(Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        });
    }
}
