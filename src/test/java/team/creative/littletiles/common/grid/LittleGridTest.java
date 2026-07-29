package team.creative.littletiles.common.grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import net.minecraft.nbt.CompoundNBT;
import team.creative.littletiles.common.math.vec.LittleVec;

public class LittleGridTest {

    @Before
    public void resetGridConfiguration() {
        LittleGrid.loadGrid(2, 7, 2, LittleGrid.OVERALL_DEFAULT);
    }

    @Test
    public void createsExpectedGridSequence() {
        assertEquals(7, LittleGrid.grids.length);
        assertEquals(2, LittleGrid.min().count);
        assertEquals(16, LittleGrid.overallDefault().count);
        assertEquals(16, LittleGrid.defaultGrid().count);
        assertEquals(128, LittleGrid.getMax().count);
        assertEquals("16", LittleGrid.names().get(3));
    }

    @Test
    public void convertsAcrossBlockAndGridCoordinates() {
        LittleGrid grid = LittleGrid.get(16);

        assertEquals(1.0D, grid.toVanillaGrid(16), 0.0D);
        assertEquals(16, grid.toGrid(1));
        assertEquals(8, grid.toGrid(0.5D));
        assertEquals(1, grid.toBlockOffset(16));
        assertEquals(0, grid.toBlockOffset(15));
        assertEquals(-1, grid.toBlockOffset(-1));
        assertTrue(grid.isAtEdge(0.5D));
        assertFalse(grid.isAtEdge(0.51D));
    }

    @Test
    public void storesOnlyNonDefaultGridInNbt() {
        CompoundNBT nbt = new CompoundNBT();

        LittleGrid.get(32).set(nbt);
        assertEquals(32, nbt.getInt("grid"));
        assertSame(LittleGrid.get(32), LittleGrid.get(nbt));

        LittleGrid.overallDefault().set(nbt);
        assertFalse(nbt.contains("grid"));
        assertSame(LittleGrid.overallDefault(), LittleGrid.get(nbt));
    }

    @Test
    public void vectorConversionPreservesWorldPosition() {
        LittleGrid grid16 = LittleGrid.get(16);
        LittleGrid grid32 = LittleGrid.get(32);
        LittleVec vec = new LittleVec(8, -16, 24);

        vec.convertTo(grid16, grid32);
        assertEquals(new LittleVec(16, -32, 48), vec);
        assertEquals(0.5D, vec.getPosX(grid32), 0.0D);
        assertEquals(-1.0D, vec.getPosY(grid32), 0.0D);

        vec.convertTo(grid32, grid16);
        assertEquals(new LittleVec(8, -16, 24), vec);
    }

    @Test
    public void vectorRoundTripsThroughNbtAndFindsSmallestGrid() {
        LittleGrid grid16 = LittleGrid.get(16);
        CompoundNBT nbt = new CompoundNBT();
        LittleVec original = new LittleVec(8, 0, -8);

        original.save("pos", nbt);
        LittleVec loaded = new LittleVec("pos", nbt);

        assertEquals(original, loaded);
        assertEquals(original.hashCode(), loaded.hashCode());
        assertEquals(2, loaded.getSmallest(grid16));
        assertEquals(16, new LittleVec(1, 0, 0).getSmallest(grid16));
    }
}
