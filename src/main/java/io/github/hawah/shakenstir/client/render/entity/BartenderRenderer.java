package io.github.hawah.shakenstir.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataAttachment.DataAttachmentTypeRegistries;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.Vec3;

public class BartenderRenderer extends LivingEntityRenderer<BartenderEntity, BartenderRenderState, BartenderModel> {

    static final PlayerSkin SKIN = create("entity/bartender_jill", PlayerModelType.SLIM);

    public BartenderRenderer(EntityRendererProvider.Context context) {
        super(context, new BartenderModel(context.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.5F);
        this.addLayer(new BartenderItemInHandLayer<>(this));
//        this.addLayer(new ArrowLayer<>(this, context));
//        this.addLayer(new Deadmau5EarsLayer(this, context.getModelSet()));
//        this.addLayer(new CapeLayer(this, context.getModelSet(), context.getEquipmentAssets()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
        this.addLayer(new WingsLayer<>(this, context.getModelSet(), context.getEquipmentRenderer()));
//        this.addLayer(new ParrotOnShoulderLayer(this, context.getModelSet()));
//        this.addLayer(new SpinAttackEffectLayer(this, context.getModelSet()));
//        this.addLayer(new BeeStingerLayer<>(this, context));
    }

    public Vec3 getRenderOffset(BartenderRenderState state) {
        Vec3 offset = super.getRenderOffset(state);
        return state.isCrouching ? offset.add(0.0, state.scale * -2.0F / 16.0, 0.0) : offset;
    }

    private static HumanoidModel.ArmPose getArmPose(BartenderEntity avatar, HumanoidArm arm) {
        ItemStack mainHandItem = avatar.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandItem = avatar.getItemInHand(InteractionHand.OFF_HAND);
        HumanoidModel.ArmPose mainHandPose = getArmPose(avatar, mainHandItem, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offHandPose = getArmPose(avatar, offHandItem, InteractionHand.OFF_HAND);
        if (mainHandPose.isTwoHanded()) {
            offHandPose = offHandItem.isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        return avatar.getMainArm() == arm ? mainHandPose : offHandPose;
    }

    private static HumanoidModel.ArmPose getArmPose(BartenderEntity avatar, ItemStack itemInHand, InteractionHand hand) {
        var extensions = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(itemInHand);
        var armPose = extensions.getArmPose(avatar, hand, itemInHand);
        if (armPose != null) {
            return armPose;
        }
        if (itemInHand.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else if (!avatar.swinging && itemInHand.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemInHand)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        } else {
            if (avatar.getUsedItemHand() == hand && avatar.getUseItemRemainingTicks() > 0) {
                ItemUseAnimation anim = itemInHand.getUseAnimation();
                if (anim == ItemUseAnimation.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (anim == ItemUseAnimation.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (anim == ItemUseAnimation.TRIDENT) {
                    return HumanoidModel.ArmPose.THROW_TRIDENT;
                }

                if (anim == ItemUseAnimation.CROSSBOW) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (anim == ItemUseAnimation.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }

                if (anim == ItemUseAnimation.TOOT_HORN) {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }

                if (anim == ItemUseAnimation.BRUSH) {
                    return HumanoidModel.ArmPose.BRUSH;
                }

                if (anim == ItemUseAnimation.SPEAR) {
                    return HumanoidModel.ArmPose.SPEAR;
                }
            }

            SwingAnimation attack = itemInHand.get(DataComponents.SWING_ANIMATION);
            if (attack != null && attack.type() == SwingAnimationType.STAB && avatar.swinging) {
                return HumanoidModel.ArmPose.SPEAR;
            } else {
                return itemInHand.is(ItemTags.SPEARS) ? HumanoidModel.ArmPose.SPEAR : HumanoidModel.ArmPose.ITEM;
            }
        }
    }

    public Identifier getTextureLocation(AvatarRenderState state) {
        return state.skin.body().texturePath();
    }

    protected void scale(AvatarRenderState state, PoseStack poseStack) {
        float s = 0.9375F;
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public BartenderRenderState createRenderState() {
        return new BartenderRenderState();
    }

    public void extractRenderState(BartenderEntity entity, BartenderRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        state.leftArmPose = getArmPose(entity, HumanoidArm.LEFT);
        state.rightArmPose = getArmPose(entity, HumanoidArm.RIGHT);
        state.skin = SKIN;
        state.arrowCount = entity.getArrowCount();
        state.stingerCount = entity.getStingerCount();
        state.isSpectator = entity.isSpectator();
        state.nameTag = null;
//        this.extractFlightData(entity, state, partialTicks);
//        this.extractCapeState(entity, state, partialTicks);
        state.parrotOnLeftShoulder = entity.getParrotVariantOnShoulder(true);
        state.parrotOnRightShoulder = entity.getParrotVariantOnShoulder(false);
        state.id = entity.getId();
        state.heldOnHead.clear();
        if (state.isUsingItem) {
            ItemStack useItem = entity.getItemInHand(state.useItemHand);
            if (useItem.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SPYGLASS_SCOPE)) {
                this.itemModelResolver.updateForLiving(state.heldOnHead, useItem, ItemDisplayContext.HEAD, entity);
            }
        }
        state.brainState = entity.getData(DataAttachmentTypeRegistries.BRAIN_STATE);
        state.shakeAmount = entity.getShakeAmount(partialTicks);
        state.readyShakeAmount = entity.getReadyShakeAmount(partialTicks);
        state.idleFrontAmount = entity.getIdleFrontAmount(partialTicks);
        state.idleBackAmount = entity.getIdleBackAmount(partialTicks);
        state.animState = entity.getState();
    }


    @Override
    public void submit(BartenderRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

        if (state.animState == BartenderEntity.AnimState.SHAKING) {
            model.shakeAnimation.apply((long) (state.shakeAmount * 1000), 1);
        }

        super.submit(state, poseStack, submitNodeCollector, camera);
        submitNodeCollector.submitNameTag(
                poseStack, state.nameTagAttachment, 0, Component.literal(state.brainState), !state.isDiscrete, state.lightCoords, state.distanceToCameraSq, camera
        );
    }

    @Override
    protected boolean shouldShowName(BartenderEntity entity, double distanceToCameraSq) {
        return super.shouldShowName(entity, distanceToCameraSq);
    }

    @Override
    public Identifier getTextureLocation(BartenderRenderState state) {
        return state.skin.body().texturePath();
    }

    private static PlayerSkin create(String body, PlayerModelType model) {
        return new PlayerSkin(new ClientAsset.ResourceTexture(ShakenStir.asResource(body)), null, null, model, true);
    }
}
