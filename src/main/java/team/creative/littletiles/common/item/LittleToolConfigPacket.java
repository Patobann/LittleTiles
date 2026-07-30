package team.creative.littletiles.common.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittleToolConfigPacket extends CreativePacket {

    public int grid;
    public int placement;
    public BlockState material;
    public int color;
    public int shape;

    public LittleToolConfigPacket() {}

    public LittleToolConfigPacket(int grid, LittlePlacementMode placement) {
        this.grid = grid;
        this.placement = placement.ordinal();
        this.material = Blocks.AIR.defaultBlockState();
    }

    public LittleToolConfigPacket(int grid, LittlePlacementMode placement, BlockState material, int color, LittleToolShape shape) {
        this.grid = grid;
        this.placement = placement.ordinal();
        this.material = material;
        this.color = color;
        this.shape = shape.ordinal();
    }

    @Override
    public void executeClient(PlayerEntity player) {}

    @Override
    public void executeServer(PlayerEntity player) {
        ItemStack held = player.getMainHandItem();
        apply(held, grid, placement, material, color, shape);
    }

    public static void apply(ItemStack stack, int grid, int placement) {
        apply(stack, grid, placement, Blocks.AIR.defaultBlockState(), 0, 0);
    }

    public static void apply(ItemStack stack, int grid, int placement, BlockState material, int color, int shape) {
        if (!(stack.getItem() instanceof ItemLittleChisel) && !(stack.getItem() instanceof ItemLittleHammer))
            return;
        try {
            LittleToolGrid.set(stack, LittleGrid.get(grid));
        } catch (RuntimeException ignored) {
            return;
        }
        LittleToolSelection.clear(stack);
        if (stack.getItem() instanceof ItemLittleChisel) {
            LittlePlacementMode.set(stack, LittlePlacementMode.byIndex(placement));
            if (material != null && !material.isAir()) {
                ItemLittleChisel.setSelectedState(stack, material);
                ItemLittleChisel.setSelectedColor(stack, color);
            }
            LittleToolShape.set(stack, LittleToolShape.byIndex(shape));
        }
    }
}
