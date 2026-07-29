package team.creative.littletiles.common.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.LittleTile;

public class ItemLittleChisel extends Item {

    public ItemLittleChisel(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState originalState = level.getBlockState(pos);

        if (context.getPlayer() != null && !context.getPlayer().mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))
            return ActionResultType.FAIL;
        if (originalState.isAir() || originalState.getBlock() == LittleTiles.TILES_BLOCK.get() || level.getBlockEntity(pos) != null
                || originalState.getDestroySpeed(level, pos) < 0)
            return ActionResultType.PASS;
        if (level.isClientSide)
            return ActionResultType.SUCCESS;

        BlockState tilesState = LittleTiles.TILES_BLOCK.get().defaultBlockState();
        if (!level.setBlock(pos, tilesState, 3))
            return ActionResultType.FAIL;

        TileEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TETiles)) {
            level.setBlock(pos, originalState, 3);
            return ActionResultType.FAIL;
        }

        LittleGrid grid = ((TETiles) blockEntity).getGrid();
        ((TETiles) blockEntity).getTiles().add(new LittleTile(originalState, ColorUtils.WHITE,
                new LittleBox(0, 0, 0, grid.count, grid.count, grid.count)));
        return ActionResultType.CONSUME;
    }
}
