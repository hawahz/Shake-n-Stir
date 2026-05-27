package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record BarAreaHolder(BoundingBox area, ResourceKey<Level> dimension) {
    public static final Codec<BarAreaHolder> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BoundingBox.CODEC.fieldOf("area").forGetter(BarAreaHolder::area),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(BarAreaHolder::dimension)
    ).apply(inst, BarAreaHolder::new));
    public static final StreamCodec<ByteBuf, BarAreaHolder> STREAM_CODEC = StreamCodec.composite(
            BoundingBox.STREAM_CODEC, BarAreaHolder::area,
            ResourceKey.streamCodec(Registries.DIMENSION), BarAreaHolder::dimension,
            BarAreaHolder::new
    );
}
