package io.github.hawah.shakenstir.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.hanlder.GlasswareHandlerRenderState;
import io.github.hawah.shakenstir.client.model.Models;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareQuadCollection;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareUnbakedModelLoader;
import io.github.hawah.shakenstir.client.clientTooltip.ClientShakeTooltipComponent;
import io.github.hawah.shakenstir.client.render.GlasswareOutlineRenderer;
import io.github.hawah.shakenstir.client.render.block.CabinetBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.block.DistillerBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.block.GlasswareBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.block.ShakeBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.item.GlasswareSpecialRenderer;
import io.github.hawah.shakenstir.client.render.item.ShakeItemSpecialRenderer;
import io.github.hawah.shakenstir.client.render.item.SpiritBottleSpecialRenderer;
import io.github.hawah.shakenstir.content.HasCup;
import io.github.hawah.shakenstir.content.ShakeTooltipComponent;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Cabinet;
import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.blockEntity.CabinetBlockEntity;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.foundation.networking.ServerboundHandItemDataChangedPacket;
import io.github.hawah.shakenstir.foundation.networking.ServerboundTryPickItemPacket;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.BiFunction;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {

    public static final float FOG_LERP = 0.01F;

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
        if (getLevel() == null) {
            return;
        }
        ShakenStirClient.GLASSWARE_HANDLER.tick();
        ShakenStirClient.SHAKE_HANDLER.tick();
        ShakenStirClient.CABINET_HUD.tick();
    }

    private static double cameraRoll = 0;
    private static double shakeIntensity = 0;
    @SubscribeEvent
    public static void modifyCameraRoll(ViewportEvent.ComputeCameraAngles event) {
        if (getPlayer() == null) {
            return;
        }
        if (!getPlayer().hasEffect(MobEffectRegistries.DRUNK) && !(cameraRoll > 0 && shakeIntensity > 0)) {
            return;
        }
        DeltaTracker deltaTracker = Minecraft.getInstance().getDeltaTracker();
        float deltaTicks = deltaTracker.getGameTimeDeltaTicks();
        int amplifier = getPlayer().hasEffect(MobEffectRegistries.DRUNK) ? getPlayer().getEffect(MobEffectRegistries.DRUNK).getAmplifier() : 0;
        float renderTime = AnimationTickHolder.getRenderTime();
        cameraRoll = Mth.lerp(0.01 * deltaTicks / 0.68, cameraRoll, amplifier / 3F);
        shakeIntensity = Mth.lerp(0.01 * deltaTicks / 0.68, cameraRoll, Math.max(0, amplifier - 5));
        event.setRoll(event.getRoll() + (float) (Math.sin(renderTime /20) * cameraRoll));
        event.setPitch((float) ((event.getPitch() + Math.sin(renderTime /20D) * shakeIntensity)));
        event.setYaw((float) ((event.getYaw() + Math.cos(renderTime /20D) * shakeIntensity)));
    }

    private static float cr = -1, cg = -1, cb = -1;

    @SubscribeEvent
    public static void onCameraOffset(ViewportEvent.ComputeFogColor event) {
        if (getPlayer() == null) {
            return;
        }
        float r;
        float g;
        float b;

        float deltaTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        float lerp = (float) (FOG_LERP * deltaTicks / 0.68);

        if (cr < 0 || cg < 0 || cb < 0) {
            cr = r = event.getRed();
            cg = g = event.getGreen();
            cb = b = event.getBlue();
        }

        if (getPlayer().hasEffect(MobEffectRegistries.DRUNK) && getPlayer().getEffect(MobEffectRegistries.DRUNK).getAmplifier() >= 3) {
            r = 255 / 255F;
            g = 109/255F;
            b = 120/255F;
        } else {
            r = event.getRed();
            g = event.getGreen();
            b = event.getBlue();
        }
        if (r != cr || g != cg || b != cb) {
            cr = Mth.lerp(lerp, cr, r);
            cg = Mth.lerp(lerp, cg, g);
            cb = Mth.lerp(lerp, cb, b);
        }
        event.setRed    (cr);
        event.setGreen  (cg);
        event.setBlue   (cb);
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        boolean pickBlock = event.isPickBlock();
        LocalPlayer player = getPlayer();
        if (pickBlock && player != null) {
            if (player.isShiftKeyDown()) {
                return;
            }
            ClientLevel level = Minecraft.getInstance().level;
            if (Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult && level != null) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = level.getBlockState(pos);
                if (state.is(BlockRegistries.CABINET)){
                    Direction facing = state.getValue(Cabinet.FACING);
                    if (hitResult.getDirection().getOpposite() != facing) {
                        return;
                    }
                    int index = Cabinet.getSlot(pos, hitResult, facing);
                    if (!(level.getBlockEntity(pos) instanceof CabinetBlockEntity blockEntity)) {
                        return;
                    }
                    ItemStack itemStack = blockEntity.contents.get(index);
                    if (itemStack.isEmpty()) {
                        return;
                    }
                    Networking.sendToServer(new ServerboundTryPickItemPacket(itemStack.copy()));
                    event.setCanceled(true);
                }
            }
        }
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
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = getPlayer();
        if (player == null || !(player.getMainHandItem().getItem() instanceof GlasswareItem) || !player.isShiftKeyDown()) {
            return;
        }
        double scrollDeltaY = event.getScrollDeltaY();
        player.getMainHandItem().set(DataComponentTypeRegistries.GLASSWARE_ROTATION, (float) (player.getMainHandItem().getOrDefault(DataComponentTypeRegistries.GLASSWARE_ROTATION, 0F) + scrollDeltaY * 10));
        Networking.sendToServer(new ServerboundHandItemDataChangedPacket(player.getUUID(), InteractionHand.MAIN_HAND, player.getMainHandItem()));
        event.setCanceled(true);
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
            event.register(
                    Identifier.fromNamespaceAndPath(ShakenStir.MODID, "glassware_special"),
                    GlasswareSpecialRenderer.Unbaked.MAP_CODEC
            );
            event.register(
                    Identifier.fromNamespaceAndPath(ShakenStir.MODID, "shake_special"),
                    ShakeItemSpecialRenderer.Unbaked.MAP_CODEC
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
            event.registerBlockEntityRenderer(
                    BlockEntityRegistries.CABINET_BLOCK_ENTITY.get(),
                    CabinetBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(
                    BlockEntityRegistries.DISTILLER_BLOCK_ENTITY.get(),
                    DistillerBlockEntityRenderer::new
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
            event.registerAboveAll(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "cabinet_hud"), ShakenStirClient.CABINET_HUD);
            event.registerAboveAll(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "distiller_hud"), ShakenStirClient.DISTILLER_HUD);
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

    private static boolean isLevelReady() {
        return getLevel() != null;
    }

    private static boolean isPlayerReady() {
        return getPlayer() != null;
    }

    private static @Nullable ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }

    private static @Nullable LocalPlayer getPlayer() {
        return Minecraft.getInstance().player;
    }

}
