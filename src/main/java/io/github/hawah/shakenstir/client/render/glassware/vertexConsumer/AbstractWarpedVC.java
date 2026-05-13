package io.github.hawah.shakenstir.client.render.glassware.vertexConsumer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public abstract class AbstractWarpedVC implements VertexConsumer {
    protected VertexConsumer source;

    public AbstractWarpedVC() {
    }

    public VertexConsumer warp(VertexConsumer source) {
        this.source = source;
        return this;
    }

    public AbstractWarpedVC(VertexConsumer source) {
        this.source = source;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        source.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        source.setColor(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        source.setColor(color);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        source.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        source.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        source.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        source.setNormal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        source.setLineWidth(width);
        return this;
    }
}
