package io.github.hawah.shakenstir.content.data;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.lib.StreamCodecUtil;
import io.github.hawah.shakenstir.util.SerializeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record SnsRecipeHolder(
        Type recipe,
        List<ItemStack> requiredItems,
        List<FluidStack> requiredFluids,
        int shakeTimes,
        ItemStack result,
        String holderGlass,
        List<GlasswareBlockEntity.Decoration> decorations,
        String name
) implements TooltipProvider {

    public static final Codec<SnsRecipeHolder> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            SerializeHelper.ofEnum(Type.class).fieldOf("recipe").forGetter(SnsRecipeHolder::recipe),
            ItemStack.CODEC.listOf().fieldOf("required_items").forGetter(SnsRecipeHolder::requiredItems),
            FluidStack.CODEC.listOf().fieldOf("required_fluids").forGetter(SnsRecipeHolder::requiredFluids),
            Codec.INT.fieldOf("shake_times").forGetter(SnsRecipeHolder::shakeTimes),
            ItemStack.CODEC.fieldOf("result_factory").forGetter(SnsRecipeHolder::result),
            Codec.STRING.fieldOf("holder_glass").forGetter(SnsRecipeHolder::holderGlass),
            GlasswareBlockEntity.Decoration.CODEC.listOf().fieldOf("decorations").forGetter(SnsRecipeHolder::decorations),
            Codec.STRING.fieldOf("name").forGetter(SnsRecipeHolder::name)
    ).apply(inst, SnsRecipeHolder::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SnsRecipeHolder> STREAM_CODEC = StreamCodec.composite(
            StreamCodecUtil.ofEnum(Type.class), SnsRecipeHolder::recipe,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), SnsRecipeHolder::requiredItems,
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), SnsRecipeHolder::requiredFluids,
            ByteBufCodecs.INT, SnsRecipeHolder::shakeTimes,
            ItemStack.STREAM_CODEC, SnsRecipeHolder::result,
            ByteBufCodecs.stringUtf8(128), SnsRecipeHolder::name,
            GlasswareBlockEntity.Decoration.STREAM_CODEC.apply(ByteBufCodecs.list()), SnsRecipeHolder::decorations,
            ByteBufCodecs.stringUtf8(128), SnsRecipeHolder::holderGlass,
            SnsRecipeHolder::new
    );

    public SnsRecipeHolder(Type recipe,
                           List<ItemStack> requiredItems,
                           List<FluidStack> requiredFluids,
                           int shakeTimes,
                           ItemStack result) {
        this(recipe, requiredItems, requiredFluids, shakeTimes, result, "martini_glass", List.of(), "");
    }

    public SnsRecipeHolder named(String name) {
        return new SnsRecipeHolder(
                recipe,
                requiredItems.stream().map(ItemStack::copy).toList(),
                requiredFluids.stream().map(FluidStack::copy).toList(),
                shakeTimes,
                result.copy(),
                holderGlass(),
                decorations(),
                name);
    }

    public SnsRecipeHolder glass(String glassType) {
        return new SnsRecipeHolder(
                recipe,
                requiredItems.stream().map(ItemStack::copy).toList(),
                requiredFluids.stream().map(FluidStack::copy).toList(),
                shakeTimes,
                result.copy(),
                glassType,
                decorations,
                name()
        );
    }

    public SnsRecipeHolder decorations(List<GlasswareBlockEntity.Decoration> decorations) {
        return new SnsRecipeHolder(
                recipe,
                requiredItems.stream().map(ItemStack::copy).toList(),
                requiredFluids.stream().map(FluidStack::copy).toList(),
                shakeTimes,
                result.copy(),
                holderGlass(),
                decorations,
                name()
        );
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        consumer.accept(Component.literal(name).withStyle(ChatFormatting.GOLD));
        consumer.accept(LangData.TOOLTIP_SCROLL_RECIPE_REQUIRED.get());
        for (ItemStack itemStack : requiredItems) {
            consumer.accept(Component.literal("- ").append(itemStack.getHoverName()));
        }
        for (FluidStack fluidStack : requiredFluids) {
            consumer.accept(Component.literal("- ").append(fluidStack.getHoverName()).append( " " + fluidStack.getAmount() + "mb"));
        }
        consumer.accept(LangData.TOOLTIP_SCROLL_RECIPE_RESULT.get(result.getHoverName()));
        consumer.accept(LangData.TOOLTIP_SCROLL_RECIPE_SHAKE_TIMES.get(Component.literal(String.valueOf(shakeTimes)).withStyle(ChatFormatting.GOLD)));
    }

    public SnsRecipeHolder copy() {
        return new SnsRecipeHolder(
                recipe,
                requiredItems.stream().map(ItemStack::copy).toList(),
                requiredFluids.stream().map(FluidStack::copy).toList(),
                shakeTimes,
                result.copy(),
                holderGlass(),
                decorations,
                name
        );
    }

    public List<Ingredient> getItemToFind() {
        List<Ingredient> itemToFind = new ArrayList<>();
        itemToFind.add(Ingredient.of(ItemRegistries.SHORT_DRINK_GLASSWARE));
        for (GlasswareBlockEntity.Decoration decoration : decorations) {
            itemToFind.add(Ingredient.of(decoration.itemStack().getItem()));
        }
        return itemToFind;
    }

    public enum Type {
        SHAKE(GlasswareBlockEntity::pourProduct),
        STIR((_, _) -> {})
        ;
        private final BiConsumer<GlasswareBlockEntity, ItemStack> action;

        Type(BiConsumer<GlasswareBlockEntity, ItemStack> action) {
            this.action = action;
        }

        public void apply(GlasswareBlockEntity glassware, ItemStack itemStack) {
            action.accept(glassware, itemStack);
        }
    }
}
