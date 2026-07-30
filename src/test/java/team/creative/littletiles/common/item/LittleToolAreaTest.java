package team.creative.littletiles.common.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import net.minecraft.util.math.BlockPos;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.LittleToolSelection.Area;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleToolAreaTest {

    @Test
    public void splitsSelectionAtPositiveBlockBoundary() {
        Area area = new Area(new BlockPos(10, 20, 30), LittleGrid.get(4), new LittleBox(3, 1, 2, 6, 3, 4));
        Map<BlockPos, LittleBox> split = area.split();

        assertEquals(2, split.size());
        assertBox(split.get(new BlockPos(10, 20, 30)), 3, 1, 2, 4, 3, 4);
        assertBox(split.get(new BlockPos(11, 20, 30)), 0, 1, 2, 2, 3, 4);
    }

    @Test
    public void splitsSelectionAtNegativeBlockBoundary() {
        Area area = new Area(BlockPos.ZERO, LittleGrid.get(4), new LittleBox(-2, 0, 0, 2, 1, 1));
        Map<BlockPos, LittleBox> split = area.split();

        assertEquals(2, split.size());
        assertBox(split.get(new BlockPos(-1, 0, 0)), 2, 0, 0, 4, 1, 1);
        assertBox(split.get(BlockPos.ZERO), 0, 0, 0, 2, 1, 1);
    }

    @Test
    public void worldBoundsFollowOriginAndGrid() {
        Area area = new Area(new BlockPos(2, 3, 4), LittleGrid.get(4), new LittleBox(-2, 1, 0, 6, 3, 4));
        assertEquals(1.5D, area.getBounds().minX, 0);
        assertEquals(3.25D, area.getBounds().minY, 0);
        assertEquals(4.0D, area.getBounds().minZ, 0);
        assertEquals(3.5D, area.getBounds().maxX, 0);
        assertEquals(3.75D, area.getBounds().maxY, 0);
        assertEquals(5.0D, area.getBounds().maxZ, 0);
        assertTrue(area.getBounds().getXsize() > 1);
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
