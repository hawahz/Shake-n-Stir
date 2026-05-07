package io.github.hawah.shakenstir.lib.client.gui;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.lib.signal.InstantSignal;
import io.github.hawah.shakenstir.foundation.mixin.ScreenAccessor;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class BaseScreen extends Screen {

    protected int textureWidth, textureHeight;
    protected int windowXOffset, windowYOffset;
    protected int guiLeft, guiTop;

    public final InstantSignal closed = new InstantSignal(0);

    @Override
    public void onClose() {
        closed.emit();
        super.onClose();
    }

    private final List<AbstractWidget> lazyRegisterComponents = new ArrayList<>();

    protected final float getScale() {
        return scale;
    }

    protected final void setScale(float scale) {
        this.scale = scale;
    }

    private float scale = 1;
    private float pausedPartialTick = -1;
    protected boolean disableRenderComponents = false;

    protected BaseScreen(Component title) {
        super(title);
    }

    /**
     * This method must be called before {@code super.init()}!
     */
    protected void setTextureSize(int width, int height) {
        textureWidth = width;
        textureHeight = height;
    }

    /**
     * This method must be called before {@code super.init()}!
     */
    @SuppressWarnings("unused")
    protected void setWindowOffset(int xOffset, int yOffset) {
        windowXOffset = xOffset;
        windowYOffset = yOffset;
    }

    @Override
    protected void init() {
        guiLeft = (width - textureWidth) / 2;
        guiTop = (height - textureHeight) / 2;
        guiLeft += windowXOffset;
        guiTop += windowYOffset;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 按照显示的覆盖顺序来添加符合逻辑的组件。后加入的组件会被渲染在更上层，逻辑上也会更先被触发
     */
    protected void addSortedRenderWidget(AbstractWidget widget) {
        this.lazyRegisterComponents.add(widget);
    }

    protected void finishRegister() {
        for (int i = 0; i < lazyRegisterComponents.size(); i++) {
            this.addRenderableOnly(lazyRegisterComponents.get(i));
            this.addWidget(lazyRegisterComponents.get(lazyRegisterComponents.size()-1-i));
        }
        this.lazyRegisterComponents.clear();
    }

    @Override
    public void tick() {
        super.tick();
        pausedPartialTick = 0;
    }

    private Iterable<? extends Renderable> getRenderables() {
        return ((ScreenAccessor) this).getRenderables();
    }

    @Override
    public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

//        a = AnimationTickHolder.getPartialTicks();

        Matrix3x2fStack poseStack = graphics.pose();

        graphics.nextStratum();
        this.extractBackground(graphics, mouseX, mouseY, a);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.ScreenEvent.Render.Background(this, graphics, mouseX, mouseY, a));
        renderWindowPre(graphics, mouseX, mouseY, a);

        poseStack.pushMatrix();
        applyScaleTransform(poseStack);
        if (!disableRenderComponents) {
            for (Renderable renderable : getRenderables())
                renderable.extractRenderState(graphics, mouseX, mouseY, a);
        }
        poseStack.pushMatrix();

        renderWindowPost(graphics, mouseX, mouseY, a);
    }


    protected void applyScaleTransform(Matrix3x2fStack poseStack) {
        if (scale == 1) {
            return;
        }
        poseStack.translate(guiLeft +textureWidth/2F, guiTop +textureHeight/2F);
        poseStack.scale(getScale(), getScale());
        poseStack.translate(-guiLeft-textureWidth/2F, -guiTop-textureHeight/2F);
    }

    protected Vec2 getOriginalMousePos(int mouseX, int mouseY) {
        return new Vec2(
                (int) ((mouseX - width/2F) * scale + width/2F),
                (int) ((mouseY - height/2F) * scale + height/2F)
        );
    }

    protected void renderWindowPre(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {}
    protected void renderWindowPost(GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {}

    public static void blit(GuiGraphicsExtractor guiGraphics,
                            Identifier texture,
                            int x,
                            int y,
                            int u,
                            int v,
                            int width,
                            int height,
                            int r,
                            int g,
                            int b,
                            int a) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
                u,
                v,
                width,
                height,
                256,
                256,
                ARGB.color(a, r, g, b)
        );
    }

    public static void blit(GuiGraphicsExtractor guiGraphics,
                            Identifier texture,
                            int x,
                            int y,
                            int u,
                            int v,
                            int width,
                            int height,
                            float r,
                            float g,
                            float b,
                            float a) {
        blit(guiGraphics, texture, x, y, u, v, width, height, (int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    public static void blit(GuiGraphicsExtractor guiGraphics,
                            Identifier texture,
                            int x,
                            int y,
                            int u,
                            int v,
                            int width,
                            int height) {
        blit(guiGraphics, texture, x, y, u, v, width, height, 1F, 1, 1, 1);
    }

    public static void blit(GuiGraphicsExtractor guiGraphics,
                            Identifier texture,
                            int x,
                            int y,
                            int u,
                            int v,
                            int width,
                            int height,
                            int argb) {
        blit(guiGraphics, texture, x, y, u, v, width, height, argb & 0xFF, (argb >> 8) & 0xFF, (argb >> 16) & 0xFF, (argb >> 24) & 0xFF);
    }

    public static void line(GuiGraphicsExtractor GuiGraphicsExtractor,
                            int x1,
                            int y1,
                            int x2,
                            int y2,
                            int color) {
        line(GuiGraphicsExtractor, x1, y1, x2, y2, color, 1);
    }
    public static void line(GuiGraphicsExtractor GuiGraphicsExtractor,
                            int x1,
                            int y1,
                            int x2,
                            int y2,
                            int color,
                            double process) {

        if (process <= 0) return;

        process = Mth.clamp(process, 0, 1);

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        int err = dx - dy;

        int totalSteps = Math.max(dx, dy) + 1;

        int drawSteps = (int) Math.round(totalSteps * process);

        int x = x1;
        int y = y1;

        for (int i = 0; i < drawSteps; i++) {

            // 画当前像素（用1px hLine避免重复逻辑）
            GuiGraphicsExtractor.horizontalLine(x, x, y, color);

            if (x == x2 && y == y2) break;

            int e2 = err << 1;

            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }

            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private static final int[] DIGIT_X = new int[]{0, 11, 22, 32, 42, 53, 63, 73, 84, 94};
    private static final int[] DIGIT_WIDTH = new int[]{11, 11, 10, 10, 11, 10, 10, 11, 10, 9};
    public static void drawHandwriteNumber(GuiGraphicsExtractor GuiGraphicsExtractor,
                                           int x,
                                           int y,
                                           int number) {
        int digitOffset = 0, readLen = 0;
        String numberString = String.valueOf(number);
        while (readLen < numberString.length()) {
            int num = (numberString.charAt(readLen++) - '0') - 1;
            num = num<0? 9: num;
            blit(
                    GuiGraphicsExtractor,
                    Textures.NUMBER_SPRITE.getResource(),
                    x + digitOffset,
                    y,
                    DIGIT_X[num],
                    Textures.NUMBER_SPRITE.getStartY(),
                    DIGIT_WIDTH[num],
                    Textures.NUMBER_SPRITE.getHeight()
            );
            digitOffset += DIGIT_WIDTH[num];
        }
    }
}
