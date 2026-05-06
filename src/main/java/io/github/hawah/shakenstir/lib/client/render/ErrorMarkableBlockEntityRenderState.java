package io.github.hawah.shakenstir.lib.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public abstract class ErrorMarkableBlockEntityRenderState extends BlockEntityRenderState {
    protected boolean error = false;
    public void markError() {
        error = true;
    }
    public boolean error() {
        return error;
    }
}
