package team.creative.littletiles.common.item;

import static org.junit.Assert.assertSame;

import org.junit.Test;

import team.creative.littletiles.common.grid.LittleGrid;

public class LittleToolGridTest {

    @Test
    public void cyclesThroughConfiguredGridsAndWraps() {
        assertSame(LittleGrid.get(32), LittleToolGrid.next(LittleGrid.get(16)));
        assertSame(LittleGrid.min(), LittleToolGrid.next(LittleGrid.getMax()));
    }
}
