package team.creative.littletiles.common.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleCollection implements Iterable<LittleTile> {

    protected List<LittleTile> content = createInternalList();

    protected List<LittleTile> createInternalList() {
        return new ArrayList<>();
    }

    public void add(LittleTile tile) {
        content.add(tile);
        added(tile);
    }

    public void addAll(Iterable<LittleTile> tiles) {
        for (LittleTile tile : tiles)
            add(tile);
    }

    public boolean remove(LittleTile tile) {
        if (!content.remove(tile))
            return false;
        removed(tile);
        return true;
    }

    public void clear() {
        content.clear();
        refresh();
    }

    protected void added(LittleTile tile) {}

    protected void removed(LittleTile tile) {}

    protected void refresh() {}

    public Iterable<Pair<LittleTile, LittleBox>> boxes() {
        return new Iterable<Pair<LittleTile, LittleBox>>() {
            @Override
            public Iterator<Pair<LittleTile, LittleBox>> iterator() {
                return iteratorBoxes();
            }
        };
    }

    protected Iterator<Pair<LittleTile, LittleBox>> iteratorBoxes() {
        return new Iterator<Pair<LittleTile, LittleBox>>() {

            private final Iterator<LittleTile> tiles = content.iterator();
            private Iterator<LittleBox> boxes = new ArrayList<LittleBox>(0).iterator();
            private LittleTile tile;

            @Override
            public boolean hasNext() {
                while (!boxes.hasNext() && tiles.hasNext()) {
                    tile = tiles.next();
                    boxes = tile.iterator();
                }
                return boxes.hasNext();
            }

            @Override
            public Pair<LittleTile, LittleBox> next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return new Pair<>(tile, boxes.next());
            }
        };
    }

    public boolean combineTiles() {
        boolean changed = false;
        for (int i = 0; i < content.size(); i++) {
            LittleTile first = content.get(i);
            for (int j = content.size() - 1; j > i; j--) {
                LittleTile second = content.get(j);
                if (first.canBeCombined(second)) {
                    first.addAll(second.boxes);
                    first.combine();
                    content.remove(j);
                    removed(second);
                    changed = true;
                }
            }
        }
        return changed;
    }

    public int boxCount() {
        int count = 0;
        for (LittleTile tile : content)
            count += tile.size();
        return count;
    }

    public double getVolume() {
        double volume = 0;
        for (LittleTile tile : content)
            volume += tile.getVolume();
        return volume;
    }

    public int getSmallest(LittleGrid grid) {
        int smallest = LittleGrid.min().count;
        for (LittleTile tile : content)
            smallest = Math.max(smallest, tile.getSmallest(grid));
        return smallest;
    }

    public void convertTo(LittleGrid from, LittleGrid to) {
        if (from == to)
            return;
        for (LittleTile tile : content)
            tile.convertTo(from, to);
    }

    public ListNBT save() {
        ListNBT list = new ListNBT();
        for (LittleTile tile : content)
            list.add(tile.save(new CompoundNBT()));
        return list;
    }

    public void load(ListNBT list) {
        clear();
        for (int i = 0; i < list.size(); i++)
            add(new LittleTile(list.getCompound(i)));
    }

    @Override
    public Iterator<LittleTile> iterator() {
        final Iterator<LittleTile> iterator = content.iterator();
        return new Iterator<LittleTile>() {

            private LittleTile current;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public LittleTile next() {
                current = iterator.next();
                return current;
            }

            @Override
            public void remove() {
                iterator.remove();
                removed(current);
            }
        };
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public int size() {
        return content.size();
    }
}
