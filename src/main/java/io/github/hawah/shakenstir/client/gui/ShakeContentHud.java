package io.github.hawah.shakenstir.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.client.render.general.GuiShakeRenderer;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Shaker;
import io.github.hawah.shakenstir.content.blockEntity.ShakeBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.ShakeContentHolder;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.BaseFluidType;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import io.github.hawah.shakenstir.lib.client.render.EaseHelper;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import io.github.hawah.shakenstir.util.Textures;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.gui.GuiLayer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ShakeContentHud implements GuiLayer {

    public static final int FADE_TIME = 10;
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
            if (state.getValue(Shaker.FACING).getAxis().isHorizontal()) {
                return false;
            }
            oCanLookThrough = canLookThrough;
            canLookThrough = state.getValue(Shaker.FACING).equals(Direction.DOWN);
        }
        return visible;
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

    private void renderShakeWithContent(GuiGraphicsExtractor guiGraphics,
                                        DeltaTracker deltaTracker,
                                        int x,
                                        int y,
                                        float fadeIn) {
        if (wasVisible) {
            height = Mth.lerp(
                    ShakenStirClient.ANI_DELTAF * deltaTracker.getGameTimeDeltaPartialTick(false) * 0.1,
                    height,
                    getLiquidHeight()
            );
        } else {
            height = getLiquidHeight();
        }

        int iceCubeCounts = cachedBE.iceCubeCounts;

        ShakeContentHolder contentHolder = ShakeContentHolder.of(cachedBE.getFluidStack(), cachedBE.getItemToRender());

        GuiShakeRenderer.extractShakeWithContent(
                x,
                y,
                guiGraphics,
                contentHolder,
                iceCubeCounts,
                height,
                getLiquidColor(contentHolder)
        );
    }

    private int getLiquidColor(ShakeContentHolder contentHolder) {
        for (ItemStack itemStack : contentHolder.itemStacks()) {
            if (itemStack.is(ItemRegistries.CONTENT_HOLDER)) {
                return itemStack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(ARGB.color(160, 216, 239))).rgb();
            }
        }
        return ShakeUtil.rgbWithWeight(contentHolder.fluidStacks().stream().map((stack) ->
                Pair.of(stack.getFluidType() instanceof BaseFluidType type ? type.getTintColor() : 0xFFFFFF, stack.getAmount())
        ).toList());
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
