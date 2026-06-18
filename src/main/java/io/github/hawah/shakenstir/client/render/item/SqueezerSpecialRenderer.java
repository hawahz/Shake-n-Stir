package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SingleItemComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public record SqueezerSpecialRenderer(ItemModelResolver itemModelResolver) implements SpecialModelRenderer<SqueezerSpecialRenderer.RenderState> {

    @Override
    public void submit(SqueezerSpecialRenderer. @Nullable RenderState argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (argument == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.8000001f, 0.35F, 0.1f);
        poseStack.mulPose(new Quaternionf().rotateLocalX(1.5000002f).rotateLocalY( 0.0f).rotateLocalZ( 0.0f));
        poseStack.scale(0.4665073f, 0.4665073f, 0.4665073f);

        argument.fruit().submit(
                poseStack,
                submitNodeCollector,
                lightCoords,
                overlayCoords,
                outlineColor
        );

        poseStack.popPose();

    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {

    }

    @Override
    public RenderState extractArgument(ItemStack stack) {
        ItemStack fruit = stack.getOrDefault(DataComponentTypeRegistries.SQUEEZER_HOLDER, SingleItemComponent.EMPTY).itemStack();
        ItemStackRenderState state = new ItemStackRenderState();
        itemModelResolver().updateForTopItem(
                state,
                fruit,
                ItemDisplayContext.FIXED,
                null,
                null,
                0
        );
        return new RenderState(state);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<SqueezerSpecialRenderer.RenderState> {
        public static final MapCodec<SqueezerSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new SqueezerSpecialRenderer.Unbaked());
        @Override
        public SqueezerSpecialRenderer bake(BakingContext context) {
            return new SqueezerSpecialRenderer(Minecraft.getInstance().getItemModelResolver());
        }

        @Override
        public MapCodec<SqueezerSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record RenderState(
            ItemStackRenderState fruit
    ) { }
}
