package team.creative.littletiles.common.item;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public final class LittleToolSelection {

    private static final String SELECTION = "selection";

    private LittleToolSelection() {}

    @Nullable
    public static Area click(ItemStack stack, BlockPos pos, LittleBox cell, LittleGrid workingGrid, LittleGrid selectedGrid) {
        int scale = workingGrid.count / selectedGrid.count;
        int x = cell.minX / scale;
        int y = cell.minY / scale;
        int z = cell.minZ / scale;
        CompoundNBT root = stack.getOrCreateTag();
        if (!root.contains(SELECTION, 10)) {
            set(root, pos, selectedGrid, x, y, z);
            return null;
        }

        CompoundNBT selection = root.getCompound(SELECTION);
        if (selection.getInt("grid") != selectedGrid.count) {
            set(root, pos, selectedGrid, x, y, z);
            return null;
        }

        BlockPos origin = getPosition(selection);
        int globalX = (pos.getX() - origin.getX()) * selectedGrid.count + x;
        int globalY = (pos.getY() - origin.getY()) * selectedGrid.count + y;
        int globalZ = (pos.getZ() - origin.getZ()) * selectedGrid.count + z;
        root.remove(SELECTION);
        return new Area(origin, selectedGrid, between(selection.getInt("sx"), selection.getInt("sy"), selection.getInt("sz"),
                globalX, globalY, globalZ));
    }

    public static LittleBox scale(LittleBox box, LittleGrid from, LittleGrid to) {
        int scale = to.count / from.count;
        return new LittleBox(box.minX * scale, box.minY * scale, box.minZ * scale, box.maxX * scale, box.maxY * scale, box.maxZ * scale);
    }

    public static boolean has(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(SELECTION, 10);
    }

    @Nullable
    public static BlockPos getPosition(ItemStack stack) {
        if (!has(stack))
            return null;
        return getPosition(stack.getTag().getCompound(SELECTION));
    }

    @Nullable
    public static AxisAlignedBB getPreviewBounds(ItemStack stack, BlockPos pos, LittleBox point, LittleGrid workingGrid, LittleGrid selectedGrid) {
        if (!has(stack))
            return null;
        CompoundNBT selection = stack.getTag().getCompound(SELECTION);
        if (selection.getInt("grid") != selectedGrid.count)
            return null;
        BlockPos origin = getPosition(selection);
        int scale = workingGrid.count / selectedGrid.count;
        int x = (pos.getX() - origin.getX()) * selectedGrid.count + point.minX / scale;
        int y = (pos.getY() - origin.getY()) * selectedGrid.count + point.minY / scale;
        int z = (pos.getZ() - origin.getZ()) * selectedGrid.count + point.minZ / scale;
        return new Area(origin, selectedGrid, between(selection.getInt("sx"), selection.getInt("sy"), selection.getInt("sz"), x, y, z))
                .getBounds();
    }

    @Nullable
    public static AxisAlignedBB getStartBounds(ItemStack stack) {
        if (!has(stack))
            return null;
        CompoundNBT selection = stack.getTag().getCompound(SELECTION);
        LittleGrid grid = LittleGrid.get(selection.getInt("grid"));
        LittleBox box = new LittleBox(selection.getInt("sx"), selection.getInt("sy"), selection.getInt("sz"),
                selection.getInt("sx") + 1, selection.getInt("sy") + 1, selection.getInt("sz") + 1);
        return new Area(getPosition(selection), grid, box).getBounds();
    }

    public static void clear(ItemStack stack) {
        if (stack.hasTag())
            stack.getTag().remove(SELECTION);
    }

    static LittleBox between(int x1, int y1, int z1, int x2, int y2, int z2) {
        return new LittleBox(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2) + 1, Math.max(y1, y2) + 1, Math.max(z1, z2) + 1);
    }

    private static BlockPos getPosition(CompoundNBT selection) {
        return new BlockPos(selection.getInt("x"), selection.getInt("y"), selection.getInt("z"));
    }

    private static void set(CompoundNBT root, BlockPos pos, LittleGrid grid, int x, int y, int z) {
        CompoundNBT selection = new CompoundNBT();
        selection.putInt("grid", grid.count);
        selection.putInt("x", pos.getX());
        selection.putInt("y", pos.getY());
        selection.putInt("z", pos.getZ());
        selection.putInt("sx", x);
        selection.putInt("sy", y);
        selection.putInt("sz", z);
        root.put(SELECTION, selection);
    }

    public static final class Area {
        public final BlockPos origin;
        public final LittleGrid grid;
        public final LittleBox box;

        Area(BlockPos origin, LittleGrid grid, LittleBox box) {
            this.origin = origin;
            this.grid = grid;
            this.box = box;
        }

        public Map<BlockPos, LittleBox> split() {
            Map<BlockPos, LittleBox> result = new LinkedHashMap<>();
            int count = grid.count;
            int minBlockX = Math.floorDiv(box.minX, count);
            int minBlockY = Math.floorDiv(box.minY, count);
            int minBlockZ = Math.floorDiv(box.minZ, count);
            int maxBlockX = Math.floorDiv(box.maxX - 1, count);
            int maxBlockY = Math.floorDiv(box.maxY - 1, count);
            int maxBlockZ = Math.floorDiv(box.maxZ - 1, count);
            for (int blockX = minBlockX; blockX <= maxBlockX; blockX++)
                for (int blockY = minBlockY; blockY <= maxBlockY; blockY++)
                    for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ++) {
                        int offsetX = blockX * count;
                        int offsetY = blockY * count;
                        int offsetZ = blockZ * count;
                        LittleBox local = new LittleBox(Math.max(0, box.minX - offsetX), Math.max(0, box.minY - offsetY),
                                Math.max(0, box.minZ - offsetZ), Math.min(count, box.maxX - offsetX),
                                Math.min(count, box.maxY - offsetY), Math.min(count, box.maxZ - offsetZ));
                        result.put(origin.offset(blockX, blockY, blockZ), local);
                    }
            return result;
        }

        public AxisAlignedBB getBounds() {
            double count = grid.count;
            return new AxisAlignedBB(origin.getX() + box.minX / count, origin.getY() + box.minY / count,
                    origin.getZ() + box.minZ / count, origin.getX() + box.maxX / count,
                    origin.getY() + box.maxY / count, origin.getZ() + box.maxZ / count);
        }
    }
}
