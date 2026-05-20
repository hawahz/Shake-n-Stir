package io.github.hawah.shakenstir.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Cabinet;
import io.github.hawah.shakenstir.content.blockEntity.CabinetBlockEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.gui.GuiLayer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CabinetHud implements GuiLayer {

    protected ItemStack tooltipItem = ItemStack.EMPTY;

    public static boolean isVisible() {
        if (ClientDataHolder.Picker.type().equals(HitResult.Type.MISS)) {
            return false;
        }
        boolean visible = BlockRegistries.CABINET.get().equals(ClientDataHolder.Picker.block().orElse(null));
        if (visible) {
            BlockState state = ClientDataHolder.Picker.blockState().get();
            if (!state.getValue(Cabinet.FACING).getOpposite().equals(ClientDataHolder.Picker.direction())) {
                return false;
            }

        }
        return visible;
    }

    public void tick() {
        if (isVisible()) {
            BlockPos pos = ClientDataHolder.Picker.pos();
            if (getLevel() != null &&
                    pos != null &&
                    getLevel().getBlockEntity(pos) instanceof CabinetBlockEntity blockEntity &&
                    ClientDataHolder.Picker.hitResult() instanceof BlockHitResult blockHitResult
            ) {
                BlockState state = blockEntity.getBlockState();
                int slot = Cabinet.getSlot(pos, blockHitResult, state.getValue(Cabinet.FACING));
                tooltipItem = blockEntity.contents.get(slot);
            } else {
                tooltipItem = ItemStack.EMPTY;
            }
        } else {
            tooltipItem = ItemStack.EMPTY;
        }
    }

    @Nullable
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }
    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!isVisible() || tooltipItem.isEmpty()) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        int windowWidth = window.getGuiScaledWidth();
        int windowHeight = window.getGuiScaledHeight();
        int x = windowWidth / 2;
        int y = windowHeight / 2;

        List<Component> tooltipFromItem = Screen.getTooltipFromItem(Minecraft.getInstance(), tooltipItem);
        Optional<TooltipComponent> tooltipImage = tooltipItem.getTooltipImage();
        List<ClientTooltipComponent> components = ClientHooks.gatherTooltipComponents(tooltipItem, tooltipFromItem, tooltipImage, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), Minecraft.getInstance().font);

        guiGraphics.tooltip(
                Minecraft.getInstance().font,
                components,
                x,
                y,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }
}
