package io.github.hawah.shakenstir.foundation.events;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Cabinet;
import io.github.hawah.shakenstir.content.blockEntity.CabinetBlockEntity;
import io.github.hawah.shakenstir.content.blockEntity.DistillerBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.EntityTypeRegistries;
import io.github.hawah.shakenstir.content.item.ShakerItem;
import io.github.hawah.shakenstir.foundation.event.SnsEventBus;
import io.github.hawah.shakenstir.util.TooltipHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.function.Consumer;

@EventBusSubscriber
public class ShakingEvents {
    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.isUsingItem() && entity.getUseItem().getItem() instanceof ShakerItem) {
            if (entity instanceof Player player) {
                player.getCooldowns().addCooldown(entity.getUseItem(), 20);
            }
            entity.stopUsingItem();
        }
    }

    @SubscribeEvent
    public static void onAddTooltips(AddAttributeTooltipsEvent event) {
        ItemStack stack = event.getStack();

        AttributeTooltipContext ctx         = event.getContext();
        TooltipDisplay display              = ctx.tooltipDisplay();
        Consumer<Component> linesConsumer   = event::addTooltipLines;
        TooltipFlag tooltipFlag             = ctx.flag();

        TooltipHandler.tryAppendShakingFlagDirect   (event, stack);
//        TooltipHandler.tryAppendShakeTooltips       (event, stack);
//        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.SHAKE_CONTENT        , ctx, display, linesConsumer, tooltipFlag);
        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.SPIRIT_CONTENT       , ctx, display, linesConsumer, tooltipFlag);
        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY, ctx, display, linesConsumer, tooltipFlag);
        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.DRINK_DATA           , ctx, display, linesConsumer, tooltipFlag);
        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.RECIPE_HOLDER        , ctx, display, linesConsumer, tooltipFlag);
        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.MINT_SIZE            , ctx, display, linesConsumer, tooltipFlag);
//        TooltipHandler.addToTooltip(stack, DataComponentTypeRegistries.SPIRIT_CONTENT       , ctx, display, linesConsumer, tooltipFlag);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.Fluid.BLOCK,
                (level, pos, state, blockEntity, context) -> {
                    if (blockEntity instanceof DistillerBlockEntity distiller && context != null) {
                        Direction facing = state.getValue(Cabinet.FACING);
                        Direction left = facing.getClockWise();
                        Direction right = facing.getCounterClockWise();
                        Direction opposite = facing.getOpposite();
                        if (context == Direction.DOWN) {
                            return distiller.getInputFluidHandler();
                        } else if (context == facing) {
                            return distiller.getProductHandler();
                        } else if (context == right) {
                            return distiller.getInputFluidHandler();
                        }
                    }
                    return null;
                },
                BlockRegistries.DISTILLER.get()
        );

        event.registerBlock(
                Capabilities.Item.BLOCK,
                (level, pos, state, blockEntity, context) -> {
                    if (blockEntity instanceof CabinetBlockEntity cabinet) {
                        return cabinet.itemHandler;
                    }
                    if (blockEntity instanceof DistillerBlockEntity distiller && context != null) {
                        Direction facing = state.getValue(Cabinet.FACING);
                        Direction left = facing.getClockWise();
                        Direction right = facing.getCounterClockWise();
                        Direction opposite = facing.getOpposite();
                        if (context == Direction.DOWN) {
                            return distiller.getInputItemHandler();
                        } else if (context == opposite) {
                            return distiller.getInputItemHandler();
                        } else if (context == right) {
                            return distiller.getInputItemHandler();
                        } else if (context == left) {
                            return distiller.getFuelHandler();
                        }
                    }
                    return null;
                },
                BlockRegistries.CABINET.get(),
                BlockRegistries.DISTILLER.get()
        );
    }

    @SubscribeEvent // on the mod event bus
    public static void createDefaultAttributes(EntityAttributeCreationEvent event) {
        event.put(
                // Your entity type.
                EntityTypeRegistries.BARTENDER.get(),
                // An AttributeSupplier. This is typically created by calling LivingEntity#createLivingAttributes,
                // setting your values on it, and calling #build. You can also create the AttributeSupplier from scratch
                // if you want, see the source of LivingEntity#createLivingAttributes for an example.
                BartenderEntity.createAttributes().build()
        );
    }

    @SubscribeEvent
    public static void onServerSetup(FMLDedicatedServerSetupEvent event) {
        SnsEventBus.initialize("io.github.hawah.shakenstir");
    }
}
