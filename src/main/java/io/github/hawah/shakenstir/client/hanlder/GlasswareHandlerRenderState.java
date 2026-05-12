package io.github.hawah.shakenstir.client.hanlder;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.client.DeltaTracker;
import net.minecraft.util.context.ContextKey;

public record GlasswareHandlerRenderState(DeltaTracker deltaTracker) {
    public static final ContextKey<GlasswareHandlerRenderState> ctxKey =
            new ContextKey<>(ShakenStir.asResource("glassware_handler"));
}
