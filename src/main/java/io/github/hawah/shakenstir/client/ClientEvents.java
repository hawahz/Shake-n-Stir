package io.github.hawah.shakenstir.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.render.block.ShakeBlockEntityRenderer;
import io.github.hawah.shakenstir.client.render.item.ShakeItemSpecialRenderer;
import io.github.hawah.shakenstir.client.render.item.SpiritBottleSpecialRenderer;
import io.github.hawah.shakenstir.content.HasCup;
import io.github.hawah.shakenstir.content.block.Shake;
import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.lib.client.render.toolkit.TransformWarper;
import io.github.hawah.shakenstir.util.Models;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;

import java.util.function.BiFunction;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterTranslucentParticles event) {
        ShakenStirClient.TIMER_NORMAL.warp(Minecraft.getInstance().getDeltaTracker());
    }

    @SubscribeEvent
    public static void modifyFov(ComputeFovModifierEvent event) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().screen != null) {
            return;
        }
        BlockPos pos = ClientDataHolder.Picker.pos();
        if (pos == null) {
            return;
        }
        BlockState state = Minecraft.getInstance().level.getBlockState(pos);
        if (state.getBlock() instanceof Shake && Minecraft.getInstance().hasAltDown()) {
            event.setNewFovModifier(event.getFovModifier() / 2);

        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        ShakenStirClient.SHAKE_CONTENT_HUD.tick();
    }

    @SubscribeEvent
    public static void modifyTurnSensitivity(CalculatePlayerTurnEvent event) {
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    static class Register {

        @SubscribeEvent
        public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
            event.register(
                    Identifier.fromNamespaceAndPath(ShakenStir.MODID, "shake_special"),
                    ShakeItemSpecialRenderer.Unbaked.MAP_CODEC
            );
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
                        value.getKey(),
                        SimpleUnbakedStandaloneModel.quadCollection(
                                // The model id, relative to `assets/<namespace>/models/<path>.json`
                                value.getLocation()
                        )
                );
            }
        }

        @SubscribeEvent
        public static void registerHUD(RegisterGuiLayersEvent event) {
            event.registerAboveAll(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "shake_content_hud"), ShakenStirClient.SHAKE_CONTENT_HUD);
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
