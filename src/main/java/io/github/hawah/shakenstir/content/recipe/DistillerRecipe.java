package io.github.hawah.shakenstir.content.recipe;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record DistillerRecipe(
        CommonInfo commonInfo,
        List<Ingredient> inputItems,
        FluidIngredient inputFluid,
        int cookingTime,
        ItemStackTemplate result
) implements Recipe<DistillerRecipeInput> {

    public static final MapCodec<DistillerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CommonInfo.MAP_CODEC.forGetter(DistillerRecipe::commonInfo),
            Ingredient.CODEC.listOf(0, 6).fieldOf("inputItems").forGetter(DistillerRecipe::inputItems),
            FluidIngredient.CODEC.fieldOf("inputFluid").forGetter(DistillerRecipe::inputFluid),
            Codec.INT.fieldOf("cookingTime").forGetter(DistillerRecipe::cookingTime),
            ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(DistillerRecipe::result)
    ).apply(inst, DistillerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DistillerRecipe> STREAM_CODEC = StreamCodec.composite(
            CommonInfo.STREAM_CODEC, DistillerRecipe::commonInfo,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), DistillerRecipe::inputItems,
            FluidIngredient.STREAM_CODEC, DistillerRecipe::inputFluid,
            ByteBufCodecs.INT, DistillerRecipe::cookingTime,
            ItemStack.STREAM_CODEC.map(
                    stack -> new ItemStackTemplate(stack.getItem(), stack.getCount(), stack.getComponentsPatch()),
                    ItemStackTemplate::create
            ), DistillerRecipe::result,
            DistillerRecipe::new
    );

    @Override
    public boolean matches(DistillerRecipeInput input, Level level) {
        List<ItemStack> remainingItems = new ArrayList<>(input.items());
        for (Ingredient ingredient : this.inputItems) {
            Optional<ItemStack> matched = remainingItems.stream()
                    .filter(ingredient)
                    .findFirst();
            if (matched.isPresent()) {
                remainingItems.remove(matched.get());
            } else {
                return false;
            }
        }

        return inputFluid.match(input.fluidStack()) >= inputFluid.amount();
    }

    @Override
    public ItemStack assemble(DistillerRecipeInput input) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "Distiller";
    }

    @Override
    public RecipeSerializer<? extends Recipe<DistillerRecipeInput>> getSerializer() {
        return RecipeTypeRegistries.DISTILLER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<DistillerRecipeInput>> getType() {
        return RecipeTypeRegistries.DISTILLER_RECIPE.get();
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
