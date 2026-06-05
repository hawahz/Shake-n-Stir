package io.github.hawah.shakenstir.client.gui;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.foundation.networking.ServerboundMenuBEChanged;
import io.github.hawah.shakenstir.foundation.networking.ServerboundMenuBERecipeChanged;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static io.github.hawah.shakenstir.client.gui.MC.getPlayer;

public class EditorMenuScreen extends AbstractMenuScreen {

    private static final Identifier HOTBAR_SPRITE = Identifier.withDefaultNamespace("hud/hotbar");
    private static final Identifier HOTBAR_SELECTION_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_selection");
    private static final Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_background");
    private static final Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_progress");
    private ItemStack carriedItem = null;
    private boolean dirty = false;

    @Override
    public void onClose() {
        if (dirty) {
            cachedBlockEntity.setPackedBkg(nativeImage.getPixels().getPixels());
        }
        super.onClose();
    }

    private final int maxInventoryPages;

    public EditorMenuScreen(BarMenuBlockEntity cachedBlockEntity) {
        super(cachedBlockEntity);
        this.maxInventoryPages = getPlayer().getInventory().getContainerSize() / 9;
    }

    @Override
    protected void renderBkgPost(GuiGraphicsExtractor guiGraphics, int originMouseX, int originMouseY, float partialTick) {
        guiGraphics.text(
                this.minecraft.font,
                "Editing...",
                width/2 - Textures.MENU_BKG.getWidth()/2 + 10,
                height/2 - Textures.MENU_BKG.getHeight()/2 + 10,
                0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)
        );
        if (mouseDown && carriedItem == null) {
            int x = (int) Mth.lerp(partialTick, mouseXo, mouseX);
            int y = (int) Mth.lerp(partialTick, mouseYo, mouseY);
            MousePos localMousePos = getLocalMousePos(x, y);
//            drawCircleOn(localMousePos.x() - guiLeft, localMousePos.y() - guiTop, 3, -1);
        }
    }

    @Override
    protected void renderWindowPost(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWindowPost(guiGraphics, mouseX, mouseY, partialTick);
        extractItemHotbar(guiGraphics, this.minecraft.getDeltaTracker());
        int idx = getSelectSlot(mouseX, mouseY);
        int startX = width / 2 - 91 + idx * 20 + 2;
        if (idx >= 0 && idx <= 8) {
            guiGraphics.fill(startX, height - 20, startX + 18, height - 2, 0xA0FFFFFF);
            guiGraphics.setTooltipForNextFrame(
                    font,
                    getPlayer().getInventory().getItem(idx),
                    mouseX,
                    mouseY
            );
        }

        if (carriedItem != null) {
            guiGraphics.item(
                    carriedItem,
                    mouseX - 8, mouseY - 8
            );
        }

    }

    private void extractItemHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Player player = getPlayer();
        if (player != null) {
            ItemStack offhand = player.getOffhandItem();
            HumanoidArm offhandArm = player.getMainArm().getOpposite();
            int screenCenter = graphics.guiWidth() / 2;
            int hotbarWidth = 182;
            int halfHotbar = 91;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, screenCenter - 91, graphics.guiHeight() - 22, 182, 22);
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    HOTBAR_SELECTION_SPRITE,
                    screenCenter - 91 - 1 + player.getInventory().getSelectedSlot() * 20,
                    graphics.guiHeight() - 22 - 1,
                    24,
                    23
            );

            int seed = 1;

            for (int i = 0; i < 9; i++) {
                int x = screenCenter - 90 + i * 20 + 2;
                int y = graphics.guiHeight() - 16 - 3;
                this.extractSlot(graphics, x, y, deltaTracker, player, player.getInventory().getItem(i), seed++);
            }

            if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
                float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
                if (attackStrengthScale < 1.0F) {
                    int y = graphics.guiHeight() - 20;
                    int x = screenCenter + 91 + 6;
                    if (offhandArm == HumanoidArm.RIGHT) {
                        x = screenCenter - 91 - 22;
                    }

                    int progress = (int)(attackStrengthScale * 19.0F);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 18, 18);
                    graphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - progress, x, y + 18 - progress, 18, progress
                    );
                }
            }
        }
    }

    private void extractSlot(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed) {
        if (!itemStack.isEmpty()) {
            float pop = itemStack.getPopTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
            if (pop > 0.0F) {
                float squeeze = 1.0F + pop / 5.0F;
                graphics.pose().pushMatrix();
                graphics.pose().translate(x + 8, y + 12);
                graphics.pose().scale(1.0F / squeeze, (squeeze + 1.0F) / 2.0F);
                graphics.pose().translate(-(x + 8), -(y + 12));
            }

            graphics.item(player, itemStack, x, y, seed);
            if (pop > 0.0F) {
                graphics.pose().popMatrix();
            }

            graphics.itemDecorations(this.minecraft.font, itemStack, x, y);
        }
    }


    protected int getSelectSlot(double mouseX, double mouseY) {
        if (mouseY < height - 22) {
            return -1;
        }
        int slot = (int) (((mouseX - width / 2F) + 91) / 20);
        return slot > 8? -1: slot;
    }

    public void drawRectOn(int x, int y, int size, int color) {
        int half = size / 2;
        int left = Math.max(x - half, 0);
        int top = Math.max(y - half, 0);
        int width = Math.min(x + half, PAINTER_WIDTH) - left;
        int height = Math.min(y + half, PAINTER_HEIGHT) - top;
        if (width > 0 && height > 0) {
            nativeImage.getPixels().fillRect(left, top, width, height, color);
        }
        nativeImage.upload();
        dirty = true;
    }

    public void drawCircleOn(int x, int y, int size, int color) {
        int radius = size / 2;
        int r2 = radius * radius;
        for (int px = x - radius; px <= x + radius; px++) {
            if (px < 0 || px >= PAINTER_WIDTH) continue;
            for (int py = y - radius; py <= y + radius; py++) {
                if (py < 0 || py >= PAINTER_HEIGHT) continue;
                int dx = px - x;
                int dy = py - y;
                if (dx * dx + dy * dy <= r2) {
                    nativeImage.getPixels().setPixel(px, py, color);
                }
            }
        }
        nativeImage.upload();
        dirty = true;
    }

    double price = 0;
    int prevSelect = -1;
    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int currentSelect = getCurrentSelect((int) x, (int) y);
        boolean changed = prevSelect != currentSelect;
        prevSelect = currentSelect;
        if (currentSelect >= 0 && currentSelect < cachedBlockEntity.recipes.size()) {
            if (changed) {
                price = cachedBlockEntity.recipes.get(currentIndex).right().price;
            }
            price += scrollY;
            price = Math.max(0, price);
            cachedBlockEntity.setRecipePrice(currentSelect, (int) price);
            Networking.sendToServer(new ServerboundMenuBEChanged(cachedBlockEntity.recipes.get(currentIndex).right(), currentIndex, cachedBlockEntity.getBlockPos()));
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }


    double mouseXo = 0;

    double mouseYo = 0;
    double mouseX = 0;
    double mouseY = 0;
    @Override
    public void mouseMoved(double x, double y) {
        mouseXo = mouseX;
        mouseYo = mouseY;
        mouseX = x;
        mouseY = y;
        MousePos localMousePosO = getLocalMousePos(mouseXo, mouseYo);
        MousePos localMousePos = getLocalMousePos(mouseX, mouseY);
        if (mouseDown && carriedItem == null) {
            int x0 = localMousePosO.x() - guiLeft;
            int y0 = localMousePosO.y() - guiTop;
            int x1 = localMousePos.x() - guiLeft;
            int y1 = localMousePos.y() - guiTop;

            int dx = Math.abs(x1 - x0);
            int dy = Math.abs(y1 - y0);
            int steps = Math.max(dx, dy);

            if (steps > 0) {
                for (int i = 0; i <= steps; i++) {
                    int px = x0 + (x1 - x0) * i / steps;
                    int py = y0 + (y1 - y0) * i / steps;
                    drawCircleOn(px, py, 3, -1);
                }
            } else {
                drawCircleOn(x0, y0, 3, -1);
            }
        }
        super.mouseMoved(x, y);
    }
    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        mouseDown = false;
        return super.mouseReleased(event);
    }

    boolean mouseDown = false;
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int selectSlot = getSelectSlot(mouseX, mouseY);
        if (selectSlot >= 0 && getPlayer() != null) {
            carriedItem = getPlayer().getInventory().getItem(selectSlot);
            SnsRecipeHolder holder;
            if (event.hasShiftDown() && (holder = carriedItem.get(DataComponentTypeRegistries.RECIPE_HOLDER)) != null) {
                cachedBlockEntity.addRecipe(holder.copy());
                carriedItem = null;
            }
        } else if (carriedItem != null) {
            int currentSelect = getCurrentSelect((int) mouseX, (int) mouseY);
            if (currentSelect >= 0) {
                if (carriedItem.getItem() instanceof GlasswareItem) {
                    SnsRecipeHolder recipeHolder = cachedBlockEntity.recipes.get(currentIndex).left();
                    SnsRecipeHolder newHolder = recipeHolder
                            .glass(carriedItem.getOrDefault(DataComponents.ITEM_MODEL, ShakenStir.asResource("martini_glass")).getPath())
                            .decorations(carriedItem.getOrDefault(DataComponentTypeRegistries.GLASSWARE_DECORATIONS, List.of()))
                            ;
                    cachedBlockEntity.recipes.get(currentIndex).setLeft(newHolder);
                    Networking.sendToServer(new ServerboundMenuBERecipeChanged(newHolder, currentIndex, cachedBlockEntity.getBlockPos()));
                } else {
                    cachedBlockEntity.setRecipeItem(currentSelect, carriedItem.copyWithCount(1));
                    Networking.sendToServer(new ServerboundMenuBEChanged(cachedBlockEntity.recipes.get(currentSelect).right(), currentSelect, cachedBlockEntity.getBlockPos()));
                }
            }
            carriedItem = null;
        }
        mouseDown = true;
        return super.mouseClicked(event, doubleClick);
    }
}
