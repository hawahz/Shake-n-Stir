package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.hanlder.ShakeHandler;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Shake;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ShakeItemSpecialRenderer() implements SpecialModelRenderer<ShakeItemSpecialRenderer.RenderState> {

    public static final Matrix4f SHAKING_SHAKE_TRANSFORM = new Matrix4f(0.99995023f, -0.0072998703f, -0.006803939f, 0.0f, 0.0040740552f, -0.32377687f, 0.94612664f, 0.0f, -0.0091095455f, -0.9461069f, -0.32373095f, 0.0f, 0.013291817f, 1.648548f, 0.02847635f, 1.0f);

    @Override
    public void submit(@Nullable RenderState argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (argument == null) {
            return;
        }

        poseStack.pushPose();

        boolean hasCup = argument.player.getItemInHand(argument.isMainHand? InteractionHand.MAIN_HAND: InteractionHand.OFF_HAND).getOrDefault(DataComponentTypeRegistries.HAS_CUP, false);
        int invert = argument.isMainHand && argument.player.getMainArm().equals(HumanoidArm.LEFT)? 1: -1;
//        warper.warp(poseStack);

        poseStack.translate(0.1f, 0.4f, 0.2f);
        poseStack.scale(.75131476f, .75131476f,.75131476f);

        if (hasCup) {
            submitWithCup(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, argument);
        } else {
            submitNoCup(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, argument);
        }

//        warper.end();
        poseStack.popPose();
    }

    private void submitNoCup(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, RenderState argument) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(BlockRegistries.SHAKE_BLOCK.get().defaultBlockState().setValue(Shake.FACING, Direction.DOWN));
        List<BlockStateModelPart> list = new ArrayList<>();
        model.collectParts(RandomSource.create(), list);

        poseStack.pushPose();
        submitShake(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, list);
        poseStack.popPose();
    }

    private void submitWithCup(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, RenderState argument) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(BlockRegistries.SHAKE_BLOCK.get().defaultBlockState().setValue(Shake.FACING, Direction.UP));
        List<BlockStateModelPart> list = new ArrayList<>();
        model.collectParts(RandomSource.create(), list);

        poseStack.pushPose();


        if (isShaking(argument)){
            poseStack.translate(0.5f, -0.09999999f, 0.3f);
            poseStack.mulPose(
                    new Quaternionf()
                            .rotateLocalX(1.4000001f)
                            .rotateLocalY(0.0f)
                            .rotateLocalZ(1.0f)
            );

            poseStack.translate(-0.1f, 0.1f, 0.0f);
            ShakeHandler shakeHandler = ShakenStirClient.SHAKE_HANDLER;
            double y = shakeHandler.x();
            poseStack.mulPose(
                    new Quaternionf()
                            .rotateLocalX((float) (-0.29999998f + y/5))
                            .rotateLocalY(0.6f)
                            .rotateLocalZ((float) (-1.5000002f + (y + 2)/6))
            );
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        submitShake(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, list);
        poseStack.popPose();

    }

    private void submitShake(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, List<BlockStateModelPart> list) {

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
    public static boolean isShaking(RenderState state) {
        return state.player.getUseItem() == (state.player.getItemInHand(state.isMainHand? InteractionHand.MAIN_HAND: InteractionHand.OFF_HAND));
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
    }

    @Override
    public RenderState extractArgument(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return null;
        }

        Player nearestPlayer = Minecraft.getInstance().level.getNearestPlayer(
                player.position().x(),
                player.position().y(),
                player.position().z(),
                10,
                entity -> {
                    if (entity instanceof Player p) {
                        return p.getMainHandItem() == stack || p.getOffhandItem() == stack;
                    }
                    return false;
                }
        );
        if (nearestPlayer == null) {
            return null;
        }
        boolean isMainHand = nearestPlayer.getMainHandItem() == stack;

        return new RenderState(nearestPlayer, stack, isMainHand);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<RenderState> {
        public static final MapCodec<ShakeItemSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());
        @Override
        public ShakeItemSpecialRenderer bake(BakingContext context) {
            return new ShakeItemSpecialRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }

    public record RenderState(
            Player player,
            ItemStack stack,
            boolean isMainHand) {
    }
}
