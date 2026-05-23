package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.client.render.LiquidRenderer;
import io.github.hawah.shakenstir.client.render.block.renderstate.DistillerBlockEntityRenderState;
import io.github.hawah.shakenstir.content.blockEntity.DistillerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillerBlockEntityRenderer implements BlockEntityRenderer<DistillerBlockEntity, DistillerBlockEntityRenderState> {

    private final ItemModelResolver itemModelResolver;

    public DistillerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public DistillerBlockEntityRenderState createRenderState() {
        return new DistillerBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(DistillerBlockEntity blockEntity, DistillerBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        NonNullList<ItemStack> items = blockEntity.getInputItems();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                ItemStackRenderState itemState = new ItemStackRenderState();
                itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.GUI, blockEntity.getLevel(), blockEntity, slot);
                state.items[slot] = itemState;
            }
        }
        state.inputFluid = blockEntity.getInputFluid();
        state.product = blockEntity.getProduct();
        state.burnTicks = blockEntity.getBurnTicks();
        state.recipeProgress = blockEntity.getRecipeProgress();
        state.maxProgress = blockEntity.getMaxProgress();
        state.animationHeight = Mth.lerp(partialTicks, blockEntity.oAnimationHeight, blockEntity.animationHeight);
        state.liquidHeight = Mth.lerp(partialTicks, blockEntity.oLiquidAnimationHeight, blockEntity.liquidAnimationHeight);
    }

    @Override
    public void submit(DistillerBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, 1, 0);
        poseStack.translate(0.5F, 0, 0.5F);
        float SCALE = 0.75F;
        for (int i = 0; i < state.items.length; i++) {
            if (state.items[i] != null) {
                poseStack.pushPose();
                poseStack.translate(0, i * 0.05F, 0);
                poseStack.scale(SCALE, SCALE, SCALE);
                poseStack.mulPose(new Quaternionf().rotateLocalX((float) Math.toRadians(90F)).rotateLocalY(i * 31415));
                state.items[i].submit(poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, 0, 0);
                poseStack.popPose();
            }
        }
        poseStack.popPose();

        poseStack.pushPose();

        poseStack.translate(0, 1, 0);

        LiquidRenderer.setTexture(Identifier.withDefaultNamespace("textures/block/water_still.png"));
        LiquidRenderer.setAnimateData(new LiquidRenderer.AnimateData(1, 32));
        LiquidRenderer.submitLiquid(
                poseStack,
                submitNodeCollector,
                new Vector3d(0.01, 0.01, 0.01),
                new Vector3d(0.99, 0.99, 0.99),
                state.liquidHeight,
                LightCoordsUtil.FULL_BRIGHT,
                BiomeColors.getAverageWaterColor(Minecraft.getInstance().level, state.blockPos)
        );

        poseStack.popPose();
    }
}