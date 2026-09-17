package com.chillzone.zenith.item;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class ZenithAbilityScheduler {
    private ZenithAbilityScheduler() {}

    private record PendingWardenBeam(UUID playerId, long fireTick) {}
    private record PendingZenithSound(UUID playerId, long playTick, int step) {}

    private static final List<PendingWardenBeam> WARDEN_BEAMS = new ArrayList<>();
    private static final List<PendingZenithSound> ZENITH_SOUNDS = new ArrayList<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(ZenithAbilityScheduler::tick);
    }

    public static void scheduleWardenBeam(ServerPlayer player, int delayTicks) {
        long now = player.level().getGameTime();
        WARDEN_BEAMS.add(new PendingWardenBeam(player.getUUID(), now + delayTicks));
    }

    public static void scheduleZenithSoundtrack(ServerPlayer player) {
        long now = player.level().getGameTime();
        int[] delays = {0, 2, 4, 6, 9, 13};

        for (int step = 0; step < delays.length; step++) {
            ZENITH_SOUNDS.add(
                    new PendingZenithSound(player.getUUID(), now + delays[step], step)
            );
        }
    }

    private static void tick(MinecraftServer server) {
        Iterator<PendingWardenBeam> iterator = WARDEN_BEAMS.iterator();

        while (iterator.hasNext()) {
            PendingWardenBeam pending = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(pending.playerId());

            if (player == null) {
                iterator.remove();
                continue;
            }

            ServerLevel level = (ServerLevel) player.level();

            if (level.getGameTime() < pending.fireTick()) {
                continue;
            }

            fireWardenBeam(level, player);
            iterator.remove();
        }

        Iterator<PendingZenithSound> soundIterator = ZENITH_SOUNDS.iterator();

        while (soundIterator.hasNext()) {
            PendingZenithSound pending = soundIterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(pending.playerId());

            if (player == null) {
                soundIterator.remove();
                continue;
            }

            ServerLevel level = (ServerLevel) player.level();

            if (level.getGameTime() < pending.playTick()) {
                continue;
            }

            playZenithSound(level, player, pending.step());
            soundIterator.remove();
        }
    }

    private static void playZenithSound(
            ServerLevel level,
            ServerPlayer player,
            int step
    ) {
        switch (step) {
            case 0 -> level.playSound(null, player.blockPosition(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.75F, 1.65F);
            case 1 -> level.playSound(null, player.blockPosition(),
                    SoundEvents.SHULKER_TELEPORT, SoundSource.PLAYERS, 0.85F, 1.35F);
            case 2 -> level.playSound(null, player.blockPosition(),
                    SoundEvents.GUARDIAN_ATTACK, SoundSource.PLAYERS, 0.9F, 1.4F);
            case 3 -> level.playSound(null, player.blockPosition(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.15F);
            case 4 -> level.playSound(null, player.blockPosition(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.15F, 1.35F);
            default -> {
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 0.75F);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 0.8F);
            }
        }
    }

    private static void fireWardenBeam(ServerLevel level, ServerPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(18.0),
                entity -> entity != player && entity.isAlive()
        )) {
            Vec3 to = target.getEyePosition().subtract(start);
            double distance = to.length();

            if (distance <= 0.01 || distance > 15.0) continue;

            double dot = to.normalize().dot(look);
            if (dot < 0.94) continue;

            double score = distance - (dot * 3.0);
            if (score < bestScore) {
                bestScore = score;
                best = target;
            }
        }

        for (double distance = 1.0; distance <= 15.0; distance += 1.0) {
            Vec3 point = start.add(look.scale(distance));
            level.sendParticles(
                    ParticleTypes.SONIC_BOOM,
                    point.x, point.y, point.z,
                    1,
                    0, 0, 0,
                    0
            );
        }

        if (best != null) {
            best.hurtServer(
                    level,
                    player.damageSources().playerAttack(player),
                    10.0F
            );

            Vec3 push = best.position().subtract(player.position()).normalize().scale(1.4);
            best.push(push.x, 0.35, push.z);
        }
    }
}
