package team.creative.littletiles.common.api.block;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public abstract class LittleBlock {

    public abstract boolean isTranslucent();

    public abstract boolean is(Block block);

    public abstract Block getBlock();

    public String blockName() {
        ResourceLocation name = getBlock().getRegistryName();
        return name != null ? name.toString() : "minecraft:air";
    }
}
