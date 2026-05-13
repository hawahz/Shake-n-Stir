package io.github.hawah.shakenstir.client.render.block.renderstate;

import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.util.IModel;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class GlasswareBlockEntityRenderState extends ShakeBlockEntityRenderState{
    public final Vector2f position = new Vector2f();
    public final List<GlasswareBlockEntity.Decoration> decorations = new ArrayList<>();
    public float rotate = 0;
    public IModel<?> model;
    public float height = 0;
}
