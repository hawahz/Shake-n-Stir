package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.core.config.builder.api.Component;

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

    public static final DataComponentType<FluidStackDataComponent> SPIRIT_CONTENT = register(
            "spirit_content",
            builder -> builder.persistent(FluidStackDataComponent.CODEC).networkSynchronized(FluidStackDataComponent.STREAM_CODEC)
    );

    // Shake

    public static final DataComponentType<Boolean> SHAKING = register(
            "shake_shaking",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<ShakeFluidDataComponent> SHAKE_CONTENT = register(
            "shake_content",
            builder -> builder.persistent(ShakeFluidDataComponent.CODEC).networkSynchronized(ShakeFluidDataComponent.STREAM_CODEC)
    );

    public static final DataComponentType<ShakeItemDataComponent> SHAKE_ITEM_INGREDIENT = register(
            "shake_item_ingredient",
            builder -> builder.persistent(ShakeItemDataComponent.CODEC).networkSynchronized(ShakeItemDataComponent.STREAM_CODEC)
    );

    public static final DataComponentType<Integer> SHAKE_ICE_CUBES = register(
            "shake_ice_cubes",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    // Product Above

    public static final DataComponentType<Boolean> SHAKE_BUBBLES = register(
            "shake_bubbles",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Integer> SHAKE_SUCCESS_TIMES = register(
            "shake_success_times",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    // 产物是否可以倒出来，也就是当结果为true的时候，产物载体可以倒到鸡尾酒杯当中，所有component都会直接继承到鸡尾酒杯上
    public static final DataComponentType<Boolean> SHAKE_PRODUCT_POURABLE = register(
            "shake_product_pourable",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    // 调酒后的产品从方块形式变成物品的时候，会变成什么样的物品。如果有此组件，则覆盖 SHAKE_PRODUCT_POURABLE 字段
    //
    public static final DataComponentType<ItemStack> SHAKE_PRODUCT_POUR_TAKE_ITEM = register(
            "shake_product",
            builder -> builder.persistent(ItemStack.OPTIONAL_CODEC).networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC)
    );

    public static final DataComponentType<ShakeProductDeferredName> SHAKE_PRODUCT_DEFERRED_NAME = register(
            "shake_product_deferred_name",
            builder -> builder.persistent(ShakeProductDeferredName.CODEC).networkSynchronized(ShakeProductDeferredName.STREAM_CODEC)
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
