package io.github.hawah.shakenstir.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.render.toolkit.Animation;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.gui.GuiLayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ShakeContentHud implements GuiLayer {

    FluidSim fluidSim = new FluidSim(100, 100, 100, 100);

    public ShakeContentHud() {
//        super(Component.empty());
    }

    double lastVisibleTime = -1;

    public static boolean isVisible() {
        if (ClientDataHolder.Picker.type().equals(HitResult.Type.MISS)) {
            return false;
        }
        return BlockRegistries.SHAKE_BLOCK.get().equals(ClientDataHolder.Picker.block().orElse(null));
    }

    public void tick() {
        fluidSim.tick();
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        float renderTime = AnimationTickHolder.getRenderTime();
        fluidSim.setTargetHeight(30);
        if (!isVisible()) {
            lastVisibleTime = renderTime;
            return;
        }
        Window window = Minecraft.getInstance().getWindow();
        int windowWidth = window.getGuiScaledWidth();
        int windowHeight = window.getGuiScaledHeight();
        int x = windowWidth / 2 + Textures.SHAKE_HUD_INSIDE.getWidth()/2;
        int y = windowHeight / 2 - Textures.SHAKE_HUD_INSIDE.getHeight()/2;
        float fadeIn = EaseHelper.easeOutPow((float) Mth.clamp((renderTime - lastVisibleTime) / 10, 0, 1), 2);


        fluidSim.setLeftWallX(x + 8);
        fluidSim.setRightWallX(x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8);
        fluidSim.setBottomY(y + 77);
        fluidSim.setWaterBaseY(y + 77 - 2);

        Textures.SHAKE_HUD_INSIDE.blit(
                guiGraphics,
                x,
                y,
                255,
                255,
                255,
                (int) (255 * fadeIn)
        );
        guiGraphics.enableScissor(x + 8, y, x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8, y + 77);
        fluidSim.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks(), fadeIn);
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
}
