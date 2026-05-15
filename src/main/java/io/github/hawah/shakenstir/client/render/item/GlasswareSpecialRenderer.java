package io.github.hawah.shakenstir.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.client.model.Models;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareQuadCollection;
import io.github.hawah.shakenstir.client.render.IGlasswareRenderState;
import io.github.hawah.shakenstir.client.render.block.GlasswareBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.glassware.vertexConsumer.VerticalGradientVertexConsumer;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import io.github.hawah.shakenstir.util.IModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Ease;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Quaternionf;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GlasswareSpecialRenderer implements SpecialModelRenderer<GlasswareSpecialRenderer.RenderState> {
    @Override
    public void submit(@Nullable RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        float globalScale = 0.5f;
        poseStack.translate(0.5, 0.8, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(180));
        poseStack.scale(globalScale, globalScale, globalScale);
        poseStack.translate(-0.5, -0.5, -0.5);

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
        state.model.submit(submitNodeCollector, poseStack, List.of(vc), lightCoords, OverlayTexture.NO_OVERLAY, RenderTypes.cutoutMovingBlock());
        GlasswareBlockEntityRenderer.submitLiquid(state, poseStack, submitNodeCollector, lightCoords);
        poseStack.translate(0.5, 0, 0.5);
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



        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {

    }



    @Override
    public @Nullable RenderState extractArgument(ItemStack stack) {
        RenderState renderState = new RenderState();
        renderState.model = Models.getModel(stack.get(DataComponents.ITEM_MODEL)).orElse(null);
        renderState.height = stack.has(DataComponentTypeRegistries.DRINK_DATA)? 1: 0;
        renderState.color = stack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb() | 0xFF000000;
        renderState.decorations.addAll(stack.getOrDefault(DataComponentTypeRegistries.GLASSWARE_DECORATIONS, List.of()));
        return renderState;
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<RenderState> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());


        @Override
        public @Nullable GlasswareSpecialRenderer bake(BakingContext context) {
            return new GlasswareSpecialRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<RenderState>> type() {
            return MAP_CODEC;
        }
    }

    public static class RenderState implements IGlasswareRenderState {
        public final List<GlasswareBlockEntity.Decoration> decorations = new ArrayList<>();
        public IModel<?> model;
        public float height = 0;
        public int color = 0xFFFFFFFF;

        @Override
        public float height() {
            return height;
        }

        @Override
        public IModel<?> model() {
            return model;
        }

        @Override
        public int color() {
            return color;
        }

    }
}
