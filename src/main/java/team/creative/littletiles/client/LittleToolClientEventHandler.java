package team.creative.littletiles.client;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.LittleToolSelection;

@Mod.EventBusSubscriber(modid = LittleTiles.MODID, value = Dist.CLIENT)
public final class LittleToolClientEventHandler {

    private LittleToolClientEventHandler() {}

    @SubscribeEvent
    public static void rightClickEmpty(RightClickEmpty event) {
        ItemStack held = event.getPlayer().getMainHandItem();
        if (!(held.getItem() instanceof ItemLittleChisel) || !LittleToolSelection.has(held))
            return;

        RayTraceResult result = Minecraft.getInstance().hitResult;
        Vector3d location = result != null ? result.getLocation()
                : event.getPlayer().getEyePosition(1.0F).add(event.getPlayer().getLookAngle().scale(5.0D));
        ItemLittleChisel.clickAir(event.getPlayer(), held, location);
    }
}
