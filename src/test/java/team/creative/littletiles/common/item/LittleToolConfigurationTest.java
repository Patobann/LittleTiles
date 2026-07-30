package team.creative.littletiles.common.item;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittleToolConfigurationTest {

    @Before
    public void resetGridConfiguration() {
        LittleGrid.loadGrid(1, 8, 2, LittleGrid.OVERALL_DEFAULT);
    }

    @Test
    public void appliesOriginalChiselConfigurationFields() {
        ItemStack chisel = new ItemStack(new ItemLittleChisel(new Item.Properties()));
        int tint = 0x804080C0;

        LittleToolConfigPacket.apply(chisel, 1, LittlePlacementMode.FILL.ordinal(),
                Blocks.STONE.defaultBlockState(), tint, LittleToolShape.CYLINDER.ordinal());

        assertEquals(1, LittleToolGrid.get(chisel).count);
        assertEquals(LittlePlacementMode.FILL, LittlePlacementMode.get(chisel));
        assertEquals(Blocks.STONE.defaultBlockState(), ItemLittleChisel.getSelectedState(chisel));
        assertEquals(tint, ItemLittleChisel.getSelectedColor(chisel));
        assertEquals(LittleToolShape.CYLINDER, LittleToolShape.get(chisel));
    }

    @Test
    public void actionPacketCarriesPlacementModeFromSecondClick() {
        LittleToolSelection.Area area = new LittleToolSelection.Area(net.minecraft.util.math.BlockPos.ZERO,
                LittleGrid.get(16), new team.creative.littletiles.common.math.box.LittleBox(0, 0, 0, 1, 1, 1));

        LittleToolActionPacket packet = new LittleToolActionPacket(LittleToolActionPacket.CHISEL, area,
                net.minecraft.util.Direction.UP, LittlePlacementMode.FILL);

        assertEquals(LittlePlacementMode.FILL.ordinal(), packet.placement);
    }
}
