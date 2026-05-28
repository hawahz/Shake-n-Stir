package io.github.hawah.shakenstir.client.gui;

import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.ClickInteractions;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.content.entity.ai.behavior.recipeProvider.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.networking.ServerboundMenuBEChanged;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.Result;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import static io.github.hawah.shakenstir.client.gui.MC.getLevel;

public class MenuHUD extends AbstractBlockTargetHUD{

    public MenuHUD() {
        ClickInteractions.registerMouseMove(this::onMouseMove);
    }

    public BarMenuBlockEntity cachedEntity;
    float fadeInProgress = 0;
    float currentSelect = -1;
    float targetSelect = -1;
    int currentIndex = -1;
    double counts = 0;
    double price = 0;

    @Override
    protected boolean isVisible() {
        return super.isVisible() && Minecraft.getInstance().hasControlDown();
    }

    @Override
    protected @NonNull Block block() {
        return BlockRegistries.BAR_MENU_BLOCK.get();
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!isVisible()) {
            cachedEntity = null;
            fadeInProgress = 0;
            currentSelect = -1;
            targetSelect = -1;
            currentIndex = -1;
            return;
        }

        BlockPos pos = ClientDataHolder.Picker.pos();
        if (getLevel() == null || pos == null) {
            return;
        }

        if (cachedEntity == null) {
            if (getLevel().getBlockEntity(pos) instanceof BarMenuBlockEntity blockEntity) {
                cachedEntity = blockEntity;
            } else {
                return;
            }
        }

        fadeInProgress = Mth.lerp(ShakenStirClient.ANI_DELTAF * deltaTracker.getGameTimeDeltaTicks(), fadeInProgress, 1);
        if (currentSelect >= 0 && targetSelect >= 0) {
            currentSelect = Mth.lerp(ShakenStirClient.ANI_DELTAF * deltaTracker.getGameTimeDeltaTicks(), currentSelect, targetSelect);
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate( 0, (1- fadeInProgress) * 24);
        pose.rotateAbout((float) Math.toRadians(-Minecraft.getInstance().player.getYRot() + cachedEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()), screenWidth / 2, screenHeight/2);

        BaseScreen.blit(
                guiGraphics,
                Textures.MENU_BKG.getResource(),
                screenWidth/2 - Textures.MENU_BKG.getWidth()/2,
                screenHeight/2 - Textures.MENU_BKG.getHeight()/2,
                Textures.MENU_BKG.getStartX(),
                Textures.MENU_BKG.getStartY(),
                Textures.MENU_BKG.getWidth(),
                Textures.MENU_BKG.getHeight(),
                0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)
        );
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.font.lineHeight;
        int DISTANCE_TOP = 50;
        int xRight = screenWidth / 2 + (Textures.MENU_BKG.getWidth() / 2 - 10);
        int xLeft = screenWidth / 2 - (Textures.MENU_BKG.getWidth() / 2 -  10);
        for (int i = 0; i < cachedEntity.recipes.size(); i++) {
            SnsRecipeHolder snsRecipeHolder = cachedEntity.recipes.get(i).left();
            BarMenuBlockEntity.PriceAndCount priceAndCount = cachedEntity.recipes.get(i).right();
            int length = mc.font.width(String.valueOf(priceAndCount.price));
            int y = screenHeight / 2 - Textures.MENU_BKG.getHeight() / 2 + DISTANCE_TOP + lineHeight * 2 * i;
            int DIST = 5;
            guiGraphics.text(
                    mc.font,
                    snsRecipeHolder.name(),
                    xLeft + DIST,
                    y,
                    0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)
            );

            guiGraphics.text(
                    mc.font,
                    String.valueOf(priceAndCount.price),
                    (xRight) - DIST - length,
                    y,
                    0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)
            );
            if (priceAndCount.count > 0) {
                int nameLength = mc.font.width(snsRecipeHolder.name());
                guiGraphics.itemDecorations(
                        Minecraft.getInstance().font,
                        ItemRegistries.CONTENT_HOLDER.toStack(),
                        xLeft + DIST + nameLength - 11,
                        y - lineHeight/2,
                        String.valueOf(priceAndCount.count)
                );
            }
        }

        if (mc.hasAltDown()) {
            if (currentSelect >= 0 && targetSelect >= 0) {
                pose.pushMatrix();
                pose.translate(0, currentSelect);
                int x = screenWidth / 2;
                int y = screenHeight / 2 - Textures.MENU_BKG.getHeight() / 2 + DISTANCE_TOP + lineHeight / 2;
                int width = Textures.MENU_BKG.getWidth() / 2 - 10;

                float intensity = 0.1F;
                float v = Mth.clamp(Math.abs(Mth.inverseLerp(currentSelect, targetSelect - Math.signum(targetSelect - currentSelect) * lineHeight * 2, targetSelect) - 0.5F) * 2 * intensity + 1 - intensity, 0, 1);

                extractRect(guiGraphics, x, y, (int) (lineHeight * 2 * v * v * v), (int) (width * v));
                pose.popMatrix();
            }
        }

        if (isOwner()) {
            guiGraphics.text(
                    mc.font,
                    "Editing...",
                    screenWidth/2 - Textures.MENU_BKG.getWidth()/2 + 10,
                    screenHeight/2 - Textures.MENU_BKG.getHeight()/2 + 10,
                    0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)
            );
        }



        pose.popMatrix();

    }

    private void extractRect(GuiGraphicsExtractor guiGraphics, int x, int y, int height, int width) {
        int yTop = y - height/2;
        int yBottom = y + height/2;
        int xLeft = x + width;
        int xRight = x - width;
        guiGraphics.horizontalLine(
                xLeft,
                xRight,
                yTop,
                0x00FFFFFF | ((int) (0xF3 * fadeInProgress) << 24)
        );
        guiGraphics.horizontalLine(
                xLeft,
                xRight,
                yBottom,
                0x00FFFFFF | ((int) (0xF3 * fadeInProgress) << 24)
        );
        guiGraphics.verticalLine(
                xLeft,
                yTop,
                yBottom,
                0x00FFFFFF | ((int) (0xF3 * fadeInProgress) << 24)
        );
        guiGraphics.verticalLine(
                xRight,
                yTop,
                yBottom,
                0x00FFFFFF | ((int) (0xF3 * fadeInProgress) << 24)
        );
    }

    private boolean isOwner() {
        return cachedEntity.getPlacerId().equals(Minecraft.getInstance().player.getUUID());
    }

    public boolean onMousePressed(int button, boolean pressed) {
        if (!isVisible()) {
            return false;
        }
        return false;
    }

    public boolean onMouseScroll(double delta) {
        if (!isVisible()) {
            return false;
        }
        if (Minecraft.getInstance().hasAltDown() && !cachedEntity.recipes.isEmpty()) {
            if (currentIndex < 0) {
                targetSelect = 0;
                currentSelect = targetSelect;
                currentIndex = 0;
                return true;
            }
            int lineHeight = Minecraft.getInstance().font.lineHeight;
            int currentPtr = (int) (currentIndex - delta);
            if (currentPtr < cachedEntity.recipes.size() && currentPtr >= 0) {
                currentIndex = currentPtr;
                targetSelect = lineHeight * 2 * currentIndex;
                counts = cachedEntity.recipes.get(getCurrentIndex()).right().count;
                price = cachedEntity.recipes.get(currentPtr).right().price;
            }
            return true;
        } else {
            return false;
        }
    }

    private int getCurrentIndex() {
        return currentIndex;
    }

    public Result onMouseMove(final double yaw, final double pitch) {
        if (!isVisible()) {
            return Result.empty();
        }
        if (Minecraft.getInstance().hasAltDown() && getCurrentIndex() >= 0) {
            BarMenuBlockEntity.PriceAndCount priceAndCount = cachedEntity.recipes.get(getCurrentIndex()).right();
            if (!isOwner()) {
                counts += pitch / 50;
                counts = Mth.clamp(counts, 0, 4);
                priceAndCount.count = (int) counts;
            } else {
                price += pitch / 50;
                price = Math.max(price, 0);
                priceAndCount.price = (int) price;
            }
            Networking.sendToServer(new ServerboundMenuBEChanged(priceAndCount, currentIndex, ClientDataHolder.Picker.pos()));
            return new Result(true);
        }
        return Result.empty();
    }
}
