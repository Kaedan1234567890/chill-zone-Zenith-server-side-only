package com.chillzone.zenith.block;

import com.chillzone.zenith.crafting.ZenithCraftingMenu;
import com.chillzone.zenith.progression.ZenithCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class ZenithCraftingTableBlock extends Block {
    private final ZenithCategory category;

    public ZenithCraftingTableBlock(Properties properties, ZenithCategory category) {
        super(properties);
        this.category = category;
    }

    public ZenithCategory category() {
        return category;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide()) {
            MenuProvider provider = state.getMenuProvider(level, pos);
            if (provider != null) player.openMenu(provider);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        return new SimpleMenuProvider(
                (containerId, inventory, player) ->
                        new ZenithCraftingMenu(
                                containerId,
                                inventory,
                                ContainerLevelAccess.create(level, pos),
                                this.category,
                                this
                        ),
                Component.translatable(this.getDescriptionId())
        );
    }
}
