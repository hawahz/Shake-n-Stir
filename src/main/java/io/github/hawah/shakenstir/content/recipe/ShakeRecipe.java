package io.github.hawah.shakenstir.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.recipe.ingredient.FluidIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @param inputFluids private final ShakeRecipeInput.BlockBookInfo bookInfo;
 */
public record ShakeRecipe(CommonInfo commonInfo, List<FluidIngredient> inputFluids, List<Ingredient> inputItems,
                          ItemStackTemplate result, int shakeTimes) implements Recipe<ShakeRecipeInput> {

    public static final MapCodec<ShakeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CommonInfo.MAP_CODEC.forGetter(ShakeRecipe::commonInfo),
            FluidIngredient.CODEC.listOf(1, 6).fieldOf("inputFluids").forGetter(ShakeRecipe::inputFluids),
            Ingredient.CODEC.listOf(1, 6).fieldOf("inputItems").forGetter(ShakeRecipe::inputItems),
            ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(ShakeRecipe::result),
            Codec.INT.fieldOf("shakeTimes").forGetter(ShakeRecipe::shakeTimes)
    ).apply(inst, ShakeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShakeRecipe> STREAM_CODEC = StreamCodec.composite(
            CommonInfo.STREAM_CODEC, ShakeRecipe::commonInfo,
            FluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), ShakeRecipe::inputFluids,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), ShakeRecipe::inputItems,
            // 对于 ItemStackTemplate，我们借助 ItemStack 进行转换
            ItemStack.STREAM_CODEC.map(
                    // 解码：将 ItemStack 转为模板（假设有 fromStack 静态方法，否则用构造函数）
                    stack -> new ItemStackTemplate(stack.getItem(), stack.getCount(), stack.getComponentsPatch()),
                    // 编码：从模板创建 ItemStack
                    ItemStackTemplate::create
            ), ShakeRecipe::result,
            ByteBufCodecs.INT, ShakeRecipe::shakeTimes,
            ShakeRecipe::new
    );

    public List<FluidStack> getFluidStacks() {
        return this.inputFluids.stream()
                .map(FluidIngredient::toFluidStack)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matches(ShakeRecipeInput input, Level level) {
        // 1. 物品无序匹配：每找到一个匹配就从输入中移除一个
        List<ItemStack> remainingItems = new ArrayList<>(input.items());
        for (Ingredient ingredient : this.inputItems) {
            Optional<ItemStack> matched = remainingItems.stream()
                    .filter(ingredient)
                    .findFirst();
            if (matched.isPresent()) {
                remainingItems.remove(matched.get());
            } else {
                return false; // 有配方要求的Ingredient找不到匹配
            }
        }
        // 可选检查：是否允许输入有额外多余物品？通常配方匹配允许输入多于要求，所以不检查 remainingItems 是否空。

        // 2. 流体无序匹配：同样逻辑，只看流体类型和组件，忽略数量
        List<FluidStack> remainingFluids = new ArrayList<>(input.fluidStacks());
        for (FluidStack required : this.getFluidStacks()) {
            Optional<FluidStack> matched = remainingFluids.stream()
                    .filter(fs -> FluidStack.isSameFluidSameComponents(fs, required))
                    .findFirst();
            if (matched.isPresent()) {
                remainingFluids.remove(matched.get());
            } else {
                return false;
            }
        }

        if (shakeTimes() > input.shakeTime()) {
            return false;
        }

        return true;
    }


    @Override
    public ItemStack assemble(ShakeRecipeInput input) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "Shake";
    }

    @Override
    public RecipeSerializer<? extends Recipe<ShakeRecipeInput>> getSerializer() {
        return RecipeTypeRegistries.SHAKE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<ShakeRecipeInput>> getType() {
        return RecipeTypeRegistries.SHAKE_RECIPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.createFromOptionals(
                this.inputItems.stream()
                        .map(Optional::of)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
