package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxReturnedVolume;
import team.creative.littletiles.common.tile.LittleTile;

public class ItemLittleHammer extends Item {

    public ItemLittleHammer(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof TETiles))
            return ActionResultType.PASS;
        if (context.getPlayer() == null)
            return ActionResultType.FAIL;
        if (level.isClientSide)
            return ActionResultType.SUCCESS;

        TETiles blockEntity = (TETiles) level.getBlockEntity(pos);
        LittleGrid grid = blockEntity.getGrid();
        LittleBox cut = clickedCell(context, grid);
        List<LittleTile> snapshot = new ArrayList<>();
        blockEntity.getTiles().forEach(snapshot::add);
        for (LittleTile tile : snapshot) {
            LittleBoxReturnedVolume removed = new LittleBoxReturnedVolume();
            List<LittleBox> remainder = tile.cutOut(cut, removed);
            if (!removed.has())
                continue;
            if (!context.getPlayer().isCreative()) {
                ItemStack bag = findBag(context.getPlayer());
                if (bag.isEmpty() || !ItemLittleBag.add(bag, tile.getState(), removed.getPercentVolume(grid)))
                    return ActionResultType.FAIL;
            }
            blockEntity.getTiles().remove(tile);
            if (!remainder.isEmpty())
                blockEntity.getTiles().add(new LittleTile(tile, remainder));
            if (blockEntity.getTiles().isEmpty())
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return ActionResultType.CONSUME;
        }
        return ActionResultType.PASS;
    }

    private static ItemStack findBag(PlayerEntity player) {
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (stack.getItem() instanceof ItemLittleBag)
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private static LittleBox clickedCell(ItemUseContext context, LittleGrid grid) {
        BlockPos pos = context.getClickedPos();
        Vector3d hit = context.getClickLocation();
        Direction face = context.getClickedFace();
        double x = hit.x - pos.getX() - face.getStepX() * 1.0E-7D;
        double y = hit.y - pos.getY() - face.getStepY() * 1.0E-7D;
        double z = hit.z - pos.getZ() - face.getStepZ() * 1.0E-7D;
        int cellX = MathHelper.clamp((int) Math.floor(x * grid.count), 0, grid.count - 1);
        int cellY = MathHelper.clamp((int) Math.floor(y * grid.count), 0, grid.count - 1);
        int cellZ = MathHelper.clamp((int) Math.floor(z * grid.count), 0, grid.count - 1);
        return new LittleBox(cellX, cellY, cellZ, cellX + 1, cellY + 1, cellZ + 1);
    }
}
