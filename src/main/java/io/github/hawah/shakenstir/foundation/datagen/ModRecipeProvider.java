package io.github.hawah.shakenstir.foundation.datagen;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.ShakeProductDeferredName;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.recipe.ingredient.FluidIngredient;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.tags.SnsFluidTags;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import io.github.hawah.shakenstir.foundation.tags.SnsSharedTags;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModRecipeProvider extends RecipeProvider {
    private final HolderLookup.RegistryLookup<Item> items;
    private final HolderLookup.RegistryLookup<Fluid> fluids;
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        items = registries.lookupOrThrow(Registries.ITEM);
        fluids = registries.lookupOrThrow(Registries.FLUID);
    }

    @Override
    protected void buildRecipes() {
        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.SHAKE_BUBBLES, true).build())
                .orWith(SnsSharedTags.BUBBLE)
                .shake(5)
                .build()
                .unlockedBy("has_bubble", this.has(ItemRegistries.SHAKE))
                .save(output, getName("bubble_product"));

        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.SHAKE_PRODUCT_DEFERRED_NAME, new ShakeProductDeferredName(LangData.NAME_SOUR)).set(DataComponentTypeRegistries.SHAKE_PRODUCT_POURABLE, true).build())
                .withFluid(SnsFluidTags.SPIRIT, 500)
                .orWith(SnsSharedTags.SWEET)
                .orWith(SnsSharedTags.SOUR)
                .shake(20)
                .build()
                .unlockedBy("has_spirit", this.has(SnsItemTags.SPIRIT))
                .save(output, getName("sour_product"));

        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.SHAKE_PRODUCT_DEFERRED_NAME, new ShakeProductDeferredName(LangData.NAME_COCKTAIL)).build())
                .withFluid(SnsFluidTags.SPIRIT, 500)
                .orWith(SnsSharedTags.BITTER)
                .orWith(SnsSharedTags.SWEET)
                .shake(10)
                .build()
                .unlockedBy("has_spirit", this.has(SnsItemTags.SPIRIT))
                .save(output, getName("cocktail_product"));

        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
        ;
    }

    public static String getName(String name) {
        return ShakenStir.asResource(name).toString();
    }

    private Builder getBuilder() {
        return new Builder();
    }


    public static class Runner extends RecipeProvider.Runner {
        // Get the parameters from the `GatherDataEvent`s.
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ModRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "shake_recipe";
        }
    }

    class Builder extends AbstractBuilder<Builder>{
        private Supplier<Item> result;
        private int resultAmount = 1;
        private DataComponentPatch patch = DataComponentPatch.EMPTY;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private final List<FluidIngredient> fluidIngredients = new ArrayList<>();
        private int shakeTime = 20;

        public Builder() {}
        public Builder(Builder builder) {
            this.result = builder.result;
            this.resultAmount = builder.resultAmount;
            this.patch = builder.patch;
            this.ingredients.addAll(builder.ingredients);
            this.fluidIngredients.addAll(builder.fluidIngredients);
            this.shakeTime = builder.shakeTime;
        }

        public Builder result(Supplier<Item> result) {
            this.result = result;
            return this;
        }

        public Builder patch(DataComponentPatch patch) {
            this.patch = patch;
            return this;
        }

        public Builder resultAmount(int amount) {
            this.resultAmount = amount;
            return this;
        }

        public Builder with(ItemLike... ingredient) {
            this.ingredients.add(Ingredient.of(ingredient));
            return this;
        }

        public Builder with(TagKey<Item> tag) {
            this.ingredients.add(Ingredient.of(items.getOrThrow(tag)));
            return this;
        }

        public Builder withFluid(TagKey<Fluid> tag) {
            this.fluidIngredients.add(FluidIngredient.of(fluids.getOrThrow(tag), 250));
            return this;
        }

        public Builder withFluid(TagKey<Fluid> tag, int amount) {
            this.fluidIngredients.add(FluidIngredient.of(fluids.getOrThrow(tag), amount));
            return this;
        }


        public Builder with(FluidIngredient fluidIngredient) {
            this.fluidIngredients.add(fluidIngredient);
            return this;
        }

        public Builder with(DeferredHolder<Fluid, FlowingFluid> fluid, int amount) {
            this.fluidIngredients.add(FluidIngredient.of(fluid, amount));
            return this;
        }

        public Builder shake(int shakeTime) {
            this.shakeTime = shakeTime;
            return this;
        }

        public ShakeRecipeBuilder build() {
            return new ShakeRecipeBuilder(
                    new ItemStackTemplate(result.get(), resultAmount, patch),
                    fluidIngredients,
                    ingredients,
                    shakeTime,
                    RecipeCategory.FOOD
            );
        }

        public MultiBuilder orWith(TagKey<Fluid> fluidTag, TagKey<Item> tag) {
            MultiBuilder multiBuilder = new MultiBuilder();
            Builder builder = new Builder(this).withFluid(fluidTag);
            with(tag);
            multiBuilder.builders.add(this);
            multiBuilder.builders.add(builder);
            return multiBuilder;
        }

        @Override
        public MultiBuilder orWith(SnsSharedTags tags) {
            return orWith(tags.fluidTag, tags.itemTag);
        }
    }

    class MultiBuilder extends AbstractBuilder<MultiBuilder>{
        List<AbstractBuilder<?>> builders = new ArrayList<>();
        public MultiBuilder result(Supplier<Item> result) {
            builders.forEach(builder -> builder.result(result));
            return this;
        }

        public MultiBuilder patch(DataComponentPatch patch) {
            builders.forEach(builder -> builder.patch(patch));
            return this;
        }

        public MultiBuilder resultAmount(int amount) {
            builders.forEach(builder -> builder.resultAmount(amount));
            return this;
        }

        public MultiBuilder with(ItemLike... ingredient) {
            builders.forEach(builder -> builder.with(ingredient));
            return this;
        }

        public MultiBuilder with(TagKey<Item> tag) {
            builders.forEach(builder -> builder.with(tag));
            return this;
        }

        public MultiBuilder withFluid(TagKey<Fluid> tag) {
            builders.forEach(builder -> builder.withFluid(tag));
            return this;
        }

        public MultiBuilder withFluid(TagKey<Fluid> tag, int amount) {
            builders.forEach(builder -> builder.withFluid(tag, amount));
            return this;
        }


        public MultiBuilder with(FluidIngredient fluidIngredient) {
            builders.forEach(builder -> builder.with(fluidIngredient));
            return this;
        }

        public MultiBuilder with(DeferredHolder<Fluid, FlowingFluid> fluid, int amount) {
            builders.forEach(builder -> builder.with(fluid, amount));
            return this;
        }

        public MultiBuilder shake(int shakeTime) {
            builders.forEach(builder -> builder.shake(shakeTime));
            return this;
        }

        public MultiRecipeBuilder build() {
            return new MultiRecipeBuilder(builders.stream());
        }

        @Override
        public MultiBuilder orWith(TagKey<Fluid> fluidTag, TagKey<Item> tag) {
            ArrayList<AbstractBuilder<?>> toCopy = new ArrayList<>(builders.stream().map(builder -> builder.orWith(fluidTag, tag)).toList());
            builders.clear();
            builders.addAll(toCopy);
            return this;
        }

        @Override
        public MultiBuilder orWith(SnsSharedTags tags) {
            return orWith(tags.fluidTag, tags.itemTag);
        }
    }

    abstract class AbstractBuilder<Self extends AbstractBuilder<Self>> {
        public abstract Self result(Supplier<Item> result);
        public abstract Self patch(DataComponentPatch patch);
        public abstract Self resultAmount(int amount);
        public abstract Self with(ItemLike... ingredient);
        public abstract Self with(TagKey<Item> tag);
        public abstract Self withFluid(TagKey<Fluid> tag);
        public abstract Self withFluid(TagKey<Fluid> tag, int amount);
        public abstract Self with(FluidIngredient fluidIngredient);
        public abstract Self with(DeferredHolder<Fluid, FlowingFluid> fluid, int amount);
        public abstract Self shake(int shakeTime);
        public abstract MultiBuilder orWith(TagKey<Fluid> fluidTag, TagKey<Item> tag);
        public abstract MultiBuilder orWith(SnsSharedTags tags);
    }

    class MultiRecipeBuilder {

        private final List<RecipeBuilder> recipeBuilders = new ArrayList<>();
        private final List<MultiRecipeBuilder> multiRecipeBuilders = new ArrayList<>();

        public MultiRecipeBuilder(Stream<AbstractBuilder<?>> recipeBuilders) {
            recipeBuilders.forEach(
                    builder -> {
                        if (builder instanceof MultiBuilder multiBuilder) {
                            multiRecipeBuilders.add(multiBuilder.build());
                        } else if (builder instanceof Builder v) {
                            this.recipeBuilders.add(v.build());
                        }
                    }
            );
        }
        public MultiRecipeBuilder() {
        }
        public MultiRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
            recipeBuilders.forEach(recipeBuilder -> recipeBuilder.unlockedBy(name, criterion));
            multiRecipeBuilders.forEach(multiRecipeBuilder -> multiRecipeBuilder.unlockedBy(name, criterion));
            return this;
        }

        public void save(RecipeOutput output, String id) {
            save(output, id, new WarpedId());
        }

        public void save(RecipeOutput output, String id, WarpedId idx) {
            for (RecipeBuilder recipeBuilder : recipeBuilders) {
                recipeBuilder.save(output, id + "_" + idx.getId());
            }
            for (MultiRecipeBuilder multiRecipeBuilder : multiRecipeBuilders) {
                multiRecipeBuilder.save(output, id, idx);
            }
        }
    }

    static class WarpedId{
        int idx = 0;
        public WarpedId() {
        }
        public WarpedId(int idx) {
            this.idx = idx;
        }
        public int getId() {
            return idx++;
        }
    }
}
