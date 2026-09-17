package com.chillzone.zenith.item;

import com.chillzone.zenith.ZenithMod;
import com.chillzone.zenith.block.ZenithBlocks;
import com.chillzone.zenith.progression.ZenithCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.function.Function;

public final class ZenithItems {
    private ZenithItems() {}

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, name)
        );
    }

    private static Item register(
            String name,
            Function<Item.Properties, Item> factory,
            Item.Properties properties
    ) {
        ResourceKey<Item> key = key(name);
        Item item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return item;
    }

    private static Item material(String name) {
        return register(name, Item::new, new Item.Properties());
    }

    private static Item sword(
            String id,
            String displayName,
            ChatFormatting color,
            ZenithAbility ability,
            ZenithCategory category,
            boolean uniqueBoss,
            String description
    ) {
        Item.Properties properties = new Item.Properties()
                .sword(ToolMaterial.NETHERITE, 1.0F, -2.4F)
                .component(
                        DataComponents.CUSTOM_NAME,
                        Component.literal(displayName).withStyle(
                                style -> style
                                        .withColor(color)
                                        .withBold(true)
                                        .withItalic(false)
                        )
                )
                .component(
                        DataComponents.LORE,
                        new ItemLore(buildLore(
                                description,
                                color,
                                ability,
                                uniqueBoss,
                                category
                        ))
                );

        return register(
                id,
                settings -> new AbilitySwordItem(settings, ability, category, uniqueBoss),
                properties
        );
    }


    private static List<Component> buildLore(
            String description,
            ChatFormatting color,
            ZenithAbility ability,
            boolean uniqueBoss,
            ZenithCategory category
    ) {
        java.util.ArrayList<Component> lore = new java.util.ArrayList<>();

        if (uniqueBoss) {
            String label = category == ZenithCategory.ZENITH
                    ? "★★★ ULTIMATE BLADE ★★★"
                    : "★★★ BOSS BLADE ★★★";

            lore.add(
                    Component.literal(label).withStyle(
                            style -> style
                                    .withColor(color)
                                    .withBold(true)
                                    .withItalic(false)
                    )
            );
        }

        lore.add(
                Component.literal(description)
                        .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false))
        );

        lore.add(
                Component.literal("Right Click • " + abilityDetails(ability))
                        .withStyle(style -> style.withColor(color).withItalic(false))
        );

        lore.add(
                Component.literal("Cooldown • " + formatCooldown(ability.cooldownSeconds()))
                        .withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY).withItalic(false))
        );

        return lore;
    }

    private static String abilityName(ZenithAbility ability) {
        return switch (ability) {
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


    private static String formatCooldown(int seconds) {
        if (seconds >= 60 && seconds % 60 == 0) return (seconds / 60) + " minutes";
        return seconds + "s";
    }

    private static String abilityDetails(ZenithAbility ability) {
        return switch (ability) {
            case ENDER_STEP -> "Blink 4 blocks forward.";
            case SHULKER_SHOT -> "Blink 5 blocks backward and slightly sideways.";
            case DRAGON_WARP -> "Moving: 12-block damaging leap. Standing: breath burst, then retreat.";
            case LAST_STAND -> "Gain Absorption VIII for 8 seconds.";
            case VEX_CALL -> "Gain Speed III + Strength III for 8 seconds.";
            case RAVAGER_CHARGE -> "Ram forward, then gain Speed III + Strength III for 10 seconds.";
            case GUARDIAN_RAY -> "Hit your aimed target with Mining Fatigue for 10 seconds.";
            case TIDAL_BURST -> "Push nearby enemies away with a non-damaging wave.";
            case ELDER_CURSE -> "Nearby enemies in water immediately lose their air.";
            case WRATH_OF_MONUMENT -> "Strong knockback + Mining Fatigue + Slowness.";
            case ECHO_SENSE -> "Reveal living targets within 18 blocks.";
            case SONIC_BOOM -> "Fire a weakened sonic hit; roughly four hits unarmoured.";
            case SONIC_DEVASTATION -> "Reveal nearby targets, then fire a delayed heavy sonic beam.";
            case INFERNO -> "Ignite nearby enemies; damage comes from burning.";
            case GOLDEN_RUSH -> "Gain Speed II + Haste II for 12 seconds.";
            case GHAST_FIREBALL -> "Fire a mini flame shot; no terrain destruction.";
            case WITHERING_BARRAGE -> "Fire one heavy Wither-style strike; about half base health unarmoured.";
            case ZENITH_STORM -> "Unleash 15 lethal spectral blade lanes at once; Totems still work.";
        };
    }

    // Materials
    // Phase 1 server-side conversion:
    // Ender Essence is now a vanilla-backed stack, NOT a registered custom item.
    // Keep this Item reference only as the vanilla base for drop plumbing.
    public static final Item ENDER_ESSENCE = Items.AMETHYST_SHARD;

    public static ItemStack createEnderEssence(int count) {
        ItemStack stack = new ItemStack(Items.AMETHYST_SHARD, count);
        ZenithStackIdentity.mark(stack, "ender_essence");
        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal("Ender Essence").withStyle(
                        style -> style
                                .withColor(ChatFormatting.DARK_PURPLE)
                                .withItalic(false)
                )
        );
        return stack;
    }
    public static final Item SHULKER_ESSENCE = material("shulker_essence");
    public static final Item RAVAGER_HORN = material("ravager_horn");
    public static final Item RAVAGER_HEART = material("ravager_heart");
    public static final Item MANSION_KEY = material("mansion_key");
    public static final Item GUARDIAN_SCALE = material("guardian_scale");
    public static final Item ELDER_GUARDIAN_CORE = material("elder_guardian_core");
    public static final Item WARDEN_HEART = material("warden_heart");
    public static final Item BLAZING_CORE = material("blazing_core");
    public static final Item WITHERED_FRAGMENT = material("withered_fragment");
    public static final Item PIGLIN_SIGIL = material("piglin_sigil");
    public static final Item BRUTES_EMBLEM = material("brutes_emblem");
    public static final Item GHAST_ESSENCE = material("ghast_essence");

    // Ender
    public static final Item ENDER_BLADE = sword(
            "ender_blade", "Ender Blade", ChatFormatting.DARK_PURPLE,
            ZenithAbility.ENDER_STEP, ZenithCategory.ENDER, false,
            "A short-range escape blade built for quick repositioning."
    );
    public static final Item SHULKER_BLADE = sword(
            "shulker_blade", "Shulker Blade", ChatFormatting.DARK_PURPLE,
            ZenithAbility.SHULKER_SHOT, ZenithCategory.ENDER, false,
            "A defensive escape blade that blinks you out of danger."
    );
    public static final Item ENDER_DRAGON_BLADE = sword(
            "ender_dragon_blade", "★ ENDER DRAGON BLADE ★", ChatFormatting.DARK_PURPLE,
            ZenithAbility.DRAGON_WARP, ZenithCategory.ENDER, true,
            "The completed End weapon, combining mobility with burst damage."
    );

    // Ravager
    public static final Item SWORD_OF_UNDYING = sword(
            "sword_of_undying", "Sword of Undying", ChatFormatting.GOLD,
            ZenithAbility.LAST_STAND, ZenithCategory.RAVAGER, false,
            "Illager-Totem magic grants a powerful temporary shield."
    );
    public static final Item MANSION_BLADE = sword(
            "mansion_blade", "Mansion Blade", ChatFormatting.GOLD,
            ZenithAbility.VEX_CALL, ZenithCategory.RAVAGER, false,
            "A short combat surge for chasing, escaping, or turning a fight."
    );
    public static final Item RAVAGER_BLADE = sword(
            "ravager_blade", "★ RAVAGER BLADE ★", ChatFormatting.GOLD,
            ZenithAbility.RAVAGER_CHARGE, ZenithCategory.RAVAGER, true,
            "A heavy charge weapon that follows impact with a combat surge."
    );

    // Guardian
    public static final Item PRISMARINE_BLADE = sword(
            "prismarine_blade", "Prismarine Blade", ChatFormatting.AQUA,
            ZenithAbility.GUARDIAN_RAY, ZenithCategory.GUARDIAN, false,
            "A control blade that weakens a target's ability to mine."
    );
    public static final Item SPONGE_BLADE = sword(
            "sponge_blade", "Sponge Blade", ChatFormatting.AQUA,
            ZenithAbility.TIDAL_BURST, ZenithCategory.GUARDIAN, false,
            "A defensive wave that creates space without direct damage."
    );
    public static final Item ELDER_TIDE_BLADE = sword(
            "elder_tide_blade", "Elder Tide Blade", ChatFormatting.AQUA,
            ZenithAbility.ELDER_CURSE, ZenithCategory.GUARDIAN, false,
            "An underwater control blade that strips nearby enemies of air."
    );
    public static final Item ELDER_GUARDIAN_BLADE = sword(
            "elder_guardian_blade", "★ ELDER GUARDIAN BLADE ★", ChatFormatting.AQUA,
            ZenithAbility.WRATH_OF_MONUMENT, ZenithCategory.GUARDIAN, true,
            "The monument's control weapon: knockback, Slowness, and Mining Fatigue."
    );

    // Warden
    public static final Item ECHO_BLADE = sword(
            "echo_blade", "Echo Blade", ChatFormatting.DARK_AQUA,
            ZenithAbility.ECHO_SENSE, ZenithCategory.WARDEN, false,
            "A tracking blade that reveals nearby living targets through walls."
    );
    public static final Item WARDENS_WRATH = sword(
            "wardens_wrath", "Warden's Wrath", ChatFormatting.DARK_AQUA,
            ZenithAbility.SONIC_BOOM, ZenithCategory.WARDEN, false,
            "A weakened sonic weapon designed to pressure, not instantly kill."
    );
    public static final Item WARDEN_BLADE = sword(
            "warden_blade", "★ WARDEN BLADE ★", ChatFormatting.DARK_AQUA,
            ZenithAbility.SONIC_DEVASTATION, ZenithCategory.WARDEN, true,
            "The completed Sculk weapon: reveal first, then fire a heavy sonic beam."
    );

    // Wither
    public static final Item BLADE_OF_FIRE = sword(
            "blade_of_fire", "Blade of Fire", ChatFormatting.DARK_RED,
            ZenithAbility.INFERNO, ZenithCategory.WITHER, false,
            "A close-range fire tool that burns nearby enemies."
    );
    public static final Item GOLDEN_DESIRE = sword(
            "golden_desire", "Golden Desire", ChatFormatting.DARK_RED,
            ZenithAbility.GOLDEN_RUSH, ZenithCategory.WITHER, false,
            "A movement and mining surge powered by Piglin gold."
    );
    public static final Item GHOST_BLADE = sword(
            "ghost_blade", "Ghost Blade", ChatFormatting.DARK_RED,
            ZenithAbility.GHAST_FIREBALL, ZenithCategory.WITHER, false,
            "A compact fire shot that burns targets without destroying terrain."
    );
    public static final Item WITHER_BLADE = sword(
            "wither_blade", "★ WITHER BLADE ★", ChatFormatting.DARK_RED,
            ZenithAbility.WITHERING_BARRAGE, ZenithCategory.WITHER, true,
            "The completed Nether weapon: one heavy Wither-style projectile strike."
    );

    // Final
    public static final Item ZENITH_BLADE = sword(
            "zenith_blade", "★★★ ZENITH BLADE ★★★", ChatFormatting.LIGHT_PURPLE,
            ZenithAbility.ZENITH_STORM, ZenithCategory.ZENITH, true,
            "The five Boss Blades united into the server's ultimate weapon."
    );


    public static final ResourceKey<CreativeModeTab> ZENITH_CREATIVE_TAB_KEY =
            ResourceKey.create(
                    BuiltInRegistries.CREATIVE_MODE_TAB.key(),
                    Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, "zenith_tab")
            );

    public static final CreativeModeTab ZENITH_CREATIVE_TAB =
            FabricCreativeModeTab.builder()
                    .icon(() -> new ItemStack(ZENITH_BLADE))
                    .title(Component.translatable("creativeTab.chillzonezenith"))
                    .displayItems((parameters, output) -> {
                    
    // Materials
                        output.accept(ENDER_ESSENCE);
                        output.accept(SHULKER_ESSENCE);
                        output.accept(RAVAGER_HORN);
                        output.accept(RAVAGER_HEART);
                        output.accept(MANSION_KEY);
                        output.accept(GUARDIAN_SCALE);
                        output.accept(ELDER_GUARDIAN_CORE);
                        output.accept(WARDEN_HEART);
                        output.accept(BLAZING_CORE);
                        output.accept(WITHERED_FRAGMENT);
                        output.accept(PIGLIN_SIGIL);
                        output.accept(BRUTES_EMBLEM);
                        output.accept(GHAST_ESSENCE);

                        // Ender
                        output.accept(ENDER_BLADE);
                        output.accept(SHULKER_BLADE);
                        output.accept(ENDER_DRAGON_BLADE);

                        // Ravager
                        output.accept(SWORD_OF_UNDYING);
                        output.accept(MANSION_BLADE);
                        output.accept(RAVAGER_BLADE);

                        // Guardian
                        output.accept(PRISMARINE_BLADE);
                        output.accept(SPONGE_BLADE);
                        output.accept(ELDER_TIDE_BLADE);
                        output.accept(ELDER_GUARDIAN_BLADE);

                        // Warden
                        output.accept(ECHO_BLADE);
                        output.accept(WARDENS_WRATH);
                        output.accept(WARDEN_BLADE);

                        // Wither
                        output.accept(BLADE_OF_FIRE);
                        output.accept(GOLDEN_DESIRE);
                        output.accept(GHOST_BLADE);
                        output.accept(WITHER_BLADE);

                        // Final
                        output.accept(ZENITH_BLADE);

                        // Custom crafting stations
                        // Fabric 26.2 supports adding Blocks directly in the
                        // custom creative-tab builder.
                        output.accept(ZenithBlocks.ENDER_CRAFTING_TABLE);
                        output.accept(ZenithBlocks.RAVAGER_CRAFTING_TABLE);
                        output.accept(ZenithBlocks.GUARDIAN_CRAFTING_TABLE);
                        output.accept(ZenithBlocks.WARDEN_CRAFTING_TABLE);
                        output.accept(ZenithBlocks.WITHER_CRAFTING_TABLE);
                        output.accept(ZenithBlocks.ZENITH_CRAFTING_TABLE);
                    })
                    .build();

    public static void initialize() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ZENITH_CREATIVE_TAB_KEY,
                ZENITH_CREATIVE_TAB
        );
    }
}
