package io.github.hawah.shakenstir.client.gui;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.foundation.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import io.github.hawah.shakenstir.util.MenuBackgroundUtils;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

import static io.github.hawah.shakenstir.client.gui.MC.getPlayer;

@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class AbstractMenuScreen extends BaseScreen {
    public static final Textures BACKGROUND = Textures.MENU_BKG;
    public static final int DISTANCE_TOP = 70;
    public static final int PAINTER_WIDTH = 256;
    public static final int PAINTER_HEIGHT = 256;
    protected final BarMenuBlockEntity cachedBlockEntity;
    protected double rot;
    protected final DynamicTexture nativeImage = new DynamicTexture("editor", PAINTER_WIDTH, PAINTER_HEIGHT, true);
    protected int maxSlots = 0;
    protected float visualSelect = -1;
    protected int currentIndex = -1;
    float fadeInProgress = 0;
    protected final Identifier UniqueName;

    protected AbstractMenuScreen(BarMenuBlockEntity cachedBlockEntity) {
        super(Component.empty());
        this.cachedBlockEntity = cachedBlockEntity;
        if (getPlayer() == null) {
            this.rot = Math.toRadians(-getPlayer().getYRot() + cachedBlockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot());
        }
        maxSlots = cachedBlockEntity.recipes.size();
        UniqueName = ShakenStir.asResource("bkg_" + System.currentTimeMillis());
        nativeImage.getPixels().fillRect(0, 0, PAINTER_WIDTH, PAINTER_HEIGHT, 0);
        if (cachedBlockEntity.bkg != null && getPlayer() != null) {
            MenuBackgroundUtils.requestBackground(
                    cachedBlockEntity.bkg.getPath(),
                    data -> {
                        for (int x = 0; x < PAINTER_WIDTH; x++) {
                            for (int y = 0; y < PAINTER_HEIGHT; y++) {
                                nativeImage.getPixels().setPixel(x, y, data[x + y * PAINTER_WIDTH]);
                            }
                        }
                        nativeImage.upload();
                    },
                    true,
                    getPlayer().getUUID()
            );
        }
        this.minecraft.getTextureManager().register(UniqueName, nativeImage);
        nativeImage.upload();
    }

    protected int getCurrentSelect(int originMouseX, int originMouseY) {
        MousePos mousePos = getLocalMousePos(originMouseX, originMouseY);
        return getCurrentSelect(mousePos.x(), mousePos.y(), originMouseX, originMouseY);
    }

    protected MousePos getLocalMousePos(double originMouseX, double originMouseY) {
        Vec3 mouse = new Vec3(originMouseX, 0, originMouseY)
                .subtract(width / 2F, 0, height / 2F)
                .yRot((float) rot)
                .add(width / 2F, 0, height / 2F);
        int mouseX = (int) mouse.x();
        int mouseY = (int) mouse.z();
        return new MousePos(mouseX, mouseY);
    }

    protected int getCurrentSelect(int mouseX, int mouseY, int originMouseX, int originMouseY) {
        if (originMouseY > height - 22 && originMouseX > width/2 - 91 && originMouseX < width/2 + 91 && this instanceof EditorMenuScreen) {
            return -1;
        }
        int slotWidth = (Textures.MENU_BKG.getWidth() / 2 - 10) * 2;
        if (mouseX < width/2 - slotWidth/2 || mouseX > width/2 + slotWidth/2) {
            return -1;
        }
        int lineHeight = this.minecraft.font.lineHeight;
        int slotHeight = lineHeight * 2;
        int yStart = height / 2 - Textures.MENU_BKG.getHeight() / 2 + DISTANCE_TOP + lineHeight / 2 - slotHeight/2;
        if (mouseY < yStart) {
            return -1;
        }
        int slot = (mouseY - yStart) / slotHeight;
        return slot >= maxSlots? -1 : slot;
    }

    protected void extractRect(GuiGraphicsExtractor guiGraphics, int x, int y, int height, int width) {
        extractRect(guiGraphics, x, y, height, width, 0xFFFFFF, 1);
    }

    private void extractRect(GuiGraphicsExtractor guiGraphics, int x, int y, int height, int width, float a) {
        extractRect(guiGraphics, x, y, height, width, 0xFFFFFF, a);
    }

    private void extractRect(GuiGraphicsExtractor guiGraphics, int x, int y, int height, int width, int color, float a) {
        int yTop = y - height/2;
        int yBottom = y + height/2;
        int xLeft = x + width;
        int xRight = x - width;
        guiGraphics.horizontalLine(
                xLeft,
                xRight,
                yTop,
                0x00FFFFFF & color | ((int) (0xF3 * fadeInProgress * a) << 24)
        );
        guiGraphics.horizontalLine(
                xLeft,
                xRight,
                yBottom,
                0x00FFFFFF & color | ((int) (0xF3 * fadeInProgress * a) << 24)
        );
        guiGraphics.verticalLine(
                xLeft,
                yTop,
                yBottom,
                0x00FFFFFF & color | ((int) (0xF3 * fadeInProgress * a) << 24)
        );
        guiGraphics.verticalLine(
                xRight,
                yTop,
                yBottom,
                0x00FFFFFF & color | ((int) (0xF3 * fadeInProgress * a) << 24)
        );
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.getTextureManager().release(UniqueName);
    }

    @Override
    protected void init() {
        setTextureSize(Textures.MENU_BKG.getWidth(), Textures.MENU_BKG.getHeight());
        super.init();
    }

    @Override
    protected void renderWindowPre(GuiGraphicsExtractor guiGraphics, int originMouseX, int originMouseY, float partialTick) {
        DeltaTracker deltaTracker = this.minecraft.getDeltaTracker();
        fadeInProgress = Mth.lerp(ShakenStirClient.ANI_DELTAF * deltaTracker.getGameTimeDeltaTicks(), fadeInProgress, 1);
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate( 0, (1- fadeInProgress) * 24);
        float radians = (float) rot;
        pose.rotateAbout(radians, width / 2F, height/2F);
        Vec3 mouse = new Vec3(originMouseX, 0, originMouseY)
                .subtract(width / 2F, 0, height / 2F)
                .yRot(radians)
                .add(width / 2F, 0, height / 2F);
        int mouseX = (int) mouse.x();
        int mouseY = (int) mouse.z();
        BaseScreen.blit(
                guiGraphics,
                BACKGROUND.getResource(),
                guiLeft,
                guiTop,
                BACKGROUND.getStartX(),
                BACKGROUND.getStartY(),
                BACKGROUND.getWidth(),
                BACKGROUND.getHeight(),
                0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)

        );
        BaseScreen.blit(
                guiGraphics,
                UniqueName,
                guiLeft,
                guiTop,
                0,
                0,
                BACKGROUND.getWidth(),
                BACKGROUND.getHeight(),
                0x00FFFFFF | ((int)(0xFF * fadeInProgress) << 24)
        );
        renderBkgPost(guiGraphics, mouseX, mouseY, partialTick);
        Minecraft mc = this.minecraft;
        int lineHeight = mc.font.lineHeight;

        int xRight = width / 2 + (Textures.MENU_BKG.getWidth() / 2 - 10);
        int xLeft = width / 2 - (Textures.MENU_BKG.getWidth() / 2 -  10);
        for (int i = 0; i < cachedBlockEntity.recipes.size(); i++) {
            SnsRecipeHolder snsRecipeHolder = cachedBlockEntity.recipes.get(i).left();
            BarMenuBlockEntity.PriceAndCount priceAndCount = cachedBlockEntity.recipes.get(i).right();
            int y = height / 2 - Textures.MENU_BKG.getHeight() / 2 + DISTANCE_TOP + lineHeight * 2 * i;
            int DIST = 5;
            guiGraphics.item(
                    snsRecipeHolder.displayItem().orElse(GlasswareItem.getShortGlass(snsRecipeHolder.holderGlass())),
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

        currentIndex = getCurrentSelect(mouseX, mouseY, originMouseX, originMouseY);

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
    }

    protected void renderBkgPost(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void renderWindowPost(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.popMatrix();
    }

    protected record MousePos(int x, int y){}
}
