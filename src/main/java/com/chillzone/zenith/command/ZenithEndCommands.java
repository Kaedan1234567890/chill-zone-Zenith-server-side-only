package com.chillzone.zenith.command;

import com.chillzone.zenith.progression.ZenithProgressionState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;

public final class ZenithEndCommands {
    private static final String PERMISSION = "chillzonezenith.command.end";
    private ZenithEndCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("end")
                .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source, PERMISSION, false))
                .then(Commands.literal("lock").executes(ctx -> {
                    ZenithProgressionState.get(ctx.getSource().getServer()).setEndLocked(true);
                    broadcast(ctx.getSource().getServer(), "THE END IS CLOSED!", ChatFormatting.RED,
                            "End travel has been locked.", ChatFormatting.GRAY);
                    return 1;
                }))
                .then(Commands.literal("unlock").executes(ctx -> {
                    ZenithProgressionState.get(ctx.getSource().getServer()).setEndLocked(false);
                    broadcast(ctx.getSource().getServer(), "THE END IS OPEN!", ChatFormatting.LIGHT_PURPLE,
                            "Good luck. Get the egg.", ChatFormatting.GOLD);
                    return 1;
                }))
            )
        );
    }

    private static void broadcast(net.minecraft.server.MinecraftServer server, String title, ChatFormatting titleColor, String subtitle, ChatFormatting subtitleColor) {
        Component titleText = Component.literal(title).withStyle(titleColor, ChatFormatting.BOLD);
        Component subtitleText = Component.literal(subtitle).withStyle(subtitleColor);
        for (var player : server.getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitleTextPacket(titleText));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitleText));
        }
    }
}
