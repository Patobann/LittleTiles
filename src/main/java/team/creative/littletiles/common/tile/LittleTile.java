package team.creative.littletiles.common.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxReturnedVolume;

/** A material/color element with one or more little-box fragments. */
public class LittleTile extends LittleElement implements Iterable<LittleBox> {

    public final List<LittleBox> boxes = new ArrayList<>(1);

    public LittleTile(LittleElement element, LittleBox box) {
        super(element);
        boxes.add(box);
    }

    public LittleTile(LittleElement element, Iterable<LittleBox> boxes) {
        super(element);
        addAll(boxes);
    }

    public LittleTile(BlockState state, int color, LittleBox box) {
        super(state, color);
        boxes.add(box);
    }

    public LittleTile(BlockState state, int color, Iterable<LittleBox> boxes) {
        super(state, color);
        addAll(boxes);
    }

    public LittleTile(String stateName, int color, Iterable<LittleBox> boxes) {
        super(stateName, color);
        addAll(boxes);
    }

    public LittleTile(CompoundNBT nbt) {
        super(nbt);
        loadBoxes(nbt);
    }

    public void addBox(LittleBox box) {
        boxes.add(box);
    }

    public void addAll(Iterable<LittleBox> boxes) {
        for (LittleBox box : boxes)
            this.boxes.add(box);
    }

    public boolean removeBox(LittleBox box) {
        return boxes.remove(box);
    }

    public int size() {
        return boxes.size();
    }

    public boolean isEmpty() {
        return boxes.isEmpty();
    }

    @Override
    public Iterator<LittleBox> iterator() {
        return boxes.iterator();
    }

    public int getSmallest(LittleGrid grid) {
        int smallest = LittleGrid.min().count;
        for (LittleBox box : boxes)
            smallest = Math.max(smallest, box.getSmallest(grid));
        return smallest;
    }

    public void convertTo(LittleGrid from, LittleGrid to) {
        if (from == to)
            return;
        for (LittleBox box : boxes)
            box.convertTo(from, to);
    }

    public void combine() {
        BasicCombiner.combineBoxes(boxes);
    }

    public double getVolume() {
        double volume = 0;
        for (LittleBox box : boxes)
            volume += box.getVolume();
        return volume;
    }

    public double getPercentVolume(LittleGrid grid) {
        return getVolume() / grid.count3d;
    }

    public boolean doesFillEntireBlock(LittleGrid grid) {
        return boxes.size() == 1 && boxes.get(0).doesFillEntireBlock(grid);
    }

    public boolean fillInSpace(LittleBox area, boolean[][][] filled) {
        boolean changed = false;
        for (LittleBox box : boxes)
            changed |= box.fillInSpace(area, filled);
        return changed;
    }

    public boolean intersectsWith(LittleBox other) {
        for (LittleBox box : boxes)
            if (LittleBox.intersectsWith(box, other))
                return true;
        return false;
    }

    public List<LittleBox> cutOut(LittleBox cut, @Nullable LittleBoxReturnedVolume volume) {
        List<LittleBox> result = new ArrayList<>();
        for (LittleBox box : boxes) {
            List<LittleBox> remainder = box.cutOut(cut, volume);
            if (remainder == null)
                result.add(box.copy());
            else
                result.addAll(remainder);
        }
        BasicCombiner.combineBoxes(result);
        return result;
    }

    @Nullable
    public LittleBox getCompleteBox() {
        if (boxes.isEmpty())
            return null;
        return new LittleBox(boxes.toArray(new LittleBox[0]));
    }

    @Nullable
    public BlockRayTraceResult rayTrace(LittleGrid grid, BlockPos blockPos, Vector3d from, Vector3d to) {
        BlockRayTraceResult closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (LittleBox box : boxes) {
            BlockRayTraceResult hit = box.rayTrace(grid, blockPos, from, to);
            if (hit != null) {
                double distance = from.distanceToSqr(hit.getLocation());
                if (distance < closestDistance) {
                    closest = hit;
                    closestDistance = distance;
                }
            }
        }
        return closest;
    }

    public boolean doesTouch(LittleTile tile) {
        for (LittleBox box : boxes)
            for (LittleBox other : tile.boxes)
                if (box.doesTouch(other))
                    return true;
        return false;
    }

    public boolean canBeCombined(LittleTile tile) {
        return super.equals(tile);
    }

    public LittleTile copy() {
        List<LittleBox> copied = new ArrayList<>(boxes.size());
        for (LittleBox box : boxes)
            copied.add(box.copy());
        return new LittleTile(this, copied);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        nbt.remove("box");
        nbt.remove("boxes");
        if (boxes.size() == 1) {
            nbt.put("box", boxes.get(0).getNBTIntArray());
        } else {
            ListNBT list = new ListNBT();
            for (LittleBox box : boxes)
                list.add(box.getNBTIntArray());
            nbt.put("boxes", list);
        }
        return nbt;
    }

    private void loadBoxes(CompoundNBT nbt) {
        if (nbt.contains("box", 11)) {
            boxes.add(LittleBox.createBox(nbt.getIntArray("box")));
            return;
        }
        if (nbt.contains("boxes", 9)) {
            ListNBT list = nbt.getList("boxes", 11);
            for (int i = 0; i < list.size(); i++)
                boxes.add(LittleBox.createBox(list.getIntArray(i)));
            return;
        }
        if (nbt.contains("bSize", 3)) {
            int count = nbt.getInt("bSize");
            for (int i = 0; i < count; i++)
                boxes.add(LittleBox.loadBox("bBox" + i, nbt));
        }
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + boxes.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof LittleTile && super.equals(object) && boxes.equals(((LittleTile) object).boxes);
    }
}
