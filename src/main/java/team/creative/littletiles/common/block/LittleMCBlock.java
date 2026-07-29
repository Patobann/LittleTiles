package team.creative.littletiles.common.block;

import java.util.Objects;

import net.minecraft.block.Block;
import team.creative.littletiles.common.api.block.LittleBlock;

public class LittleMCBlock extends LittleBlock {

    public final Block block;
    private final boolean translucent;

    public LittleMCBlock(Block block) {
        this.block = Objects.requireNonNull(block, "block");
        this.translucent = !block.defaultBlockState().canOcclude();
    }

    @Override
    public boolean isTranslucent() {
        return translucent;
    }

    @Override
    public boolean is(Block block) {
        return this.block == block;
    }

    @Override
    public Block getBlock() {
        return block;
    }
}
