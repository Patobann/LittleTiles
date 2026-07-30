package team.creative.littletiles.common.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.LittleCollection;
import team.creative.littletiles.common.tile.LittleTile;

public class BlockTiles extends Block {

    public BlockTiles() {
        super(AbstractBlock.Properties.of(Material.STONE).strength(1.5F, 6.0F).noOcclusion().dynamicShape());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TETiles();
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity blockEntity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (!(blockEntity instanceof TETiles))
            return super.getDrops(state, builder);

        TETiles tiles = (TETiles) blockEntity;
        Map<BlockState, Integer> fullMaterials = countFullBlocks(tiles.getGrid(), tiles.getTiles());

        ServerWorld level = builder.getLevel();
        BlockPos pos = new BlockPos(builder.getParameter(LootParameters.ORIGIN));
        Entity breaker = builder.getOptionalParameter(LootParameters.THIS_ENTITY);
        ItemStack tool = builder.getOptionalParameter(LootParameters.TOOL);
        if (tool == null)
            tool = ItemStack.EMPTY;

        List<ItemStack> drops = new ArrayList<>();
        for (Map.Entry<BlockState, Integer> entry : fullMaterials.entrySet())
            for (int i = 0; i < entry.getValue(); i++)
                drops.addAll(Block.getDrops(entry.getKey(), level, pos, null, breaker, tool));
        return drops;
    }

    public static Map<BlockState, Integer> countFullBlocks(LittleGrid grid, LittleCollection tiles) {
        Map<BlockState, Double> materialVolume = new HashMap<>();
        for (LittleTile tile : tiles)
            materialVolume.merge(tile.getState(), tile.getVolume(), Double::sum);

        Map<BlockState, Integer> result = new HashMap<>();
        for (Map.Entry<BlockState, Double> entry : materialVolume.entrySet())
            result.put(entry.getKey(), (int) Math.floor(entry.getValue() / grid.count3d));
        return result;
    }
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TETiles tiles = getTiles(world, pos);
        if (tiles == null || tiles.getTiles().isEmpty())
            return VoxelShapes.block();
        return createShape(tiles.getGrid(), tiles.getTiles());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TETiles tiles = getTiles(world, pos);
        if (tiles == null)
            return VoxelShapes.empty();
        return createShape(tiles.getGrid(), tiles.getTiles());
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    public static VoxelShape createShape(LittleGrid grid, LittleCollection tiles) {
        VoxelShape shape = VoxelShapes.empty();
        for (Pair<LittleTile, LittleBox> pair : tiles.boxes())
            shape = VoxelShapes.or(shape, VoxelShapes.create(pair.value.getBB(grid)));
        return shape.optimize();
    }

    private static TETiles getTiles(IBlockReader world, BlockPos pos) {
        TileEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TETiles)
            return (TETiles) blockEntity;
        return null;
    }
}
