package io.github.hawah.shakenstir.client.hanlder;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.ClickInteractions;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.client.gui.AbstractBlockTargetHUD;
import io.github.hawah.shakenstir.client.gui.MenuScreen;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.networking.ServerboundMenuBEChanged;
import io.github.hawah.shakenstir.foundation.networking.ServerboundMenuBERecipeChanged;
import io.github.hawah.shakenstir.lib.client.KeyBinding;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import io.github.hawah.shakenstir.lib.client.gui.ScreenOpener;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.Result;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static io.github.hawah.shakenstir.client.hanlder.MC.getPlayer;
import static io.github.hawah.shakenstir.client.hanlder.MC.level;

@SuppressWarnings("resource")
public class MenuHUD extends AbstractBlockTargetHUD implements IHandler {

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

    protected boolean isActive = false;

    @Override
    public void tick() {

    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && !Minecraft.getInstance().hasControlDown() && Minecraft.getInstance().screen == null;
    }

    @Override
    protected @NonNull Block block() {
        return BlockRegistries.BAR_MENU_BLOCK.get();
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!isVisible()) {
            reset();
            return;
        }
        BlockPos pos = ClientDataHolder.Picker.pos();
        if (level() == null || pos == null) {
            return;
        }

        if (cachedEntity == null) {
            if (level().getBlockEntity(pos) instanceof BarMenuBlockEntity blockEntity) {
                cachedEntity = blockEntity;
            } else {
                return;
            }
        }

        if (true) {
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            boolean keyDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_F);
            if (keyDown) {
                ScreenOpener.open(new MenuScreen(cachedEntity));
            }
            guiGraphics.tooltip(
                    Minecraft.getInstance().font,
                    List.of(ClientTooltipComponent.create(Component.literal("Press ").append(Component.literal("F").withStyle(ChatFormatting.AQUA)).getVisualOrderText())),
                    screenWidth/2,
                    screenHeight/2,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
            return;
        }

//        guiGraphics.submitPictureInPictureRenderState(new MenuRenderState());


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
                    y - lineHeight/2,
                    0x00FFFFFF | ((int)(0xF3 * fadeInProgress) << 24)
            );

            guiGraphics.itemDecorations(
                    mc.font,
                    priceAndCount.item.toStack(),
                    xRight - DIST - 16,
                    y - lineHeight/2,
                    String.valueOf(priceAndCount.price)
            );
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

    private void reset() {
        cachedEntity = null;
        fadeInProgress = 0;
        currentSelect = -1;
        targetSelect = -1;
        currentIndex = -1;
        counts = 0;
        price = 0;
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
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && pressed) {
            return onModifyCostItem();
        }
        return false;
    }

    public boolean onModifyCostItem() {
        if (!isOwner() || KeyBinding.hasShiftDown()) {
            return false;
        }
        if (currentIndex < 0 || getPlayer() == null) {
            return false;
        }
        ItemStack itemStack = getPlayer().getMainHandItem();
        if (itemStack.getItem() instanceof GlasswareItem) {
            SnsRecipeHolder recipeHolder = cachedEntity.recipes.get(currentIndex).left();
            SnsRecipeHolder newHolder = recipeHolder
                    .glass(itemStack.getOrDefault(DataComponents.ITEM_MODEL, ShakenStir.asResource("martini_glass")).getPath())
                    .decorations(itemStack.getOrDefault(DataComponentTypeRegistries.GLASSWARE_DECORATIONS, List.of()))
            ;
            cachedEntity.recipes.get(currentIndex).setLeft(newHolder);
            Networking.sendToServer(new ServerboundMenuBERecipeChanged(newHolder, currentIndex, cachedEntity.getBlockPos()));
            return true;
        }
        BarMenuBlockEntity.PriceAndCount priceAndCount = cachedEntity.recipes.get(currentIndex).right();
        if (itemStack.isEmpty()) {
            priceAndCount.price = 0;
            priceAndCount.item = ItemResource.EMPTY;
        } else {
            priceAndCount.item = ItemResource.of(itemStack);
        }
        return true;
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
                cachedEntity.setRecipeCount(getCurrentIndex(), (int) counts);
            } else {
                price += pitch / 50;
                price = Math.max(price, 0);
                cachedEntity.setRecipePrice(getCurrentIndex(), (int) price);
            }
            Networking.sendToServer(new ServerboundMenuBEChanged(cachedEntity.recipes.get(getCurrentIndex()).right(), currentIndex, ClientDataHolder.Picker.pos()));
            return new Result(true);
        }
        return Result.empty();
    }
}
