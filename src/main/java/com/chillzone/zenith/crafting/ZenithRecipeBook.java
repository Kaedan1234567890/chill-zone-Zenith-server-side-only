package com.chillzone.zenith.crafting;

import com.chillzone.zenith.item.ZenithStackIdentity;

import com.chillzone.zenith.ZenithMod;
import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ZenithRecipeBook {
    private ZenithRecipeBook() {}

    public record RecipeDef(
            ZenithCategory category,
            String output,
            boolean uniqueBoss,
            String[] grid
    ) {}

    private static String mc(String path) {
        return "minecraft:" + path;
    }

    private static String cz(String path) {
        return ZenithMod.MOD_ID + ":" + path;
    }

    private static RecipeDef recipe(
            ZenithCategory category,
            String output,
            boolean uniqueBoss,
            String... grid
    ) {
        if (grid.length != 9) throw new IllegalArgumentException("Recipe grid must contain 9 entries.");
        return new RecipeDef(category, cz(output), uniqueBoss, grid);
    }

    public static final List<RecipeDef> RECIPES = List.of(
        // ENDER
        recipe(ZenithCategory.ENDER, "ender_blade", false,
                cz("ender_essence"), mc("ender_eye"), cz("ender_essence"),
                mc("ender_eye"), mc("diamond_sword"), mc("ender_eye"),
                mc("chorus_fruit"), mc("stick"), mc("chorus_fruit")),

        recipe(ZenithCategory.ENDER, "shulker_blade", false,
                cz("shulker_essence"), mc("shulker_shell"), cz("shulker_essence"),
                mc("shulker_shell"), mc("diamond_sword"), mc("shulker_shell"),
                mc("chorus_fruit"), mc("ender_eye"), mc("chorus_fruit")),

        recipe(ZenithCategory.ENDER, "ender_dragon_blade", true,
                mc("end_rod"), mc("dragon_breath"), mc("end_rod"),
                cz("ender_blade"), mc("netherite_sword"), cz("shulker_blade"),
                mc("ender_eye"), mc("dragon_breath"), mc("ender_eye")),

        // RAVAGER
        recipe(ZenithCategory.RAVAGER, "sword_of_undying", false,
                mc("totem_of_undying"), cz("ravager_horn"), mc("totem_of_undying"),
                mc("emerald_block"), mc("diamond_sword"), mc("emerald_block"),
                mc("crossbow"), mc("redstone"), mc("crossbow")),

        recipe(ZenithCategory.RAVAGER, "mansion_blade", false,
                mc("totem_of_undying"), cz("mansion_key"), mc("totem_of_undying"),
                mc("emerald_block"), mc("diamond_sword"), mc("emerald_block"),
                mc("dark_oak_log"), mc("crossbow"), mc("dark_oak_log")),

        recipe(ZenithCategory.RAVAGER, "ravager_blade", true,
                mc("totem_of_undying"), cz("ravager_horn"), mc("totem_of_undying"),
                cz("sword_of_undying"), cz("ravager_heart"), cz("mansion_blade"),
                mc("emerald_block"), mc("netherite_ingot"), mc("emerald_block")),

        // GUARDIAN
        recipe(ZenithCategory.GUARDIAN, "prismarine_blade", false,
                mc("prismarine_shard"), cz("guardian_scale"), mc("prismarine_shard"),
                mc("prismarine_crystals"), mc("diamond_sword"), mc("prismarine_crystals"),
                mc("dark_prismarine"), cz("guardian_scale"), mc("dark_prismarine")),

        recipe(ZenithCategory.GUARDIAN, "sponge_blade", false,
                mc("wet_sponge"), mc("sponge"), mc("wet_sponge"),
                mc("prismarine_bricks"), mc("diamond_sword"), mc("prismarine_bricks"),
                mc("prismarine_shard"), mc("heart_of_the_sea"), mc("prismarine_shard")),

        recipe(ZenithCategory.GUARDIAN, "elder_tide_blade", false,
                mc("sea_lantern"), cz("elder_guardian_core"), mc("sea_lantern"),
                mc("dark_prismarine"), mc("netherite_sword"), mc("dark_prismarine"),
                mc("wet_sponge"), cz("elder_guardian_core"), mc("wet_sponge")),

        recipe(ZenithCategory.GUARDIAN, "elder_guardian_blade", true,
                mc("sponge"), cz("elder_guardian_core"), mc("sponge"),
                cz("prismarine_blade"), cz("elder_tide_blade"), cz("sponge_blade"),
                mc("sea_lantern"), mc("heart_of_the_sea"), mc("sea_lantern")),

        // WARDEN
        recipe(ZenithCategory.WARDEN, "echo_blade", false,
                mc("echo_shard"), mc("sculk_sensor"), mc("echo_shard"),
                mc("echo_shard"), mc("diamond_sword"), mc("echo_shard"),
                mc("sculk"), mc("recovery_compass"), mc("sculk")),

        recipe(ZenithCategory.WARDEN, "wardens_wrath", false,
                mc("sculk_catalyst"), cz("warden_heart"), mc("sculk_catalyst"),
                mc("sculk_sensor"), mc("netherite_sword"), mc("sculk_sensor"),
                mc("deepslate_tiles"), cz("warden_heart"), mc("deepslate_tiles")),

        recipe(ZenithCategory.WARDEN, "warden_blade", true,
                mc("echo_shard"), cz("warden_heart"), mc("echo_shard"),
                cz("echo_blade"), mc("sculk_catalyst"), cz("wardens_wrath"),
                mc("sculk_sensor"), cz("warden_heart"), mc("sculk_sensor")),

        // WITHER
        recipe(ZenithCategory.WITHER, "blade_of_fire", false,
                mc("blaze_rod"), mc("wither_skeleton_skull"), mc("blaze_rod"),
                cz("blazing_core"), mc("diamond_sword"), cz("blazing_core"),
                mc("nether_bricks"), cz("withered_fragment"), mc("nether_bricks")),

        recipe(ZenithCategory.WITHER, "golden_desire", false,
                mc("gold_block"), cz("brutes_emblem"), mc("gold_block"),
                cz("piglin_sigil"), mc("golden_sword"), cz("piglin_sigil"),
                mc("gilded_blackstone"), mc("ancient_debris"), mc("gilded_blackstone")),

        recipe(ZenithCategory.WITHER, "ghost_blade", false,
                mc("ghast_tear"), cz("ghast_essence"), mc("ghast_tear"),
                mc("soul_sand"), mc("diamond_sword"), mc("soul_sand"),
                mc("soul_soil"), cz("ghast_essence"), mc("soul_soil")),

        recipe(ZenithCategory.WITHER, "wither_blade", true,
                mc("wither_skeleton_skull"), mc("soul_sand"), mc("wither_skeleton_skull"),
                cz("blade_of_fire"), mc("nether_star"), cz("golden_desire"),
                mc("blaze_rod"), cz("ghost_blade"), mc("ghast_tear")),

        // ZENITH
        recipe(ZenithCategory.ZENITH, "zenith_blade", true,
                cz("ender_dragon_blade"), mc("netherite_ingot"), cz("ravager_blade"),
                mc("netherite_ingot"), cz("elder_guardian_blade"), mc("netherite_ingot"),
                cz("warden_blade"), mc("netherite_ingot"), cz("wither_blade"))
    );

    public static RecipeDef findMatch(
            ServerLevel level,
            ZenithCategory category,
            Container grid
    ) {
        ZenithProgressionState state = ZenithProgressionState.get(level.getServer());

        // If this branch is not live, NOTHING from that branch can be crafted.
        if (!state.isEnabled(category)) return null;

        for (RecipeDef recipe : RECIPES) {
            if (recipe.category() != category) continue;

            // The five branch Boss Blades + Zenith can only be legitimately crafted once.
            if (recipe.uniqueBoss() && state.isBossCrafted(category)) continue;

            if (matches(recipe.grid(), grid)) return recipe;
        }

        return null;
    }

    public static ItemStack makeResult(RecipeDef recipe) {
        if (recipe == null) return ItemStack.EMPTY;

        Identifier id = Identifier.parse(recipe.output());
        if (!BuiltInRegistries.ITEM.containsKey(id)) return ItemStack.EMPTY;

        Item item = BuiltInRegistries.ITEM.getValue(id);
        return new ItemStack(item);
    }

    public static boolean stackIs(ItemStack stack, String id) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier actual = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return actual != null && actual.toString().equals(id);
    }

    private static boolean matches(String[] expected, Container grid) {
        if (grid.getContainerSize() < 9) return false;

        for (int i = 0; i < 9; i++) {
            String actual = "";
            ItemStack stack = grid.getItem(i);

            String want = expected[i] == null ? "" : expected[i];

            if (!stack.isEmpty()) {
                // Phase 1: Ender Essence is vanilla-backed and identified by built-in CUSTOM_DATA.
                if ("chillzonezenith:ender_essence".equals(want)
                        && ZenithStackIdentity.is(stack, "ender_essence")) {
                    continue;
                }

                Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                actual = id == null ? "" : id.toString();
            }

            if (!want.equals(actual)) return false;
        }

        return true;
    }
}
