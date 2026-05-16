package io.github.hawah.shakenstir.client.render.item.firstPerson;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Shake;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.render.toolkit.TransformWarper;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.*;
import org.jspecify.annotations.Nullable;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class ShakeRenderer {

    public static final Matrix4f ARM_TRANSFORM = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5403024f, -0.84147114f, 0.0f, 0.0f, 0.84147114f, 0.5403024f, 0.0f, 0.9000001f, 0.12046755f, 0.95270944f, 1.0f);
    public static final Matrix4f SHAKING_ARM_TRANSFORM = new Matrix4f(0.59046054f, -0.0043104887f, -0.004017652f, 0.0f, 0.005534787f, 0.2675616f, 0.5263646f, 0.0f, -0.0020219171f, -0.52637595f, 0.2675891f, 0.0f, 0.08839527f, 1.0911926f, 0.32556745f, 1.0f);
    public static final Matrix4f SHAKING_SHAKE_TRANSFORM = new Matrix4f(0.99995023f, -0.0072998703f, -0.006803939f, 0.0f, 0.0040740552f, -0.32377687f, 0.94612664f, 0.0f, -0.0091095455f, -0.9461069f, -0.32373095f, 0.0f, 0.013291817f, 1.648548f, 0.02847635f, 1.0f);
    public static final Matrix4f LEFT_ARM_TRANSFORM = new Matrix4f(0.9210611f, -0.38941836f, -7.450581E-9f, 0.0f, 0.3874729f, 0.9164596f, -0.09983347f, 0.0f, 0.038876962f, 0.09195268f, 0.9950044f, 0.0f, -0.317625f, 0.6759194f, 0.2094838f, 1.0f);
    public static final Matrix4f SHAKE_PRE_TRANSFORM = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.1f, -0.20000002f, -0.1f, 1.0f);

    @SubscribeEvent
    public static void onRenderShakeHand(RenderHandEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (!itemStack.is(ItemRegistries.SHAKE)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        float interpolatedPitch = event.getInterpolatedPitch();
        boolean isMainHand = event.getHand() == InteractionHand.MAIN_HAND;
        HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isRightArm = arm == HumanoidArm.RIGHT;
        int invert = isRightArm ? 1 : -1;

        if (!isRightArm) {
            return;
        }

        event.setCanceled(true);
        SubmitNodeCollector submitNodeCollector = event.getSubmitNodeCollector();
        PoseStack poseStack = event.getPoseStack();
        int packedLight = event.getPackedLight();
        TransformWarper warper = TransformWarper.instance(1);
        poseStack.pushPose();
        float inverseArmHeight = event.getEquipProgress();
        float attack = event.getSwingProgress();
        applyItemArmTransform(poseStack, arm, inverseArmHeight);
        swingArm(attack, poseStack, invert, arm);

        if (ShakenStirClient.SHAKE_HANDLER.isActive()) {
            poseStack.mulPose(SHAKE_PRE_TRANSFORM);
        }
        poseStack.translate(-1.0, 0, 0);
        poseStack.translate(1 * 0.56F, -0.52F + inverseArmHeight * -0.6F, -0.72F);

        submit(null, poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY, false, 0);
        warper.end();
        poseStack.popPose();
    }

    private static void swingArm(float attack, PoseStack poseStack, int invert, HumanoidArm arm) {
        float xSwingPosition = -0.4F * Mth.sin(Mth.sqrt(attack) * (float) Math.PI);
        float ySwingPosition = 0.2F * Mth.sin(Mth.sqrt(attack) * (float) (Math.PI * 2));
        float zSwingPosition = -0.2F * Mth.sin(attack * (float) Math.PI);
        poseStack.translate(invert * xSwingPosition, ySwingPosition, zSwingPosition);
        applyItemArmAttackTransform(poseStack, arm, attack);
    }

    private static void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight) {
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(invert * 0.56F, -0.52F + inverseArmHeight * -0.6F, -0.72F);
    }

    private static void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackValue) {
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        float ySwingRotation = Mth.sin(attackValue * attackValue * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * (45.0F + ySwingRotation * -20.0F)));
        float xzSwingRotation = Mth.sin(Mth.sqrt(attackValue) * (float) Math.PI);
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * xzSwingRotation * -20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(xzSwingRotation * -80.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * -45.0F));
    }

    @SubscribeEvent
    public static void onRenderThirdPersonHand(RenderPlayerEvent.Pre<AbstractClientPlayer> event) {
        AvatarRenderer<AbstractClientPlayer> renderer = event.getRenderer();
        AvatarRenderState renderState = event.getRenderState();
    }

    public static void submit(@Nullable Vector2f argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();



        boolean hasCup = Minecraft.getInstance().player.getMainHandItem().getOrDefault(DataComponentTypeRegistries.HAS_CUP, false);

        if (hasCup) {
            submitWithCup(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        } else {
            submitNoCup(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        }

        poseStack.popPose();
    }

    private static void submitNoCup(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(BlockRegistries.SHAKE_BLOCK.get().defaultBlockState().setValue(Shake.FACING, Direction.DOWN));
        List<BlockStateModelPart> list = new ArrayList<>();
        model.collectParts(RandomSource.create(), list);

        poseStack.pushPose();
        submitShake(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, list);
        poseStack.popPose();

        poseStack.pushPose();

        poseStack.mulPose(ARM_TRANSFORM);

        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        AvatarRenderer<AbstractClientPlayer> playerRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skinTexture = player.getSkin().body().texturePath();
        playerRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), player);
        poseStack.popPose();
    }

    private static void submitWithCup(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(BlockRegistries.SHAKE_BLOCK.get().defaultBlockState().setValue(Shake.FACING, Direction.UP));
        List<BlockStateModelPart> list = new ArrayList<>();
        model.collectParts(RandomSource.create(), list);

        if (isShaking()){
            applyShakingTransform(poseStack);
        }

        poseStack.pushPose();

        if (isShaking()) {
            poseStack.mulPose(SHAKING_SHAKE_TRANSFORM);
        }
        submitShake(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, list);
        poseStack.popPose();

        poseStack.pushPose();

        if (isShaking()) {
            poseStack.mulPose(SHAKING_ARM_TRANSFORM);
        }

        poseStack.mulPose(ARM_TRANSFORM);

        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        AvatarRenderer<AbstractClientPlayer> playerRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skinTexture = player.getSkin().body().texturePath();
        boolean skipDrawRight = playerRenderer.getModel().rightArm.skipDraw;
        playerRenderer.getModel().rightArm.skipDraw = false;
        playerRenderer.renderRightHand(poseStack, submitNodeCollector, lightCoords, skinTexture, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), player);
        playerRenderer.getModel().rightArm.skipDraw = skipDrawRight;


        poseStack.popPose();

        if (isShaking()) {
            poseStack.mulPose(LEFT_ARM_TRANSFORM);
            boolean skipDrawLeft = playerRenderer.getModel().leftArm.skipDraw;
            playerRenderer.getModel().leftArm.skipDraw = false;
            playerRenderer.renderLeftHand(poseStack, submitNodeCollector, lightCoords, skinTexture, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE), player);
            playerRenderer.getModel().leftArm.skipDraw = skipDrawLeft;
        }
    }

    private static void submitShake(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, List<BlockStateModelPart> list) {
        poseStack.mulPose(new Transformation(
                new Vector3f(0, 5.25F/16, -0.75F/16),
                new Quaternionf(),
                new Vector3f(0.74609F, 0.74609F, 0.74609F),
                new Quaternionf()
        ));
        submitNodeCollector.submitBlockModel(
                poseStack,
                RenderTypes.solidMovingBlock(),
                list,
                new int[]{0},
                lightCoords,
                overlayCoords,
                outlineColor
        );
    }

    public static void applyShakingTransform(PoseStack poseStack) {
        float liftProcess = EaseHelper.easeOutPow(Mth.clamp((AnimationTickHolder.getRenderTime() - ShakenStirClient.SHAKE_HANDLER.firstTimeShake())/5, 0, 1), 2);
        if (liftProcess < 1) {
            poseStack.translate(0, (liftProcess-1) * 2, 0);
        }
        poseStack.translate(0, -0.2, 0);
        poseStack.translate(0, ShakenStirClient.SHAKE_HANDLER.y()/10, ShakenStirClient.SHAKE_HANDLER.x()/10);
        double mapping = (-ShakenStirClient.SHAKE_HANDLER.y() + 2)/4 * 1.5;
        poseStack.mulPose(Axis.XP.rotationDegrees((float) ((ShakenStirClient.SHAKE_HANDLER.x() - 1)/2 * 20 * (mapping))));
    }

    public static boolean isShaking() {
        return ShakenStirClient.SHAKE_HANDLER.isActive();
    }
}
