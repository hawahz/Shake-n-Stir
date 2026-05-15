package io.github.hawah.shakenstir.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareQuadCollection;
import io.github.hawah.shakenstir.client.render.block.renderstate.GlasswareBlockEntityRenderState;
import io.github.hawah.shakenstir.client.render.glassware.vertexConsumer.VerticalGradientVertexConsumer;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Ease;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3dc;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class GlasswareBlockEntityRenderer implements BlockEntityRenderer<GlasswareBlockEntity, GlasswareBlockEntityRenderState> {
    public GlasswareBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void extractRenderState(GlasswareBlockEntity blockEntity, GlasswareBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.position.set(blockEntity.position);
        state.rotate = blockEntity.rotation;
        state.model = blockEntity.getModel();
        state.height = Mth.lerp(partialTicks, blockEntity.oHeight, blockEntity.height);
        state.color = blockEntity.getColor();
        state.decorations.addAll(blockEntity.decorationsList);
        state.lightCoords = blockEntity.getLevel() == null? state.lightCoords: LightCoordsUtil.pack(Math.min(15, 3 + blockEntity.getLevel().getBrightness(LightLayer.BLOCK, blockEntity.getBlockPos())), blockEntity.getLevel().getBrightness(LightLayer.SKY, blockEntity.getBlockPos()));
    }

    @Override
    public void submit(GlasswareBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(state.position.x, 0, state.position.y);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.rotate));
        poseStack.translate(-0.5, 0, -0.5);
        VerticalGradientVertexConsumer vc = new VerticalGradientVertexConsumer();
        vc.setGradientStyle(Ease::inOutCirc);

        double bottom;
        double top;
        if (state.model.getModel() instanceof GlasswareQuadCollection quadCollection) {
            bottom = quadCollection.start().y() * 0.8;
            top = quadCollection.end().y();
        } else  {
            bottom = state.model.getShape().min(Direction.Axis.Y);
            top = state.model.getShape().max(Direction.Axis.Y);
        }
        vc.setMinY((float) (bottom + (top - bottom) * 0.15));
        vc.setMaxY((float) (top - (top - bottom) * 0.1));
        vc.setSourceAlpha((int) (120 * state.height));
        vc.setTargetAlpha((int) (255 * state.height));
        vc.setModulate(state.color);
        state.model.submit(submitNodeCollector, poseStack, List.of(vc), state.lightCoords, OverlayTexture.NO_OVERLAY, RenderTypes.cutoutMovingBlock());
        submitLiquid(state, poseStack, submitNodeCollector);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(state.position.x, 0, state.position.y);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.rotate));
//        poseStack.translate(-0.5, 0, -0.5);
        for (GlasswareBlockEntity.Decoration decoration : state.decorations) {
            ItemStack itemStack = decoration.itemStack();
            poseStack.pushPose();
            poseStack.translate(decoration.position().x, decoration.position().y, decoration.position().z);
            poseStack.mulPose(new Quaternionf(decoration.quaternionf()));
            if (itemStack.is(SnsItemTags.BLOCK_LIKE_DRINK_DECORATION)) {
                BlockState decorationState = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
                BlockStateModel blockStateModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(decorationState);
                List<BlockStateModelPart> parts = new ArrayList<>();
                blockStateModel.collectParts(Minecraft.getInstance().level.getRandom(), parts);
                double size = decorationState.getShape(Minecraft.getInstance().level, BlockPos.ZERO).bounds().getSize();
                float scale = (float) (0.225F / size);

                poseStack.translate(-0.5 * scale, 0 * scale, -0.5 * scale);
                poseStack.scale(scale, scale, scale);

                submitNodeCollector.submitBlockModel(
                        poseStack,
                        RenderTypes.cutoutMovingBlock(),
                        parts,
                        new int[]{0},
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        0
                );
            } else if (itemStack.is(SnsItemTags.ITEM_LIKE_DRINK_DECORATION)) {
                List<ItemModel> itemModels = new ArrayList<>();
                Identifier modelId = decoration.itemStack().get(DataComponents.ITEM_MODEL);
                if (modelId != null) {
                    ItemModel itemModel = Minecraft.getInstance().getModelManager().getItemModel(modelId);
                    itemModels.add(itemModel);
                }
                float scale = (float) (0.225F / Shapes.box(0, 0, 0, .5, .5, .5).bounds().getSize());
                for (ItemModel itemModel : itemModels) {
                    poseStack.scale(scale, scale, scale * 2);
                    ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                    itemModel.update(
                            itemStackRenderState,
                            decoration.itemStack(),
                            Minecraft.getInstance().getItemModelResolver(),
                            ItemDisplayContext.NONE,
                            null,
                            null,
                            0
                    );
                    itemStackRenderState.submit(
                            poseStack,
                            submitNodeCollector,
                            state.lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            0
                    );
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void submitLiquid(GlasswareBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        if (state.height > 0 && state.model.getModel() instanceof GlasswareQuadCollection quadCollection) {
            Vector3dc start = quadCollection.start();
            Vector3dc end = quadCollection.end();

            float minX = (float) Math.min(start.x(), end.x());
            float minY = (float) Math.min(start.y(), end.y());
            float minZ = (float) Math.min(start.z(), end.z());

            float maxX = (float) Math.max(start.x(), end.x());
            float maxY = ((float) Math.max(start.y(), end.y()) - minY) * state.height + minY;
            float maxZ = (float) Math.max(start.z(), end.z());

            submitNodeCollector.submitCustomGeometry(
                    poseStack,
                    RenderTypes.entityTranslucent(ShakenStir.asResource("textures/block/liquid_overlay.png")),
                    (pose, buffer) -> {

                        Matrix4f mat = pose.pose();

                        int r = ARGB.red(state.color);
                        int g = ARGB.green(state.color);
                        int b = ARGB.blue(state.color);
                        int a = 200;
                        int lightCoords = state.lightCoords;

                        // DOWN
                        quad(buffer, mat, pose,
                                minX, minY, minZ,
                                maxX, minY, minZ,
                                maxX, minY, maxZ,
                                minX, minY, maxZ,
                                r, g, b, a, lightCoords);

                        // UP
                        quad(buffer, mat, pose,
                                minX, maxY, minZ,
                                minX, maxY, maxZ,
                                maxX, maxY, maxZ,
                                maxX, maxY, minZ,
                                r, g, b, a, lightCoords);

                        // NORTH
                        quad(buffer, mat, pose,
                                minX, minY, minZ,
                                minX, maxY, minZ,
                                maxX, maxY, minZ,
                                maxX, minY, minZ,
                                r, g, b, a, lightCoords);

                        // SOUTH
                        quad(buffer, mat, pose,
                                minX, minY, maxZ,
                                maxX, minY, maxZ,
                                maxX, maxY, maxZ,
                                minX, maxY, maxZ,
                                r, g, b, a, lightCoords);

                        // WEST
                        quad(buffer, mat, pose,
                                minX, minY, minZ,
                                minX, minY, maxZ,
                                minX, maxY, maxZ,
                                minX, maxY, minZ,
                                r, g, b, a, lightCoords);

                        // EAST
                        quad(buffer, mat, pose,
                                maxX, minY, minZ,
                                maxX, maxY, minZ,
                                maxX, maxY, maxZ,
                                maxX, minY, maxZ,
                                r, g, b, a, lightCoords);
                    }
            );
        }
    }

    private static void quad(
            VertexConsumer buffer,
            Matrix4f mat,
            PoseStack.Pose pose,

            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,

            int r, int g, int b, int a,
            int lightCoords
    ) {

        new Color(255, 249, 200);

        buffer.addVertex(mat, x1, y1, z1)
                .setColor(r, g, b, a)
                .setUv(0f, 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0f, 1f, 0f);

        buffer.addVertex(mat, x2, y2, z2)
                .setColor(r, g, b, a)
                .setUv(0f, 1f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0f, 1f, 0f);

        buffer.addVertex(mat, x3, y3, z3)
                .setColor(r, g, b, a)
                .setUv(1f, 1f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0f, 1f, 0f);

        buffer.addVertex(mat, x4, y4, z4)
                .setColor(r, g, b, a)
                .setUv(1f, 0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0f, 1f, 0f);
    }

    @Override
    public GlasswareBlockEntityRenderState createRenderState() {
        return new GlasswareBlockEntityRenderState();
    }
}
