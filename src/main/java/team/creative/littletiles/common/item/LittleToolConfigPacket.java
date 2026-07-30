package team.creative.littletiles.common.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittleToolConfigPacket extends CreativePacket {

    public int grid;
    public int placement;

    public LittleToolConfigPacket() {}

    public LittleToolConfigPacket(int grid, LittlePlacementMode placement) {
        this.grid = grid;
        this.placement = placement.ordinal();
    }

    @Override
    public void executeClient(PlayerEntity player) {}

    @Override
    public void executeServer(PlayerEntity player) {
        ItemStack held = player.getMainHandItem();
        apply(held, grid, placement);
    }

    public static void apply(ItemStack stack, int grid, int placement) {
        if (!(stack.getItem() instanceof ItemLittleChisel) && !(stack.getItem() instanceof ItemLittleHammer))
            return;
        try {
            LittleToolGrid.set(stack, LittleGrid.get(grid));
        } catch (RuntimeException ignored) {
            return;
        }
        LittleToolSelection.clear(stack);
        if (stack.getItem() instanceof ItemLittleChisel)
            LittlePlacementMode.set(stack, LittlePlacementMode.byIndex(placement));
    }
}
