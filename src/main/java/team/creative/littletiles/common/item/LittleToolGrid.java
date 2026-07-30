package team.creative.littletiles.common.item;

import net.minecraft.item.ItemStack;
import team.creative.littletiles.common.grid.LittleGrid;

public final class LittleToolGrid {

    private static final String GRID = "grid";

    private LittleToolGrid() {}

    public static LittleGrid get(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(GRID, 3)) {
            int count = stack.getTag().getInt(GRID);
            for (LittleGrid grid : LittleGrid.grids)
                if (grid.count == count)
                    return grid;
        }
        return LittleGrid.overallDefault();
    }

    public static LittleGrid cycle(ItemStack stack) {
        LittleToolSelection.clear(stack);
        LittleGrid next = next(get(stack));
        stack.getOrCreateTag().putInt(GRID, next.count);
        return next;
    }

    static LittleGrid next(LittleGrid current) {
        for (int i = 0; i < LittleGrid.grids.length; i++)
            if (LittleGrid.grids[i] == current)
                return LittleGrid.grids[(i + 1) % LittleGrid.grids.length];
        return LittleGrid.overallDefault();
    }
}
