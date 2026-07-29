package team.creative.littletiles.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.LittleTile;

public class TilesRenderer extends TileEntityRenderer<TETiles> {

    public TilesRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TETiles blockEntity, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int packedLight, int packedOverlay) {
        LittleGrid grid = blockEntity.getGrid();
        for (Pair<LittleTile, LittleBox> pair : blockEntity.getTiles().boxes())
            renderBox(pair.key, pair.value, grid, stack, buffers, packedLight, packedOverlay);
    }

    private static void renderBox(LittleTile tile, LittleBox box, LittleGrid grid, MatrixStack stack, IRenderTypeBuffer buffers, int packedLight,
            int packedOverlay) {
        float sizeX = (float) grid.toVanillaGrid(box.maxX - box.minX);
        float sizeY = (float) grid.toVanillaGrid(box.maxY - box.minY);
        float sizeZ = (float) grid.toVanillaGrid(box.maxZ - box.minZ);
        stack.pushPose();
        stack.translate(grid.toVanillaGrid(box.minX), grid.toVanillaGrid(box.minY), grid.toVanillaGrid(box.minZ));
        stack.scale(sizeX, sizeY, sizeZ);
        IRenderTypeBuffer tileBuffers = buffers;
        if (tile.hasColor()) {
            boolean translucent = (tile.color >>> 24 & 255) < 255;
            tileBuffers = type -> new ColorVertexBuilder(buffers.getBuffer(translucent ? RenderType.translucent() : type), tile.color);
        }
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(tile.getState(), stack, tileBuffers, packedLight, packedOverlay);
        stack.popPose();
    }
}
