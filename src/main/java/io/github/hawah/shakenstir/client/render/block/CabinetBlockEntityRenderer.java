package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.client.render.block.renderstate.CabinetBlockEntityRenderState;
import io.github.hawah.shakenstir.content.block.Cabinet;
import io.github.hawah.shakenstir.content.blockEntity.CabinetBlockEntity;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.ShelfRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CabinetBlockEntityRenderer implements BlockEntityRenderer<CabinetBlockEntity, CabinetBlockEntityRenderState> {

    final BlockModelResolver blockModelResolver;
    final ItemModelResolver itemModelResolver;

    public CabinetBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        blockModelResolver = context.blockModelResolver();
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public CabinetBlockEntityRenderState createRenderState() {
        return new CabinetBlockEntityRenderState();
    }

    @Override
    public void submit(CabinetBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        for (int i = 0; i < state.renderStateEither.size(); i++) {
            if (state.renderStateEither.get(i) == null) {
                continue;
            }
            int finalI = i;
            state.renderStateEither.get(i)
                    .ifLeft(blockModelRenderState -> {

                        poseStack.pushPose();
                        int inverse = (int) -((finalI - 0.5) * 2);
                        int rotateDegree = switch (state.facing) {
                            case NORTH -> 0;
                            case SOUTH -> 180;
                            case EAST -> 90;
                            case WEST -> 270;
                            default -> throw new IllegalStateException("Invalid facing: " + state.facing);
                        };

                        float scale = 0.8F, offset = 0.24F;
                        Vec3 left = state.facing.getClockWise().getUnitVec3();

                        poseStack.translate(0.5F, 0.5F, 0.5F);
                        poseStack.translate(0, -0.05, 0);
                        poseStack.translate(left.x * offset * inverse, 0, left.z * offset * inverse);
                        poseStack.mulPose(Axis.YP.rotationDegrees(rotateDegree));
                        poseStack.scale(scale, scale, scale);
                        poseStack.translate(-0.5F, -0.5F, -0.5F);

                        blockModelRenderState.submit(
                                    poseStack,
                                    submitNodeCollector,
                                    state.lightCoords,
                                    OverlayTexture.NO_OVERLAY,
                                    0);
                        poseStack.popPose();
                    })
                    .ifRight( itemStackRenderState ->
                            itemStackRenderState.submit(
                                    poseStack,
                                    submitNodeCollector,
                                    state.lightCoords,
                                    OverlayTexture.NO_OVERLAY,
                                    0
                            ));
        }
    }

    @Override
    public void extractRenderState(CabinetBlockEntity blockEntity, CabinetBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        int seed = HashCommon.long2int(blockEntity.getBlockPos().asLong());
        state.facing = blockEntity.getBlockState().getValue(Cabinet.FACING);
        for (int i = 0; i < 2; i++) {
            state.renderStateEither.add(null);
            ItemStack itemStack = blockEntity.contents.get(i);
            if (itemStack.isEmpty()) {
                state.renderStateEither.set(i, null);
                continue;
            }
            if (itemStack.getItem() instanceof BlockItem blockItem){
                BlockState blockState = blockItem.getBlock().defaultBlockState();
                BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
                blockModelResolver.update(blockModelRenderState, blockState, BlockDisplayContext.create());

                state.renderStateEither.set(i, Either.left(blockModelRenderState));
            } else {
                ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                itemModelResolver.updateForTopItem(
                        itemStackRenderState,
                        itemStack,
                        ItemDisplayContext.GROUND,
                        blockEntity.getLevel(),
                        blockEntity,
                        seed
                );
                state.renderStateEither.set(i, Either.right(itemStackRenderState));
            }
            state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        }
    }

    private void submitItem(
            ShelfRenderState state, ItemStackRenderState itemStackRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int slot, float yRot
    ) {
        float itemSlotPosition = (slot - 1) * 0.3125F;
        Vec3 itemOffset = new Vec3(itemSlotPosition, state.alignToBottom ? -0.25 : 0.0, -0.25);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.translate(itemOffset);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        AABB box = itemStackRenderState.getModelBoundingBox();
        double offsetY = -box.minY;
        if (!state.alignToBottom) {
            offsetY += -(box.maxY - box.minY) / 2.0;
        }

        poseStack.translate(0.0, offsetY, 0.0);
        itemStackRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
