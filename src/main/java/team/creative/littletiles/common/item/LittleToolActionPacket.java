package team.creative.littletiles.common.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.LittleToolSelection.Area;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleToolActionPacket extends CreativePacket {

    static final int CHISEL = 0;
    static final int HAMMER = 1;

    public int action;
    public BlockPos origin;
    public int grid;
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;
    public Direction face;

    public LittleToolActionPacket() {}

    public LittleToolActionPacket(int action, Area area, Direction face) {
        this.action = action;
        this.origin = area.origin;
        this.grid = area.grid.count;
        this.minX = area.box.minX;
        this.minY = area.box.minY;
        this.minZ = area.box.minZ;
        this.maxX = area.box.maxX;
        this.maxY = area.box.maxY;
        this.maxZ = area.box.maxZ;
        this.face = face;
    }

    @Override
    public void executeClient(PlayerEntity player) {}

    @Override
    public void executeServer(PlayerEntity player) {
        ItemStack held = player.getMainHandItem();
        LittleToolSelection.clear(held);
        if (origin == null || face == null || action < CHISEL || action > HAMMER)
            return;

        LittleGrid selectedGrid;
        try {
            selectedGrid = LittleGrid.get(grid);
        } catch (RuntimeException ignored) {
            return;
        }
        LittleBox box = new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
        int limit = selectedGrid.count * 16;
        if (box.maxX <= box.minX || box.maxY <= box.minY || box.maxZ <= box.minZ
                || Math.abs(box.minX) > limit || Math.abs(box.minY) > limit || Math.abs(box.minZ) > limit
                || Math.abs(box.maxX) > limit || Math.abs(box.maxY) > limit || Math.abs(box.maxZ) > limit)
            return;

        Area area = new Area(origin, selectedGrid, box);
        if (action == CHISEL && held.getItem() instanceof ItemLittleChisel)
            ItemLittleChisel.applyArea(player.level, player, held, area, face);
        else if (action == HAMMER && held.getItem() instanceof ItemLittleHammer)
            ItemLittleHammer.applyArea(player.level, player, held, area, face);
    }
}
