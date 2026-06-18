package io.github.hawah.shakenstir.client.clientTooltip;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.render.ClientFluidColorGetter;
import io.github.hawah.shakenstir.content.dataComponent.ShakeContentHolder;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.tooltip.ShakeTooltipComponent;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.Textures;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ClientShakeTooltipComponent(ShakeContentHolder contentHolder, int iceCounts, boolean canLookThrough) implements ClientTooltipComponent {

    public static int offsetX = 10;
    public static int offsetY = 0;
    public static double height = 0;

    public ClientShakeTooltipComponent(ShakeTooltipComponent server) {
        this(server.contentHolder(), server.iceCounts(), server.canLookThrough());
    }

    @Override
    public int getHeight(Font font) {
        return Textures.SHAKE_HUD_FRONT.getHeight() + offsetY;
    }

    @Override
    public int getWidth(Font font) {
        offsetX = 0;
        if (canLookThrough()) {
            for (FluidStack fluidStack : contentHolder().fluidStacks()) {
                offsetX = Math.max(offsetX, 3 + font.width(fluidStack.getHoverName().getString() + " " + fluidStack.getAmount() + "mb"));
            }
        }
        return Textures.SHAKE_HUD_FRONT.getWidth() + offsetX;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        x += Textures.SHAKE_HUD_FRONT.getWidth();
        ClientTooltipComponent.super.extractText(graphics, font, x, y);
        if (!canLookThrough()) {
            return;
        }
        double[] splitWeights = new double[contentHolder().fluidStacks().size()];
        for (int i = 0; i < splitWeights.length; i++) {
            splitWeights[i] = contentHolder().fluidStacks().get(i).getAmount() / (float) contentHolder().fluidVolume();
        }
        int currentHeightPrt = getHeight(font);
        for (int i = 0; i < contentHolder().fluidStacks().size(); i++) {
            FluidStack fluidStack = contentHolder.fluidStacks().get(i);
            graphics.textWithBackdrop(
                    font,
                    LangData.TOOLTIP_SHAKER_FLUID.get(fluidStack.getHoverName(), fluidStack.getAmount()),
                    x,
                    (int) (y + currentHeightPrt - splitWeights[i] * height / 2 - font.lineHeight),
                    255,
                    0xFFFFFFFF
            );
            currentHeightPrt -= (int) (splitWeights[i] * height);
        }
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        if (!canLookThrough()) {
            Textures.SHAKE_HUD_FRONT.blit(
                    graphics,
                    x,
                    y + offsetY,
                    255,
                    255,
                    255,
                    255
            );
        } else {
            extractShakeWithContent(x, y + offsetY, graphics);
        }
    }

    private void extractShakeWithContent(int x, int y, GuiGraphicsExtractor graphics) {
        float fadeIn = 1;
        Textures.SHAKE_HUD_INSIDE.blit(
                graphics,
                x,
                y,
                255,
                255,
                255,
                (int) (255 * fadeIn)
        );
        height = Mth.lerp(
                ShakenStirClient.ANI_DELTAF * Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false),
                height,
                getLiquidHeight()
        );

        if (height < 1e-20) {
            height = 0;
        }

        for (int i = 0; i < contentHolder().Item().stackToRender().size(); i++) {
            ItemStack itemStack = contentHolder().Item().stackToRender().get(i);
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

        int iceCubeCounts = iceCounts();
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
            int rgb = getLiquidColor();
            // 160, 216, 239
            graphics.fill(
                    x + 8,
                    y + 77 - 2 - (int) height,
                    x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8,
                    y + 77,
                    ARGB.color((int) Mth.clamp(100 * fadeIn, 0, 255), ARGB.red(rgb), ARGB.green(rgb), ARGB.blue(rgb))
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

    private int getLiquidColor() {
        for (ItemStack itemStack : contentHolder().itemStacks()) {
            if (itemStack.is(ItemRegistries.CONTENT_HOLDER)) {
                return itemStack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(ARGB.color(160, 216, 239))).rgb();
            }
        }
        return ShakeUtil.rgbWithWeight(contentHolder().fluidStacks().stream().map((stack) ->
                Pair.of(ClientFluidColorGetter.getColor(stack), stack.getAmount())
        ).toList());
    }

    private float getLiquidHeight() {
        for (ItemStack itemStack : contentHolder().itemStacks()) {
            if (itemStack.is(ItemRegistries.CONTENT_HOLDER)) {
                return 70;
            }
        }
        return contentHolder().fluidVolume() / 1000F * 70;
    }
}
