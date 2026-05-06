package io.github.hawah.shakenstir.client.render.block.renderstate;

import io.github.hawah.shakenstir.content.blockEntity.ShakeBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class ShakeBlockEntityRenderState extends BlockEntityRenderState {
    public final @Nullable ItemStackRenderState[] items = new ItemStackRenderState[ShakeBlockEntity.MAX_HOLD_ITEMS];
    public Vector3f color = new Vector3f(1, 1, 1);
}
