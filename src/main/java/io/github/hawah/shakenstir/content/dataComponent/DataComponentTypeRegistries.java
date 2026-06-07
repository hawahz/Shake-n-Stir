package io.github.hawah.shakenstir.content.dataComponent;

import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.recipe.Quality;
import io.github.hawah.shakenstir.content.recipe.datapack.DrinkData;
import io.github.hawah.shakenstir.content.recipe.datapack.cocktaileType.CocktailType;
import io.github.hawah.shakenstir.lib.util.MutablePair;
import io.github.hawah.shakenstir.util.SerializeHelper;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.joml.Vector2f;

import java.util.List;
import java.util.UUID;
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

    public static final DataComponentType<SpiritContent> SPIRIT_CONTENT = register(
            "spirit_content",
            builder -> builder.persistent(SpiritContent.CODEC).networkSynchronized(SpiritContent.STREAM_CODEC)
    );

    public static final DataComponentType<DeferredFluidStackHolder> DEFERRED_FLUID = register(
            "deferred_fluid",
            builder -> builder.persistent(DeferredFluidStackHolder.CODEC).networkSynchronized(DeferredFluidStackHolder.STREAM_CODEC)
    );

    // Shake

    public static final DataComponentType<Boolean> SHAKING = register(
            "shake_shaking",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<ShakeContentHolder> SHAKE_CONTENT = register(
            "shake_content",
            builder -> builder.persistent(ShakeContentHolder.CODEC).networkSynchronized(ShakeContentHolder.STREAM_CODEC)
    );

    public static final DataComponentType<Integer> SHAKE_ICE_CUBES = register(
            "shake_ice_cubes",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DataComponentType<Integer> SHAKE_FALI_TIMES = register(
            "shake_fali_times",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

    // Glassware

    public static final DataComponentType<Vector2f> GLASSWARE_POSITION = register(
            "glassware_position",
            builder -> builder.persistent(SerializeHelper.VEC2F_CODEC).networkSynchronized(SerializeHelper.VEC2F_STREAM_CODEC)
    );

    public static final DataComponentType<Float> GLASSWARE_ROTATION = register(
            "glassware_rotation",
            builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)
    );

    public static final DataComponentType<List<GlasswareBlockEntity.Decoration>> GLASSWARE_DECORATIONS = register(
            "glassware_decorations",
            builder -> builder.persistent(GlasswareBlockEntity.Decoration.CODEC.listOf()).networkSynchronized(GlasswareBlockEntity.Decoration.STREAM_CODEC.apply(ByteBufCodecs.list()))
    );

    public static final DataComponentType<Boolean> GLASSWARE_HAS_FLOWER = register(
            "glassware_has_flower",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Boolean> GLASSWARE_HAS_LEMON = register(
            "glassware_has_lemon",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final DataComponentType<Component> GLASSWARE_NAME = register(
            "glassware_name",
            builder -> builder.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC)
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

    public static final DataComponentType<Quality> SHAKE_PRODUCT_QUALITY = register(
            "shake_product_quality",
            builder -> builder.persistent(Quality.CODEC).networkSynchronized(Quality.STREAM_CODEC)
    );

    public static final DataComponentType<DrinkData> DRINK_DATA = register(
            "drink_data",
            builder -> builder.persistent(DrinkData.CODEC).networkSynchronized(DrinkData.STREAM_CODEC)
    );

    public static final DataComponentType<CocktailType> COCKTAIL_TYPE = register(
            "cocktail_type",
            builder -> builder.persistent(CocktailType.CODEC).networkSynchronized(CocktailType.STREAM_CODEC)
    );

    public static final DataComponentType<BarAreaHolder> BAR_AREA = register(
            "bar_area",
            builder -> builder.persistent(BarAreaHolder.CODEC).networkSynchronized(BarAreaHolder.STREAM_CODEC)
    );

    public static final DataComponentType<UUID> PLACER = register(
            "placer",
            builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC)
    );

    public static final DataComponentType<SnsRecipeHolder> RECIPE_HOLDER = register(
            "recipe_holder",
            builder -> builder.persistent(SnsRecipeHolder.CODEC).networkSynchronized(SnsRecipeHolder.STREAM_CODEC)
    );

    public static final DataComponentType<List<MutablePair<SnsRecipeHolder, BarMenuBlockEntity.PriceAndCount>>> RECIPES_DATA = register(
            "recipes_data",
            builder -> builder.persistent(BarMenuBlockEntity.LIST_RECIPE_CODEC).networkSynchronized(BarMenuBlockEntity.LIST_RECIPES_STREAM_CODEC)
    );

    public static final DataComponentType<Identifier> MENU_BKG = register(
            "menu_bkg",
            builder -> builder.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC)
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
