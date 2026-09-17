package com.chillzone.zenith.command;

import com.chillzone.zenith.ZenithMod;
import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import com.chillzone.zenith.item.ZenithTestMode;
import com.chillzone.zenith.item.ZenithItems;
import com.chillzone.zenith.crafting.ZenithRecipeBook;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class ZenithCommands {
    private ZenithCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                Commands.literal("zenith")

                    /*
                     * PUBLIC PLAYER RECIPE GUIDE
                     * Exact syntax:
                     * /zenith crafting recipe <category> <weapon>
                     *
                     * LuckPerms node:
                     * chillzonezenith.command.crafting.recipe
                     *
                     * Fallback is TRUE so normal members can use the guide
                     * even when no explicit LuckPerms rule exists.
                     */
                    .then(Commands.literal("crafting")
                        .requires(source -> hasPermission(
                                source,
                                "chillzonezenith.command.crafting.recipe",
                                true
                        ))
                        .then(Commands.literal("recipe")
                            .requires(source -> hasPermission(
                                    source,
                                    "chillzonezenith.command.crafting.recipe",
                                    true
                            ))
                            .then(recipeCategoryArgument())))

                    /*
                     * ADMIN / MANAGEMENT COMMANDS
                     * Each command/subcommand has its own LuckPerms node.
                     * Fallback requires vanilla gamemaster/operator permission.
                     */
                    .then(permissionLiteral(
                            "status",
                            "chillzonezenith.command.status"
                    ).executes(ctx -> showStatus(ctx.getSource())))

                    .then(permissionLiteral(
                            "test",
                            "chillzonezenith.command.test.self"
                    )
                        .executes(ctx -> setTestModeSelf(ctx.getSource(), true))
                        .then(Commands.literal("all")
                            .requires(source -> hasPermission(
                                    source,
                                    "chillzonezenith.command.test.all",
                                    false
                            ))
                            .executes(ctx -> setTestModeAll(ctx.getSource(), true)))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .requires(source -> hasPermission(
                                    source,
                                    "chillzonezenith.command.test.targets",
                                    false
                            ))
                            .executes(ctx -> setTestModeTargets(
                                    ctx.getSource(),
                                    EntityArgument.getPlayers(ctx, "targets"),
                                    true
                            ))))

                    .then(permissionLiteral(
                            "untest",
                            "chillzonezenith.command.untest.self"
                    )
                        .executes(ctx -> setTestModeSelf(ctx.getSource(), false))
                        .then(Commands.literal("all")
                            .requires(source -> hasPermission(
                                    source,
                                    "chillzonezenith.command.untest.all",
                                    false
                            ))
                            .executes(ctx -> setTestModeAll(ctx.getSource(), false)))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .requires(source -> hasPermission(
                                    source,
                                    "chillzonezenith.command.untest.targets",
                                    false
                            ))
                            .executes(ctx -> setTestModeTargets(
                                    ctx.getSource(),
                                    EntityArgument.getPlayers(ctx, "targets"),
                                    false
                            ))))

                    .then(permissionLiteral(
                            "activate",
                            "chillzonezenith.command.activate"
                    ).then(categoryArgument(true)))

                    .then(permissionLiteral(
                            "deactivate",
                            "chillzonezenith.command.deactivate"
                    ).then(categoryArgument(false)))

                    .then(permissionLiteral(
                            "activateall",
                            "chillzonezenith.command.activateall"
                    ).executes(ctx -> setAll(ctx.getSource(), true)))

                    .then(permissionLiteral(
                            "deactivateall",
                            "chillzonezenith.command.deactivateall"
                    ).executes(ctx -> setAll(ctx.getSource(), false)))

                    .then(permissionLiteral(
                            "give",
                            "chillzonezenith.command.give"
                    )
                        .then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("item", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String id : zenithItemIds()) {
                                        builder.suggest(id);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> giveTestingItem(
                                    ctx.getSource(),
                                    EntityArgument.getPlayers(ctx, "targets"),
                                    StringArgumentType.getString(ctx, "item"),
                                    1
                                ))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                    .executes(ctx -> giveTestingItem(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "targets"),
                                        StringArgumentType.getString(ctx, "item"),
                                        IntegerArgumentType.getInteger(ctx, "count")
                                    ))))))

                    .then(permissionLiteral(
                            "detect",
                            "chillzonezenith.command.detect"
                    )
                        .then(Commands.argument("category", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (ZenithCategory category : ZenithCategory.values()) {
                                    builder.suggest(category.id());
                                }
                                return builder.buildFuture();
                            })
                            .executes(ctx -> detectBossBlade(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "category")
                            ))))

                    .then(permissionLiteral(
                            "resetboss",
                            "chillzonezenith.command.resetboss"
                    )
                        .then(Commands.argument("category", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                for (ZenithCategory category : ZenithCategory.values()) {
                                    builder.suggest(category.id());
                                }
                                return builder.buildFuture();
                            })
                            .executes(ctx -> resetBoss(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "category")
                            ))))
            );
        });
    }

    private static boolean hasPermission(
            CommandSourceStack source,
            String node,
            boolean publicFallback
    ) {
        if (publicFallback) {
            return me.lucko.fabric.api.permissions.v0.Permissions.check(source, node, true);
        }

        int fallbackLevel = source.permissions()
                .hasPermission(Permissions.COMMANDS_GAMEMASTER)
                ? 0
                : 4;

        /*
         * Permissions.check(..., level) asks LuckPerms first.
         * If the node is not set, the level is used as the fallback.
         *
         * For actual operators/gamemasters we use a permissive fallback.
         * For everyone else, level 4 keeps admin commands unavailable
         * unless LuckPerms explicitly grants the individual node.
         */
        return me.lucko.fabric.api.permissions.v0.Permissions.check(source, node, fallbackLevel);
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack>
    permissionLiteral(String name, String node) {
        return Commands.literal(name)
                .requires(source -> hasPermission(source, node, false));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String>
    recipeCategoryArgument() {
        return Commands.argument("category", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    ZenithProgressionState state =
                            ZenithProgressionState.get(ctx.getSource().getServer());

                    java.util.List<String> activeCategories = new java.util.ArrayList<>();

                    for (ZenithCategory category : ZenithCategory.values()) {
                        if (state.isEnabled(category)) {
                            activeCategories.add(category.id());
                        }
                    }

                    return SharedSuggestionProvider.suggest(
                            activeCategories,
                            builder
                    );
                })
                .then(Commands.argument("weapon", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            ZenithCategory category = ZenithCategory.fromId(
                                    StringArgumentType.getString(ctx, "category")
                            ).orElse(null);

                            if (category == null) {
                                return builder.buildFuture();
                            }

                            ZenithProgressionState state =
                                    ZenithProgressionState.get(ctx.getSource().getServer());

                            if (!state.isEnabled(category)) {
                                return builder.buildFuture();
                            }

                            java.util.List<String> weapons = new java.util.ArrayList<>();

                            for (ZenithRecipeBook.RecipeDef recipe : ZenithRecipeBook.RECIPES) {
                                if (recipe.category() == category) {
                                    weapons.add(pathOf(recipe.output()));
                                }
                            }

                            return SharedSuggestionProvider.suggest(
                                    weapons,
                                    builder
                            );
                        })
                        .executes(ctx -> showRecipe(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "category"),
                                StringArgumentType.getString(ctx, "weapon")
                        )));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String>
    categoryArgument(boolean enabled) {
        return Commands.argument("category", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    for (ZenithCategory category : ZenithCategory.values()) {
                        builder.suggest(category.id());
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> setCategory(
                        ctx.getSource(),
                        StringArgumentType.getString(ctx, "category"),
                        enabled
                ));
    }


    private static int showRecipe(
            CommandSourceStack source,
            String rawCategory,
            String rawWeapon
    ) {
        ZenithCategory category = ZenithCategory.fromId(rawCategory).orElse(null);

        if (category == null) {
            source.sendFailure(
                    Component.literal("[Zenith] Unknown recipe category: " + rawCategory)
            );
            return 0;
        }

        ZenithProgressionState state =
                ZenithProgressionState.get(source.getServer());

        if (!state.isEnabled(category)) {
            source.sendFailure(
                    Component.literal(
                            "[Zenith] The " + category.id()
                                    + " branch is not currently active."
                    )
            );
            return 0;
        }

        String weaponPath = normalizeWeapon(rawWeapon);
        ZenithRecipeBook.RecipeDef found = null;

        for (ZenithRecipeBook.RecipeDef recipe : ZenithRecipeBook.RECIPES) {
            if (recipe.category() == category
                    && pathOf(recipe.output()).equals(weaponPath)) {
                found = recipe;
                break;
            }
        }

        if (found == null) {
            source.sendFailure(
                    Component.literal(
                            "[Zenith] Unknown " + category.id()
                                    + " weapon recipe: " + rawWeapon
                    )
            );
            return 0;
        }

        final ZenithRecipeBook.RecipeDef recipe = found;
        ChatFormatting branchColor = categoryColor(category);
        String outputName = prettyItemName(recipe.output());
        String stationName = category == ZenithCategory.ZENITH
                ? "Zenith Crafting Table"
                : titleCase(category.id()) + " Crafting Table";

        source.sendSuccess(
                () -> Component.literal("================================")
                        .withStyle(ChatFormatting.DARK_GRAY),
                false
        );

        source.sendSuccess(
                () -> Component.literal("  " + outputName)
                        .withStyle(style ->
                                style.withColor(branchColor).withBold(true)
                        ),
                false
        );

        source.sendSuccess(
                () -> Component.literal("Station: " + stationName)
                        .withStyle(ChatFormatting.GRAY),
                false
        );

        if (recipe.uniqueBoss()) {
            source.sendSuccess(
                    () -> Component.literal(
                                    category == ZenithCategory.ZENITH
                                            ? "★★★ ULTIMATE BLADE RECIPE ★★★"
                                            : "★ BOSS BLADE RECIPE ★"
                            )
                            .withStyle(style ->
                                    style.withColor(branchColor).withBold(true)
                            ),
                    false
            );
        }

        source.sendSuccess(
                () -> Component.literal("+--------- 3 x 3 CRAFTING GRID ---------+")
                        .withStyle(ChatFormatting.DARK_GRAY),
                false
        );

        String[] grid = recipe.grid();

        for (int row = 0; row < 3; row++) {
            int first = row * 3;
            String rowText =
                    "[ " + prettyItemName(grid[first]) + " ]"
                            + "  [ " + prettyItemName(grid[first + 1]) + " ]"
                            + "  [ " + prettyItemName(grid[first + 2]) + " ]";

            source.sendSuccess(
                    () -> Component.literal(rowText)
                            .withStyle(ChatFormatting.WHITE),
                    false
            );
        }

        source.sendSuccess(
                () -> Component.literal("+---------------------------------------+")
                        .withStyle(ChatFormatting.DARK_GRAY),
                false
        );

        Map<String, Integer> totals = new LinkedHashMap<>();

        for (String ingredient : grid) {
            String name = prettyItemName(ingredient);
            totals.put(name, totals.getOrDefault(name, 0) + 1);
        }

        StringBuilder needed = new StringBuilder("Needed: ");
        boolean firstIngredient = true;

        for (Map.Entry<String, Integer> entry : totals.entrySet()) {
            if (!firstIngredient) {
                needed.append(" | ");
            }

            needed.append(entry.getValue())
                    .append("x ")
                    .append(entry.getKey());

            firstIngredient = false;
        }

        source.sendSuccess(
                () -> Component.literal(needed.toString())
                        .withStyle(ChatFormatting.GRAY),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                                "Use this exact layout in the "
                                        + stationName + "."
                        )
                        .withStyle(branchColor),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                                "Tip: /zenith crafting recipe "
                                        + category.id()
                                        + " <weapon>"
                        )
                        .withStyle(ChatFormatting.DARK_GRAY),
                false
        );

        return 1;
    }

    private static String normalizeWeapon(String raw) {
        if (raw == null) return "";

        String normalized = raw.toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        if (normalized.startsWith(ZenithMod.MOD_ID + ":")) {
            normalized = normalized.substring(ZenithMod.MOD_ID.length() + 1);
        }

        return normalized;
    }

    private static String pathOf(String id) {
        int colon = id.indexOf(':');
        return colon >= 0 ? id.substring(colon + 1) : id;
    }

    private static String prettyItemName(String fullId) {
        String path = pathOf(fullId);

        return switch (path) {
            case "ender_eye" -> "Eye of Ender";
            case "heart_of_the_sea" -> "Heart of the Sea";
            case "totem_of_undying" -> "Totem of Undying";
            case "wardens_wrath" -> "Warden's Wrath";
            case "brutes_emblem" -> "Brute's Emblem";
            case "elder_guardian_core" -> "Elder Guardian Core";
            default -> titleCase(path);
        };
    }

    private static String titleCase(String value) {
        String[] parts = value.replace('_', ' ').split(" ");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) continue;

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(Character.toUpperCase(part.charAt(0)));

            if (part.length() > 1) {
                result.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }

        return result.toString();
    }

    private static ChatFormatting categoryColor(ZenithCategory category) {
        return switch (category) {
            case ENDER -> ChatFormatting.DARK_PURPLE;
            case RAVAGER -> ChatFormatting.GOLD;
            case GUARDIAN -> ChatFormatting.AQUA;
            case WARDEN -> ChatFormatting.DARK_AQUA;
            case WITHER -> ChatFormatting.DARK_RED;
            case ZENITH -> ChatFormatting.LIGHT_PURPLE;
        };
    }

    private static List<String> zenithItemIds() {
        List<String> ids = new ArrayList<>();

        BuiltInRegistries.ITEM.keySet().forEach(id -> {
            if (ZenithMod.MOD_ID.equals(id.getNamespace())) {
                ids.add(id.getPath());
            }
        });

        // Vanilla-backed Zenith items are not present in our registry namespace,
        // so add their logical IDs explicitly.
        if (!ids.contains("ender_essence")) {
            ids.add("ender_essence");
        }

        ids.sort(String::compareTo);
        return ids;
    }

    private static int giveTestingItem(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            String shortId,
            int amount
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, shortId);

        // Phase 1: Ender Essence is a vanilla-backed ItemStack with hidden Zenith identity.
        boolean enderEssence = "ender_essence".equals(shortId);

        if (!enderEssence && !BuiltInRegistries.ITEM.containsKey(id)) {
            source.sendFailure(Component.literal("[Zenith] Unknown custom item: " + shortId));
            return 0;
        }

        Item item = enderEssence ? null : BuiltInRegistries.ITEM.getValue(id);
        int count = 0;

        for (ServerPlayer player : targets) {
            ItemStack stack = enderEssence
                    ? ZenithItems.createEnderEssence(amount)
                    : new ItemStack(item, amount);

            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }

            count++;
        }

        int finalCount = count;
        source.sendSuccess(
                () -> Component.literal(
                        "[Zenith] Gave " + amount + "x " + shortId + " to " + finalCount + " player(s)."
                ),
                true
        );

        return count;
    }


    private static int detectBossBlade(CommandSourceStack source, String rawCategory) {
        ZenithCategory category = ZenithCategory.fromId(rawCategory).orElse(null);

        if (category == null) {
            source.sendFailure(Component.literal("[Zenith] Unknown category: " + rawCategory));
            return 0;
        }

        String bossPath = switch (category) {
            case ENDER -> "ender_dragon_blade";
            case RAVAGER -> "ravager_blade";
            case GUARDIAN -> "elder_guardian_blade";
            case WARDEN -> "warden_blade";
            case WITHER -> "wither_blade";
            case ZENITH -> "zenith_blade";
        };

        Identifier bossId = Identifier.fromNamespaceAndPath(ZenithMod.MOD_ID, bossPath);
        int visibleCopies = 0;

        source.sendSuccess(
                () -> Component.literal("----- Zenith Detect: " + category.id() + " -----"),
                false
        );

        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            int inv = countInContainer(player.getInventory(), bossId);
            int ender = countInContainer(player.getEnderChestInventory(), bossId);

            if (inv > 0) {
                visibleCopies += inv;
                int found = inv;
                source.sendSuccess(
                        () -> Component.literal(
                                "FOUND " + found + "x in "
                                        + player.getGameProfile().name()
                                        + "'s inventory"
                        ),
                        false
                );
            }

            if (ender > 0) {
                visibleCopies += ender;
                int found = ender;
                source.sendSuccess(
                        () -> Component.literal(
                                "FOUND " + found + "x in "
                                        + player.getGameProfile().name()
                                        + "'s Ender Chest"
                        ),
                        false
                );
            }
        }

        for (var level : source.getServer().getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (!(entity instanceof ItemEntity dropped)) {
                    continue;
                }

                if (stackIs(dropped.getItem(), bossId)) {
                    int found = dropped.getItem().getCount();
                    visibleCopies += found;

                    source.sendSuccess(
                            () -> Component.literal(
                                    "FOUND " + found + "x dropped at "
                                            + dropped.blockPosition().toShortString()
                            ),
                            false
                    );
                }
            }
        }

        ZenithProgressionState state =
                ZenithProgressionState.get(source.getServer());

        int finalVisibleCopies = visibleCopies;

        if (visibleCopies == 0) {
            source.sendSuccess(
                    () -> Component.literal(
                            "No visible copy found in ONLINE player inventories, "
                                    + "ONLINE Ender Chests, or loaded dropped items."
                    ),
                    false
            );
        } else {
            source.sendSuccess(
                    () -> Component.literal(
                            "Visible copies found: " + finalVisibleCopies
                    ),
                    false
            );
        }

        source.sendSuccess(
                () -> Component.literal(
                        "Craft lock: "
                                + (state.isBossCrafted(category) ? "LOCKED" : "AVAILABLE")
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        "Note: this does NOT scan offline players, chests, barrels, "
                                + "shulker boxes, or unloaded chunks."
                ),
                false
        );

        return visibleCopies > 0 ? 1 : 0;
    }

    private static int countInContainer(Container container, Identifier itemId) {
        int count = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stackIs(stack, itemId)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private static boolean stackIs(ItemStack stack, Identifier itemId) {
        if (stack == null || stack.isEmpty()) return false;

        Identifier actual = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId.equals(actual);
    }

    private static int setCategory(
            CommandSourceStack source,
            String rawCategory,
            boolean enabled
    ) {
        ZenithCategory category = ZenithCategory.fromId(rawCategory).orElse(null);

        if (category == null) {
            source.sendFailure(Component.literal("Unknown Zenith category: " + rawCategory));
            return 0;
        }

        ZenithProgressionState.get(source.getServer()).setEnabled(category, enabled);

        String action = enabled ? "ACTIVATED" : "DEACTIVATED";
        source.sendSuccess(
                () -> Component.literal("[Zenith] " + category.id() + " " + action),
                true
        );

        return 1;
    }

    private static int setAll(CommandSourceStack source, boolean enabled) {
        ZenithProgressionState.get(source.getServer()).setAll(enabled);

        String action = enabled ? "ACTIVATED" : "DEACTIVATED";
        source.sendSuccess(
                () -> Component.literal("[Zenith] ALL CATEGORIES " + action),
                true
        );

        return 1;
    }

    private static int resetBoss(CommandSourceStack source, String rawCategory) {
        ZenithCategory category = ZenithCategory.fromId(rawCategory).orElse(null);

        if (category == null) {
            source.sendFailure(Component.literal("Unknown Zenith category: " + rawCategory));
            return 0;
        }

        ZenithProgressionState.get(source.getServer()).resetBossCrafted(category);
        source.sendSuccess(
                () -> Component.literal(
                        "[Zenith] Reset unique crafting flag for " + category.id()
                ),
                true
        );

        return 1;
    }



    private static int setTestModeSelf(
            CommandSourceStack source,
            boolean enabled
    ) {
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(
                    Component.literal(
                            "[Zenith] Console must use /zenith test <player>, "
                                    + "/zenith test all, /zenith untest <player>, "
                                    + "or /zenith untest all."
                    )
            );
            return 0;
        }

        return setTestModeTargets(
                source,
                java.util.List.of(player),
                enabled
        );
    }

    private static int setTestModeAll(
            CommandSourceStack source,
            boolean enabled
    ) {
        return setTestModeTargets(
                source,
                source.getServer().getPlayerList().getPlayers(),
                enabled
        );
    }

    private static int setTestModeTargets(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            boolean enabled
    ) {
        int changed = 0;

        for (ServerPlayer player : targets) {
            if (enabled) {
                ZenithTestMode.enable(player.getUUID());
            } else {
                ZenithTestMode.disable(player.getUUID());
            }

            player.sendSystemMessage(
                    Component.literal(
                            "[Zenith] Test mode "
                                    + (enabled
                                    ? "ENABLED - Zenith weapon cooldowns are ignored."
                                    : "DISABLED - normal Zenith cooldowns apply.")
                    )
            );

            changed++;
        }

        int finalChanged = changed;

        source.sendSuccess(
                () -> Component.literal(
                        "[Zenith] Test mode "
                                + (enabled ? "enabled" : "disabled")
                                + " for "
                                + finalChanged
                                + " player(s)."
                ),
                true
        );

        return changed;
    }

    private static int showStatus(CommandSourceStack source) {
        ZenithProgressionState state = ZenithProgressionState.get(source.getServer());

        source.sendSuccess(
                () -> Component.literal("----- Zenith Progression -----"),
                false
        );

        for (ZenithCategory category : ZenithCategory.values()) {
            String onOff = state.isEnabled(category) ? "ON" : "OFF";
            String unique = state.isBossCrafted(category) ? "BOSS CRAFTED" : "boss available";

            source.sendSuccess(
                    () -> Component.literal(
                            category.id() + ": " + onOff + " | " + unique
                    ),
                    false
            );
        }

        return 1;
    }
}
