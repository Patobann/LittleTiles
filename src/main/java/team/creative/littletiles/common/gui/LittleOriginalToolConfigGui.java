package team.creative.littletiles.common.gui;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.GuiButtonFixed;
import team.creative.creativecore.common.gui.controls.GuiComboBox;
import team.creative.creativecore.common.gui.controls.GuiLabel;
import team.creative.creativecore.common.gui.controls.GuiSlider;
import team.creative.creativecore.common.gui.controls.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.GuiStateButton;
import team.creative.creativecore.common.util.text.TextListBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.LittlePlacementMode;
import team.creative.littletiles.common.item.LittleToolConfigPacket;
import team.creative.littletiles.common.item.LittleToolGrid;
import team.creative.littletiles.common.item.LittleToolShape;

/**
 * 1.16 recreation of the original SubGuiChisel layout: tint, material,
 * shape and the tool placement settings in one screen.
 */
public class LittleOriginalToolConfigGui extends GuiLayer {

    private final PlayerEntity player;

    public LittleOriginalToolConfigGui(PlayerEntity player) {
        super("littletiles_tool", 190, 218);
        this.player = player;
    }

    @Override
    public void create() {
        ItemStack held = player.getMainHandItem();
        boolean chisel = held.getItem() instanceof ItemLittleChisel;

        if (chisel) {
            add(new GuiLabel("title", 4, 4).setTitle(new StringTextComponent("Little Chisel")));
            addColorControls(held);

            add(new GuiLabel("material_label", 4, 75).setTitle(new StringTextComponent("Material")));
            GuiStackSelector material = new GuiStackSelector("material", 4, 85, 182, player,
                    new GuiStackSelector.CreativeCollector(new GuiStackSelector.GuiBlockSelector()), true);
            BlockState selected = ItemLittleChisel.getSelectedState(held);
            if (!selected.isAir())
                material.setSelectedForce(new ItemStack(selected.getBlock()));
            add(material);

            add(new GuiLabel("shape_label", 4, 106).setTitle(new StringTextComponent("Shape")));
            GuiComboBox shape = new GuiComboBox("shape", 48, 103,
                    new TextListBuilder().add(LittleToolShape.titles()));
            shape.setWidth(138);
            shape.setHeight(18);
            shape.select(LittleToolShape.get(held).ordinal());
            add(shape);
        } else {
            add(new GuiLabel("title", 4, 4).setTitle(new StringTextComponent("Little Hammer")));
        }

        add(new GuiLabel("settings_label", 4, 130).setTitle(new StringTextComponent("Placement settings")));
        add(new GuiLabel("grid_label", 4, 145).setTitle(new StringTextComponent("Grid")));
        add(new GuiStateButton("grid", 74, 142, gridIndex(LittleToolGrid.get(held)),
                LittleGrid.names().toArray(new String[0])));

        if (chisel) {
            add(new GuiLabel("mode_label", 4, 166).setTitle(new StringTextComponent("Mode")));
            add(new GuiStateButton("placement", 74, 163, LittlePlacementMode.get(held).ordinal(), "normal", "fill"));
        }

        add(new GuiButtonFixed("cancel", 4, 193, 82, 18, button -> closeTopLayer())
                .setTitle(new StringTextComponent("Cancel")));
        add(new GuiButtonFixed("save", 104, 193, 82, 18, button -> save(held, chisel))
                .setTitle(new StringTextComponent("Save")));
    }

    private void addColorControls(ItemStack held) {
        int color = ItemLittleChisel.getSelectedColor(held);
        add(new GuiLabel("color_label", 4, 18).setTitle(new StringTextComponent("Color")));
        addColorSlider("red", "R", 28, color >> 16 & 255);
        addColorSlider("green", "G", 40, color >> 8 & 255);
        addColorSlider("blue", "B", 52, color & 255);
        addColorSlider("alpha", "A", 64, color >>> 24 & 255);
    }

    private void addColorSlider(String name, String label, int y, int value) {
        add(new GuiLabel(name + "_label", 4, y + 1).setTitle(new StringTextComponent(label)));
        add(new GuiSlider(name, 18, y, 168, 11, value, 0, 255));
    }

    private void save(ItemStack held, boolean chisel) {
        int grid = LittleGrid.grids[((GuiStateButton) get("grid")).getState()].count;
        int placement = chisel ? ((GuiStateButton) get("placement")).getState() : 0;
        if (!chisel) {
            LittleToolConfigPacket.apply(held, grid, placement);
            LittleTiles.NETWORK.sendToServer(new LittleToolConfigPacket(grid, LittlePlacementMode.NORMAL));
            closeTopLayer();
            return;
        }

        BlockState material = ItemLittleChisel.getSelectedState(held);
        ItemStack selectedStack = ((GuiStackSelector) get("material")).getSelected();
        Block selectedBlock = Block.byItem(selectedStack.getItem());
        if (material.isAir() || material.getBlock() != selectedBlock)
            material = selectedBlock.defaultBlockState();

        int color = color();
        LittleToolShape shape = LittleToolShape.byIndex(((GuiComboBox) get("shape")).getIndex());
        LittlePlacementMode mode = LittlePlacementMode.byIndex(placement);
        LittleToolConfigPacket.apply(held, grid, placement, material, color, shape.ordinal());
        LittleTiles.NETWORK.sendToServer(new LittleToolConfigPacket(grid, mode, material, color, shape));
        closeTopLayer();
    }

    private int color() {
        int red = (int) Math.round(((GuiSlider) get("red")).value);
        int green = (int) Math.round(((GuiSlider) get("green")).value);
        int blue = (int) Math.round(((GuiSlider) get("blue")).value);
        int alpha = (int) Math.round(((GuiSlider) get("alpha")).value);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int gridIndex(LittleGrid selected) {
        for (int i = 0; i < LittleGrid.grids.length; i++)
            if (LittleGrid.grids[i] == selected)
                return i;
        return LittleGrid.overallDefaultIndex;
    }
}
