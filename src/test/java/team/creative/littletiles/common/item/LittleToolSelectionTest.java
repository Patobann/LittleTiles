package team.creative.littletiles.common.item;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleToolSelectionTest {

    @Test
    public void buildsInclusiveBoxInEitherDirection() {
        LittleBox box = LittleToolSelection.between(7, 2, 12, 3, 5, 9);
        assertBox(box, 3, 2, 9, 8, 6, 13);
    }

    @Test
    public void scalesSelectionToWorkingGrid() {
        LittleBox box = LittleToolSelection.scale(new LittleBox(3, 2, 1, 5, 4, 3), LittleGrid.get(16), LittleGrid.get(32));
        assertBox(box, 6, 4, 2, 10, 8, 6);
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
