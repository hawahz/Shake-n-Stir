package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.client.model.Models;
import io.github.hawah.shakenstir.client.render.IGlasswareRenderState;
import io.github.hawah.shakenstir.client.render.block.GlasswareBlockEntityRenderer;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.util.IModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GlasswareSpecialRenderer implements SpecialModelRenderer<GlasswareSpecialRenderer.RenderState> {
    @Override
    public void submit(@Nullable RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        float globalScale = 0.5f;
        poseStack.translate(0.5, 0.7, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees((float) (180 + state.rotate)));
        poseStack.scale(globalScale, globalScale, globalScale);
        poseStack.translate(-0.5, -0.5, -0.5);

        GlasswareBlockEntityRenderer.submitGlassware(state, poseStack, submitNodeCollector, lightCoords, 0.5F, 0.5F, 0, false);


        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
    }



    @Override
    public @Nullable RenderState extractArgument(ItemStack stack) {
        RenderState renderState = new RenderState();
        renderState.model = Models.getModel(stack.get(DataComponents.ITEM_MODEL)).orElse(null);
        renderState.height = stack.has(DataComponentTypeRegistries.DRINK_DATA)? 1: 0;
        renderState.color = stack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb() | 0xFF000000;
        renderState.decorations.addAll(stack.getOrDefault(DataComponentTypeRegistries.GLASSWARE_DECORATIONS, List.of()));
        renderState.rotate = stack.getOrDefault(DataComponentTypeRegistries.GLASSWARE_ROTATION, 0F);
        return renderState;
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<RenderState> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());


        @Override
        public @Nullable GlasswareSpecialRenderer bake(BakingContext context) {
            return new GlasswareSpecialRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<RenderState>> type() {
            return MAP_CODEC;
        }
    }

    public static class RenderState implements IGlasswareRenderState {
        public final List<GlasswareBlockEntity.Decoration> decorations = new ArrayList<>();
        public IModel<?> model;
        public float height = 0;
        public double rotate = 0;
        public int color = 0xFFFFFFFF;

        @Override
        public float height() {
            return height;
        }

        @Override
        public IModel<?> model() {
            return model;
        }

        @Override
        public int color() {
            return color;
        }

        @Override
        public Iterable<? extends GlasswareBlockEntity.Decoration> decorations() {
            return decorations;
        }

    }
}
