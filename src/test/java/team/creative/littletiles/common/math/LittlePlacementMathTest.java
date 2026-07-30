package team.creative.littletiles.common.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

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

    @Test
    public void coarserSelectionAlignsInsideFinerGrid() {
        LittleBox box = LittlePlacementMath.cell(ORIGIN, new Vector3d(0.53D, 1, 0.47D), Direction.UP, LittleGrid.get(32), GRID, true);
        assertBox(box, 16, 30, 14, 18, 32, 16);
    }

    @Test
    public void fillModeSubtractsOccupiedVolume() {
        LittleBox occupied = new LittleBox(1, 1, 1, 3, 3, 3);
        List<LittleBox> remaining = LittlePlacementMath.subtract(new LittleBox(0, 0, 0, 4, 4, 4), Arrays.asList(occupied));
        int volume = 0;
        for (LittleBox box : remaining) {
            volume += box.getVolume();
            assertFalse(LittleBox.intersectsWith(box, occupied));
        }
        assertEquals(56, volume);
    }

    private static void assertCell(LittleBox box, int x, int y, int z) {
        assertBox(box, x, y, z, x + 1, y + 1, z + 1);
    }

    private static void assertBox(LittleBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        assertEquals(minX, box.minX);
        assertEquals(minY, box.minY);
        assertEquals(minZ, box.minZ);
        assertEquals(maxX, box.maxX);
        assertEquals(maxY, box.maxY);
        assertEquals(maxZ, box.maxZ);
    }
}
