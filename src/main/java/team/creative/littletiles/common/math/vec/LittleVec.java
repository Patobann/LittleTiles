package team.creative.littletiles.common.math.vec;

import java.security.InvalidParameterException;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittleVec {

    public static final LittleVec ZERO = new LittleVec(0, 0, 0);

    public int x;
    public int y;
    public int z;

    public LittleVec(String name, CompoundNBT nbt) {
        int[] array = nbt.getIntArray(name);
        if (array.length != 3)
            throw new InvalidParameterException("No valid coords given " + nbt);
        set(array[0], array[1], array[2]);
    }

    public LittleVec(LittleGrid grid, Vector3d vec) {
        this(grid.toGrid(vec.x), grid.toGrid(vec.y), grid.toGrid(vec.z));
    }

    public LittleVec(LittleGrid grid, Vec3d vec) {
        this(grid.toGrid(vec.x), grid.toGrid(vec.y), grid.toGrid(vec.z));
    }

    public LittleVec(LittleGrid grid, Vector3i vec) {
        this(grid.toGrid(vec.getX()), grid.toGrid(vec.getY()), grid.toGrid(vec.getZ()));
    }

    public LittleVec(Facing facing) {
        this(0, 0, 0);
        set(facing.axis, facing.offset());
    }

    public LittleVec(int x, int y, int z) {
        set(x, y, z);
    }

    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(LittleGrid grid, Vector3i vec) {
        set(grid.toGrid(vec.getX()), grid.toGrid(vec.getY()), grid.toGrid(vec.getZ()));
    }

    public int getSmallest(LittleGrid grid) {
        int size = LittleGrid.base;
        size = Math.max(size, grid.getMinGrid(x));
        size = Math.max(size, grid.getMinGrid(y));
        size = Math.max(size, grid.getMinGrid(z));
        return size;
    }

    public void convertTo(LittleGrid from, LittleGrid to) {
        if (from == to)
            return;
        if (from.count > to.count) {
            int ratio = from.count / to.count;
            x /= ratio;
            y /= ratio;
            z /= ratio;
        } else {
            int ratio = to.count / from.count;
            x *= ratio;
            y *= ratio;
            z *= ratio;
        }
    }

    public BlockPos getBlockPos(LittleGrid grid) {
        return new BlockPos(
            (int) Math.floor(grid.toVanillaGrid(x)),
            (int) Math.floor(grid.toVanillaGrid(y)),
            (int) Math.floor(grid.toVanillaGrid(z))
        );
    }

    public Vec3d getVec(LittleGrid grid) {
        return new Vec3d(grid.toVanillaGrid(x), grid.toVanillaGrid(y), grid.toVanillaGrid(z));
    }

    public Vector3d getVector(LittleGrid grid) {
        return new Vector3d(grid.toVanillaGrid(x), grid.toVanillaGrid(y), grid.toVanillaGrid(z));
    }

    public double getPosX(LittleGrid grid) {
        return grid.toVanillaGrid(x);
    }

    public double getPosY(LittleGrid grid) {
        return grid.toVanillaGrid(y);
    }

    public double getPosZ(LittleGrid grid) {
        return grid.toVanillaGrid(z);
    }

    public void add(Facing facing) {
        set(facing.axis, get(facing.axis) + facing.offset());
    }

    public void add(LittleVec vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
    }

    public void add(BlockPos pos, LittleGrid grid) {
        x += grid.toGrid(pos.getX());
        y += grid.toGrid(pos.getY());
        z += grid.toGrid(pos.getZ());
    }

    public void sub(Facing facing) {
        set(facing.axis, get(facing.axis) - facing.offset());
    }

    public void sub(LittleVec vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
    }

    public void sub(BlockPos pos, LittleGrid grid) {
        x -= grid.toGrid(pos.getX());
        y -= grid.toGrid(pos.getY());
        z -= grid.toGrid(pos.getZ());
    }

    public void flip(Axis axis) {
        set(axis, -get(axis));
    }

    public void rotateVec(Rotation rotation) {
        int oldX = x;
        int oldY = y;
        int oldZ = z;
        x = rotation.getMatrix().getX(oldX, oldY, oldZ);
        y = rotation.getMatrix().getY(oldX, oldY, oldZ);
        z = rotation.getMatrix().getZ(oldX, oldY, oldZ);
    }

    public double distanceTo(LittleVec vec) {
        return Math.sqrt(distanceToSqr(vec));
    }

    public double distanceToSqr(LittleVec vec) {
        int deltaX = vec.x - x;
        int deltaY = vec.y - y;
        int deltaZ = vec.z - z;
        return (double) deltaX * deltaX + (double) deltaY * deltaY + (double) deltaZ * deltaZ;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LittleVec))
            return false;
        LittleVec vec = (LittleVec) object;
        return x == vec.x && y == vec.y && z == vec.z;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(x);
        result = 31 * result + Integer.hashCode(y);
        return 31 * result + Integer.hashCode(z);
    }

    public LittleVec copy() {
        return new LittleVec(x, y, z);
    }

    public void save(String name, CompoundNBT nbt) {
        nbt.putIntArray(name, new int[] { x, y, z });
    }

    public void writeToNBT(String name, CompoundNBT nbt) {
        save(name, nbt);
    }

    public void invert() {
        set(-x, -y, -z);
    }

    public void scale(int factor) {
        x *= factor;
        y *= factor;
        z *= factor;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setX(int value) {
        x = value;
    }

    public void setY(int value) {
        y = value;
    }

    public void setZ(int value) {
        z = value;
    }

    public int get(Axis axis) {
        switch (axis) {
        case X:
            return x;
        case Y:
            return y;
        case Z:
            return z;
        default:
            throw new IllegalArgumentException("Unknown axis " + axis);
        }
    }

    public void set(Axis axis, int value) {
        switch (axis) {
        case X:
            x = value;
            break;
        case Y:
            y = value;
            break;
        case Z:
            z = value;
            break;
        default:
            throw new IllegalArgumentException("Unknown axis " + axis);
        }
    }

    public int getVolume() {
        return x * y * z;
    }

    public double getPercentVolume(LittleGrid grid) {
        return getVolume() / (double) grid.count3d;
    }

    public LittleVec calculateInvertedCenter() {
        return new LittleVec((int) Math.ceil(x / 2D), (int) Math.ceil(y / 2D), (int) Math.ceil(z / 2D));
    }

    public LittleVec calculateCenter() {
        return new LittleVec((int) Math.floor(x / 2D), (int) Math.floor(y / 2D), (int) Math.floor(z / 2D));
    }

    public LittleVec max(LittleVec size) {
        x = Math.max(x, size.x);
        y = Math.max(y, size.y);
        z = Math.max(z, size.z);
        return this;
    }

    public Axis getLongestAxis() {
        if (Math.abs(x) > Math.abs(y))
            return Math.abs(x) > Math.abs(z) ? Axis.X : Axis.Z;
        return Math.abs(z) > Math.abs(y) ? Axis.Z : Axis.Y;
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "," + z + "]";
    }
}
