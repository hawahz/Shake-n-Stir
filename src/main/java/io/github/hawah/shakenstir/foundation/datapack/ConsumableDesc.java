package io.github.hawah.shakenstir.foundation.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.Consumable;

/**
 * A datapack-registered mapping from a {@link Consumable} to a localization key suffix.
 * <p>
 * The full localization key is: {@code shakenstir.consumable.<descriptionKey>}
 * <p>
 * This allows data packs to override or add consumable tooltip descriptions.
 */
public record ConsumableDesc(Consumable consumable, String descriptionKey) {
    public static final Codec<ConsumableDesc> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Consumable.CODEC.fieldOf("consumable").forGetter(ConsumableDesc::consumable),
            Codec.STRING.fieldOf("description_key").forGetter(ConsumableDesc::descriptionKey)
    ).apply(inst, ConsumableDesc::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConsumableDesc> STREAM_CODEC = StreamCodec.composite(
            Consumable.STREAM_CODEC, ConsumableDesc::consumable,
            ByteBufCodecs.STRING_UTF8, ConsumableDesc::descriptionKey,
            ConsumableDesc::new
    );
}
