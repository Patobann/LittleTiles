package team.creative.littletiles.common.math.box;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.grid.LittleGrid;

/** Tracks the volume discarded while a box is split or clipped. */
public class LittleBoxReturnedVolume {

    private int volume;

    public void addPixel() {
        volume++;
    }

    public void addDifBox(LittleBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int sizeX = (maxX - minX) - box.getSize(Axis.X);
        int sizeY = (maxY - minY) - box.getSize(Axis.Y);
        int sizeZ = (maxZ - minZ) - box.getSize(Axis.Z);
        volume += sizeX * sizeY * sizeZ;
    }

    public void addBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        volume += (maxX - minX) * (maxY - minY) * (maxZ - minZ);
    }

    public boolean has() {
        return volume > 0;
    }

    public int getVolume() {
        return volume;
    }

    public double getPercentVolume(LittleGrid grid) {
        return volume / (double) grid.count3d;
    }

    public void clear() {
        volume = 0;
    }
}
