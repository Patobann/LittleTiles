package team.creative.littletiles.common.math.box;

import net.minecraft.util.Direction;
import team.creative.littletiles.common.tile.LittleCollection;

public final class LittleFaceCulling {

    private LittleFaceCulling() {}

    public static boolean isCovered(LittleBox box, Direction face, LittleCollection tiles) {
        java.util.List<LittleBox> boxes = new java.util.ArrayList<>();
        tiles.boxes().forEach(pair -> boxes.add(pair.value));
        return isCovered(box, face, boxes);
    }

    public static boolean isCovered(LittleBox box, Direction face, Iterable<LittleBox> boxes) {
        int firstSize = face.getAxis() == Direction.Axis.X ? box.maxY - box.minY : box.maxX - box.minX;
        int secondSize = face.getAxis() == Direction.Axis.Z ? box.maxY - box.minY : box.maxZ - box.minZ;
        boolean[] covered = new boolean[firstSize * secondSize];
        for (LittleBox other : boxes) {
            if (other == box || !touchesFace(box, other, face))
                continue;
            int firstMin = face.getAxis() == Direction.Axis.X ? Math.max(box.minY, other.minY) : Math.max(box.minX, other.minX);
            int firstMax = face.getAxis() == Direction.Axis.X ? Math.min(box.maxY, other.maxY) : Math.min(box.maxX, other.maxX);
            int secondMin = face.getAxis() == Direction.Axis.Z ? Math.max(box.minY, other.minY) : Math.max(box.minZ, other.minZ);
            int secondMax = face.getAxis() == Direction.Axis.Z ? Math.min(box.maxY, other.maxY) : Math.min(box.maxZ, other.maxZ);
            int firstOffset = face.getAxis() == Direction.Axis.X ? box.minY : box.minX;
            int secondOffset = face.getAxis() == Direction.Axis.Z ? box.minY : box.minZ;
            for (int first = firstMin; first < firstMax; first++)
                for (int second = secondMin; second < secondMax; second++)
                    covered[(first - firstOffset) * secondSize + second - secondOffset] = true;
        }
        for (boolean cell : covered)
            if (!cell)
                return false;
        return covered.length > 0;
    }

    private static boolean touchesFace(LittleBox box, LittleBox other, Direction face) {
        switch (face) {
        case EAST:
            return other.minX == box.maxX;
        case WEST:
            return other.maxX == box.minX;
        case UP:
            return other.minY == box.maxY;
        case DOWN:
            return other.maxY == box.minY;
        case SOUTH:
            return other.minZ == box.maxZ;
        case NORTH:
            return other.maxZ == box.minZ;
        default:
            return false;
        }
    }
}
