package io.github.hawah.shakenstir.client.itemConditionals;

import com.mojang.serialization.MapCodec;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record Warped() implements ConditionalItemModelProperty {
    public static final MapCodec<Warped> MAP_CODEC = MapCodec.unit(new Warped());
    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return itemStack.has(DataComponentTypeRegistries.RECIPE_HOLDER);
    }
}
