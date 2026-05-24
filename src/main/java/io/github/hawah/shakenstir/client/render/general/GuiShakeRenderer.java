package io.github.hawah.shakenstir.client.render.general;

import io.github.hawah.shakenstir.content.dataComponent.ShakeContentHolder;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class GuiShakeRenderer {
    public static void extractShakeWithContent(int x, int y, GuiGraphicsExtractor graphics, ShakeContentHolder contentHolder, int iceCounts, double height, int liquidColor, float fadeIn) {
        x += Textures.SHAKE_HUD_FRONT.getWidth();
        double[] splitWeights = new double[contentHolder.fluidStacks().size()];
        for (int i = 0; i < splitWeights.length; i++) {
            splitWeights[i] = contentHolder.fluidStacks().get(i).getAmount() / (float) contentHolder.fluidVolume();
        }
        Font font = Minecraft.getInstance().font;
        int currentHeightPrt = Textures.SHAKE_HUD_FRONT.getHeight();
        for (int i = 0; i < contentHolder.fluidStacks().size(); i++) {
            FluidStack fluidStack = contentHolder.fluidStacks().get(i);
            graphics.textWithBackdrop(
                    font,
                    LangData.TOOLTIP_SHAKER_FLUID.get(fluidStack.getHoverName(), fluidStack.getAmount()),
                    x,
                    (int) (y + currentHeightPrt - splitWeights[i] * height / 2 - font.lineHeight),
                    255,
                    0x00FFFFFF | ((int) (255 * fadeIn) << 24)
            );
            currentHeightPrt -= (int) (splitWeights[i] * height);
        }

        x -= Textures.SHAKE_HUD_FRONT.getWidth();


        Textures.SHAKE_HUD_INSIDE.blit(
                graphics,
                x,
                y,
                255,
                255,
                255,
                (int) (255 * fadeIn)
        );

        for (int i = 0; i < contentHolder.Item().stackToRender().size(); i++) {
            ItemStack itemStack = contentHolder.Item().stackToRender().get(i);
            if (itemStack.isEmpty()) {
                break;
            }
            graphics.item(
                    itemStack,
                    x + 21,
                    y + 53 - i * 16
            );
        }

        graphics.enableScissor(x + 8, y - 10, x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8, y + 77);

        int iceCubeCounts = iceCounts;
        float renderTime = height == 0? 0: AnimationTickHolder.getRenderTime() / 10;
        if (iceCubeCounts > 0) {
            Textures.ICE_HUD_0.blit(graphics, x + 10, y + 66+ (int) (-height + 2 * Math.sin(renderTime)));
        }
        iceCubeCounts --;
        if (iceCubeCounts > 0) {
            Textures.ICE_HUD_1.blit(graphics, x + 26, y + 66 + (int) (-height + 2 * Math.sin(renderTime + 1)));
        }
        iceCubeCounts --;
        if (iceCubeCounts > 0) {
            Textures.ICE_HUD_2.blit(graphics, x + 36, y + 66 + (int) (-height + 2 * Math.sin(renderTime + 2)));
        }


        if (height > 0) {
            // 160, 216, 239
            graphics.fill(
                    x + 8,
                    y + 77 - 2 - (int) height,
                    x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8,
                    y + 77,
                    ARGB.color((int) Mth.clamp(100 * fadeIn, 0, 255), ARGB.red(liquidColor), ARGB.green(liquidColor), ARGB.blue(liquidColor))
            );
            graphics.horizontalLine(
                    x + 8,
                    x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8,
                    y + 77 - 2 - (int) height,
                    ARGB.color(160, 216, 239, (int) Mth.clamp(255 * fadeIn, 0, 255))
            );
        }

        graphics.disableScissor();

        Textures.SHAKE_HUD_OUTSIDE.blit(
                graphics,
                x,
                y,
                255,
                255,
                255,
                (int) (255 * fadeIn)
        );
    }
}
