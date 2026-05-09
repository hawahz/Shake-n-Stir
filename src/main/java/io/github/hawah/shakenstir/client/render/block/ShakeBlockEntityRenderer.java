package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.client.render.block.renderstate.ShakeBlockEntityRenderState;
import io.github.hawah.shakenstir.content.blockEntity.ShakeBlockEntity;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ShakeBlockEntityRenderer implements BlockEntityRenderer<ShakeBlockEntity, ShakeBlockEntityRenderState> {

    final ItemModelResolver itemModelResolver;

    public ShakeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ShakeBlockEntityRenderState createRenderState() {
        return new ShakeBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(ShakeBlockEntity blockEntity, ShakeBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        NonNullList<ItemStack> itemToRender = blockEntity.getItemToRender();
        int seed = HashCommon.long2int(blockEntity.getBlockPos().asLong());
        for (int slot = 0; slot < itemToRender.size(); slot++) {
            ItemStack itemStack = itemToRender.get(slot);
            if (!itemStack.isEmpty()) {
                ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                this.itemModelResolver
                        .updateForTopItem(itemStackRenderState, itemStack, ItemDisplayContext.NONE, blockEntity.getLevel(), blockEntity, seed + slot);
                state.items[slot] = itemStackRenderState;
            }
        }
        state.liquidHeight = Mth.lerp(partialTicks, blockEntity.oAnimationHeight, blockEntity.animationHeight);
    }

    @Override
    public void submit(ShakeBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        submitContents(state, poseStack, submitNodeCollector);
        renderLiquidPlane(state, poseStack);
    }

    private static void renderLiquidPlane(ShakeBlockEntityRenderState state, PoseStack poseStack) {
        if (state.liquidHeight <= 0.01) {
            return;
        }
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.debugQuads());
        final float HEIGHT = 7/16f;
        final float BOTTOM = 1/16f;
        final int COLOR = ARGB.color(Mth.clamp(100, 0, 255), 160, 216, 239);

        float height = Mth.lerp(state.liquidHeight, BOTTOM, HEIGHT);
        consumer.addVertex(poseStack.last(), 3/8f, height, 3/8f)
                .setColor(COLOR);
        consumer.addVertex(poseStack.last(), 3/8f, height, 5/8f)
                .setColor(COLOR);
        consumer.addVertex(poseStack.last(), 5/8f, height, 5/8f)
                .setColor(COLOR);
        consumer.addVertex(poseStack.last(), 5/8f, height, 3/8f)
                .setColor(COLOR);
        bufferSource.endBatch();
    }

    private void submitContents(ShakeBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        for (int slot = 0; slot < state.items.length; slot++) {
            ItemStackRenderState itemStackRenderState = state.items[slot];
            if (itemStackRenderState != null) {
                this.submitItem(state, itemStackRenderState, poseStack, submitNodeCollector, slot);
            }
        }
    }

    private void submitItem(
            ShakeBlockEntityRenderState state, ItemStackRenderState itemStackRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int slot
    ) {
        double offsetY = 1/16.0 * 0.5;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.translate(0, -(0.5 - 1/16f) + offsetY * slot, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZN.rotationDegrees(slot * 100));
        float scale = 0.25F;
        poseStack.scale(scale, scale, scale);
        itemStackRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
