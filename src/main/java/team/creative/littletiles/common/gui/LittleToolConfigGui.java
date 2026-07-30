package team.creative.littletiles.common.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.GuiButtonFixed;
import team.creative.creativecore.common.gui.controls.GuiLabel;
import team.creative.creativecore.common.gui.controls.GuiStateButton;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.LittlePlacementMode;
import team.creative.littletiles.common.item.LittleToolConfigPacket;
import team.creative.littletiles.common.item.LittleToolGrid;

public class LittleToolConfigGui extends GuiLayer {

    private final PlayerEntity player;

    public LittleToolConfigGui(PlayerEntity player) {
        super("littletiles_tool", 180, 105);
        this.player = player;
    }

    @Override
    public void create() {
        ItemStack held = player.getMainHandItem();
        boolean chisel = held.getItem() instanceof ItemLittleChisel;

        add(new GuiLabel("title", 10, 8).setTitle(new StringTextComponent(chisel ? "Little Chisel" : "Little Hammer")));
        add(new GuiLabel("grid_label", 10, 30).setTitle(new StringTextComponent("Grid")));
        add(new GuiStateButton("grid", 70, 27, gridIndex(LittleToolGrid.get(held)),
                LittleGrid.names().toArray(new String[0])));

        if (chisel) {
            add(new GuiLabel("mode_label", 10, 52).setTitle(new StringTextComponent("Placement")));
            add(new GuiStateButton("placement", 70, 49, LittlePlacementMode.get(held).ordinal(), "normal", "fill"));
        }

        add(new GuiButtonFixed("cancel", 10, 78, 70, 18, button -> closeTopLayer())
                .setTitle(new StringTextComponent("Cancel")));
        add(new GuiButtonFixed("save", 100, 78, 70, 18, button -> save(held, chisel))
                .setTitle(new StringTextComponent("Save")));
    }

    private void save(ItemStack held, boolean chisel) {
        int grid = LittleGrid.grids[((GuiStateButton) get("grid")).getState()].count;
        int placement = chisel ? ((GuiStateButton) get("placement")).getState() : 0;
        LittleToolConfigPacket.apply(held, grid, placement);
        LittleTiles.NETWORK.sendToServer(new LittleToolConfigPacket(grid, LittlePlacementMode.byIndex(placement)));
        closeTopLayer();
    }

    private static int gridIndex(LittleGrid selected) {
        for (int i = 0; i < LittleGrid.grids.length; i++)
            if (LittleGrid.grids[i] == selected)
                return i;
        return LittleGrid.overallDefaultIndex;
    }
}
