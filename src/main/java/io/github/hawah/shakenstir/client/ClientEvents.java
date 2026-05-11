package io.github.hawah.shakenstir.client;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.render.GlasswareOutlineRenderer;
import io.github.hawah.shakenstir.client.render.block.GlasswareBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.block.ShakeBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.item.SpiritBottleSpecialRenderer;
import io.github.hawah.shakenstir.content.HasCup;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.lib.client.render.outliner.Outliner;
import io.github.hawah.shakenstir.util.Models;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

        Outliner.renderInto(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), Minecraft.getInstance().player.getEyePosition(), Minecraft.getInstance().getDeltaTracker());
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
        GlasswareRaycast.clearCache();
        ShakenStirClient.SHAKE_CONTENT_HUD.tick();
    }

    @SubscribeEvent
    public static void onRenderOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (event.getLevel().getBlockEntity(event.getBlockPos()) instanceof GlasswareBlockEntity blockEntity) {
            Vec3 hitLocation = event.getHitResult().getLocation();
            BlockPos blockPos = event.getBlockPos();
            Vec3 source = event.getCamera().position();

            GlasswareRaycast result = GlasswareRaycast.checkHitGlasswareDirect(blockEntity, blockPos, source, hitLocation);

            if (result.direction() != null) {
//
                event.addCustomRenderer(new GlasswareOutlineRenderer(result.localPosition(), result.rotation(), blockEntity.getModel()));
            } else {
                event.setCanceled(true);
            }
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
