package io.github.hawah.shakenstir.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.ClickInteractions;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Cabinet;
import io.github.hawah.shakenstir.content.blockEntity.CabinetBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.WarpedMint;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.content.item.StackedMintItem;
import io.github.hawah.shakenstir.foundation.networking.ServerboundHandItemDataChangedPacket;
import io.github.hawah.shakenstir.foundation.networking.ServerboundTryPickItemPacket;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.function.BiFunction;

import static io.github.hawah.shakenstir.client.event.MC.*;

@EventBusSubscriber(Dist.CLIENT)
public class ClientInputEvents {
    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        boolean pickBlock = event.isPickBlock();
        LocalPlayer player = getPlayer();
        if (pickBlock && player != null) {
            if (player.isShiftKeyDown()) {
                return;
            }
            ClientLevel level = Minecraft.getInstance().level;
            if (Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult && level != null) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = level.getBlockState(pos);
                if (state.is(BlockRegistries.CABINET)){
                    Direction facing = state.getValue(Cabinet.FACING);
                    if (hitResult.getDirection().getOpposite() != facing) {
                        return;
                    }
                    int index = Cabinet.getSlot(pos, hitResult, facing);
                    if (!(level.getBlockEntity(pos) instanceof CabinetBlockEntity blockEntity)) {
                        return;
                    }
                    ItemStack itemStack = blockEntity.contents.get(index);
                    if (itemStack.isEmpty()) {
                        return;
                    }
                    Networking.sendToServer(new ServerboundTryPickItemPacket(itemStack.copy()));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        int button = event.getButton();
        boolean down = event.getAction() == InputConstants.PRESS;
        if (ShakenStirClient.DECORATE_PLACE_HANDLER.onMousePressed(button, down)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = getPlayer();
        if (player == null) {
            return;
        }
        double delta = event.getScrollDeltaY();
        if (player.getMainHandItem().getItem() instanceof GlasswareItem && player.isShiftKeyDown()) {
            player.getMainHandItem().set(DataComponentTypeRegistries.GLASSWARE_ROTATION, (float) (player.getMainHandItem().getOrDefault(DataComponentTypeRegistries.GLASSWARE_ROTATION, 0F) + delta * 10));
            Networking.sendToServer(new ServerboundHandItemDataChangedPacket(player.getUUID(), InteractionHand.MAIN_HAND, player.getMainHandItem()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenScroll(ScreenEvent.MouseScrolled.Pre event) {
        Screen screen = event.getScreen();
        if (screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            Slot hoveredSlot = abstractContainerScreen.getHoveredSlot();
            if (hoveredSlot == null) {
                return;
            }
            ItemStack item = hoveredSlot.getItem();
            if (item.getItem() instanceof StackedMintItem) {
                int select = item.getOrDefault(DataComponentTypeRegistries.SELECT_MINT, 0);
                select -= (int) event.getScrollDeltaY();
                int variety = item.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint()).variety();
                select = Mth.clamp(select, 0, variety-1);
                item.set(DataComponentTypeRegistries.SELECT_MINT, select);
                event.setCanceled(true);
            }
        }
    }

    public static Result onMouseMove(final double yaw, final double pitch) {
        for (BiFunction<Double, Double, Result> mouseMove : ClickInteractions.mouseMoves) {
            Result result = mouseMove.apply(yaw, -pitch);
            if (result.cancelled()) {
                return result;
            }
        }
        return Result.empty();
    }

    @SubscribeEvent
    public static void modifyTurnSensitivity(CalculatePlayerTurnEvent event) {
        if (ClientDataHolder.shouldModifyView()) {
            event.setCinematicCameraEnabled(true);
        }
    }
}
