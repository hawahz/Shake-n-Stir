package io.github.hawah.shakenstir.foundation.recipe.datapack.spirit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.foundation.recipe.Quality;
import io.github.hawah.shakenstir.foundation.recipe.datapack.EffectData;
import io.github.hawah.shakenstir.foundation.datapack.Registries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Supplier;

/**
 * 流体数据记录，描述一种流体及其对应的药水效果和容积信息。
 *
 * @param fluidType  流体对应的 Fluid Holder
 * @param effectData 效果对应的数据，包含效果和对应的等级
 * @param amount     该流体内容物的容积，单位为 mB（millibucket）
 */
public record FluidData(Holder<Fluid> fluidType, EffectData effectData, int amount) {
    public static final Codec<FluidData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            FluidStack.FLUID_HOLDER_CODEC.fieldOf("fluid").forGetter(FluidData::fluidType),
            EffectData.CODEC.fieldOf("positive").forGetter(FluidData::effectData),
            Codec.INT.optionalFieldOf("amount", 250).forGetter(FluidData::amount)
    ).apply(inst, FluidData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidData> STREAM_CODEC = StreamCodec.composite(
            FluidStack.FLUID_HOLDER_STREAM_CODEC, FluidData::fluidType,
            EffectData.STREAM_CODEC, FluidData::effectData,
            ByteBufCodecs.INT, FluidData::amount,
            FluidData::new
    );

    public static FluidData get(Level level, Holder<Fluid> fluidType) {
        return level.registryAccess()
                .lookup(Registries.FLUID_REGISTRY_KEY)
                .flatMap(registry ->
                        registry.stream()
                                .filter(fluidData -> fluidData.fluidType().equals(fluidType))
                                .findFirst()
                )
                .or(() -> Spirits.getBuiltIn(fluidType))
                .orElseThrow();
    }

    public static FluidData getOr(Level level, Holder<Fluid> fluidType, Supplier<FluidData> or) {
        return level.registryAccess()
                .lookup(Registries.FLUID_REGISTRY_KEY)
                .flatMap(registry ->
                        registry.stream()
                                .filter(fluidData -> fluidData.fluidType().equals(fluidType))
                                .findFirst()
                )
                .or(() -> Spirits.getBuiltIn(fluidType))
                .orElseGet(or);
    }

    public MobEffectInstance get(Quality phase) {
        return effectData().get(phase);
    }
}
