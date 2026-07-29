package team.creative.littletiles.common.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittlePlacementMathTest {

    private static final LittleGrid GRID = LittleGrid.get(16);
    private static final BlockPos ORIGIN = BlockPos.ZERO;

    @Test
    public void inwardHitSelectsSurfaceCell() {
        assertCell(LittlePlacementMath.cell(ORIGIN, new Vector3d(1, 0.5, 0.5), Direction.EAST, GRID, true), 15, 8, 8);
    }

    @Test
    public void outwardHitCrossesEastBlockBoundary() {
        Vector3d hit = new Vector3d(1, 0.5, 0.5);
        BlockPos target = LittlePlacementMath.outwardBlock(ORIGIN, hit, Direction.EAST);
        assertEquals(new BlockPos(1, 0, 0), target);
        assertCell(LittlePlacementMath.cell(target, hit, Direction.EAST, GRID, false), 0, 8, 8);
    }

    @Test
    public void outwardHitCrossesWestBlockBoundary() {
        Vector3d hit = new Vector3d(0, 0.5, 0.5);
        BlockPos target = LittlePlacementMath.outwardBlock(ORIGIN, hit, Direction.WEST);
        assertEquals(new BlockPos(-1, 0, 0), target);
        assertCell(LittlePlacementMath.cell(target, hit, Direction.WEST, GRID, false), 15, 8, 8);
    }

    @Test
    public void recessedFaceTargetsCellInsideSameBlock() {
        Vector3d hit = new Vector3d(15 / 16.0D, 0.5, 0.5);
        BlockPos target = LittlePlacementMath.outwardBlock(ORIGIN, hit, Direction.EAST);
        assertEquals(ORIGIN, target);
        assertCell(LittlePlacementMath.cell(target, hit, Direction.EAST, GRID, false), 15, 8, 8);
    }

    private static void assertCell(LittleBox box, int x, int y, int z) {
        assertEquals(x, box.minX);
        assertEquals(y, box.minY);
        assertEquals(z, box.minZ);
        assertEquals(x + 1, box.maxX);
        assertEquals(y + 1, box.maxY);
        assertEquals(z + 1, box.maxZ);
    }
}
