package com.chillzone.zenith.crafting;

import com.chillzone.zenith.progression.ZenithCategory;
import com.chillzone.zenith.progression.ZenithProgressionState;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class ZenithCraftingMenu extends CraftingMenu {
    private final ContainerLevelAccess access;
    private final Player player;
    private final ZenithCategory category;
    private final Block tableBlock;

    private ZenithRecipeBook.RecipeDef pendingUniqueRecipe;

    public ZenithCraftingMenu(
            int containerId,
            Inventory inventory,
            ContainerLevelAccess access,
            ZenithCategory category,
            Block tableBlock
    ) {
        super(containerId, inventory, access);
        this.access = access;
        this.player = inventory.player;
        this.category = category;
        this.tableBlock = tableBlock;

        // Recalculate once our custom fields are initialized.
        slotsChanged(this.craftSlots);
    }

    @Override
    public boolean stillValid(Player player) {
        /*
         * Vanilla CraftingMenu validates against minecraft:crafting_table.
         * Our custom tables must validate against their own actual block.
         */
        return stillValid(this.access, player, this.tableBlock);
    }

    @Override
    public void slotsChanged(Container container) {
        /*
         * CraftingMenu's constructor can call this before our subclass fields
         * have been assigned, so ignore that early call.
         */
        if (this.category == null || this.access == null || this.player == null) {
            return;
        }

        this.access.execute((level, blockPos) -> {
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }

            if (container != this.craftSlots) {
                return;
            }

            /*
             * If a unique Boss Blade was visible as the previous result and the
             * player now owns it, the legitimate craft has completed.
             *
             * Important Minecraft 26.2 fix:
             * Player no longer exposes getServer(), so obtain the server from
             * the ServerLevel supplied by ContainerLevelAccess.
             */
            if (this.pendingUniqueRecipe != null
                    && playerHasResult(this.pendingUniqueRecipe.output())) {

                ZenithProgressionState
                        .get(serverLevel.getServer())
                        .markBossCrafted(this.pendingUniqueRecipe.category());

                this.pendingUniqueRecipe = null;
            }

            ZenithRecipeBook.RecipeDef match =
                    ZenithRecipeBook.findMatch(
                            serverLevel,
                            this.category,
                            this.craftSlots
                    );

            ItemStack result = ZenithRecipeBook.makeResult(match);

            if (match != null && match.uniqueBoss()) {
                this.pendingUniqueRecipe = match;
            } else {
                this.pendingUniqueRecipe = null;
            }

            this.resultSlots.setItem(0, result);
            this.setRemoteSlot(0, result);

            if (this.player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(
                        new ClientboundContainerSetSlotPacket(
                                this.containerId,
                                this.incrementStateId(),
                                0,
                                result
                        )
                );
            }
        });
    }

    private boolean playerHasResult(String itemId) {
        if (ZenithRecipeBook.stackIs(this.getCarried(), itemId)) {
            return true;
        }

        Inventory inventory = this.player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (ZenithRecipeBook.stackIs(inventory.getItem(i), itemId)) {
                return true;
            }
        }

        return false;
    }
}
