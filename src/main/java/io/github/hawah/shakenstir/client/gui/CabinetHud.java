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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.ClientHooks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CabinetHud extends AbstractBlockTargetHUD {

    protected ItemStack tooltipItem = ItemStack.EMPTY;

    public boolean isVisible() {
        if (!super.isVisible()) {
            return false;
        }
        BlockState state = ClientDataHolder.Picker.blockState().get();
        return state.getValue(Cabinet.FACING).getOpposite().equals(ClientDataHolder.Picker.direction());
    }

    @Override
    protected Block block() {
        return BlockRegistries.CABINET.get();
    }

    public void tick() {
        if (isVisible()) {
            BlockPos pos = ClientDataHolder.Picker.pos();
            if (MC.getLevel() != null &&
                    pos != null &&
                    MC.getLevel().getBlockEntity(pos) instanceof CabinetBlockEntity blockEntity &&
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
