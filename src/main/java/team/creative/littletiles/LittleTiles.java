package team.creative.littletiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;

@Mod(LittleTiles.MODID)
public class LittleTiles {
    
    public static final String MODID = "littletiles";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public LittleTiles() {
        LOGGER.info("LittleTiles 1.16.5 port bootstrap loaded");
    }
    
}
