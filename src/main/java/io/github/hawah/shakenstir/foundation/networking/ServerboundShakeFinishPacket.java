package io.github.hawah.shakenstir.foundation.networking;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.IFluidDataHolder;
import io.github.hawah.shakenstir.content.dataComponent.IItemDataHolder;
import io.github.hawah.shakenstir.content.entity.ai.behavior.recipeProvider.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.recipe.Quality;
import io.github.hawah.shakenstir.content.recipe.RecipeTypeRegistries;
import io.github.hawah.shakenstir.content.recipe.ShakeRecipe;
import io.github.hawah.shakenstir.content.recipe.ShakeRecipeInput;
import io.github.hawah.shakenstir.foundation.datapack.DrinkData;
import io.github.hawah.shakenstir.foundation.datapack.spirit.SpiritData;
import io.github.hawah.shakenstir.foundation.recipeRecord.ServerRecipeHelper;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import io.github.hawah.shakenstir.lib.networking.ClientToServerPacket;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;

public record ServerboundShakeFinishPacket(UUID playerUUID, ItemStack shakeItem, int shakeSuccessTimes, float pastProcess, int iceCount) implements ClientToServerPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundShakeFinishPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,  ServerboundShakeFinishPacket::playerUUID,
            ItemStack.STREAM_CODEC, ServerboundShakeFinishPacket::shakeItem,
            ByteBufCodecs.INT,      ServerboundShakeFinishPacket::shakeSuccessTimes,
            ByteBufCodecs.FLOAT,   ServerboundShakeFinishPacket::pastProcess,
            ByteBufCodecs.INT,      ServerboundShakeFinishPacket::iceCount,
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
        ShakeRecipeInput recipeInput = new ShakeRecipeInput(itemData, fluidData, shakeSuccessTimes());
        Optional<RecipeHolder<ShakeRecipe>> result = recipeManager.getRecipeFor(
                RecipeTypeRegistries.SHAKE_RECIPE.get(),
                recipeInput,
                level
        );
        if (result.isEmpty()) {
            mainHandItem.set(DataComponentTypeRegistries.SHAKING, true);
            mainHandItem.set(DataComponentTypeRegistries.SHAKE_FALI_TIMES, mainHandItem.getOrDefault(DataComponentTypeRegistries.SHAKE_FALI_TIMES, 0) + 1);
            mainHandItem.remove(DataComponentTypeRegistries.SHAKE_ICE_CUBES);
            return;
        }
        if (!mainHandItem.is(ItemRegistries.SHAKER)) {
            return;
        }
        int finalShakeSuccessTimes = shakeSuccessTimes;
        result.map(RecipeHolder::value).ifPresent(recipe -> {
            int shakeAdditionTimes = finalShakeSuccessTimes - recipe.shakeTimes();
            ItemStack resultItem = recipe.assemble(recipeInput);
            int failTimes = shakeItem.getOrDefault(DataComponentTypeRegistries.SHAKE_FALI_TIMES, 0);
            Quality quality = Quality.calculate(failTimes, pastProcess, mainHandItem.getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 1), shakeAdditionTimes);
            resultItem.set(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY, quality);
            resultItem.set(DataComponentTypeRegistries.DRINK_DATA, new DrinkData(
                    resultItem.get(DataComponentTypeRegistries.COCKTAIL_TYPE),
                    SpiritData.get(level, fluidData.stream().max(Comparator.comparing(FluidStack::getAmount)).orElseThrow().typeHolder()),
                    List.of(),
                    List.of(),
                    quality,
                    iceCount()
            ));
            ServerRecipeHelper.writeRecipe(player, new ArrayList<>(itemData), new ArrayList<>(fluidData), resultItem.copy(), SnsRecipeHolder.Type.SHAKE, shakeSuccessTimes());
            ShakeUtil.clearContent(mainHandItem);
            ShakeUtil.setItemData(mainHandItem, List.of(resultItem));
        });
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NetworkPackets.SHAKE_FINISH;
    }
}
