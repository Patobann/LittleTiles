package team.creative.littletiles.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.item.LittleToolGrid;
import team.creative.littletiles.common.item.LittleToolSelection;
import team.creative.littletiles.common.math.LittlePlacementMath;
import team.creative.littletiles.common.math.box.LittleBox;

@Mod.EventBusSubscriber(modid = LittleTiles.MODID, value = Dist.CLIENT)
public final class LittleSelectionRenderer {

    private LittleSelectionRenderer() {}

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null)
            return;

        ItemStack held = minecraft.player.getMainHandItem();
        if (!(held.getItem() instanceof ItemLittleChisel) && !(held.getItem() instanceof ItemLittleHammer))
            return;
        if (!LittleToolSelection.has(held))
            return;

        AxisAlignedBB bounds = null;
        RayTraceResult result = minecraft.hitResult;
        if (result instanceof BlockRayTraceResult) {
            BlockRayTraceResult hit = (BlockRayTraceResult) result;
            boolean inward = held.getItem() instanceof ItemLittleHammer;
            BlockPos target = inward ? hit.getBlockPos()
                    : LittlePlacementMath.outwardBlock(hit.getBlockPos(), hit.getLocation(), hit.getDirection());
            LittleGrid selectedGrid = LittleToolGrid.get(held);
            LittleGrid workingGrid = selectedGrid;
            TileEntity blockEntity = minecraft.level.getBlockEntity(target);
            if (blockEntity instanceof TETiles)
                workingGrid = LittleGrid.max(((TETiles) blockEntity).getGrid(), selectedGrid);
            LittleBox point = LittlePlacementMath.cell(target, hit.getLocation(), hit.getDirection(), workingGrid, selectedGrid, inward);
            bounds = LittleToolSelection.getPreviewBounds(held, target, point, workingGrid, selectedGrid);
        }
        if (bounds == null)
            bounds = LittleToolSelection.getStartBounds(held);
        if (bounds == null)
            return;

        ActiveRenderInfo camera = minecraft.gameRenderer.getMainCamera();
        Vector3d cameraPosition = camera.getPosition();
        MatrixStack matrices = event.getMatrixStack();
        matrices.pushPose();
        matrices.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        IRenderTypeBuffer.Impl buffers = minecraft.renderBuffers().bufferSource();
        WorldRenderer.renderLineBox(matrices, buffers.getBuffer(RenderType.lines()),
                bounds.inflate(0.002D), 1.0F, 1.0F, 1.0F, 1.0F);
        buffers.endBatch(RenderType.lines());
        matrices.popPose();
    }
}
