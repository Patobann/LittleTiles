package team.creative.littletiles.common.math.box;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.minecraft.util.Direction;

public class LittleFaceCullingTest {

    @Test
    public void adjacentBoxFullyCoversFace() {
        LittleBox left = new LittleBox(0, 0, 0, 8, 16, 16);
        java.util.List<LittleBox> tiles = java.util.Arrays.asList(left, new LittleBox(8, 0, 0, 16, 16, 16));
        assertTrue(LittleFaceCulling.isCovered(left, Direction.EAST, tiles));
    }

    @Test
    public void partialNeighborDoesNotHideWholeFace() {
        LittleBox left = new LittleBox(0, 0, 0, 8, 16, 16);
        java.util.List<LittleBox> tiles = java.util.Arrays.asList(left, new LittleBox(8, 0, 0, 16, 8, 16));
        assertFalse(LittleFaceCulling.isCovered(left, Direction.EAST, tiles));
    }
}
