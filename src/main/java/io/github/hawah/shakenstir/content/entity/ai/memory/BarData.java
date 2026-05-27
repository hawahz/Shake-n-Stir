package io.github.hawah.shakenstir.content.entity.ai.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;

public record BarData(
        List<BlockPos> barCounter,
        List<BlockPos> bartenderArea,
        ResourceKey<Level> dimension
) {
    public static final Codec<BarData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BlockPos.CODEC.listOf().fieldOf("bar_counter").forGetter(BarData::barCounter),
            BlockPos.CODEC.listOf().fieldOf("bartender_area").forGetter(BarData::bartenderArea),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(BarData::dimension)
    ).apply(inst, BarData::new));

}
