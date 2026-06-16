package io.github.hawah.shakenstir.client.event;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.clientTooltip.ClientShakeTooltipComponent;
import io.github.hawah.shakenstir.client.clientTooltip.ClientWarpedMintTooltip;
import io.github.hawah.shakenstir.client.hanlder.MenuHUD;
import io.github.hawah.shakenstir.client.model.Models;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareQuadCollection;
import io.github.hawah.shakenstir.client.model.glassware.GlasswareUnbakedModelLoader;
import io.github.hawah.shakenstir.client.render.block.*;
import io.github.hawah.shakenstir.client.render.entity.BartenderRenderer;
import io.github.hawah.shakenstir.client.render.item.GlasswareSpecialRenderer;
import io.github.hawah.shakenstir.client.render.item.ShakeItemSpecialRenderer;
import io.github.hawah.shakenstir.client.render.item.SpiritBottleSpecialRenderer;
import io.github.hawah.shakenstir.content.*;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.dataAttachment.DataAttachmentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.WarpedMint;
import io.github.hawah.shakenstir.content.entity.EntityTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.utils.ContextKeys;
import io.github.hawah.shakenstir.lib.client.gui.KeyTipHUD;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientRegistryEvents {

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
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState renderState) {
                int data = avatar.getData(DataAttachmentTypeRegistries.FALL_DOWN);

                renderState.setRenderData(ContextKeys.FALLDOWN, data);
            }
        });
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
        event.registerBlockEntityRenderer(
                BlockEntityRegistries.BAR_MENU_BLOCK_ENTITY.get(),
                BarMenuBlockEntityRenderer::new
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
        event.register(
                // The name to reference as the type
                Identifier.fromNamespaceAndPath(ShakenStir.MODID, "warped"),
                // The map codec
                Warped.MAP_CODEC
        );
        event.register(
                // The name to reference as the type
                Identifier.fromNamespaceAndPath(ShakenStir.MODID, "mint_size"),
                // The map codec
                MintSize.MAP_CODEC
        );
        event.register(
                ShakenStir.asResource("wawrped_mint_display"),
                WarpedMintDisplay.MAP_CODEC
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
        event.registerAboveAll(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "bar_menu_hud"), ShakenStirClient.MENU_HUD);
        event.registerAboveAll(Identifier.fromNamespaceAndPath(ShakenStir.MODID, "key_tip_hud"), new KeyTipHUD());
    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.registerCategory(MenuHUD.MENU_CAT);
        event.register(MenuHUD.OPEN_MENU.get());
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
        event.register(WarpedMintTooltip.class, ClientWarpedMintTooltip::new);
    }

    @SubscribeEvent
    public static void registerChunkCacheElement(AddSectionGeometryEvent event) {
//            event.addRenderer(context -> {
//                context.getOrCreateChunkBuffer()
//            });
    }

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityTypeRegistries.BARTENDER.get(), BartenderRenderer::new);
    }

    @SubscribeEvent
    public static void registerSprite(TextureAtlasStitchedEvent event) {

    }

    @SubscribeEvent
    public static void registerRenderer(RegisterPictureInPictureRenderersEvent event) {
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(ItemRegistries.STACKED_MINT, new IItemDecorator() {
            @Override
            public boolean render(GuiGraphicsExtractor guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
                WarpedMint warpedMint = stack.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint());
                int variety = warpedMint.variety();
                itemTextDeco(guiGraphics, font, xOffset, yOffset - 18 + font.lineHeight, String.valueOf(variety));
                return true;
            }

            private void itemTextDeco(GuiGraphicsExtractor graphics, Font font, int x, int y, String countText) {
                int count = Integer.parseInt(countText);
                graphics.text(
                        font,
                        countText,
                        x + 19 - 2 - font.width(countText),
                        y + 6 + 3,
                        ARGB.color(255, count == 0 ? 0xFFFF00 : -1),
                        true
                );
            }
        });
    }

}
