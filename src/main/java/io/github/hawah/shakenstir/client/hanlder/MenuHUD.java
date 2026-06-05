package io.github.hawah.shakenstir.client.hanlder;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.client.gui.AbstractBlockTargetHUD;
import io.github.hawah.shakenstir.client.gui.EditorMenuScreen;
import io.github.hawah.shakenstir.client.gui.MenuScreen;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.foundation.networking.ServerboundMenuBERecipeChanged;
import io.github.hawah.shakenstir.lib.client.KeyBinding;
import io.github.hawah.shakenstir.lib.client.gui.ScreenOpener;
import io.github.hawah.shakenstir.lib.client.handler.IHandler;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static io.github.hawah.shakenstir.client.hanlder.MC.getPlayer;
import static io.github.hawah.shakenstir.client.hanlder.MC.level;

@SuppressWarnings("resource")
@EventBusSubscriber(value = Dist.CLIENT)
public class MenuHUD extends AbstractBlockTargetHUD implements IHandler {

    public static final  KeyMapping.Category MENU_CAT = new KeyMapping.Category(ShakenStir.asResource("menu"));
    public static final Lazy<KeyMapping> OPEN_MENU = Lazy.of(() -> new KeyMapping("Open Menu", GLFW.GLFW_KEY_F, MENU_CAT));

    public MenuHUD() {
    }

    public BarMenuBlockEntity cachedEntity;
    float fadeInProgress = 0;
    float currentSelect = -1;
    float targetSelect = -1;
    int currentIndex = -1;
    double counts = 0;
    double price = 0;

    @Override
    public void tick() {


        while (OPEN_MENU.get().consumeClick()) {
            if (!isVisible()) {
                continue;
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
            ScreenOpener.open(isOwner()? new EditorMenuScreen(cachedEntity): new MenuScreen(cachedEntity));
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && Minecraft.getInstance().screen == null;
    }

    @Override
    protected Block block() {
        return BlockRegistries.BAR_MENU_BLOCK.get();
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!isVisible()) {
            reset();
            return;
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        guiGraphics.tooltip(
                Minecraft.getInstance().font,
                List.of(ClientTooltipComponent.create(Component.literal("Press ").append(Component.literal(OPEN_MENU.get().getKey().getDisplayName().getString()).withStyle(ChatFormatting.AQUA)).getVisualOrderText())),
                screenWidth/2,
                screenHeight/2,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
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

    private boolean isOwner() {
        return cachedEntity.getPlacerId().equals(Minecraft.getInstance().player.getUUID());
    }

    public boolean onMousePressed(int button, boolean pressed) {
        if (!isVisible()) {
            return false;
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


}
