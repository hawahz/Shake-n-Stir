package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.IFluidDataHolder;
import io.github.hawah.shakenstir.content.dataComponent.IItemDataHolder;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.recipe.RecipeTypeRegistries;
import io.github.hawah.shakenstir.content.recipe.ShakeRecipe;
import io.github.hawah.shakenstir.content.recipe.ShakeRecipeInput;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ServerboundShakeFinishPacket(UUID playerUUID, ItemStack shakeItem, int shakeSuccessTimes) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundShakeFinishPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,  ServerboundShakeFinishPacket::playerUUID,
            ItemStack.STREAM_CODEC, ServerboundShakeFinishPacket::shakeItem,
            ByteBufCodecs.INT,      ServerboundShakeFinishPacket::shakeSuccessTimes,
            ServerboundShakeFinishPacket::new
    );

    @Override
    public void handle(ServerPlayer serverPlayer) {
        ServerLevel level = serverPlayer.level();
        Player player;
        if (!serverPlayer.getUUID().equals(playerUUID)) {
            player = level.getPlayerByUUID(playerUUID);
        } else {
            player = serverPlayer;
        }
        if (player == null) {
            return;
        }
        int shakeSuccessTimes = this.shakeSuccessTimes();
        ItemStack mainHandItem = player.getMainHandItem();
        List<ItemStack> itemData = new ArrayList<>(ShakeUtil.getItemStacks(shakeItem));
        List<FluidStack> fluidData = new ArrayList<>(ShakeUtil.getFluidStacks(shakeItem));
        ItemStack predicatedImmProduct;
        if (!itemData.isEmpty() && (predicatedImmProduct = itemData.getFirst())!=null && predicatedImmProduct.is(ItemRegistries.CONTENT_HOLDER) && predicatedImmProduct.has(DataComponentTypeRegistries.SHAKE_SUCCESS_TIMES)) {
            IItemDataHolder item = ShakeUtil.getItemData(predicatedImmProduct);
            itemData.removeFirst();
            itemData.addAll(item.itemStacks());
            IFluidDataHolder fluid = ShakeUtil.getFluidData(predicatedImmProduct);
            fluidData.addAll(fluid.fluidStacks());
            shakeSuccessTimes += predicatedImmProduct.getOrDefault(DataComponentTypeRegistries.SHAKE_SUCCESS_TIMES, 0);
        }

        RecipeManager recipeManager = level.recipeAccess();
        ShakeRecipeInput recipeInput = new ShakeRecipeInput(itemData, fluidData, shakeSuccessTimes);
        Optional<RecipeHolder<ShakeRecipe>> result = recipeManager.getRecipeFor(
                RecipeTypeRegistries.SHAKE_RECIPE.get(),
                recipeInput,
                level
        );
        if (result.isEmpty()) {
            ShakeUtil.clearFluidData(shakeItem);
            ShakeUtil.clearItemData(shakeItem);
            mainHandItem.set(DataComponentTypeRegistries.SHAKING, true);
            ItemStack holder = ItemRegistries.CONTENT_HOLDER.get().getDefaultInstance();
            ShakeUtil.setFluidData(holder, fluidData);
            ShakeUtil.setItemData(holder, itemData);
            holder.set(DataComponentTypeRegistries.SHAKE_SUCCESS_TIMES, shakeSuccessTimes);
            ShakeUtil.setItemData(mainHandItem, List.of(holder));
            return;
        }
        if (!mainHandItem.is(ItemRegistries.SHAKE)) {
            return;
        }
        result.map(RecipeHolder::value).ifPresent(recipe -> {
            ItemStack itemStack = recipe.result().create();
            if (itemStack.has(DataComponentTypeRegistries.SHAKE_PRODUCT_DEFERRED_NAME)) {
                MutableComponent name = itemStack.get(DataComponentTypeRegistries.SHAKE_PRODUCT_DEFERRED_NAME).getName(fluidData, itemData);
                itemStack.set(DataComponents.ITEM_NAME, name);
            }
            ShakeUtil.clearFluidData(mainHandItem);
            ShakeUtil.clearItemData(mainHandItem);
            mainHandItem.remove(DataComponentTypeRegistries.SHAKE_ICE_CUBES);
            mainHandItem.remove(DataComponentTypeRegistries.SHAKING);
            ShakeUtil.setItemData(mainHandItem, List.of(itemStack));
        });
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SHAKE_FINISH;
    }
}
