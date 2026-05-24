package io.github.hawah.shakenstir.client.render.general;

import io.github.hawah.shakenstir.content.blockEntity.ShakeBlockEntity;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuiShakeRenderer {
    public static void renderShakeWithContent(GuiGraphicsExtractor guiGraphics,
                                        DeltaTracker deltaTracker,
                                        int x,
                                        int y,
                                        float fadeIn,
                                        RenderState state) {
        if (!state.canLookThrough()) {
            Textures.SHAKE_HUD_FRONT.blit(
                    guiGraphics,
                    x,
                    y,
                    255,
                    255,
                    255,
                    (int) (255 * fadeIn * (state.oCanLookThrough ? deltaTracker.getGameTimeDeltaPartialTick(false): 1))
            );
        }
        Textures.SHAKE_HUD_INSIDE.blit(
                guiGraphics,
                x,
                y,
                255,
                255,
                255,
                (int) (255 * fadeIn)
        );

        if (state.cachedBE != null) {
            for (int i = 0; i < state.cachedBE.getItemToRender().size(); i++) {
                ItemStack itemStack = state.cachedBE.getItemToRender().get(i);
                if (itemStack.isEmpty()) {
                    break;
                }
                guiGraphics.item(
                        itemStack,
                        x + 21,
                        y + 53 - i * 16
                );
            }
        }

        guiGraphics.enableScissor(x + 8, y - 10, x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8, y + 77);

        int iceCubeCounts = 0;
        if (state.cachedBE != null) {
            iceCubeCounts = state.cachedBE.iceCubeCounts;
        }
        float renderTime = state.height == 0? 0: AnimationTickHolder.getRenderTime() / 10;
        if (iceCubeCounts > 0) {
            Textures.ICE_HUD_0.blit(guiGraphics, x + 10, y + 66+ (int) (-state.height + 2 * Math.sin(renderTime)));
        }
        iceCubeCounts --;
        if (iceCubeCounts > 0) {
            Textures.ICE_HUD_1.blit(guiGraphics, x + 26, y + 66 + (int) (-state.height + 2 * Math.sin(renderTime + 1)));
        }
        iceCubeCounts --;
        if (iceCubeCounts > 0) {
            Textures.ICE_HUD_2.blit(guiGraphics, x + 36, y + 66 + (int) (-state.height + 2 * Math.sin(renderTime + 2)));
        }


        if (state.height > 0) {
            guiGraphics.fill(
                    x + 8,
                    y + 77 - 2 - (int) state.height,
                    x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8,
                    y + 77,
                    ARGB.color((int) Mth.clamp(100 * fadeIn, 0, 255), 160, 216, 239)
            );
            guiGraphics.horizontalLine(
                    x + 8,
                    x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8,
                    y + 77 - 2 - (int) state.height,
                    ARGB.color(160, 216, 239, (int) Mth.clamp(255 * fadeIn, 0, 255))
            );
        }

        guiGraphics.disableScissor();

        Textures.SHAKE_HUD_OUTSIDE.blit(
                guiGraphics,
                x,
                y,
                255,
                255,
                255,
                (int) (255 * fadeIn)
        );
    }

    private static float getLiquidHeight(RenderState state) {
        return getLiquidAmount(state) * 70;
    }

    public static float getLiquidAmount(RenderState state) {
        if (state.cachedBE == null) {
            return 0;
        }
        return state.cachedBE.getFluidAmount() / 1000F;
    }

    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }

    public record RenderState(double height, ShakeBlockEntity cachedBE, boolean canLookThrough, boolean oCanLookThrough) {

    }
}
