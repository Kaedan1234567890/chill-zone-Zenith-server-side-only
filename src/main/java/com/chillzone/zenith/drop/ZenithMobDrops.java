package com.chillzone.zenith.drop;

import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithGate;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Custom progression drops.
 *
 * Drops are gated by the matching /zenith activate/deactivate branch.
 * Vanilla loot is untouched; these stacks are spawned in addition to it.
 */
public final class ZenithMobDrops {
    private ZenithMobDrops() {}

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.level() instanceof ServerLevel level)) {
                return;
            }

            Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (typeId == null) return;
            String mob = typeId.getPath();

            // ENDER
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.ENDER)) {
                if (mob.equals("enderman")) {
                    roll(entity, level, Items.ENDER_EYE, 0.12);
                } else if (mob.equals("shulker")) {
                    roll(entity, level, Items.SHULKER_SHELL, 0.25);
                }
            }

            // RAVAGER
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.RAVAGER)) {
                if (mob.equals("ravager")) {
                    roll(entity, level, Items.GOAT_HORN, 0.35);
                    roll(entity, level, Items.TOTEM_OF_UNDYING, 0.20);
                } else if (mob.equals("evoker")) {
                    // Current prototype: Evokers can drop the Mansion Key.
                    // The original design calls for mansion-only/first-time logic,
                    // which can be tightened separately without blocking testing.
                    roll(entity, level, Items.OMINOUS_TRIAL_KEY, 1.00);
                }
            }

            // GUARDIAN / ELDER GUARDIAN
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.GUARDIAN)) {
                if (mob.equals("guardian")) {
                    roll(entity, level, Items.PRISMARINE_SHARD, 0.10);
                } else if (mob.equals("elder_guardian")) {
                    roll(entity, level, Items.HEART_OF_THE_SEA, 1.00);
                }
            }

            // WARDEN
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.WARDEN)
                    && mob.equals("warden")) {
                roll(entity, level, Items.SCULK_CATALYST, 1.00);
            }

            // WITHER / NETHER
            if (ZenithGate.canDrop(level.getServer(), ZenithCategory.WITHER)) {
                if (mob.equals("blaze")) {
                    roll(entity, level, Items.FIRE_CHARGE, 0.15);
                } else if (mob.equals("wither_skeleton")) {
                    roll(entity, level, Items.WITHER_SKELETON_SKULL, 0.12);
                } else if (mob.equals("piglin")) {
                    roll(entity, level, Items.GOLD_INGOT, 0.08);
                } else if (mob.equals("piglin_brute")) {
                    roll(entity, level, Items.GILDED_BLACKSTONE, 1.00);
                } else if (mob.equals("ghast")) {
                    roll(entity, level, Items.GHAST_TEAR, 0.15);
                }
            }
        });
    }

    private static void roll(
            LivingEntity entity,
            ServerLevel level,
            Item item,
            double chance
    ) {
        if (level.getRandom().nextDouble() < chance) {
            entity.spawnAtLocation(level, new ItemStack(item));
        }
    }
}
