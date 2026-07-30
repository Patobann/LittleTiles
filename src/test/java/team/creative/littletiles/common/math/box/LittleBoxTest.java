package team.creative.littletiles.common.math.box;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.BlockPos;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.tile.BasicCombiner;

public class LittleBoxTest {

    @Before
    public void resetGridConfiguration() {
        LittleGrid.loadGrid(2, 7, 2, LittleGrid.OVERALL_DEFAULT);
    }

    @Test
    public void convertsGridWithoutChangingWorldBounds() {
        LittleBox box = new LittleBox(4, -8, 0, 12, 8, 16);

        box.convertTo(LittleGrid.get(16), LittleGrid.get(32));
        assertEquals(new LittleBox(8, -16, 0, 24, 16, 32), box);
        assertEquals(0.5D, box.getPercentVolume(LittleGrid.get(32)), 0.0D);

        box.convertTo(LittleGrid.get(32), LittleGrid.get(16));
        assertEquals(new LittleBox(4, -8, 0, 12, 8, 16), box);
    }

    @Test
    public void splitsAcrossBlockBoundaryIntoLocalBoxes() {
        LittleGrid grid = LittleGrid.get(16);
        HashMapList<BlockPos, LittleBox> split = new HashMapList<>();

        new LittleBox(12, 0, 0, 20, 8, 8).split(grid, BlockPos.ZERO, split, null);

        assertEquals(2, split.sizeOfValues());
        assertEquals(new LittleBox(12, 0, 0, 16, 8, 8), split.get(BlockPos.ZERO).get(0));
        assertEquals(new LittleBox(0, 0, 0, 4, 8, 8), split.get(new BlockPos(1, 0, 0)).get(0));
    }

    @Test
    public void combinesAdjacentBoxesAndCutsOutMiddleSlab() {
        List<LittleBox> boxes = new ArrayList<>(Arrays.asList(
                new LittleBox(0, 0, 0, 2, 4, 4),
                new LittleBox(2, 0, 0, 4, 4, 4)));

        assertTrue(BasicCombiner.combineBoxes(boxes));
        assertEquals(Arrays.asList(new LittleBox(0, 0, 0, 4, 4, 4)), boxes);

        List<LittleBox> remainder = boxes.get(0).cutOut(new LittleBox(1, 0, 0, 3, 4, 4), null);
        assertNotNull(remainder);
        assertEquals(2, remainder.size());
        assertTrue(remainder.contains(new LittleBox(0, 0, 0, 1, 4, 4)));
        assertTrue(remainder.contains(new LittleBox(3, 0, 0, 4, 4, 4)));
    }

    @Test
    public void roundTripsCurrentNbtAndMigratesLegacyCoordinates() {
        CompoundNBT current = new CompoundNBT();
        LittleBox original = new LittleBox(-1, 2, 3, 4, 5, 6);
        current.put("box", original.getNBTIntArray());
        assertEquals(original, LittleBox.loadBox("box", current));

        CompoundNBT legacy = new CompoundNBT();
        legacy.putInt("boxminX", -1);
        legacy.putInt("boxminY", 2);
        legacy.putInt("boxminZ", 3);
        legacy.putInt("boxmaxX", 4);
        legacy.putInt("boxmaxY", 5);
        legacy.putInt("boxmaxZ", 6);

        assertEquals(original, LittleBox.loadBox("box", legacy));
        assertFalse(legacy.contains("boxminX"));
        assertEquals(6, legacy.getIntArray("box").length);
    }

    @Test
    public void rayTraceReturnsNearestBoxFace() {
        LittleBox box = new LittleBox(0, 0, 0, 8, 16, 16);

        BlockRayTraceResult hit = box.rayTrace(LittleGrid.get(16), BlockPos.ZERO,
                new Vector3d(-1, 0.5D, 0.5D), new Vector3d(1, 0.5D, 0.5D));

        assertNotNull(hit);
        assertEquals(Direction.WEST, hit.getDirection());
        assertEquals(0.0D, hit.getLocation().x, 0.0D);
    }

    @Test
    public void loadsLegacySlicePayloadAsItsBoundingBox() {
        LittleBox expected = new LittleBox(1, 2, 3, 4, 5, 6);

        assertEquals(expected, LittleBox.createBox(new int[] { 1, 2, 3, 4, 5, 6, 0 }));
        assertEquals(expected, LittleBox.createBox(new int[] { 1, 2, 3, 4, 5, 6, 0, 0, 0, 0, 0 }));
    }

    @Test
    public void tracksReturnedVolumeInGridUnits() {
        LittleBoxReturnedVolume volume = new LittleBoxReturnedVolume();
        volume.addBox(0, 0, 0, 4, 4, 4);
        volume.addDifBox(new LittleBox(0, 0, 0, 2, 2, 2), 0, 0, 0, 4, 4, 4);

        assertEquals(72, volume.getVolume());
        assertTrue(volume.has());
        assertEquals(72D / 4096D, volume.getPercentVolume(LittleGrid.get(16)), 0.0D);
        volume.clear();
        assertFalse(volume.has());
        assertEquals(0, volume.getVolume());
    }

    @Test
    public void derivesSolidCutVolumeFromRemainder() {
        LittleBox source = new LittleBox(0, 0, 0, 4, 4, 4);
        LittleBoxReturnedVolume approximationLoss = new LittleBoxReturnedVolume();
        List<LittleBox> remainder = source.cutOut(new LittleBox(1, 0, 0, 3, 4, 4), approximationLoss);

        int remainingVolume = 0;
        for (LittleBox box : remainder)
            remainingVolume += box.getVolume();

        assertFalse(approximationLoss.has());
        assertEquals(32D, source.getVolume() - remainingVolume, 0.0D);
    }
}
