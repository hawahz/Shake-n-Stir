package io.github.hawah.shakenstir.client.model.glassware;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.util.IModel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import org.joml.Vector3dc;

import java.util.List;

public record GlasswareQuadCollection(QuadCollection holder, Vector3dc start, Vector3dc end) implements IModel.QuadProvider {
    public static final ContextKey<Vector3dc> FLUID_START = ShakenStir.asContextKey("fluid_start");
    public static final ContextKey<Vector3dc> FLUID_END = ShakenStir.asContextKey("fluid_end");

    public static SimpleUnbakedStandaloneModel<GlasswareQuadCollection> collect(Identifier modelId) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId,
                (model, baker, name) ->
                    ((GlasswareUnbakedGeometry) model.getTopGeometry())
                            .bakeGlassware(model.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY, model, model.getTopAdditionalProperties())

        );
    }

    @Override
    public List<BakedQuad> getAll() {
        return holder().getAll();
    }
}
