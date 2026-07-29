package team.creative.littletiles.common.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.LittleCollection;
import team.creative.littletiles.common.tile.LittleTile;

public class BlockTiles extends Block {

    public BlockTiles() {
        super(AbstractBlock.Properties.of(Material.STONE).strength(1.5F, 6.0F).noOcclusion());
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
            return VoxelShapes.block();
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
