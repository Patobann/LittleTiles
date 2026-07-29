package team.creative.littletiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.common.Mod;
import team.creative.littletiles.common.block.LittleBlockRegistry;
import team.creative.littletiles.common.tile.LittleElement;

@Mod(LittleTiles.MODID)
public class LittleTiles {
    
    public static final String MODID = "littletiles";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public LittleTiles() {
        BlockState state = Blocks.OAK_STAIRS.defaultBlockState().setValue(HorizontalBlock.FACING, Direction.EAST);
        String serialized = LittleBlockRegistry.saveState(state);
        if (LittleBlockRegistry.loadState(serialized) != state)
            throw new IllegalStateException("Little block-state codec failed for " + serialized);

        LittleElement element = new LittleElement(state, 0x80402010);
        LittleElement loaded = new LittleElement(element.save(new CompoundNBT()));
        if (!element.equals(loaded))
            throw new IllegalStateException("Little element NBT codec failed for " + serialized);

        LOGGER.info("LittleTiles 1.16.5 port bootstrap loaded");
        LOGGER.info("Little block-state codec loaded: {}", serialized);
    }
    
}
