package io.github.hawah.shakenstir.client.render.gui;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jspecify.annotations.Nullable;

public class MenuRenderState implements PictureInPictureRenderState {
    @Override
    public int x0() {
        return 0;
    }

    @Override
    public int x1() {
        return 256;
    }

    @Override
    public int y0() {
        return 0;
    }

    @Override
    public int y1() {
        return 256;
    }

    @Override
    public float scale() {
        return 1;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return null;
    }
}
