package team.creative.littletiles.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import team.creative.littletiles.common.api.block.LittleBlock;

public class LittleMissingBlock extends LittleBlock {

    private final String name;

    public LittleMissingBlock(String name) {
        this.name = name;
    }

    @Override
    public boolean isTranslucent() {
        return false;
    }

    @Override
    public boolean is(Block block) {
        return false;
    }

    @Override
    public Block getBlock() {
        return Blocks.AIR;
    }

    @Override
    public String blockName() {
        return name;
    }
}
