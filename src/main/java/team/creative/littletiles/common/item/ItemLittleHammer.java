package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.LittlePlacementMath;
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
        LittleBox cut = LittlePlacementMath.cell(pos, context.getClickLocation(), context.getClickedFace(), grid, true);
        List<LittleTile> snapshot = new ArrayList<>();
        blockEntity.getTiles().forEach(snapshot::add);
        for (LittleTile tile : snapshot) {
            LittleBoxReturnedVolume removed = new LittleBoxReturnedVolume();
            List<LittleBox> remainder = tile.cutOut(cut, removed);
            if (!removed.has())
                continue;
            if (!context.getPlayer().isCreative()) {
                ItemStack bag = ItemLittleBag.find(context.getPlayer());
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
}
