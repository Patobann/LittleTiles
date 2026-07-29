package team.creative.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.LittleBlockRegistry;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.LittleTile;

public class ItemLittleChisel extends Item {

    private static final String SELECTED_STATE = "selected";

    public ItemLittleChisel(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> tooltip, ITooltipFlag flag) {
        String selected = stack.hasTag() ? stack.getTag().getString(SELECTED_STATE) : "";
        tooltip.add(new StringTextComponent(selected.isEmpty() ? "Material: convert a block first" : "Material: " + selected));
    }
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState originalState = level.getBlockState(pos);

        if (context.getPlayer() == null || !context.getPlayer().mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))
            return ActionResultType.FAIL;
        if (level.getBlockEntity(pos) instanceof TETiles)
            return placeCell(context, (TETiles) level.getBlockEntity(pos));
        if (originalState.isAir() || level.getBlockEntity(pos) != null || originalState.getDestroySpeed(level, pos) < 0)
            return ActionResultType.PASS;
        if (level.isClientSide)
            return ActionResultType.SUCCESS;

        context.getItemInHand().getOrCreateTag().putString(SELECTED_STATE, LittleBlockRegistry.saveState(originalState));
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

    private static ActionResultType placeCell(ItemUseContext context, TETiles clickedTiles) {
        ItemStack chisel = context.getItemInHand();
        if (!chisel.hasTag() || !chisel.getTag().contains(SELECTED_STATE, 8))
            return ActionResultType.FAIL;
        BlockState selected = LittleBlockRegistry.loadState(chisel.getTag().getString(SELECTED_STATE));
        if (selected.isAir())
            return ActionResultType.FAIL;
        if (context.getLevel().isClientSide)
            return ActionResultType.SUCCESS;

        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        Vector3d hit = context.getClickLocation();
        double localX = hit.x - clickedPos.getX() + face.getStepX() * 1.0E-7D;
        double localY = hit.y - clickedPos.getY() + face.getStepY() * 1.0E-7D;
        double localZ = hit.z - clickedPos.getZ() + face.getStepZ() * 1.0E-7D;
        BlockPos targetPos = clickedPos;
        if (localX < 0 || localX >= 1 || localY < 0 || localY >= 1 || localZ < 0 || localZ >= 1)
            targetPos = clickedPos.relative(face);
        if (!context.getPlayer().mayUseItemAt(targetPos, face, chisel))
            return ActionResultType.FAIL;

        World level = context.getLevel();
        TileEntity existing = level.getBlockEntity(targetPos);
        LittleGrid grid = existing instanceof TETiles ? ((TETiles) existing).getGrid() : clickedTiles.getGrid();
        int cellX = MathHelper.clamp((int) Math.floor((hit.x - targetPos.getX() + face.getStepX() * 1.0E-7D) * grid.count), 0, grid.count - 1);
        int cellY = MathHelper.clamp((int) Math.floor((hit.y - targetPos.getY() + face.getStepY() * 1.0E-7D) * grid.count), 0, grid.count - 1);
        int cellZ = MathHelper.clamp((int) Math.floor((hit.z - targetPos.getZ() + face.getStepZ() * 1.0E-7D) * grid.count), 0, grid.count - 1);
        LittleBox cell = new LittleBox(cellX, cellY, cellZ, cellX + 1, cellY + 1, cellZ + 1);

        if (existing instanceof TETiles)
            for (LittleTile tile : ((TETiles) existing).getTiles())
                if (tile.intersectsWith(cell))
                    return ActionResultType.FAIL;
        if (!(existing instanceof TETiles) && !level.getBlockState(targetPos).isAir())
            return ActionResultType.FAIL;

        ItemStack bag = ItemLittleBag.find(context.getPlayer());
        double volume = 1.0D / grid.count3d;
        if (!context.getPlayer().isCreative() && !ItemLittleBag.canTake(bag, selected, volume))
            return ActionResultType.FAIL;

        boolean created = false;
        if (!(existing instanceof TETiles)) {
            if (!level.setBlock(targetPos, LittleTiles.TILES_BLOCK.get().defaultBlockState(), 3))
                return ActionResultType.FAIL;
            existing = level.getBlockEntity(targetPos);
            created = true;
        }
        if (!(existing instanceof TETiles) || (!context.getPlayer().isCreative() && !ItemLittleBag.take(bag, selected, volume))) {
            if (created)
                level.setBlock(targetPos, net.minecraft.block.Blocks.AIR.defaultBlockState(), 3);
            return ActionResultType.FAIL;
        }
        ((TETiles) existing).getTiles().add(new LittleTile(selected, ColorUtils.WHITE, cell));
        ((TETiles) existing).getTiles().combineTiles();
        return ActionResultType.CONSUME;
    }
}
