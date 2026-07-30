package team.creative.littletiles.common.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import team.creative.littletiles.LittleTiles;

@Mod.EventBusSubscriber(modid = LittleTiles.MODID)
public final class LittleToolEventHandler {

    private LittleToolEventHandler() {}

    @SubscribeEvent
    public static void leftClick(LeftClickBlock event) {
        ItemStack stack = event.getPlayer().getMainHandItem();
        if (!(stack.getItem() instanceof ItemLittleHammer) || event.getFace() == null)
            return;

        Vector3d hit = Vector3d.atCenterOf(event.getPos());
        RayTraceResult trace = event.getPlayer().pick(event.getPlayer().isCreative() ? 5.0D : 4.5D, 1.0F, false);
        if (trace instanceof BlockRayTraceResult) {
            BlockRayTraceResult blockTrace = (BlockRayTraceResult) trace;
            if (blockTrace.getBlockPos().equals(event.getPos()))
                hit = blockTrace.getLocation();
        }
        if (!event.getWorld().isClientSide)
            event.setCanceled(true);
        ItemLittleHammer.click(event.getWorld(), event.getPlayer(), stack, event.getPos(), event.getFace(), hit);
    }
}
