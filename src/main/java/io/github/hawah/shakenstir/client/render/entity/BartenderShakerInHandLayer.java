package io.github.hawah.shakenstir.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.SwingAnimationType;
import org.joml.Quaternionf;

public class BartenderShakerInHandLayer extends RenderLayer<BartenderRenderState, BartenderModel> {
    public BartenderShakerInHandLayer(RenderLayerParent<BartenderRenderState, BartenderModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, BartenderRenderState state, float yRot, float xRot) {
        if (!state.shakeInHand) {
            return;
        }
        if (state.shaking) {
            submitShaking(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        } else {
            submitNotShaking(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
        }
    }

    private void submitNotShaking(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, BartenderRenderState state, float yRot, float xRot) {
        poseStack.pushPose();
        this.getParentModel().translateToHand(state, HumanoidArm.RIGHT, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        HumanoidArm arm = HumanoidArm.RIGHT;
        float offsetX = this.useBabyOffset(state) ? 0.0F : 1.0F;
        float offsetY = this.useBabyOffset(state) ? 1.0F : 2.0F;
        float offsetZ = this.useBabyOffset(state) ? -4.5F : -10.0F;
        poseStack.translate(1 * offsetX / 16.0F, offsetY / 16.0F, offsetZ / 16.0F);
        if (state.attackTime > 0.0F && state.attackArm == arm && state.swingAnimationType == SwingAnimationType.STAB) {
            SpearAnimations.thirdPersonAttackItem(state, poseStack);
        }
        poseStack.translate(-0.1f, 0.1f, 0.20000002f);
        poseStack.mulPose(new Quaternionf().rotateLocalX(-0.4f).rotateLocalY( -0.2f).rotateLocalZ( 0.3f));
        poseStack.scale(0.513158f, 0.513158f, 0.513158f);
        state.shakerItem.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private boolean useBabyOffset(BartenderRenderState state) {
        return state.isBaby && state.entityType != EntityType.ARMOR_STAND;
    }

    public void submitShaking(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, BartenderRenderState state, float yRot, float xRot) {
        poseStack.pushPose();
        this.getParentModel().translateToHand(state, HumanoidArm.RIGHT, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        HumanoidArm arm = HumanoidArm.RIGHT;
        float offsetX = this.useBabyOffset(state) ? 0.0F : 1.0F;
        float offsetY = this.useBabyOffset(state) ? 1.0F : 2.0F;
        float offsetZ = this.useBabyOffset(state) ? -4.5F : -10.0F;
        poseStack.translate(1 * offsetX / 16.0F, offsetY / 16.0F, offsetZ / 16.0F);
        if (state.attackTime > 0.0F && state.attackArm == arm && state.swingAnimationType == SwingAnimationType.STAB) {
            SpearAnimations.thirdPersonAttackItem(state, poseStack);
        }
        poseStack.translate(-0.10000002f, -0.4f, 1.4901161E-8f);
        poseStack.mulPose(new Quaternionf().rotateLocalX(0.19999999f).rotateLocalY( -0.1f).rotateLocalZ( 0.3f));
        poseStack.scale(0.56447387f, 0.56447387f, 0.56447387f);
        state.shakerItem.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
