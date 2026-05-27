package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.client.render.block.renderstate.BarMenuBlockEntityRenderState;
import io.github.hawah.shakenstir.content.block.BarMenuBlock;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.locale.Language;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BarMenuBlockEntityRenderer implements BlockEntityRenderer<BarMenuBlockEntity, BarMenuBlockEntityRenderState> {
    public BarMenuBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) {
    }

    @Override
    public BarMenuBlockEntityRenderState createRenderState() {
        return new BarMenuBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(BarMenuBlockEntity blockEntity, BarMenuBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = blockEntity.getBlockState().getValue(BarMenuBlock.FACING);
        if (blockEntity.content.isEmpty()) {
            blockEntity.content.addAll(
                    List.of(
                            EnchantmentNames.getInstance().getRandomName(Minecraft.getInstance().font, 25),
                            EnchantmentNames.getInstance().getRandomName(Minecraft.getInstance().font, 15),
                            EnchantmentNames.getInstance().getRandomName(Minecraft.getInstance().font, 20)
                    )
            );
        }
        state.content.addAll(blockEntity.content);
    }

    @Override
    public void submit(BarMenuBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        float scale = 1/16F/4;
        poseStack.translate(0, 1/32F, 0);
        poseStack.translate(0.5, 0, 0.5);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(new Quaternionf()
                .rotateLocalX((float) Math.PI/2)
                .rotateLocalY((float) -Math.toRadians(state.facing.toYRot() + 180))
        );

        for (int i = 0; i < state.content.size(); i++) {
            poseStack.pushPose();
            poseStack.translate(-0.15/scale, (0.1-i * 0.2)/scale, 0);
            submitNodeCollector.submitText(
                    poseStack,
                    0,
                    0,
                    Language.getInstance().getVisualOrder(state.content.get(i)),
                    false,
                    Font.DisplayMode.NORMAL,
                    state.lightCoords,
                    0xFFFFFFFF,
                    0x000000,
                    0x000000
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
