package io.github.hawah.shakenstir.client.gui;

import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

public class MenuScreen extends BaseScreen {

    public static final Textures BACKGROUND = Textures.MENU_BKG;
    public static final  int DISTANCE_TOP = 50;
    private final BarMenuBlockEntity cachedBlockEntity;
    float fadeInProgress = 0;

    public MenuScreen(BarMenuBlockEntity cachedBlockEntity) {
        super(Component.empty());
        this.cachedBlockEntity = cachedBlockEntity;
    }

    float visualSelect = -1;
    int currentIndex = -1;

    @Override
    protected void renderWindowPre(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {

        DeltaTracker deltaTracker = Minecraft.getInstance().getDeltaTracker();
        fadeInProgress = Mth.lerp(ShakenStirClient.ANI_DELTAF * deltaTracker.getGameTimeDeltaTicks(), fadeInProgress, 1);
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate( 0, (1- fadeInProgress) * 24);
        float radians = (float) Math.toRadians(-Minecraft.getInstance().player.getYRot() + cachedBlockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot());
        pose.rotateAbout(radians, width / 2F, height/2F);
        Vec3 mouse = new Vec3(mouseX, 0, mouseY)
                .subtract(width / 2F, 0, height / 2F)
                .yRot(radians)
                .add(width / 2F, 0, height / 2F);
        mouseX = (int) mouse.x();
        mouseY = (int) mouse.z();
        BaseScreen.blit(
                guiGraphics,
                BACKGROUND.getResource(),
                guiLeft,
                guiTop,
                BACKGROUND.getStartX(),
                BACKGROUND.getStartY(),
                BACKGROUND.getWidth(),
                BACKGROUND.getHeight()
        );
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.font.lineHeight;

        int xRight = width / 2 + (Textures.MENU_BKG.getWidth() / 2 - 10);
        int xLeft = width / 2 - (Textures.MENU_BKG.getWidth() / 2 -  10);
        for (int i = 0; i < cachedBlockEntity.recipes.size(); i++) {
            SnsRecipeHolder snsRecipeHolder = cachedBlockEntity.recipes.get(i).left();
            BarMenuBlockEntity.PriceAndCount priceAndCount = cachedBlockEntity.recipes.get(i).right();
            int length = mc.font.width(String.valueOf(priceAndCount.price));
            int y = height / 2 - Textures.MENU_BKG.getHeight() / 2 + DISTANCE_TOP + lineHeight * 2 * i;
            int DIST = 5;
            guiGraphics.item(
                    GlasswareItem.getShortGlass(snsRecipeHolder.holderGlass()),
                    xLeft + DIST,
                    y - 8
            );
            guiGraphics.itemDecorations(
                    mc.font,
                    ItemRegistries.CONTENT_HOLDER.toStack(),
                    xLeft + DIST,
                    y - 8,
                    String.valueOf(priceAndCount.count)
            );
            guiGraphics.text(
                    mc.font,
                    snsRecipeHolder.name(),
                    xLeft + DIST + 20,
                    y,
                    0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)
            );

            guiGraphics.item(
                    priceAndCount.item.toStack(),
                    xRight - DIST - 16,
                    y - lineHeight/2 - 2,
                    0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)
            );

            guiGraphics.itemDecorations(
                    mc.font,
                    priceAndCount.item.toStack(),
                    xRight - DIST - 16,
                    y - lineHeight/2 - 2,
                    String.valueOf(priceAndCount.price)
            );
        }

        currentIndex = getCurrentSelect(mouseX, mouseY);

        if (currentIndex >= 0) {
            visualSelect = Mth.lerp(ShakenStirClient.ANI_DELTAF * deltaTracker.getGameTimeDeltaTicks(), visualSelect, currentIndex);
        }

        if (currentIndex >= 0) {

            int x = width / 2;
            int y = height / 2 - Textures.MENU_BKG.getHeight() / 2 + DISTANCE_TOP + lineHeight / 2 - 2;
            if (visualSelect >= 0) {
                y += (int) (visualSelect * lineHeight * 2);
            }
            int width = Textures.MENU_BKG.getWidth() / 2 - 10;

            float intensity = 0.5F;
            float v = Mth.clamp(
                    Math.abs(
                            Mth.inverseLerp(visualSelect, currentIndex - Math.signum(currentIndex - visualSelect) * lineHeight * 2, currentIndex) - 0.5F
                    ) * 2 * intensity + 1 - intensity,
                    0, 1);
            extractRect(guiGraphics, x, y, (int) (lineHeight * 2 * v * v * v), (int) (width * v));
        }


        if (isOwner()) {
            guiGraphics.text(
                    mc.font,
                    "Editing...",
                    width/2 - Textures.MENU_BKG.getWidth()/2 + 10,
                    height/2 - Textures.MENU_BKG.getHeight()/2 + 10,
                    0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)
            );
        }
    }

    private int getCurrentSelect(int mouseX, int mouseY) {
        int slotWidth = (Textures.MENU_BKG.getWidth() / 2 - 10) * 2;
        if (mouseX < width/2 - slotWidth/2 || mouseX > width/2 + slotWidth/2) {
            return -1;
        }
        int lineHeight = Minecraft.getInstance().font.lineHeight;
        int slotHeight = lineHeight * 2;
        int yStart = height / 2 - Textures.MENU_BKG.getHeight() / 2 + DISTANCE_TOP + lineHeight / 2 - slotHeight/2;
        if (mouseY < yStart) {
            return -1;
        }
        return (mouseY - yStart) / slotHeight;
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
        return cachedBlockEntity.getPlacerId().equals(Minecraft.getInstance().player.getUUID());
    }


    @Override
    protected void renderWindowPost(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWindowPost(guiGraphics, mouseX, mouseY, partialTick);
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.popMatrix();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void init() {
        setTextureSize(Textures.MENU_BKG.getWidth(), Textures.MENU_BKG.getHeight());
        super.init();
        int x = guiLeft;
        int y = guiTop + 2;
    }
}
