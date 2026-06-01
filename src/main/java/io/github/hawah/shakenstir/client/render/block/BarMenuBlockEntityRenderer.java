package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.client.render.block.renderstate.BarMenuBlockEntityRenderState;
import io.github.hawah.shakenstir.content.block.BarMenuBlock;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BarMenuBlockEntityRenderer implements BlockEntityRenderer<BarMenuBlockEntity, BarMenuBlockEntityRenderState> {

    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();

    public BarMenuBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
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
        List<ItemStack> recipeCosts = blockEntity.getRecipeCosts();
        for (ItemStack recipeCost : recipeCosts) {
            ItemClusterRenderState itemState = new ItemClusterRenderState();
            this.itemModelResolver.updateForTopItem(
                    itemState.item,
                    recipeCost,
                    ItemDisplayContext.GROUND,
                    blockEntity.getLevel(),
                    null,
                    0
            );
            itemState.count = ItemClusterRenderState.getRenderedAmount(recipeCost.getCount());;
            itemState.seed = ItemClusterRenderState.getSeedForItemStack(recipeCost);
            state.displayItems.add(itemState);
        }
        state.spin = (float) (AnimationTickHolder.getRenderTime() % (180)) * 2;
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

        poseStack.pushPose();

        for (int i = 0; i < state.displayItems.size(); i++) {
            poseStack.pushPose();
            poseStack.translate(0.5, (0.1+i * 0.25), 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
            float SCALE = 0.5F;
            poseStack.scale(SCALE, SCALE, SCALE);
            ItemClusterRenderState renderState = state.displayItems.get(i);
            ItemEntityRenderer.renderMultipleFromCount(poseStack, submitNodeCollector, state.lightCoords, renderState, this.random);
            poseStack.popPose();

            poseStack.pushPose();
            //FIXME :向右
            submitNodeCollector.submitNameTag(
                    poseStack,
                    new Vec3(0.5, (0.1+i * 0.25), 0.5),
                    0,
                    Component.literal(String.valueOf(renderState.count)),
                    false,
                    state.lightCoords,
                    camera.pos.distanceToSqr(state.blockPos.getCenter()),
                    camera
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
