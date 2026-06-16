package io.github.hawah.shakenstir.client.render.general;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.hawah.shakenstir.client.model.Models;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareQuadCollection;
import io.github.hawah.shakenstir.client.render.IGlasswareRenderState;
import io.github.hawah.shakenstir.client.render.LiquidRenderer;
import io.github.hawah.shakenstir.client.render.glassware.vertexConsumer.VerticalGradientVertexConsumer;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import io.github.hawah.shakenstir.util.IModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Ease;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class GlasswareRenderer {
    public static void submitGlassware(IGlasswareRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float offsetX, float offsetZ, float rotate, boolean shouldReset) {
        submitGlassware(state, poseStack, submitNodeCollector, lightCoords, offsetX, offsetZ, rotate, shouldReset, 255);
    }
    public static void submitGlassware(IGlasswareRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float offsetX, float offsetZ, float rotate, boolean shouldReset, int alpha) {
        VerticalGradientVertexConsumer vc = new VerticalGradientVertexConsumer();
        vc.setGradientStyle(Ease::inOutCirc);

        double bottom;
        double top;
        if (state.model() == null) {
            return;
        }
        if (state.model().getModel() instanceof GlasswareQuadCollection quadCollection) {
            bottom = quadCollection.start().y() * 0.8;
            top = quadCollection.end().y();
        } else  {
            bottom = state.model().getShape().min(Direction.Axis.Y);
            top = state.model().getShape().max(Direction.Axis.Y);
        }
        vc.setMinY((float) (bottom + (top - bottom) * 0.15));
        vc.setMaxY((float) (top - (top - bottom) * 0.1));
        vc.setSourceAlpha((int) (120 * state.height()));
        vc.setTargetAlpha((int) (255 * state.height()));
        vc.setModulate(state.color());
        state.model().submit(submitNodeCollector, poseStack, List.of(vc), lightCoords, OverlayTexture.NO_OVERLAY, Sheets.translucentBlockSheet());
        submitLiquid(state, poseStack, submitNodeCollector, lightCoords);
        if (shouldReset) {
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(offsetX, 0, offsetZ);
            poseStack.mulPose(Axis.YN.rotationDegrees(rotate));
        } else {
            poseStack.translate(offsetX, 0, offsetZ);
        }
//        poseStack.translate(-0.5, 0, -0.5);
        for (GlasswareBlockEntity.Decoration decoration : state.decorations()) {
            ItemStack itemStack = decoration.itemStack();
            poseStack.pushPose();
            poseStack.translate(decoration.position().x, decoration.position().y, decoration.position().z);
            poseStack.mulPose(new Quaternionf(decoration.quaternionf()));
            Identifier decorateModel;
            VoxelShape shape = Shapes.empty();
            ModelSelector selector = new ModelSelector();
            if ((itemStack.has(DataComponentTypeRegistries.DECORATE_MODEL) && (decorateModel = itemStack.get(DataComponentTypeRegistries.DECORATE_MODEL)) != null) ||
                    ((decorateModel = GlasswareDecorations.maps.entrySet().stream().filter(entry -> entry.getKey().test(itemStack)).map(Map.Entry::getValue).findAny().orElse(null)) != null)) {
                Optional<IModel<?>> model = Models.getModel(decorateModel);
                AtomicReference<VoxelShape> vs = new AtomicReference<>(shape);
                model.ifPresent(deco -> {
                    selector.select(deco);
                    vs.set(deco.getShape());
                });
                shape = vs.get();
                double size = shape.isEmpty()? 0.1 : shape.bounds().getSize();
                float scale = (float) (0.225F / size);
                selector.submit(poseStack, scale, submitNodeCollector, () -> lightCoords);
            } else if (itemStack.is(SnsItemTags.BLOCK_LIKE_DRINK_DECORATION) && itemStack.getItem() instanceof BlockItem modelProvider) {
                BlockState decorationState = modelProvider.getBlock().defaultBlockState();
                BlockModelRenderState blockModelRenderState = new BlockModelRenderState();
                Minecraft.getInstance().getBlockModelResolver().update(
                        blockModelRenderState,
                        decorationState,
                        BlockDisplayContext.create()
                );

                double size = 0;
                if (Minecraft.getInstance().level != null) {
                    size = decorationState.getShape(Minecraft.getInstance().level, BlockPos.ZERO).bounds().getSize();
                }
                float scale = (float) (0.225F / size);

                poseStack.translate(-0.5 * scale, 0 * scale, -0.5 * scale);
                poseStack.scale(scale, scale, scale);

                blockModelRenderState.submit(
                        poseStack,
                        submitNodeCollector,
                        lightCoords,
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
                    // TODO extract + submit
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
                            lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            0
                    );
                }
            }
            poseStack.popPose();
        }
    }

    public static void submitLiquid(IGlasswareRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        if (state.height() > 0.1 && state.model().getModel() instanceof GlasswareQuadCollection quadCollection) {
            Vector3dc start = quadCollection.start();
            Vector3dc end = quadCollection.end();
            int color = state.color();
            float heightRate = state.height();
            LiquidRenderer.setTexture(LiquidRenderer.DEFAULT);
            LiquidRenderer.submitLiquid(poseStack, submitNodeCollector, start, end, heightRate, lightCoords, color);
        }
    }

    public static class ModelSelector {
        private IModel<?> decoration;
        private boolean isDecoration;
        private BlockModelRenderState blockModelRenderState;
        private boolean isBlock;
        private ItemStackRenderState itemStackRenderState;
        private boolean isItem;

        public void select(IModel<?> decoration) {
            this.decoration = decoration;
            isDecoration = true;
        }
        public void select(BlockModelRenderState blockModelRenderState) {
            this.blockModelRenderState = blockModelRenderState;
            isBlock = true;
        }
        public void select(ItemStackRenderState itemStackRenderState) {
            this.itemStackRenderState = itemStackRenderState;
            isItem = true;
        }

        public void submit(PoseStack poseStack, float scale, SubmitNodeCollector submitNodeCollector, LightCoordsGetter state) {
            if (isDecoration) {
                decoration.submit(
                        submitNodeCollector,
                        poseStack,
                        state.lightCord(),
                        OverlayTexture.NO_OVERLAY,
                        Sheets.translucentBlockSheet()
                );
            } else if (isBlock) {
                poseStack.translate(-0.5 * scale, 0 * scale, -0.5 * scale);
                poseStack.scale(scale, scale, scale);

                blockModelRenderState.submit(
                        poseStack,
                        submitNodeCollector,
                        state.lightCord(),
                        OverlayTexture.NO_OVERLAY,
                        0
                );
            } else if (isItem) {
                poseStack.scale(scale, scale, scale * 2);
                itemStackRenderState.submit(
                        poseStack,
                        submitNodeCollector,
                        state.lightCord(),
                        OverlayTexture.NO_OVERLAY,
                        0
                );
            }
        }
    }

    public interface LightCoordsGetter {
        int lightCord();
    }
}
