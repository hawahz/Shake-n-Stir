package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record SingleItemComponent(ItemStack itemStack) {

    public static final SingleItemComponent EMPTY = new SingleItemComponent(ItemStack.EMPTY);

    public static final Codec<SingleItemComponent> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ItemStack.OPTIONAL_CODEC.fieldOf("item")
                    .forGetter(SingleItemComponent::itemStack))
            .apply(instance, SingleItemComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SingleItemComponent> STREAM_CODEC =
            StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, SingleItemComponent::itemStack, SingleItemComponent::new);

    @Override
    public boolean equals(Object arg0) {
        return arg0 instanceof ItemStack otherItem && ItemStack.isSameItemSameComponents(otherItem, itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemStack.getItem(), itemStack.getCount(), itemStack.getComponents());
    }
}
