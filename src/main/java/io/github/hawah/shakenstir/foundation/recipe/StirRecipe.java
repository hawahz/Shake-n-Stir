package io.github.hawah.shakenstir.foundation.recipe;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.foundation.recipe.ingredient.FluidIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record StirRecipe(Recipe.CommonInfo commonInfo, List<FluidIngredient> inputFluids, List<Ingredient> inputItems,
                         ItemStackTemplate result) implements Recipe<StirRecipeInput>, IScoreSortedRecipe<StirRecipeInput>{

    public static final MapCodec<StirRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CommonInfo.MAP_CODEC.forGetter(StirRecipe::commonInfo),
            FluidIngredient.CODEC.listOf(0, 6).fieldOf("inputFluids").forGetter(StirRecipe::inputFluids),
            Ingredient.CODEC.listOf(0, 6).fieldOf("inputItems").forGetter(StirRecipe::inputItems),
            ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(StirRecipe::result)
    ).apply(inst, StirRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StirRecipe> STREAM_CODEC = StreamCodec.composite(
            CommonInfo.STREAM_CODEC, StirRecipe::commonInfo,
            FluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), StirRecipe::inputFluids,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), StirRecipe::inputItems,
            // 对于 ItemStackTemplate，我们借助 ItemStack 进行转换
            ItemStack.STREAM_CODEC.map(
                    // 解码：将 ItemStack 转为模板（假设有 fromStack 静态方法，否则用构造函数）
                    stack -> new ItemStackTemplate(stack.getItem(), stack.getCount(), stack.getComponentsPatch()),
                    // 编码：从模板创建 ItemStack
                    ItemStackTemplate::create
            ), StirRecipe::result,
            StirRecipe::new
    );
    @Override
    public boolean matches(StirRecipeInput input, Level level) {
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
        List<FluidStack> remainingFluids = new ArrayList<>(input.fluidStacks().stream().map(FluidStack::copy).toList());
        for (FluidIngredient required : inputFluids) {
            int matched = remainingFluids.stream()
                    .mapToInt(required::match)
                    .sum();
            if (matched >= required.amount()) {
                int consumed = 0;
                for (int i = 0; i < remainingFluids.size() && consumed < required.amount(); i++) {
                    FluidStack stack = remainingFluids.get(i);
                    if (required.fluidId().test(stack)) {
                        int available = stack.getAmount();
                        int needed = required.amount() - consumed;
                        int toConsume = Math.min(available, needed);
                        stack.shrink(toConsume);
                        consumed += toConsume;
                        if (stack.isEmpty()) {
                            remainingFluids.remove(i);
                            i--;
                        }
                    }
                }
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(StirRecipeInput input) {
        return null;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "Stir";
    }

    @Override
    public RecipeSerializer<? extends Recipe<StirRecipeInput>> getSerializer() {
        return RecipeTypeRegistries.STIR_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<StirRecipeInput>> getType() {
        return RecipeTypeRegistries.STIR_RECIPE.get();
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

    // TODO Check Vibe Code
    @Override
    public int score(StirRecipeInput recipeInput) {
        int score = 0;

        // 1. 物品匹配评分：消耗的物品越多且剩余越少，分数越高
        List<ItemStack> remainingItems = new ArrayList<>(recipeInput.items());
        int matchedItemCount = 0;
        for (Ingredient ingredient : this.inputItems) {
            Optional<ItemStack> matched = remainingItems.stream()
                    .filter(ingredient)
                    .findFirst();
            if (matched.isPresent()) {
                matchedItemCount++;
                remainingItems.remove(matched.get());
            }
        }
        // 基础分：成功匹配的物品数 * 10
        score += matchedItemCount * 10;
        // 惩罚分：剩余未使用的物品每个扣5分（鼓励精确匹配）
        score -= remainingItems.size() * 5;

        // 2. 流体匹配评分
        List<FluidStack> remainingFluids = new ArrayList<>(recipeInput.fluidStacks().stream().map(FluidStack::copy).toList());
        int totalRequiredFluid = 0;
        int totalMatchedFluid = 0;
        for (FluidIngredient required : inputFluids) {
            totalRequiredFluid += required.amount();
            int matched = remainingFluids.stream()
                    .mapToInt(required::match)
                    .sum();
            if (matched >= required.amount()) {
                int consumed = 0;
                for (int i = 0; i < remainingFluids.size() && consumed < required.amount(); i++) {
                    FluidStack stack = remainingFluids.get(i);
                    if (required.fluidId().test(stack)) {
                        int available = stack.getAmount();
                        int needed = required.amount() - consumed;
                        int toConsume = Math.min(available, needed);
                        stack.shrink(toConsume);
                        consumed += toConsume;
                        totalMatchedFluid += toConsume;
                        if (stack.isEmpty()) {
                            remainingFluids.remove(i);
                            i--;
                        }
                    }
                }
            }
        }
        // 流体匹配分：每匹配1单位流体得1分
        score += totalMatchedFluid;
        // 剩余流体惩罚：每个剩余流体栈扣3分
        score -= remainingFluids.size() * 3;


        return Math.max(0, score);
    }
}
