package io.github.hawah.shakenstir.client.render.block.renderstate;

import io.github.hawah.shakenstir.util.IModel;
import org.joml.Vector2f;

public class GlasswareBlockEntityRenderState extends ShakeBlockEntityRenderState{
    public final Vector2f position = new Vector2f();
    public float rotate = 0;
    public IModel<?> model;
    public float height = 0;
}
