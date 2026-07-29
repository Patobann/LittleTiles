package team.creative.littletiles.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.tile.LittleCollectionSafe;

public class TETiles extends TileEntity implements IGridBased {

    private LittleGrid grid = LittleGrid.overallDefault();
    private final LittleCollectionSafe tiles = new LittleCollectionSafe();

    public TETiles() {
        super(LittleTiles.TILES_TE_TYPE.get());
    }

    public LittleCollectionSafe getTiles() {
        return tiles;
    }

    @Override
    public LittleGrid getGrid() {
        return grid;
    }

    @Override
    public void convertTo(LittleGrid to) {
        if (grid == to)
            return;
        tiles.convertTo(grid, to);
        grid = to;
        setChanged();
    }

    @Override
    public int getSmallest() {
        return tiles.getSmallest(grid);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        grid = LittleGrid.get(nbt);
        tiles.load(nbt.getList("tiles", 10));
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        load(state, tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager network, SUpdateTileEntityPacket packet) {
        load(getBlockState(), packet.getTag());
    }
    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        grid.set(nbt);
        nbt.put("tiles", tiles.save());
        return nbt;
    }
}
