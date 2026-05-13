package io.github.hawah.shakenstir.client.model;

import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GlasswareUnbakedModel extends AbstractUnbakedModel {
    private final GlasswareUnbakedGeometry geometry;
    private final GlasswareRenderParameters params;

    protected GlasswareUnbakedModel(StandardModelParameters parameters, GlasswareUnbakedGeometry geometry, GlasswareRenderParameters params) {
        super(parameters);
        this.geometry = geometry;
        this.params = params;
    }

    @Override
    public @Nullable UnbakedGeometry geometry() {
        return geometry;
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        super.fillAdditionalProperties(propertiesBuilder);
        propertiesBuilder.withParameter(GlasswareQuadCollection.FLUID_START,    params.min());
        propertiesBuilder.withParameter(GlasswareQuadCollection.FLUID_END,      params.max());
    }
}
