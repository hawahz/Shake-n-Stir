package io.github.hawah.shakenstir.foundation.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.Consumable;

/**
 * 数据包注册的 {@link Consumable} → 本地化键后缀映射。
 * <p>
 * 完整本地化键为：{@code shakenstir.consumable.<descriptionKey>}
 * <p>
 * 允许数据包覆盖或添加消耗品工具提示描述。
 */
// TODO: 人工审查 - 2026-06-27 - 从旧 Identifier consumable 字段重构为 Consumable + descriptionKey 记录，新增 CODEC/STREAM_CODEC 以支持数据包注册
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
