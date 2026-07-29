package team.creative.littletiles.client.render;

import com.mojang.blaze3d.vertex.IVertexBuilder;

public class ColorVertexBuilder implements IVertexBuilder {

    private final IVertexBuilder delegate;
    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    public ColorVertexBuilder(IVertexBuilder delegate, int color) {
        this.delegate = delegate;
        this.alpha = color >>> 24 & 255;
        this.red = color >>> 16 & 255;
        this.green = color >>> 8 & 255;
        this.blue = color & 255;
    }

    public static int multiply(int value, int tint) {
        return value * tint / 255;
    }

    @Override
    public IVertexBuilder vertex(double x, double y, double z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public IVertexBuilder color(int red, int green, int blue, int alpha) {
        delegate.color(multiply(red, this.red), multiply(green, this.green), multiply(blue, this.blue), multiply(alpha, this.alpha));
        return this;
    }

    @Override
    public IVertexBuilder uv(float u, float v) {
        delegate.uv(u, v);
        return this;
    }

    @Override
    public IVertexBuilder overlayCoords(int u, int v) {
        delegate.overlayCoords(u, v);
        return this;
    }

    @Override
    public IVertexBuilder uv2(int u, int v) {
        delegate.uv2(u, v);
        return this;
    }

    @Override
    public IVertexBuilder normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }
}
