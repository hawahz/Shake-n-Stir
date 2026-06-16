package io.github.hawah.shakenstir.client.itemConditionals;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.MintSizeComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record MintSize(int index) implements ConditionalItemModelProperty {
    public static final MapCodec<MintSize> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("size", 0).forGetter(MintSize::index)
            ).apply(i, MintSize::new));
    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return itemStack.getOrDefault(DataComponentTypeRegistries.MINT_SIZE, MintSizeComponent.of(-1)).size() == index();
    }
}
