package team.creative.littletiles.common.item;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittlePlacementModeTest {

    @Before
    public void resetGridConfiguration() {
        LittleGrid.loadGrid(2, 7, 2, LittleGrid.OVERALL_DEFAULT);
    }

    @Test
    public void defaultsToNormalAndPersistsFill() {
        ItemStack chisel = new ItemStack(new ItemLittleChisel(new Item.Properties()));

        assertEquals(LittlePlacementMode.NORMAL, LittlePlacementMode.get(chisel));
        LittlePlacementMode.set(chisel, LittlePlacementMode.FILL);
        assertEquals(LittlePlacementMode.FILL, LittlePlacementMode.get(chisel));
        LittlePlacementMode.set(chisel, LittlePlacementMode.NORMAL);
        assertEquals(LittlePlacementMode.NORMAL, LittlePlacementMode.get(chisel));
    }

    @Test
    public void configAppliesValidatedGridAndPlacement() {
        ItemStack chisel = new ItemStack(new ItemLittleChisel(new Item.Properties()));

        LittleToolConfigPacket.apply(chisel, 32, LittlePlacementMode.FILL.ordinal());

        assertEquals(LittleGrid.get(32), LittleToolGrid.get(chisel));
        assertEquals(LittlePlacementMode.FILL, LittlePlacementMode.get(chisel));
    }
}
