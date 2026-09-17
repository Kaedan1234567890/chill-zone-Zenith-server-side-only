package com.chillzone.zenith.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Server-safe Zenith identity stored inside Minecraft's built-in CUSTOM_DATA component.
 *
 * IMPORTANT: This class does NOT register a new DataComponentType. Registering a custom
 * component would put us back on the client-registry path we are trying to remove.
 */
public final class ZenithStackIdentity {
    private ZenithStackIdentity() {}

    private static final String KEY = "chillzonezenith_id";

    public static ItemStack mark(ItemStack stack, String zenithId) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        data = data.update(tag -> tag.putString(KEY, zenithId));
        stack.set(DataComponents.CUSTOM_DATA, data);
        return stack;
    }

    public static boolean is(ItemStack stack, String zenithId) {
        if (stack == null || stack.isEmpty()) return false;

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;

        CompoundTag tag = data.copyTag();
        return zenithId.equals(tag.getStringOr(KEY, ""));
    }

    public static String get(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return "";

        return data.copyTag().getStringOr(KEY, "");
    }
}
