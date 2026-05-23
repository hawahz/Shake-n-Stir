package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.client.render.block.renderstate.DistillerBlockEntityRenderState;
import io.github.hawah.shakenstir.content.blockEntity.DistillerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillerBlockEntityRenderer implements BlockEntityRenderer<DistillerBlockEntity, DistillerBlockEntityRenderState> {

    public DistillerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public DistillerBlockEntityRenderState createRenderState() {
        return new DistillerBlockEntityRenderState();
    }

    @Override
    public void submit(DistillerBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    }
}
