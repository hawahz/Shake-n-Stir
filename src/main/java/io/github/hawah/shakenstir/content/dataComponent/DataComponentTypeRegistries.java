package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class DataComponentTypeRegistries {

    public static DeferredRegister.DataComponents DATA_COMPONENT = DeferredRegister.createDataComponents(
            Registries.DATA_COMPONENT_TYPE,
            ShakenStir.MODID
    );
    public static final DataComponentType<Boolean> HAS_CUP = register(
            "has_cup",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Boolean> SHAKING = register(
            "shake_shaking",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<FluidStackDataComponent> SPIRIT_CONTENT = register(
            "spirit_content",
            builder -> builder.persistent(FluidStackDataComponent.CODEC).networkSynchronized(FluidStackDataComponent.STREAM_CODEC)
    );

    public static final DataComponentType<ShakeFluidDataComponent> SHAKE_CONTENT = register(
            "shake_content",
            builder -> builder.persistent(ShakeFluidDataComponent.CODEC).networkSynchronized(ShakeFluidDataComponent.STREAM_CODEC)
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT.register(eventBus);
    }

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENT.register(name, () -> type);
        return type;
    }
}
