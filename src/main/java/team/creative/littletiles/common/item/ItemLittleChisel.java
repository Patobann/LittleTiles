package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
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
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.LittleBlockRegistry;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.LittleToolSelection.Area;
import team.creative.littletiles.common.math.LittlePlacementMath;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.LittleTile;

public class ItemLittleChisel extends Item {

    private static final String SELECTED_STATE = "selected";
    private static final String SELECTED_COLOR = "selectedColor";

    public ItemLittleChisel(Properties properties) {
        super(properties);
    }

    public static boolean hasSelectedMaterial(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(SELECTED_STATE, 8);
    }

    public static BlockState getSelectedState(ItemStack stack) {
        if (!hasSelectedMaterial(stack))
            return net.minecraft.block.Blocks.AIR.defaultBlockState();
        return LittleBlockRegistry.loadState(stack.getTag().getString(SELECTED_STATE));
    }

    public static void setSelectedState(ItemStack stack, BlockState state) {
        stack.getOrCreateTag().putString(SELECTED_STATE, LittleBlockRegistry.saveState(state));
    }

    public static int getSelectedColor(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(SELECTED_COLOR, 3)
                ? stack.getTag().getInt(SELECTED_COLOR) : ColorUtils.WHITE;
    }

    public static void setSelectedColor(ItemStack stack, int color) {
        if (color == ColorUtils.WHITE && stack.hasTag())
            stack.getTag().remove(SELECTED_COLOR);
        else
            stack.getOrCreateTag().putInt(SELECTED_COLOR, color);
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
        tooltip.add(new StringTextComponent("Placement: " + LittlePlacementMode.get(stack).name().toLowerCase()));
        String selected = stack.hasTag() ? stack.getTag().getString(SELECTED_STATE) : "";
        tooltip.add(new StringTextComponent(selected.isEmpty() ? "Material: convert a block first" : "Material: " + selected));
        tooltip.add(new StringTextComponent("Right-click two corners to apply an area"));
        tooltip.add(new StringTextComponent("Sneak-use a little tile to sample its material"));
        tooltip.add(new StringTextComponent("Press C to configure"));
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
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState originalState = level.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack chisel = context.getItemInHand();

        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), chisel))
            return ActionResultType.FAIL;
        if (player.isShiftKeyDown()) {
            if (LittleToolSelection.has(chisel)) {
                LittleToolSelection.clear(chisel);
                if (!level.isClientSide)
                    player.displayClientMessage(new StringTextComponent("LittleTiles: selection cancelled"), true);
                return level.isClientSide ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
            }
            if (level.getBlockEntity(pos) instanceof TETiles)
                return selectMaterial(context, (TETiles) level.getBlockEntity(pos), chisel);
            return ActionResultType.PASS;
        }

        if (LittleToolSelection.has(chisel) || level.getBlockEntity(pos) instanceof TETiles)
            return placeSelection(context);
        if (originalState.isAir() || level.getBlockEntity(pos) != null || originalState.getDestroySpeed(level, pos) < 0)
            return ActionResultType.PASS;
        if (level.isClientSide)
            return ActionResultType.SUCCESS;

        setSelectedState(chisel, originalState);
        BlockState tilesState = LittleTiles.TILES_BLOCK.get().defaultBlockState();
        if (!level.setBlock(pos, tilesState, 3))
            return ActionResultType.FAIL;
        TileEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TETiles)) {
            level.setBlock(pos, originalState, 3);
            return ActionResultType.FAIL;
        }

        LittleGrid grid = LittleToolGrid.get(chisel);
        ((TETiles) blockEntity).convertTo(grid);
        ((TETiles) blockEntity).getTiles().add(new LittleTile(originalState, ColorUtils.WHITE,
                new LittleBox(0, 0, 0, grid.count, grid.count, grid.count)));
        return ActionResultType.CONSUME;
    }

    private static ActionResultType selectMaterial(ItemUseContext context, TETiles clickedTiles, ItemStack chisel) {
        LittleBox cell = LittlePlacementMath.cell(context.getClickedPos(), context.getClickLocation(), context.getClickedFace(),
                clickedTiles.getGrid(), true);
        for (LittleTile tile : clickedTiles.getTiles()) {
            if (!tile.intersectsWith(cell))
                continue;
            if (!context.getLevel().isClientSide) {
                setSelectedState(chisel, tile.getState());
                setSelectedColor(chisel, tile.color);
            }
            return context.getLevel().isClientSide ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
        }
        return ActionResultType.PASS;
    }

    private static ActionResultType placeSelection(ItemUseContext context) {
        ItemStack chisel = context.getItemInHand();
        if (!hasSelectedMaterial(chisel))
            return ActionResultType.FAIL;
        BlockState selected = getSelectedState(chisel);
        if (selected.isAir())
            return ActionResultType.FAIL;
        World level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        Vector3d hit = context.getClickLocation();
        BlockPos targetPos = LittlePlacementMath.outwardBlock(clickedPos, hit, face);
        if (!context.getPlayer().mayUseItemAt(targetPos, face, chisel))
            return ActionResultType.FAIL;

        TileEntity target = level.getBlockEntity(targetPos);
        LittleGrid selectedGrid = LittleToolGrid.get(chisel);
        LittleGrid workingGrid = target instanceof TETiles ? LittleGrid.max(((TETiles) target).getGrid(), selectedGrid) : selectedGrid;
        LittleBox point = LittlePlacementMath.cell(targetPos, hit, face, workingGrid, selectedGrid, false);
        Area area = LittleToolSelection.click(chisel, targetPos, point, workingGrid, selectedGrid);
        if (!level.isClientSide)
            return ActionResultType.CONSUME;
        if (area == null) {
            context.getPlayer().displayClientMessage(new StringTextComponent("LittleTiles: first corner set"), true);
            return ActionResultType.SUCCESS;
        }
        LittleTiles.NETWORK.sendToServer(new LittleToolActionPacket(LittleToolActionPacket.CHISEL, area, face,
                LittlePlacementMode.get(chisel)));
        return ActionResultType.SUCCESS;
    }

    public static void clickAir(PlayerEntity player, ItemStack chisel, Vector3d hit) {
        if (!LittleToolSelection.has(chisel))
            return;
        LittleGrid grid = LittleToolGrid.get(chisel);
        BlockPos pos = new BlockPos(hit.x, hit.y, hit.z);
        LittleBox point = LittlePlacementMath.cell(pos, hit, grid, grid);
        Area area = LittleToolSelection.click(chisel, pos, point, grid, grid);
        if (area != null)
            LittleTiles.NETWORK.sendToServer(new LittleToolActionPacket(LittleToolActionPacket.CHISEL, area, Direction.UP,
                    LittlePlacementMode.get(chisel)));
    }

    public static ActionResultType applyArea(World level, PlayerEntity player, ItemStack chisel, Area area, Direction face) {
        if (!hasSelectedMaterial(chisel))
            return ActionResultType.FAIL;
        BlockState selected = getSelectedState(chisel);
        if (selected.isAir())
            return ActionResultType.FAIL;

        LittlePlacementMode mode = LittlePlacementMode.get(chisel);
        List<Placement> placements = new ArrayList<>();
        double totalVolume = 0;
        for (Map.Entry<BlockPos, LittleBox> entry : area.split().entrySet()) {
            BlockPos pos = entry.getKey();
            if (!player.mayUseItemAt(pos, face, chisel))
                return ActionResultType.FAIL;
            TileEntity blockEntity = level.getBlockEntity(pos);
            TETiles tiles = blockEntity instanceof TETiles ? (TETiles) blockEntity : null;
            if (tiles == null && !level.getBlockState(pos).isAir()) {
                if (mode == LittlePlacementMode.NORMAL)
                    return ActionResultType.FAIL;
                continue;
            }

            LittleGrid grid = tiles == null ? area.grid : LittleGrid.max(tiles.getGrid(), area.grid);
            LittleBox localArea = LittleToolSelection.scale(entry.getValue(), area.grid, grid);
            List<LittleBox> free = new ArrayList<>();
            if (tiles == null) {
                free.add(localArea);
            } else {
                List<LittleBox> occupied = new ArrayList<>();
                for (LittleTile tile : tiles.getTiles())
                    for (LittleBox box : tile)
                        occupied.add(LittleToolSelection.scale(box, tiles.getGrid(), grid));
                if (mode == LittlePlacementMode.NORMAL) {
                    for (LittleBox box : occupied)
                        if (LittleBox.intersectsWith(localArea, box))
                            return ActionResultType.FAIL;
                    free.add(localArea);
                } else {
                    free.addAll(LittlePlacementMath.subtract(localArea, occupied));
                }
            }
            if (free.isEmpty())
                continue;
            for (LittleBox box : free)
                totalVolume += (double) box.getVolume() / grid.count3d;
            placements.add(new Placement(pos, tiles, grid, free));
        }
        if (placements.isEmpty())
            return ActionResultType.PASS;

        ItemStack bag = ItemLittleBag.find(player);
        if (!player.isCreative() && !ItemLittleBag.canTake(bag, selected, totalVolume))
            return ActionResultType.FAIL;

        List<BlockPos> created = new ArrayList<>();
        for (Placement placement : placements) {
            if (placement.tiles != null)
                continue;
            if (!level.setBlock(placement.pos, LittleTiles.TILES_BLOCK.get().defaultBlockState(), 3)) {
                rollbackCreated(level, created);
                return ActionResultType.FAIL;
            }
            TileEntity blockEntity = level.getBlockEntity(placement.pos);
            if (!(blockEntity instanceof TETiles)) {
                rollbackCreated(level, created);
                return ActionResultType.FAIL;
            }
            placement.tiles = (TETiles) blockEntity;
            created.add(placement.pos);
        }
        if (!player.isCreative() && !ItemLittleBag.take(bag, selected, totalVolume)) {
            rollbackCreated(level, created);
            return ActionResultType.FAIL;
        }

        for (Placement placement : placements) {
            if (placement.tiles.getGrid() != placement.grid)
                placement.tiles.convertTo(placement.grid);
            placement.tiles.getTiles().add(new LittleTile(selected, getSelectedColor(chisel), placement.boxes));
            placement.tiles.getTiles().combineTiles();
        }
        return ActionResultType.CONSUME;
    }

    private static void rollbackCreated(World level, List<BlockPos> created) {
        for (BlockPos pos : created)
            level.setBlock(pos, net.minecraft.block.Blocks.AIR.defaultBlockState(), 3);
    }

    private static final class Placement {
        final BlockPos pos;
        TETiles tiles;
        final LittleGrid grid;
        final List<LittleBox> boxes;

        Placement(BlockPos pos, TETiles tiles, LittleGrid grid, List<LittleBox> boxes) {
            this.pos = pos;
            this.tiles = tiles;
            this.grid = grid;
            this.boxes = boxes;
        }
    }
}
