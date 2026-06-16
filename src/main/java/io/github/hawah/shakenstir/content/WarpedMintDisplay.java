package io.github.hawah.shakenstir.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.MintSizeComponent;
import io.github.hawah.shakenstir.content.dataComponent.WarpedMint;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record WarpedMintDisplay(int cap) implements ConditionalItemModelProperty {
    public static final MapCodec<WarpedMintDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(Codec.INT.optionalFieldOf("cap", 0).forGetter(WarpedMintDisplay::cap)
            ).apply(i, WarpedMintDisplay::new));
    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        if (!itemStack.has(DataComponentTypeRegistries.WARPED_MINT) && cap == -1) {
            return true;
        }
        WarpedMint warpedMint = itemStack.getOrDefault(DataComponentTypeRegistries.WARPED_MINT, new WarpedMint());
        if (warpedMint.isEmpty()) {
            return cap == -1;
        }
        int idx = itemStack.getOrDefault(DataComponentTypeRegistries.SELECT_MINT, 0);
        int size = warpedMint.contents().get(idx).getOrDefault(DataComponentTypeRegistries.MINT_SIZE, new MintSizeComponent(-1)).size();

        return size == cap;
    }
}
