package team.creative.littletiles.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleFaceCulling;
import team.creative.littletiles.common.tile.LittleTile;

public class TilesRenderer extends TileEntityRenderer<TETiles> {

    public TilesRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TETiles blockEntity, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int packedLight, int packedOverlay) {
        LittleGrid grid = blockEntity.getGrid();
        for (Pair<LittleTile, LittleBox> pair : blockEntity.getTiles().boxes())
            renderBox(blockEntity, pair.key, pair.value, grid, stack, buffers, packedLight, packedOverlay);
    }

    private static void renderBox(TETiles blockEntity, LittleTile tile, LittleBox box, LittleGrid grid, MatrixStack stack, IRenderTypeBuffer buffers,
            int packedLight, int packedOverlay) {
        BlockState state = tile.getState();
        IBakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        int alpha = tile.color >>> 24 & 255;
        RenderType renderType = alpha < 255 ? RenderType.translucent() : RenderTypeLookup.getChunkRenderType(state);
        IVertexBuilder builder = buffers.getBuffer(renderType);
        if (tile.hasColor())
            builder = new ColorVertexBuilder(builder, tile.color);

        float minX = grid.toVanillaGrid((float) box.minX);
        float minY = grid.toVanillaGrid((float) box.minY);
        float minZ = grid.toVanillaGrid((float) box.minZ);
        float maxX = grid.toVanillaGrid((float) box.maxX);
        float maxY = grid.toVanillaGrid((float) box.maxY);
        float maxZ = grid.toVanillaGrid((float) box.maxZ);
        MatrixStack.Entry matrix = stack.last();

        for (Direction face : Direction.values()) {
            if (LittleFaceCulling.isCovered(box, face, blockEntity.getTiles()))
                continue;
            int faceLight = packedLight;
            if (blockEntity.getLevel() != null && isOuterFace(box, face, grid))
                faceLight = WorldRenderer.getLightColor(blockEntity.getLevel(), state, blockEntity.getBlockPos().relative(face));
            for (FaceData data : faceData(model, state, face)) {
                float shade = blockEntity.getLevel() != null ? blockEntity.getLevel().getShade(face, data.shade) : shade(face);
                int color = data.tintIndex >= 0 && blockEntity.getLevel() != null
                        ? Minecraft.getInstance().getBlockColors().getColor(state, blockEntity.getLevel(), blockEntity.getBlockPos(), data.tintIndex)
                        : 0xFFFFFF;
                if (color == -1)
                    color = 0xFFFFFF;
                int red = (int) ((color >> 16 & 255) * shade);
                int green = (int) ((color >> 8 & 255) * shade);
                int blue = (int) ((color & 255) * shade);
                renderFace(builder, matrix, face, data.sprite, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, faceLight, packedOverlay);
            }
        }
    }

    static boolean isOuterFace(LittleBox box, Direction face, LittleGrid grid) {
        switch (face) {
        case DOWN:
            return box.minY == 0;
        case UP:
            return box.maxY == grid.count;
        case NORTH:
            return box.minZ == 0;
        case SOUTH:
            return box.maxZ == grid.count;
        case WEST:
            return box.minX == 0;
        case EAST:
            return box.maxX == grid.count;
        default:
            return false;
        }
    }

    private static List<FaceData> faceData(IBakedModel model, BlockState state, Direction face) {
        Random random = new Random(42L);
        List<BakedQuad> quads = new ArrayList<>(model.getQuads(state, face, random));
        if (quads.isEmpty()) {
            random.setSeed(42L);
            for (BakedQuad quad : model.getQuads(state, null, random))
                if (quad.getDirection() == face)
                    quads.add(quad);
        }
        List<FaceData> data = new ArrayList<>(Math.max(1, quads.size()));
        for (BakedQuad quad : quads)
            data.add(new FaceData(quad.getSprite(), quad.isTinted() ? quad.getTintIndex() : -1, quad.isShade()));
        if (data.isEmpty())
            data.add(new FaceData(model.getParticleIcon(), -1, true));
        return data;
    }

    private static float shade(Direction face) {
        switch (face) {
        case DOWN:
            return 0.5F;
        case NORTH:
        case SOUTH:
            return 0.8F;
        case EAST:
        case WEST:
            return 0.6F;
        default:
            return 1.0F;
        }
    }

    private static void renderFace(IVertexBuilder builder, MatrixStack.Entry matrix, Direction face, TextureAtlasSprite sprite, float x0, float y0,
            float z0, float x1, float y1, float z1, int red, int green, int blue, int light, int overlay) {
        switch (face) {
        case DOWN:
            vertex(builder, matrix, sprite, x0, y0, z1, x0, z1, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x0, y0, z0, x0, z0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y0, z0, x1, z0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y0, z1, x1, z1, face, red, green, blue, light, overlay);
            break;
        case UP:
            vertex(builder, matrix, sprite, x0, y1, z0, x0, z0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x0, y1, z1, x0, z1, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y1, z1, x1, z1, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y1, z0, x1, z0, face, red, green, blue, light, overlay);
            break;
        case NORTH:
            vertex(builder, matrix, sprite, x1, y1, z0, x1, 1 - y1, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y0, z0, x1, 1 - y0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x0, y0, z0, x0, 1 - y0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x0, y1, z0, x0, 1 - y1, face, red, green, blue, light, overlay);
            break;
        case SOUTH:
            vertex(builder, matrix, sprite, x0, y1, z1, x0, 1 - y1, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x0, y0, z1, x0, 1 - y0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y0, z1, x1, 1 - y0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y1, z1, x1, 1 - y1, face, red, green, blue, light, overlay);
            break;
        case WEST:
            vertex(builder, matrix, sprite, x0, y1, z0, z0, 1 - y1, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x0, y0, z0, z0, 1 - y0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x0, y0, z1, z1, 1 - y0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x0, y1, z1, z1, 1 - y1, face, red, green, blue, light, overlay);
            break;
        case EAST:
            vertex(builder, matrix, sprite, x1, y1, z1, z1, 1 - y1, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y0, z1, z1, 1 - y0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y0, z0, z0, 1 - y0, face, red, green, blue, light, overlay);
            vertex(builder, matrix, sprite, x1, y1, z0, z0, 1 - y1, face, red, green, blue, light, overlay);
            break;
        }
    }

    private static void vertex(IVertexBuilder builder, MatrixStack.Entry matrix, TextureAtlasSprite sprite, float x, float y, float z, float u,
            float v, Direction normal, int red, int green, int blue, int light, int overlay) {
        builder.vertex(matrix.pose(), x, y, z).color(red, green, blue, 255).uv(sprite.getU(u * 16), sprite.getV(v * 16)).overlayCoords(overlay)
                .uv2(light).normal(matrix.normal(), normal.getStepX(), normal.getStepY(), normal.getStepZ()).endVertex();
    }

    private static class FaceData {
        final TextureAtlasSprite sprite;
        final int tintIndex;
        final boolean shade;

        FaceData(TextureAtlasSprite sprite, int tintIndex, boolean shade) {
            this.sprite = sprite;
            this.tintIndex = tintIndex;
            this.shade = shade;
        }
    }
}
