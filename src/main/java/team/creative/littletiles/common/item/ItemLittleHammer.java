package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.LittleToolSelection.Area;
import team.creative.littletiles.common.math.LittlePlacementMath;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxReturnedVolume;
import team.creative.littletiles.common.tile.LittleTile;

public class ItemLittleHammer extends Item {

    public ItemLittleHammer(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
        if (!selected)
            LittleToolSelection.clear(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> tooltip, ITooltipFlag flag) {
        LittleGrid grid = LittleToolGrid.get(stack);
        tooltip.add(new StringTextComponent("Grid: " + grid.count + "x" + grid.count + "x" + grid.count));
        tooltip.add(new StringTextComponent("Left-click two corners to remove an area"));
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown())
            return ActionResult.pass(stack);
        if (LittleToolSelection.has(stack)) {
            LittleToolSelection.clear(stack);
            if (!level.isClientSide)
                player.displayClientMessage(new StringTextComponent("LittleTiles: selection cancelled"), true);
            return ActionResult.sidedSuccess(stack, level.isClientSide);
        }
        LittleGrid grid = LittleToolGrid.cycle(stack);
        if (!level.isClientSide)
            player.displayClientMessage(new StringTextComponent("LittleTiles grid: " + grid.count), true);
        return ActionResult.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null)
            return ActionResultType.FAIL;
        if (!player.isShiftKeyDown())
            return ActionResultType.PASS;
        LittleToolSelection.clear(context.getItemInHand());
        if (!context.getLevel().isClientSide)
            player.displayClientMessage(new StringTextComponent("LittleTiles: selection cancelled"), true);
        return context.getLevel().isClientSide ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0;
    }

    public static ActionResultType click(World level, PlayerEntity player, ItemStack hammer, BlockPos pos, Direction face, Vector3d hit) {
        if (!player.mayUseItemAt(pos, face, hammer))
            return ActionResultType.FAIL;
        if (player.isShiftKeyDown()) {
            LittleToolSelection.clear(hammer);
            if (!level.isClientSide)
                player.displayClientMessage(new StringTextComponent("LittleTiles: selection cancelled"), true);
            return level.isClientSide ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
        }

        TileEntity target = level.getBlockEntity(pos);
        LittleGrid selectedGrid = LittleToolGrid.get(hammer);
        LittleGrid workingGrid = target instanceof TETiles ? LittleGrid.max(((TETiles) target).getGrid(), selectedGrid) : selectedGrid;
        LittleBox point = LittlePlacementMath.cell(pos, hit, face, workingGrid, selectedGrid, true);
        Area area = LittleToolSelection.click(hammer, pos, point, workingGrid, selectedGrid);
        if (area == null) {
            if (!level.isClientSide)
                player.displayClientMessage(new StringTextComponent("LittleTiles: first corner set"), true);
            return level.isClientSide ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
        }
        if (level.isClientSide)
            return ActionResultType.SUCCESS;

        List<Removal> removals = new ArrayList<>();
        Map<BlockPos, TETiles> affected = new LinkedHashMap<>();
        for (Map.Entry<BlockPos, LittleBox> entry : area.split().entrySet()) {
            BlockPos blockPos = entry.getKey();
            if (!player.mayUseItemAt(blockPos, face, hammer))
                return ActionResultType.FAIL;
            TileEntity blockEntity = level.getBlockEntity(blockPos);
            if (!(blockEntity instanceof TETiles))
                continue;
            TETiles tiles = (TETiles) blockEntity;
            LittleGrid grid = LittleGrid.max(tiles.getGrid(), area.grid);
            LittleBox cut = LittleToolSelection.scale(entry.getValue(), area.grid, grid);
            List<LittleTile> snapshot = new ArrayList<>();
            tiles.getTiles().forEach(snapshot::add);
            for (LittleTile tile : snapshot) {
                List<LittleBox> scaledBoxes = new ArrayList<>();
                for (LittleBox box : tile)
                    scaledBoxes.add(LittleToolSelection.scale(box, tiles.getGrid(), grid));
                LittleTile workingTile = new LittleTile(tile, scaledBoxes);
                LittleBoxReturnedVolume removed = new LittleBoxReturnedVolume();
                List<LittleBox> remainder = workingTile.cutOut(cut, removed);
                if (removed.has())
                    removals.add(new Removal(blockPos, tiles, tile, remainder, removed.getPercentVolume(grid), grid));
            }
            affected.put(blockPos, tiles);
        }
        if (removals.isEmpty())
            return ActionResultType.PASS;

        ItemStack bag = ItemStack.EMPTY;
        ItemStack stagedBag = ItemStack.EMPTY;
        if (!player.isCreative()) {
            bag = ItemLittleBag.find(player);
            if (bag.isEmpty())
                return ActionResultType.FAIL;
            stagedBag = bag.copy();
            for (Removal removal : removals)
                if (!ItemLittleBag.add(stagedBag, removal.tile.getState(), removal.volume))
                    return ActionResultType.FAIL;
        }

        for (Removal removal : removals) {
            if (removal.owner.getGrid() != removal.grid)
                removal.owner.convertTo(removal.grid);
            removal.owner.getTiles().remove(removal.tile);
            if (!removal.remainder.isEmpty())
                removal.owner.getTiles().add(new LittleTile(removal.tile, removal.remainder));
        }
        if (!player.isCreative())
            bag.setTag(stagedBag.getTag() == null ? null : stagedBag.getTag().copy());
        for (Map.Entry<BlockPos, TETiles> entry : affected.entrySet()) {
            if (entry.getValue().getTiles().isEmpty())
                level.setBlock(entry.getKey(), Blocks.AIR.defaultBlockState(), 3);
            else
                entry.getValue().getTiles().combineTiles();
        }
        return ActionResultType.CONSUME;
    }

    private static final class Removal {
        final BlockPos pos;
        final TETiles owner;
        final LittleTile tile;
        final List<LittleBox> remainder;
        final double volume;
        final LittleGrid grid;

        Removal(BlockPos pos, TETiles owner, LittleTile tile, List<LittleBox> remainder, double volume, LittleGrid grid) {
            this.pos = pos;
            this.owner = owner;
            this.tile = tile;
            this.remainder = remainder;
            this.volume = volume;
            this.grid = grid;
        }
    }
}
