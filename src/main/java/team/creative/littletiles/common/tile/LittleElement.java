package team.creative.littletiles.common.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.LittleBlockRegistry;
import team.creative.littletiles.common.block.LittleMissingBlock;

/** Block state and tint shared by one or more little boxes. */
public class LittleElement {

    private BlockState state;
    private LittleBlock block;
    public int color;

    public LittleElement(BlockState state, int color) {
        setState(state);
        this.color = color;
    }

    public LittleElement(String stateName, int color) {
        this.state = LittleBlockRegistry.loadState(stateName);
        this.block = LittleBlockRegistry.getLittleBlock(stateName);
        this.color = color;
    }

    public LittleElement(CompoundNBT nbt) {
        this(readStateName(nbt), nbt.contains("c", 3) ? nbt.getInt("c") : nbt.contains("color", 3) ? nbt.getInt("color") : ColorUtils.WHITE);
    }

    public LittleElement(LittleElement element) {
        this.state = element.state;
        this.block = element.block;
        this.color = element.color;
    }

    public BlockState getState() {
        return state;
    }

    public LittleBlock getBlock() {
        return block;
    }

    public void setState(BlockState state) {
        this.state = state;
        this.block = LittleBlockRegistry.getLittleBlock(state.getBlock());
    }

    public String getBlockName() {
        if (block instanceof LittleMissingBlock)
            return block.blockName();
        return LittleBlockRegistry.saveState(state);
    }

    public boolean is(Block block) {
        return this.block.is(block);
    }

    public boolean isTranslucent() {
        return block.isTranslucent() || ColorUtils.isTransparent(color);
    }

    public boolean hasColor() {
        return color != ColorUtils.WHITE;
    }

    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putString("s", getBlockName());
        nbt.putInt("c", color);
        return nbt;
    }

    private static String readStateName(CompoundNBT nbt) {
        if (nbt.contains("s", 8))
            return nbt.getString("s");
        if (nbt.contains("block", 8))
            return nbt.getString("block");
        return "minecraft:air";
    }

    @Override
    public int hashCode() {
        return 31 * state.hashCode() + color;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LittleElement))
            return false;
        LittleElement other = (LittleElement) object;
        return state == other.state && block.blockName().equals(other.block.blockName()) && color == other.color;
    }

    @Override
    public String toString() {
        return "[" + getBlockName() + "|" + color + "]";
    }
}
