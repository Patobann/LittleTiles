package team.creative.littletiles.common.item;

import java.util.Locale;

import net.minecraft.item.ItemStack;

public enum LittlePlacementMode {

    NORMAL,
    FILL;

    private static final String KEY = "placement";

    public static LittlePlacementMode get(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(KEY, 8))
            return NORMAL;
        try {
            return valueOf(stack.getTag().getString(KEY).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NORMAL;
        }
    }

    public static void set(ItemStack stack, LittlePlacementMode mode) {
        if (mode == NORMAL) {
            if (stack.hasTag())
                stack.getTag().remove(KEY);
        } else {
            stack.getOrCreateTag().putString(KEY, mode.name().toLowerCase(Locale.ROOT));
        }
    }

    public static LittlePlacementMode byIndex(int index) {
        LittlePlacementMode[] modes = values();
        return modes[Math.max(0, Math.min(index, modes.length - 1))];
    }
}
