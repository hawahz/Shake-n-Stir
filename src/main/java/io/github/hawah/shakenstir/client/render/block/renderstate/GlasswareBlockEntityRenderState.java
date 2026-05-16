package io.github.hawah.shakenstir.client.render.block.renderstate;

import io.github.hawah.shakenstir.client.render.IGlasswareRenderState;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.util.IModel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class GlasswareBlockEntityRenderState extends BlockEntityRenderState implements IGlasswareRenderState {
    public final Vector2f position = new Vector2f();
    public final List<GlasswareBlockEntity.Decoration> decorations = new ArrayList<>();
    public float rotate = 0;
    public IModel<?> model;
    public int color = 0xFFFFFFFF;
    public float height = 0;

    @Override
    public float height() {
        return height;
    }

    @Override
    public IModel<?> model() {
        return model;
    }

    @Override
    public int color() {
        return color;
    }

    @Override
    public Iterable<? extends GlasswareBlockEntity.Decoration> decorations() {
        return decorations;
    }

}
