package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.hanlder.ShakerHandler;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Shaker;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ShakeItemSpecialRenderer() implements SpecialModelRenderer<ShakeItemSpecialRenderer.RenderState> {

    public static final Matrix4f SHAKING_SHAKE_TRANSFORM = new Matrix4f(0.99995023f, -0.0072998703f, -0.006803939f, 0.0f, 0.0040740552f, -0.32377687f, 0.94612664f, 0.0f, -0.0091095455f, -0.9461069f, -0.32373095f, 0.0f, 0.013291817f, 1.648548f, 0.02847635f, 1.0f);

    @Override
    public void submit(@Nullable RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) {
            return;
        }

        poseStack.pushPose();
        int invert = state.invert();

        poseStack.translate(0.1f, 0.4f, 0.2f);
        poseStack.scale(.75131476f, .75131476f,.75131476f);

        if (state.hasCup()) {
            submitWithCup(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, state);
        } else {
            submitNoCup(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor, state);
        }

        poseStack.popPose();
    }

    private void submitNoCup(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, RenderState state) {

        poseStack.pushPose();
        state.blockRenderState().submit(
                poseStack,
                submitNodeCollector,
                lightCoords,
                overlayCoords,
                outlineColor
        );
        poseStack.popPose();
    }

    private void submitWithCup(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, RenderState state) {

        poseStack.pushPose();


        if (isShaking(state)){
            poseStack.translate(0.5f, -0.09999999f, 0.3f);
            poseStack.mulPose(
                    new Quaternionf()
                            .rotateLocalX(1.4000001f)
                            .rotateLocalY(0.0f)
                            .rotateLocalZ(1.0f)
            );

            poseStack.translate(-0.1f, 0.1f, 0.0f);
            ShakerHandler shakerHandler = ShakenStirClient.SHAKE_HANDLER;
            double y = shakerHandler.x();
            poseStack.mulPose(
                    new Quaternionf()
                            .rotateLocalX((float) (-0.29999998f + y/5))
                            .rotateLocalY(0.6f)
                            .rotateLocalZ((float) (-1.5000002f + (y + 2)/6))
            );
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        state.blockRenderState().submit(
                poseStack,
                submitNodeCollector,
                lightCoords,
                overlayCoords,
                outlineColor
        );
        poseStack.popPose();

    }

    public static boolean isShaking(RenderState state) {
        return state.player.getUseItem() == (state.player.getItemInHand(state.isMainHand? InteractionHand.MAIN_HAND: InteractionHand.OFF_HAND));
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
    }

    @Override
    public @Nullable RenderState extractArgument(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;

        if (player == null || level == null) {
            return null;
        }

        Player nearestPlayer = level.getNearestPlayer(
                player.position().x(),
                player.position().y(),
                player.position().z(),
                Minecraft.getInstance().options.renderDistance().get() * 16,
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
        boolean hasCup = nearestPlayer.getItemInHand(isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).getOrDefault(DataComponentTypeRegistries.HAS_CUP, false);

        BlockModelResolver blockModelResolver = Minecraft.getInstance().getBlockModelResolver();
        BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
        blockModelResolver.update(
                blockModelRenderState,
                BlockRegistries.SHAKE_BLOCK.get().defaultBlockState().setValue(Shaker.FACING, hasCup? Direction.UP: Direction.DOWN),
                BlockDisplayContext.create()
        );

        return new RenderState(nearestPlayer,
                stack,
                isMainHand,
                hasCup,
                isMainHand && nearestPlayer.getMainArm().equals(HumanoidArm.LEFT)? 1: -1,
                blockModelRenderState
        );
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
            boolean isMainHand,
            boolean hasCup,
            int invert,
            BlockModelRenderState blockRenderState
    ) { }
}
