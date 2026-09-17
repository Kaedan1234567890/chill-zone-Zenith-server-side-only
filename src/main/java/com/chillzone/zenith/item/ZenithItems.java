package com.chillzone.zenith.item;

import com.chillzone.zenith.ZenithMod;
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

    private static Item polymerBaseForSword(String id) {
        return switch (id) {
            case "ender_blade",
                 "shulker_blade",
                 "sword_of_undying",
                 "mansion_blade",
                 "prismarine_blade",
                 "sponge_blade",
                 "echo_blade",
                 "blade_of_fire",
                 "ghost_blade",
                 "golden_desire" -> Items.DIAMOND_SWORD;
            default -> Items.NETHERITE_SWORD;
        };
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
                settings -> new AbilitySwordItem(settings, ability, category, uniqueBoss, polymerBaseForSword(id)),
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
            case LAST_STAND -> "Gain exactly 8 absorption hearts for 8 seconds.";
            case VEX_CALL -> "Gain Speed III + Strength III for 8 seconds.";
            case RAVAGER_CHARGE -> "Ram forward, then gain Speed III + Strength III for 5 seconds.";
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


    public static void initialize() {
        // Static fields register the real server-side custom swords.
        // No custom vanilla creative-tab registry entry is created.
        // Use /zenith give for admin testing.
    }
}
