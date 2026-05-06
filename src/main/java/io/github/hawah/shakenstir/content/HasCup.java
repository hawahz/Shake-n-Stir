package io.github.hawah.shakenstir.content;

import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record HasCup() implements ConditionalItemModelProperty {
    public static final MapCodec<HasCup> MAP_CODEC = MapCodec.unit(new HasCup());
    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return itemStack.getOrDefault(DataComponentTypeRegistries.HAS_CUP, true);
    }
}
