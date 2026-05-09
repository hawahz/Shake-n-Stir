package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Shake;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.render.toolkit.TransformWarper;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.joml.*;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Deprecated
public record ShakeItemSpecialRenderer() implements SpecialModelRenderer<Vector2f> {

    public static final Matrix4f SHAKING_SHAKE_TRANSFORM = new Matrix4f(0.99995023f, -0.0072998703f, -0.006803939f, 0.0f, 0.0040740552f, -0.32377687f, 0.94612664f, 0.0f, -0.0091095455f, -0.9461069f, -0.32373095f, 0.0f, 0.013291817f, 1.648548f, 0.02847635f, 1.0f);

    @Override
    public void submit(@Nullable Vector2f argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();

        boolean hasCup = Minecraft.getInstance().player.getMainHandItem().getOrDefault(DataComponentTypeRegistries.HAS_CUP, false);
        TransformWarper warper = TransformWarper.instance(this);
        warper.warp(poseStack);

        if (hasCup) {
            submitWithCup(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        } else {
            submitNoCup(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        }

        warper.end();
        poseStack.popPose();
    }

    private void submitNoCup(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(BlockRegistries.SHAKE_BLOCK.get().defaultBlockState().setValue(Shake.FACING, Direction.DOWN));
        List<BlockStateModelPart> list = new ArrayList<>();
        model.collectParts(RandomSource.create(), list);

        poseStack.pushPose();
        submitShake(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, list);
        poseStack.popPose();
    }

    private void submitWithCup(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(BlockRegistries.SHAKE_BLOCK.get().defaultBlockState().setValue(Shake.FACING, Direction.UP));
        List<BlockStateModelPart> list = new ArrayList<>();
        model.collectParts(RandomSource.create(), list);

        if (isShaking()){
            applyShakingTransform(poseStack);
        }

        poseStack.pushPose();

        if (isShaking()) {
            poseStack.mulPose(SHAKING_SHAKE_TRANSFORM);
            poseStack.mulPose(new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.1f, -0.2f, -0.2f, 1.0f));
        }
        submitShake(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, list);
        poseStack.popPose();
    }

    private void submitShake(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, List<BlockStateModelPart> list) {
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

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
    }

    @Override
    public Vector2f extractArgument(ItemStack stack) {
        boolean isMainHand = Minecraft.getInstance().player.getMainHandItem().equals(stack);

        return new Vector2f(isMainHand?1 : 0);
    }

    @Deprecated
    public record Unbaked() implements SpecialModelRenderer.Unbaked<Vector2f> {
        public static final MapCodec<ShakeItemSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());
        @Override
        public ShakeItemSpecialRenderer bake(BakingContext context) {
            return new ShakeItemSpecialRenderer();
        }

        public Vector3f rot() {
            return new Vector3f();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
