package io.github.hawah.shakenstir.client.model.glassware;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import org.joml.Vector3dc;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record GlasswareUnbakedGeometry(
        Either<Identifier, UnbakedModel> warped) implements ExtendedUnbakedGeometry {
    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GlasswareQuadCollection bakeGlassware(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
        Vector3dc start = additionalProperties.getOptional(GlasswareQuadCollection.FLUID_START);
        Vector3dc end = additionalProperties.getOptional(GlasswareQuadCollection.FLUID_END);
        QuadCollection quadCollection = warped().map(identifier -> {
            ResolvedModel model = baker.getModel(identifier);
            return model.bakeTopGeometry(model.getTopTextureSlots(), baker, state);
        }, unbakedModel -> unbakedModel.geometry().bake(textureSlots, baker, state, debugName, additionalProperties));
        return new GlasswareQuadCollection(quadCollection, start, end);
    }
}
