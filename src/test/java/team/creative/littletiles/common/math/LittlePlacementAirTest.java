package team.creative.littletiles.common.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittlePlacementAirTest {

    @Test
    public void alignsAirPointToSelectedGrid() {
        LittleBox box = LittlePlacementMath.cell(new BlockPos(1, 2, 3), new Vector3d(1.62D, 2.01D, 3.99D),
                LittleGrid.get(16), LittleGrid.get(8));
        assertEquals(8, box.minX);
        assertEquals(0, box.minY);
        assertEquals(14, box.minZ);
        assertEquals(10, box.maxX);
        assertEquals(2, box.maxY);
        assertEquals(16, box.maxZ);
    }
}
