package io.github.hawah.shakenstir.content.recipe.shake;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.IFluidDataHolder;
import io.github.hawah.shakenstir.content.dataComponent.IItemDataHolder;
import io.github.hawah.shakenstir.content.dataComponent.ShakeProductDeferredName;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.recipe.IScoreSortedRecipe;
import io.github.hawah.shakenstir.content.recipe.Quality;
import io.github.hawah.shakenstir.content.recipe.RecipeTypeRegistries;
import io.github.hawah.shakenstir.content.recipe.datapack.DrinkData;
import io.github.hawah.shakenstir.content.recipe.datapack.cocktaileType.CocktailTypes;
import io.github.hawah.shakenstir.content.recipe.datapack.spirit.SpiritData;
import io.github.hawah.shakenstir.content.recipe.ingredient.FluidIngredient;
import io.github.hawah.shakenstir.foundation.BaseFluidType;
import io.github.hawah.shakenstir.foundation.recipeRecord.ServerRecipeHelper;
import io.github.hawah.shakenstir.foundation.utils.ShakeUtil;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ShakeRecipe(
        CommonInfo commonInfo,
        List<FluidIngredient> inputFluids,
        List<Ingredient> inputItems,
        ItemStackTemplate result,
        int shakeTimes
) implements Recipe<ShakeRecipeInput>, IScoreSortedRecipe<ShakeRecipeInput> {

    public static final MapCodec<ShakeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CommonInfo.MAP_CODEC.forGetter(ShakeRecipe::commonInfo),
            FluidIngredient.CODEC.listOf(0, 6).fieldOf("inputFluids").forGetter(ShakeRecipe::inputFluids),
            Ingredient.CODEC.listOf(0, 6).fieldOf("inputItems").forGetter(ShakeRecipe::inputItems),
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

    public static void createAndAddShakeResult(
            float pastProcess,
            ItemStack shaker,
            ShakeRecipe recipe,
            int finalShakeSuccessTimes,
            ShakeRecipeInput recipeInput,
            ItemStack mainHandItem,
            ServerLevel level,
            int iceCount,
            Player player
    ) {
        int shakeAdditionTimes = finalShakeSuccessTimes - recipe.shakeTimes();
        int failTimes = shaker.getOrDefault(DataComponentTypeRegistries.SHAKE_FALI_TIMES, 0);
        ItemStack resultItem = recipe.assemble(recipeInput);
        Quality quality = Quality.calculate(
                failTimes,
                pastProcess,
                mainHandItem.getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 1),
                shakeAdditionTimes
        );
        resultItem.set(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY, quality);
        resultItem.set(DataComponentTypeRegistries.DRINK_DATA, new DrinkData(
                resultItem.get(DataComponentTypeRegistries.COCKTAIL_TYPE),
                SpiritData.get(
                        level,
                        recipeInput.fluidStacks()
                                .stream()
                                .max(Comparator.comparing(FluidStack::getAmount))
                                .orElseThrow()
                                .typeHolder()
                ),
                List.of(),
                List.of(),
                quality,
                iceCount
        ));
        applyResult(finalShakeSuccessTimes, recipeInput, mainHandItem, player, resultItem);
    }

    private static void applyResult(int finalShakeSuccessTimes, ShakeRecipeInput recipeInput, ItemStack mainHandItem, Player player, ItemStack resultItem) {

        ServerRecipeHelper.writeRecipe(
                player,
                new ArrayList<>(recipeInput.items()),
                new ArrayList<>(recipeInput.fluidStacks()),
                resultItem.copy(),
                SnsRecipeHolder.Type.SHAKE,
                finalShakeSuccessTimes,
                GlasswareItem.getDefaultDisplay(resultItem)
        );
        ShakeUtil.clearContent(mainHandItem);
        ShakeUtil.setItemData(mainHandItem, List.of(resultItem));
    }

    public static void cook(ItemStack shaker, int shakeSuccessTimes, ServerLevel level, ItemStack mainHandItem, float past, int iceCount, Player player) {
        ItemStack predicatedImmProduct;
        List<ItemStack> itemData = new ArrayList<>(ShakeUtil.getItemStacks(shaker));
        List<FluidStack> fluidData = new ArrayList<>(ShakeUtil.getFluidStacks(shaker));
        if (!itemData.isEmpty() && (predicatedImmProduct = itemData.getFirst())!=null && predicatedImmProduct.is(ItemRegistries.CONTENT_HOLDER) && predicatedImmProduct.has(DataComponentTypeRegistries.SHAKE_SUCCESS_TIMES)) {
            IItemDataHolder item = ShakeUtil.getItemData(predicatedImmProduct);
            itemData.removeFirst();
            itemData.addAll(item.itemStacks());
            IFluidDataHolder fluid = ShakeUtil.getFluidData(predicatedImmProduct);
            fluidData.addAll(fluid.fluidStacks());
            shakeSuccessTimes += predicatedImmProduct.getOrDefault(DataComponentTypeRegistries.SHAKE_SUCCESS_TIMES, 0);
        }

        RecipeManager recipeManager = level.recipeAccess();
        ShakeRecipeInput recipeInput = new ShakeRecipeInput(itemData, fluidData, shakeSuccessTimes);
        Optional<RecipeHolder<ShakeRecipe>> result = recipeManager.getRecipeFor(
                RecipeTypeRegistries.SHAKE_RECIPE.get(),
                recipeInput,
                level
        );
        int failTimes = mainHandItem.getOrDefault(DataComponentTypeRegistries.SHAKE_FALI_TIMES, 0);

        if (result.isEmpty()) {
            if (failTimes > 1) {
                ItemStack resultItem = createSuspiciousResult(
                        past,
                        shaker,
                        recipeInput,
                        level,
                        iceCount
                );
                applyResult(
                        shakeSuccessTimes,
                        recipeInput,
                        mainHandItem,
                        player,
                        resultItem
                );
                return;
            }
            mainHandItem.set(DataComponentTypeRegistries.SHAKING, Unit.INSTANCE);
            mainHandItem.set(DataComponentTypeRegistries.SHAKE_FALI_TIMES, failTimes + 1);
            mainHandItem.remove(DataComponentTypeRegistries.SHAKE_ICE_CUBES);
            return;
        }
        final int finalShakeSuccessTimes = shakeSuccessTimes;
        result.map(RecipeHolder::value).ifPresent(recipe -> {
            createAndAddShakeResult(
                    past,
                    shaker,
                    recipe,
                    finalShakeSuccessTimes,
                    recipeInput,
                    mainHandItem,
                    level,
                    iceCount,
                    player
            );
        });
    }

    public static ItemStack createSuspiciousResult(
            float pastProcess,
            ItemStack shaker,
            ShakeRecipeInput recipeInput,
            ServerLevel level,
            int iceCount
    ) {
        int failTimes = shaker.getOrDefault(DataComponentTypeRegistries.SHAKE_FALI_TIMES, 0);
        ItemStack resultItem = ItemRegistries.CONTENT_HOLDER.toStack();
        resultItem.set(DataComponentTypeRegistries.COCKTAIL_TYPE, CocktailTypes.SUSPICIOUS_VALUE);
        Quality quality = Quality.calculate(
                failTimes,
                pastProcess,
                shaker.getOrDefault(DataComponentTypeRegistries.SHAKE_ICE_CUBES, 1),
                0
        );
        int rgb = ShakeUtil.rgbWithWeight(recipeInput.fluidStacks().stream().map((stack) ->
                Pair.of(stack.getFluidType() instanceof BaseFluidType type ? type.getTintColor() : 0xFFFFFF, stack.getAmount())
        ).toList());
        resultItem.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
        resultItem.set(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY, quality);
        resultItem.set(DataComponentTypeRegistries.DRINK_DATA, new DrinkData(
                resultItem.get(DataComponentTypeRegistries.COCKTAIL_TYPE),
                SpiritData.get(
                        level,
                        recipeInput.fluidStacks()
                                .stream()
                                .max(Comparator.comparing(FluidStack::getAmount))
                                .orElseThrow()
                                .typeHolder()
                ),
                List.of(),
                List.of(),
                quality,
                iceCount
        ));
        return resultItem;
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

        return shakeTimes() <= input.shakeTime();
    }


    @Override
    public ItemStack assemble(ShakeRecipeInput input) {
        ItemStack resultItem = this.result.create();
        if (resultItem.has(DataComponentTypeRegistries.COCKTAIL_TYPE)) {
            MutableComponent name = resultItem.getOrDefault(DataComponentTypeRegistries.COCKTAIL_TYPE, ShakeProductDeferredName.EMPTY).translate(input.fluidStacks(), input.items());
            resultItem.set(DataComponents.ITEM_NAME, name);
        }
        int rgb = ShakeUtil.rgbWithWeight(input.fluidStacks().stream().map((stack) ->
                Pair.of(stack.getFluidType() instanceof BaseFluidType type ? type.getTintColor() : 0xFFFFFF, stack.getAmount())
        ).toList());
        resultItem.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
        return resultItem;
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

    // TODO Check Vibe Code
    @Override
    public int score(ShakeRecipeInput recipeInput) {
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

        // 3. 摇晃次数匹配评分：刚好等于要求时分数最高
        int shakeDiff = recipeInput.shakeTime() - shakeTimes();
        if (shakeDiff == 0) {
            score += 20; // 完美匹配
        } else {
            score -= shakeDiff * 2; // 超出部分扣分
        }

        return Math.max(0, score);
    }
}
