package com.chillzone.zenith.item;

import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Set;

public class AbilitySwordItem extends Item implements PolymerItem {

    private final ZenithAbility ability;
    private final ZenithCategory category;
    private final boolean uniqueBoss;
    private final Item polymerBaseItem;

    public AbilitySwordItem(
            Properties properties,
            ZenithAbility ability,
            ZenithCategory category,
            boolean uniqueBoss,
            Item polymerBaseItem
    ) {
        super(properties);
        this.ability = ability;
        this.category = category;
        this.uniqueBoss = uniqueBoss;
        this.polymerBaseItem = polymerBaseItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.polymerBaseItem;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        // null tells Polymer to use the vanilla base item's own model instead of
        // the custom Zenith registry item's model id. This is required for
        // unmodded clients when we intentionally use vanilla visuals only.
        return null;
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ZenithProgressionState state = ZenithProgressionState.get(serverLevel.getServer());

        if (!state.isEnabled(this.category)) {
            user.sendSystemMessage(
                    Component.literal("This Zenith branch is currently deactivated.")
                            .withStyle(ChatFormatting.RED)
            );
            return InteractionResult.FAIL;
        }
        ItemStack heldStack = user.getItemInHand(hand);

        boolean testMode = ZenithTestMode.isTesting(user.getUUID());

        if (!testMode) {
            int remainingTicks =
                    ZenithCooldownTracker.remainingTicks(user.getUUID(), this.ability);

            if (remainingTicks > 0) {
                // Reapply the vanilla overlay after reconnect and keep the
                // server-side tracker as the actual anti-bypass authority.
                user.getCooldowns().addCooldown(heldStack, remainingTicks);
                return InteractionResult.FAIL;
            }

            if (user.getCooldowns().isOnCooldown(heldStack)) {
                return InteractionResult.FAIL;
            }
        }

        activate(serverLevel, user);

        if (!testMode) {
            ZenithCooldownTracker.start(user.getUUID(), this.ability);
            user.getCooldowns().addCooldown(heldStack, this.ability.cooldownTicks());
        }

        user.sendSystemMessage(
                Component.literal(abilityDisplayName() + " activated!")
                        .withStyle(ChatFormatting.AQUA)
        );

        return InteractionResult.SUCCESS;
    }

    private void activate(ServerLevel level, Player user) {
        switch (this.ability) {
            case ENDER_STEP -> {
                teleportForward(level, user, 4.0);
                play(level, user, SoundEvents.ENDERMAN_TELEPORT, 0.8F, 1.25F);
            }
            case SHULKER_SHOT -> {
                // Escape tool: blink backwards/sideways at the same height.
                Vec3 look = horizontalLook(user);
                Vec3 side = new Vec3(-look.z, 0.0, look.x);
                Vec3 escape = user.position().subtract(look.scale(5.0)).add(side.scale(2.0));
                teleportTo(level, user, escape);
                portalBurst(level, escape, 28);
                play(level, user, SoundEvents.SHULKER_TELEPORT, 1.0F, 1.1F);
            }
            case DRAGON_WARP -> {
                Vec3 horizontal = new Vec3(user.getDeltaMovement().x, 0.0, user.getDeltaMovement().z);
                if (horizontal.lengthSqr() > 0.01) {
                    teleportForward(level, user, 12.0);
                    blast(level, user, 4.5, 10.0F, 1.4);
                } else {
                    // Standing still: breath burst, then escape backwards.
                    for (LivingEntity target : coneTargets(level, user, 8.0, 0.72)) {
                        damage(level, user, target, 8.0F);
                    }
                    portalTrail(level, user, 8.0);
                    Vec3 escape = user.position().subtract(horizontalLook(user).scale(6.0));
                    teleportTo(level, user, escape);
                }
                play(level, user, SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.15F);
            }

            case LAST_STAND -> {
                // Exactly 8 absorption hearts for 8 seconds.
                // Absorption IV = 16 absorption health points = 8 hearts.
                user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 3));
                play(level, user, SoundEvents.TOTEM_USE, 0.8F, 1.2F);
            }
            case VEX_CALL -> {
                // Prototype: spectral "assist" represented by temporary combat buffs.
                user.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 160, 2));
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 2));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        user.getX(), user.getY() + 1.0, user.getZ(),
                        25, 1.2, 1.0, 1.2, 0.03);
                play(level, user, SoundEvents.EVOKER_CAST_SPELL, 1.0F, 1.1F);
            }
            case RAVAGER_CHARGE -> {
                Vec3 look = horizontalLook(user).scale(1.8);
                user.setDeltaMovement(look.x, 0.35, look.z);
                user.hurtMarked = true;
                blast(level, user, 3.0, 10.0F, 2.2);
                user.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 2));
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 2));
                play(level, user, SoundEvents.RAVAGER_ROAR, 1.0F, 1.1F);
            }

            case GUARDIAN_RAY -> {
                LivingEntity target = targetInFront(level, user, 22.0);
                if (target != null) {
                    target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 1));
                }
                prismarineTrail(level, user, 20.0);
                play(level, user, SoundEvents.GUARDIAN_ATTACK, 0.9F, 1.2F);
            }
            case TIDAL_BURST -> {
                pushAway(level, user, 6.0, 1.35);
                level.sendParticles(ParticleTypes.SPLASH,
                        user.getX(), user.getY() + 0.5, user.getZ(),
                        60, 2.5, 1.0, 2.5, 0.12);
            }
            case ELDER_CURSE -> {
                for (LivingEntity target : nearby(level, user, 12.0)) {
                    if (target.isInWaterOrRain()) {
                        target.setAirSupply(0);
                    }
                }
                play(level, user, SoundEvents.ELDER_GUARDIAN_CURSE, 1.0F, 1.0F);
            }
            case WRATH_OF_MONUMENT -> {
                pushAway(level, user, 8.0, 1.8);
                LivingEntity target = targetInFront(level, user, 26.0);
                if (target != null) {
                    target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 2));
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1));
                }
                prismarineTrail(level, user, 24.0);
                play(level, user, SoundEvents.ELDER_GUARDIAN_CURSE, 1.0F, 0.85F);
            }

            case ECHO_SENSE -> {
                for (LivingEntity target : nearby(level, user, 18.0)) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL,
                        user.getX(), user.getY() + 1.0, user.getZ(),
                        35, 2.0, 1.0, 2.0, 0.02);
                play(level, user, SoundEvents.WARDEN_HEARTBEAT, 1.0F, 1.0F);
            }
            case SONIC_BOOM -> {
                LivingEntity target = targetInFront(level, user, 24.0);
                if (target != null) damage(level, user, target, 5.0F);
                sonicTrail(level, user, 22.0);
            }
            case SONIC_DEVASTATION -> {
                // Warden Blade prototype:
                // 1) reveal nearby living targets,
                // 2) give the wielder 2.5 seconds to aim,
                // 3) fire a 15-block Warden beam along the CURRENT aim direction.
                for (LivingEntity target : nearby(level, user, 18.0)) {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                }

                level.sendParticles(
                        ParticleTypes.SCULK_SOUL,
                        user.getX(), user.getY() + 1.0, user.getZ(),
                        45,
                        2.5, 1.2, 2.5,
                        0.03
                );

                if (user instanceof ServerPlayer serverPlayer) {
                    ZenithAbilityScheduler.scheduleWardenBeam(serverPlayer, 50);
                }
            }

            case INFERNO -> {
                for (LivingEntity target : nearby(level, user, 5.0)) {
                    target.igniteForSeconds(5.0F);
                }
                level.sendParticles(ParticleTypes.FLAME,
                        user.getX(), user.getY() + 0.5, user.getZ(),
                        55, 2.2, 1.0, 2.2, 0.06);
            }
            case GOLDEN_RUSH -> {
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 240, 1));
                user.addEffect(new MobEffectInstance(MobEffects.HASTE, 240, 1));
                play(level, user, SoundEvents.PIGLIN_CELEBRATE, 0.8F, 1.2F);
            }
            case GHAST_FIREBALL -> {
                // Prototype: ray-hit version; terrain is never damaged.
                LivingEntity target = targetInFront(level, user, 28.0);
                if (target != null) {
                    damage(level, user, target, 5.0F);
                    target.igniteForSeconds(4.0F);
                }
                level.sendParticles(ParticleTypes.FLAME,
                        user.getX(), user.getEyeY(), user.getZ(),
                        20, 0.3, 0.3, 0.3, 0.04);
            }
            case WITHERING_BARRAGE -> {
                // Boss version: one focused Wither-skull style shot, ~half base health unarmoured.
                LivingEntity target = targetInFront(level, user, 28.0);
                if (target != null) {
                    damage(level, user, target, 10.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
                }
                trail(level, user, 28.0, ParticleTypes.SOUL);
                play(level, user, SoundEvents.WITHER_SHOOT, 1.0F, 1.0F);
            }

            case ZENITH_STORM -> {
                if (user instanceof ServerPlayer serverPlayer) {
                    ZenithAbilityScheduler.scheduleZenithSoundtrack(serverPlayer);
                }

                // Fifteen simultaneous spectral lanes:
                // 7 left + centre + 7 right.
                for (int lane = -7; lane <= 7; lane++) {
                    zenithLane(level, user, lane * 0.65);
                }

                level.sendParticles(
                        ParticleTypes.END_ROD,
                        user.getX(), user.getEyeY(), user.getZ(),
                        100,
                        1.8, 1.1, 1.8,
                        0.12
                );
            }
        }
    }

    /*
     * Vanilla calls Item#onDestroyed when a dropped ItemEntity is actually destroyed
     * (for example by fire/explosion). Pickup, storage, Ender Chests and player logout
     * are NOT destruction, so they do not unlock the unique recipe.
     */
    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        if (!this.uniqueBoss) return;
        if (!(itemEntity.level() instanceof ServerLevel serverLevel)) return;

        ZenithProgressionState state =
                ZenithProgressionState.get(serverLevel.getServer());

        if (this.category == ZenithCategory.ZENITH) {
            // The five Boss Blades were consumed to make Zenith.
            // While Zenith exists, all six unique locks remain active.
            // If Zenith is genuinely destroyed as an ItemEntity, release all six.
            state.resetBossCrafted(ZenithCategory.ENDER);
            state.resetBossCrafted(ZenithCategory.RAVAGER);
            state.resetBossCrafted(ZenithCategory.GUARDIAN);
            state.resetBossCrafted(ZenithCategory.WARDEN);
            state.resetBossCrafted(ZenithCategory.WITHER);
            state.resetBossCrafted(ZenithCategory.ZENITH);
        } else {
            state.resetBossCrafted(this.category);
        }
    }


    private void teleportTo(ServerLevel level, Player user, Vec3 destination) {
        if (user instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(
                    level, destination.x, destination.y, destination.z,
                    Set.of(), user.getYRot(), user.getXRot(), false
            );
        } else {
            user.setPos(destination);
        }
    }

    private void pushAway(ServerLevel level, Player user, double radius, double strength) {
        for (LivingEntity target : nearby(level, user, radius)) {
            Vec3 delta = target.position().subtract(user.position());
            Vec3 flat = new Vec3(delta.x, 0.0, delta.z);
            if (flat.lengthSqr() < 0.001) flat = new Vec3(0, 0, 1);
            flat = flat.normalize().scale(strength);
            target.push(flat.x, 0.18, flat.z);
        }
    }

    private void play(ServerLevel level, Player user, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        level.playSound(null, user.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private void damage(ServerLevel level, Player attacker, LivingEntity target, float amount) {
        target.hurtServer(level, attacker.damageSources().playerAttack(attacker), amount);
    }

    private List<LivingEntity> nearby(ServerLevel level, Player user, double radius) {
        AABB box = user.getBoundingBox().inflate(radius);
        return level.getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> entity != user && entity.isAlive()
        );
    }

    private List<LivingEntity> coneTargets(
            ServerLevel level,
            Player user,
            double range,
            double minDot
    ) {
        Vec3 origin = user.getEyePosition();
        Vec3 look = user.getLookAngle().normalize();

        return level.getEntitiesOfClass(
                LivingEntity.class,
                user.getBoundingBox().inflate(range),
                entity -> {
                    if (entity == user || !entity.isAlive()) return false;

                    Vec3 to = entity.getEyePosition().subtract(origin);
                    double distance = to.length();

                    if (distance <= 0.01 || distance > range) return false;
                    return to.normalize().dot(look) >= minDot;
                }
        );
    }

    private LivingEntity targetInFront(ServerLevel level, Player user, double range) {
        Vec3 origin = user.getEyePosition();
        Vec3 look = user.getLookAngle().normalize();

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                user.getBoundingBox().inflate(range),
                candidate -> candidate != user && candidate.isAlive()
        )) {
            Vec3 to = entity.getEyePosition().subtract(origin);
            double distance = to.length();

            if (distance <= 0.01 || distance > range) continue;

            double dot = to.normalize().dot(look);
            if (dot < 0.94) continue;

            double score = distance - (dot * 2.0);
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }

        return best;
    }

    private void teleportForward(ServerLevel level, Player user, double distance) {
        Vec3 look = horizontalLook(user);
        Vec3 destination = user.position().add(look.scale(distance));

        if (user instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(
                    level,
                    destination.x,
                    destination.y,
                    destination.z,
                    Set.of(),
                    user.getYRot(),
                    user.getXRot(),
                    false
            );
        } else {
            user.setPos(destination);
        }

        portalBurst(level, destination, 25);
    }

    private Vec3 horizontalLook(Player user) {
        Vec3 look = user.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0, look.z);

        if (horizontal.lengthSqr() < 0.0001) {
            return new Vec3(0, 0, 1);
        }

        return horizontal.normalize();
    }

    private void blast(
            ServerLevel level,
            Player user,
            double radius,
            float damage,
            double knockback
    ) {
        for (LivingEntity target : nearby(level, user, radius)) {
            damage(level, user, target, damage);

            Vec3 push = target.position()
                    .subtract(user.position())
                    .normalize()
                    .scale(knockback);

            target.push(push.x, 0.35, push.z);
        }
    }

    private void portalBurst(ServerLevel level, Vec3 pos, int count) {
        level.sendParticles(
                ParticleTypes.PORTAL,
                pos.x,
                pos.y + 1.0,
                pos.z,
                count,
                1.0,
                1.0,
                1.0,
                0.12
        );
    }

    private void portalTrail(ServerLevel level, Player user, double range) {
        trail(level, user, range, ParticleTypes.PORTAL);
    }

    private void prismarineTrail(ServerLevel level, Player user, double range) {
        trail(level, user, range, ParticleTypes.END_ROD);
    }

    private void sonicTrail(ServerLevel level, Player user, double range) {
        trail(level, user, range, ParticleTypes.SONIC_BOOM);
    }

    private void trail(
            ServerLevel level,
            Player user,
            double range,
            net.minecraft.core.particles.ParticleOptions particle
    ) {
        Vec3 start = user.getEyePosition();
        Vec3 look = user.getLookAngle().normalize();

        for (double d = 1.0; d <= range; d += 1.5) {
            Vec3 p = start.add(look.scale(d));
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    private void zenithLane(ServerLevel level, Player user, double sideways) {
        Vec3 look = user.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0.0, look.x).normalize();
        Vec3 origin = user.getEyePosition().add(side.scale(sideways));

        for (double d = 2.0; d <= 28.0; d += 2.0) {
            Vec3 p = origin.add(look.scale(d));

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    p.x, p.y, p.z,
                    2,
                    0.1, 0.1, 0.1,
                    0.01
            );

            AABB hitBox = new AABB(
                    p.x - 0.8, p.y - 0.8, p.z - 0.8,
                    p.x + 0.8, p.y + 0.8, p.z + 0.8
            );

            for (LivingEntity target : level.getEntitiesOfClass(
                    LivingEntity.class,
                    hitBox,
                    entity -> entity != user && entity.isAlive()
            )) {
                damage(level, user, target, 10000.0F);
            }
        }
    }

    private String abilityDisplayName() {
        return switch (this.ability) {
            case ENDER_STEP -> "Ender Step";
            case SHULKER_SHOT -> "Shulker Shot";
            case DRAGON_WARP -> "Dragon Warp";
            case LAST_STAND -> "Last Stand";
            case VEX_CALL -> "Vex Call";
            case RAVAGER_CHARGE -> "Ravager Charge";
            case GUARDIAN_RAY -> "Guardian Ray";
            case TIDAL_BURST -> "Tidal Burst";
            case ELDER_CURSE -> "Elder Curse";
            case WRATH_OF_MONUMENT -> "Wrath of the Monument";
            case ECHO_SENSE -> "Echo Sense";
            case SONIC_BOOM -> "Sonic Boom";
            case SONIC_DEVASTATION -> "Sonic Devastation";
            case INFERNO -> "Inferno";
            case GOLDEN_RUSH -> "Golden Rush";
            case GHAST_FIREBALL -> "Ghast Fireball";
            case WITHERING_BARRAGE -> "Withering Barrage";
            case ZENITH_STORM -> "Zenith Storm";
        };
    }
}
