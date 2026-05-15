package io.github.hawah.shakenstir.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.hanlder.GlasswareHandlerRenderState;
import io.github.hawah.shakenstir.client.model.Models;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareQuadCollection;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareUnbakedModelLoader;
import io.github.hawah.shakenstir.client.render.ClientShakeTooltipComponent;
import io.github.hawah.shakenstir.client.render.GlasswareOutlineRenderer;
import io.github.hawah.shakenstir.client.render.block.GlasswareBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.block.ShakeBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.item.SpiritBottleSpecialRenderer;
import io.github.hawah.shakenstir.content.HasCup;
import io.github.hawah.shakenstir.content.ShakeTooltipComponent;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;

import java.util.List;
import java.util.function.BiFunction;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentParticles event) {
        ShakenStirClient.TIMER_NORMAL.warp(Minecraft.getInstance().getDeltaTracker());
        //Outliner.renderInto(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), Minecraft.getInstance().player.getEyePosition(), Minecraft.getInstance().getDeltaTracker());
    }

    @SubscribeEvent
    public static void onExtractLevelStage(ExtractLevelRenderStateEvent event) {
        event.getRenderState().setRenderData(
                GlasswareHandlerRenderState.ctxKey,
                new GlasswareHandlerRenderState(event.getDeltaTracker())
        );
        ShakenStirClient.DECORATE_PLACE_HANDLER.extract(event);
    }

    @SubscribeEvent
    public static void onSubmitLevel(SubmitCustomGeometryEvent event) {
        SubmitNodeCollector submitNodeCollector = event.getSubmitNodeCollector();
        PoseStack poseStack = event.getPoseStack();
        LevelRenderState levelRenderState = event.getLevelRenderState();

        poseStack.pushPose();

        ShakenStirClient.GLASSWARE_HANDLER.submit(submitNodeCollector, poseStack, levelRenderState);
        ShakenStirClient.DECORATE_PLACE_HANDLER.submit(submitNodeCollector, poseStack, levelRenderState);
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void modifyFov(ComputeFovModifierEvent event) {
        if (ClientDataHolder.shouldModifyView()) {
            event.setNewFovModifier(event.getFovModifier() / 2);
        }
    }

    @SubscribeEvent
    public static void modifyTurnSensitivity(CalculatePlayerTurnEvent event) {
        if (ClientDataHolder.shouldModifyView()) {
            event.setCinematicCameraEnabled(true);
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        ShakenStirClient.SHAKE_CONTENT_HUD.tick();
        ShakenStirClient.GLASSWARE_HANDLER.tick();
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        int button = event.getButton();
        boolean down = event.getAction() == InputConstants.PRESS;
        if (ShakenStirClient.DECORATE_PLACE_HANDLER.onMousePressed(button, down)) {
            event.setCanceled(true);
        };
    }

    @SubscribeEvent
    public static void onRenderOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (event.getLevel().getBlockEntity(event.getBlockPos()) instanceof GlasswareBlockEntity blockEntity) {
            event.addCustomRenderer(new GlasswareOutlineRenderer(blockEntity));
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    static class Register {

        @SubscribeEvent
        public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
            event.register(
                    Identifier.fromNamespaceAndPath(ShakenStir.MODID, "spirit_special"),
                    SpiritBottleSpecialRenderer.Unbaked.MAP_CODEC
            );
        }

        @SubscribeEvent
        public static void registerSpecialBlockRenderers(RegisterBlockModelsEvent event) {
//        event.register(
//                new SpecialBlockModelWrapper.Unbaked<>(
//                        new ShakeItemSpecialRenderer.Unbaked(Transformation.IDENTITY),
//                        Optional.empty()
//                ),
//                BlockRegistries.SHAKE_BLOCK.get()
//        );
        }

        @SubscribeEvent
        public static void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(
                    BlockEntityRegistries.SHAKE_BLOCK_ENTITY.get(),
                    ShakeBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(
                    BlockEntityRegistries.GLASSWARE_BLOCK_ENTITY.get(),
                    GlasswareBlockEntityRenderer::new
            );
        }

        @SubscribeEvent // on the mod event bus only on the physical client
        public static void registerConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
            event.register(
                    // The name to reference as the type
                    Identifier.fromNamespaceAndPath(ShakenStir.MODID, "has_cup"),
                    // The map codec
                    HasCup.MAP_CODEC
            );
        }

        @SubscribeEvent
        public static void registerModelLoader(ModelEvent.RegisterLoaders event) {
            event.register(GlasswareUnbakedModelLoader.ID, GlasswareUnbakedModelLoader.INSTANCE);
        }

        @SubscribeEvent
        public static void registerModels(ModelEvent.RegisterStandalone event) {
            for (Models value : Models.values()) {
                event.register(
                        value.key(),
                        SimpleUnbakedStandaloneModel.quadCollection(
                                // The model id, relative to `assets/<namespace>/models/<path>.json`
                                value.getLocation()
                        )
                );
            }
            Models.buildModelsFromResourcePack();
            for (Models.Mutable resourcePackModel : Models.resourcePackModels.values()) {
                event.register(
                        resourcePackModel.key(),
                        SimpleUnbakedStandaloneModel.quadCollection(
                                resourcePackModel.location()
                        )
                );
            }
            for (Models.Glassware glasswareModel : Models.glasswareModels.values()) {
                event.register(
                        glasswareModel.key(),
                        GlasswareQuadCollection.collect(glasswareModel.location())
                );
            }
        }

        @SubscribeEvent
        public static void registerHUD(RegisterGuiLayersEvent event) {
            event.registerAboveAll(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "shake_content_hud"), ShakenStirClient.SHAKE_CONTENT_HUD);
        }

        @SubscribeEvent
        public static void registerBlockColorHandlers(RegisterColorHandlersEvent.BlockTintSources event) {
            event.register(
                    List.of(
                            new BlockTintSource() {
                                @Override
                                public int color(BlockState state) {
                                    return 0xFFFFFFFF;
                                }

                                @Override
                                public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                                    if (level.getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity) {
                                        return blockEntity.getColor();
                                    }
                                    return BlockTintSource.super.colorInWorld(state, level, pos);
                                }
                            }
                    ),
                    BlockRegistries.LONG_DRINK_GLASSWARE.get(),
                    BlockRegistries.SHORT_DRINK_GLASSWARE.get()
            );
        }

        @SubscribeEvent
        public static void registerTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(ShakeTooltipComponent.class, ClientShakeTooltipComponent::new);
        }

    }

    public static Result onMouseMove(final double yaw, final double pitch) {
        for (BiFunction<Double, Double, Result> mouseMove : ClickInteractions.mouseMoves) {
            Result result = mouseMove.apply(yaw, -pitch);
            if (result.cancelled()) {
                return result;
            }
        }
        return Result.empty();
    }

}
