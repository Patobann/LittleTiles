package team.creative.littletiles.common.math;

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
        double direction = inward ? -EPSILON : EPSILON;
        int x = coordinate(hit.x - targetPos.getX() + face.getStepX() * direction, grid);
        int y = coordinate(hit.y - targetPos.getY() + face.getStepY() * direction, grid);
        int z = coordinate(hit.z - targetPos.getZ() + face.getStepZ() * direction, grid);
        return new LittleBox(x, y, z, x + 1, y + 1, z + 1);
    }

    private static int coordinate(double value, LittleGrid grid) {
        return MathHelper.clamp((int) Math.floor(value * grid.count), 0, grid.count - 1);
    }
}
