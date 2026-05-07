package io.github.hawah.shakenstir.util;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.HashMap;

public enum Textures {
    NUMBER_SPRITE("textures/gui/general.png", 0, 0, 11, 12, 0),
    KEYMAP("textures/gui/buttons.png", 0, 0, 16, 16, 5, 0, 16, 0, 32, 0, 48, 0, 64, 0, 80),
    SHAKE_HUD_FRONT("textures/gui/shake_hud.png", 82, 10, 58, 83, 0),
    SHAKE_HUD_OUTSIDE("textures/gui/shake_hud.png", 11, 10, 58, 83, 0),
    SHAKE_HUD_INSIDE("textures/gui/shake_hud.png", 11, 96, 58, 83, 0),
    ;
    private final String resource;
    private final int startX;
    private final int startY;
    private final int width;
    private final int height;
    private final int variantCounts;
    private final int[] variant;
    private final Builder builder;
    private static final HashMap<String, Identifier> resourceCache = new HashMap<>();

    Textures(String path, int startX, int startY, int width, int height, int variantCounts, int... variant) {
        this.resource = path;
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.variantCounts = variantCounts;
        assert variant.length == variantCounts * 2;
        this.variant = variant;
        this.builder = new Builder(this);
    }

    public Identifier getResource() {
        return resourceCache.computeIfAbsent(resource, p -> Identifier.fromNamespaceAndPath(ShakenStir.MODID, resource));
    }

    public void blit(GuiGraphicsExtractor guiGraphics, int x, int y, int r, int g, int b ,int a) {
        BaseScreen.blit(guiGraphics, getResource(), x, y, getStartX(), getStartY(), getWidth(), getHeight(), r, g, b, a);
    }

    public void blit(GuiGraphicsExtractor guiGraphics, int x, int y) {
        BaseScreen.blit(guiGraphics, getResource(), x, y, getStartX(), getStartY(), getWidth(), getHeight());
    }

    public void blit(GuiGraphicsExtractor guiGraphics, int x, int y, int color) {
        BaseScreen.blit(guiGraphics, getResource(), x, y, getStartX(), getStartY(), getWidth(), getHeight(), color);
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Builder builder() {
        return builder.variant(0);
    }

    public static class Builder {
        private final Textures textures;
        private int variant = 0;

        public Builder(Textures textures) {
            this.textures = textures;
        }
        public Builder variant(int variant) {
            this.variant = Mth.clamp(variant, 0, textures.variantCounts);
            return this;
        }
        public <T extends Enum<T>> Builder variant(Enum<T> variant) {
            return this.variant(variant.ordinal());
        }
        public Identifier getResource() {
            return textures.getResource();
        }

        public int getStartX() {
            return variant == 0? textures.getStartX() : textures.variant[(variant - 1) * 2];
        }

        public int getStartY() {
            return variant == 0? textures.getStartY() : textures.variant[(variant - 1) * 2 + 1];
        }

        public int getWidth() {
            return textures.getWidth();
        }

        public int getHeight() {
            return textures.getHeight();
        }

        public Builder reset() {
            variant = 0;
            return this;
        }
    }

    public enum Variants {
        NORMAL,
        HOVER,
        DISABLED;
    }
    public enum ToggleVariants {
        NORMAL,
        NORBAL_HOVER,
        TOGGLE,
        TOGGLE_HOVER;
    }
    public enum KeyVariants {
        CTRL,
        SHIFT,
        ALT,
        RIGHT,
        LEFT,
        SCROLL
    }
}
