package team.creative.littletiles.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.TilesRenderer;

@Mod.EventBusSubscriber(modid = LittleTiles.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LittleTilesClient {

    private LittleTilesClient() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(LittleTiles.TILES_TE_TYPE.get(), TilesRenderer::new);
        LittleTiles.LOGGER.info("LittleTiles block entity renderer registered");
    }
}
