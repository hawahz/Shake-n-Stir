package io.github.hawah.shakenstir.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Shake;
import io.github.hawah.shakenstir.content.blockEntity.ShakeBlockEntity;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.gui.GuiLayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ShakeContentHud implements GuiLayer {

    public static final int FADE_TIME = 10;
    FluidSim fluidSim = new FluidSim(100, 100, 100, 100);
    boolean wasVisible = false;
    static boolean oCanLookThrough = false;
    static boolean canLookThrough = false;
    double lastVisibleTime = -1;
    double height = 0;
    BlockPos prevPos;
    ShakeBlockEntity cachedBE;

    public static boolean isVisible() {
        if (ClientDataHolder.Picker.type().equals(HitResult.Type.MISS)) {
            return false;
        }
        boolean visible = BlockRegistries.SHAKE_BLOCK.get().equals(ClientDataHolder.Picker.block().orElse(null));
        if (visible) {
            BlockState state = ClientDataHolder.Picker.blockState().get();
            if (state.getValue(Shake.FACING).getAxis().isHorizontal()) {
                return false;
            }
            oCanLookThrough = canLookThrough;
            canLookThrough = state.getValue(Shake.FACING).equals(Direction.DOWN);
        }
        return visible;
    }

    public void tick() {
//        fluidSim.tick();
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        float renderTime = AnimationTickHolder.getRenderTime();
        boolean isVisible = isVisible();
        if (!isVisible) {
            lastVisibleTime = renderTime;
        }
        BlockPos pos = ClientDataHolder.Picker.pos();
        if (isVisible && (prevPos == null || !prevPos.equals(pos)) && pos != null) {
            if (getLevel().getBlockEntity(pos) instanceof ShakeBlockEntity blockEntity) {
                cachedBE = blockEntity;
            }
        }
        Window window = Minecraft.getInstance().getWindow();
        int windowWidth = window.getGuiScaledWidth();
        int windowHeight = window.getGuiScaledHeight();
        int x = windowWidth / 2 + Textures.SHAKE_HUD_INSIDE.getWidth()/2;
        int y = windowHeight / 2 - Textures.SHAKE_HUD_INSIDE.getHeight()/2;

        if (isVisible){
            double fadeProcess = (renderTime - lastVisibleTime) / FADE_TIME;
            float fadeIn = EaseHelper.easeOutPow((float) Mth.clamp(fadeProcess, 0, 1), 2);

            if (canLookThrough) {
                renderShakeWithContent(guiGraphics, deltaTracker, x, y, fadeIn);
            }

            if (!canLookThrough) {
                Textures.SHAKE_HUD_FRONT.blit(
                        guiGraphics,
                        x,
                        y,
                        255,
                        255,
                        255,
                        (int) (255 * fadeIn * (oCanLookThrough ? deltaTracker.getGameTimeDeltaPartialTick(false): 1))
                );
            }

        }
        wasVisible = isVisible;
    }

    private void renderShakeWithContent(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, int x, int y, float fadeIn) {
        Textures.SHAKE_HUD_INSIDE.blit(
                guiGraphics,
                x,
                y,
                255,
                255,
                255,
                (int) (255 * fadeIn)
        );
        if (wasVisible) {
            height = Mth.lerp(
                    ShakenStirClient.ANI_DELTAF * deltaTracker.getGameTimeDeltaPartialTick(false) * 0.1,
                    height,
                    getLiquidHeight()
            );
        } else {
            height = getLiquidHeight();
        }

        if (cachedBE != null) {
            for (int i = 0; i < cachedBE.getItemToRender().size(); i++) {
                ItemStack itemStack = cachedBE.getItemToRender().get(i);
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

        int iceCubeCounts = cachedBE.iceCubeCounts;
        float renderTime = height == 0? 0: AnimationTickHolder.getRenderTime() / 10;
        if (iceCubeCounts > 0) {
            Textures.ICE_HUD_0.blit(guiGraphics, x + 10, y + 66+ (int) (-height + 2 * Math.sin(renderTime)));
        }
        iceCubeCounts --;
        if (iceCubeCounts > 0) {
            Textures.ICE_HUD_1.blit(guiGraphics, x + 26, y + 66 + (int) (-height + 2 * Math.sin(renderTime + 1)));
        }
        iceCubeCounts --;
        if (iceCubeCounts > 0) {
            Textures.ICE_HUD_2.blit(guiGraphics, x + 36, y + 66 + (int) (-height + 2 * Math.sin(renderTime + 2)));
        }


        if (height > 0) {
            guiGraphics.fill(
                    x + 8,
                    y + 77 - 2 - (int) height,
                    x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8,
                    y + 77,
                    ARGB.color((int) Mth.clamp(100 * fadeIn, 0, 255), 160, 216, 239)
            );
            guiGraphics.horizontalLine(
                    x + 8,
                    x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8,
                    y + 77 - 2 - (int) height,
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

    private void submitFluidSim(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, int x, int y, boolean isVisible, float fadeIn) {
        fluidSim.setLeftWallX(x + 8);
        fluidSim.setRightWallX(x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8);
        fluidSim.setBottomY(y + 77);
        fluidSim.setWaterBaseY(y + 77 - 2);

        if (!wasVisible && isVisible) {
            fluidSim.forceSetHeight(getLiquidHeight());
        }
        fluidSim.setTargetHeight(getLiquidHeight());
        guiGraphics.enableScissor(x + 8, y - 20, x + Textures.SHAKE_HUD_INSIDE.getWidth() - 8, y + 77);
        fluidSim.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks(), fadeIn);
        guiGraphics.disableScissor();
    }

    private float getLiquidHeight() {
        return getLiquidAmount() * 70;
    }

    public float getLiquidAmount() {
        if (cachedBE == null) {
            return 0;
        }
        return cachedBE.getFluidAmount() / 1000F;
    }

    public Level getLevel() {
        return Minecraft.getInstance().level;
    }
}
