package io.github.hawah.shakenstir.client.render;

import io.github.hawah.shakenstir.util.IModel;

public interface IGlasswareRenderState {
    float height();

    IModel<?> model();

    int color();

}
