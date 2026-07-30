package team.creative.littletiles.common.math;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public final class LittlePlacementMath {

    private static final double EPSILON = 1.0E-7D;

    private LittlePlacementMath() {}

    public static BlockPos outwardBlock(BlockPos clickedPos, Vector3d hit, Direction face) {
        double x = hit.x - clickedPos.getX() + face.getStepX() * EPSILON;
        double y = hit.y - clickedPos.getY() + face.getStepY() * EPSILON;
        double z = hit.z - clickedPos.getZ() + face.getStepZ() * EPSILON;
        if (x < 0 || x >= 1 || y < 0 || y >= 1 || z < 0 || z >= 1)
            return clickedPos.relative(face);
        return clickedPos;
    }

    public static LittleBox cell(BlockPos targetPos, Vector3d hit, Direction face, LittleGrid grid, boolean inward) {
        return cell(targetPos, hit, face, grid, grid, inward);
    }

    public static LittleBox cell(BlockPos targetPos, Vector3d hit, Direction face, LittleGrid grid, LittleGrid selectedGrid, boolean inward) {
        if (grid.count < selectedGrid.count || grid.count % selectedGrid.count != 0)
            throw new IllegalArgumentException("Selected grid " + selectedGrid.count + " does not fit working grid " + grid.count);
        double direction = inward ? -EPSILON : EPSILON;
        int size = grid.count / selectedGrid.count;
        int x = align(coordinate(hit.x - targetPos.getX() + face.getStepX() * direction, grid), size);
        int y = align(coordinate(hit.y - targetPos.getY() + face.getStepY() * direction, grid), size);
        int z = align(coordinate(hit.z - targetPos.getZ() + face.getStepZ() * direction, grid), size);
        return new LittleBox(x, y, z, x + size, y + size, z + size);
    }

    public static LittleBox cell(BlockPos targetPos, Vector3d hit, LittleGrid grid, LittleGrid selectedGrid) {
        if (grid.count < selectedGrid.count || grid.count % selectedGrid.count != 0)
            throw new IllegalArgumentException("Selected grid " + selectedGrid.count + " does not fit working grid " + grid.count);
        int size = grid.count / selectedGrid.count;
        int x = align(coordinate(hit.x - targetPos.getX(), grid), size);
        int y = align(coordinate(hit.y - targetPos.getY(), grid), size);
        int z = align(coordinate(hit.z - targetPos.getZ(), grid), size);
        return new LittleBox(x, y, z, x + size, y + size, z + size);
    }

    private static int align(int coordinate, int size) {
        return coordinate / size * size;
    }

    public static List<LittleBox> subtract(LittleBox area, Iterable<LittleBox> occupied) {
        List<LittleBox> remaining = new ArrayList<>();
        remaining.add(area.copy());
        for (LittleBox obstacle : occupied) {
            List<LittleBox> next = new ArrayList<>();
            for (LittleBox candidate : remaining) {
                List<LittleBox> cut = candidate.cutOut(obstacle, null);
                if (cut == null)
                    next.add(candidate);
                else
                    next.addAll(cut);
            }
            remaining = next;
            if (remaining.isEmpty())
                break;
        }
        return remaining;
    }

    private static int coordinate(double value, LittleGrid grid) {
        return MathHelper.clamp((int) Math.floor(value * grid.count), 0, grid.count - 1);
    }
}
