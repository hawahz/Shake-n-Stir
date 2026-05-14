package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Deprecated
public record ShakeItemDataComponent(List<ItemStack> itemStacks) implements IItemDataHolder{
    public static final ShakeItemDataComponent EMPTY = new ShakeItemDataComponent(NonNullList.of(ItemStack.EMPTY));

    public static final Codec<ShakeItemDataComponent> CODEC = ItemStack.CODEC.listOf().xmap(ShakeItemDataComponent::new, ShakeItemDataComponent::itemStacks);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShakeItemDataComponent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), ShakeItemDataComponent::itemStacks,
            ShakeItemDataComponent::new
    );

    public int itemCount() {
        return itemStacks.size();
    }
}
