package io.github.hawah.shakenstir.foundation.datagen;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.DeferredFluidStackHolder;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.recipe.ingredient.FluidIngredient;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DistillerRecipeProvider extends RecipeProvider {
    private final HolderLookup.RegistryLookup<Item> items;
    private final HolderLookup.RegistryLookup<Fluid> fluids;

    protected DistillerRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        items = registries.lookupOrThrow(Registries.ITEM);
        fluids = registries.lookupOrThrow(Registries.FLUID);
    }

    @Override
    protected void buildRecipes() {
        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.DEFERRED_FLUID, new DeferredFluidStackHolder(FluidRegistries.GIN_SOURCE, 100)).build())
                .with(ItemTags.LOGS).with(ItemTags.LOGS)
                .with(Tags.Items.CROPS_WHEAT)
                .withFluid(FluidTags.WATER, 1000)
                .cook(600)
                .build()
                .unlockedBy("has_wheat", this.has(Tags.Items.CROPS_WHEAT))
                .save(output, getName("gin"));
        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.DEFERRED_FLUID, new DeferredFluidStackHolder(FluidRegistries.VODKA_SOURCE, 100)).build())
                .with(Items.POTATO).with(Items.POTATO)
                .with(Tags.Items.CROPS_WHEAT)
                .withFluid(FluidTags.WATER, 1000)
                .cook(600)
                .build()
                .unlockedBy("has_potato", this.has(Items.POTATO))
                .save(output, getName("vodka"));
        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.DEFERRED_FLUID, new DeferredFluidStackHolder(FluidRegistries.VODKA_SOURCE, 100)).build())
                .with(Tags.Items.FOODS_FRUIT).with(Tags.Items.FOODS_FRUIT).with(Tags.Items.FOODS_FRUIT)
                .withFluid(FluidTags.WATER, 1000)
                .cook(600)
                .build()
                .unlockedBy("has_fruit", this.has(Tags.Items.FOODS_FRUIT))
                .save(output, getName("brandy"));
        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.DEFERRED_FLUID, new DeferredFluidStackHolder(FluidRegistries.VODKA_SOURCE, 100)).build())
                .with(Tags.Items.CROPS_WHEAT).with(Tags.Items.CROPS_WHEAT).with(Tags.Items.CROPS_WHEAT)
                .withFluid(FluidTags.WATER, 1000)
                .cook(600)
                .build()
                .unlockedBy("has_wheat", this.has(Tags.Items.CROPS_WHEAT))
                .save(output, getName("whisky"));
        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.DEFERRED_FLUID, new DeferredFluidStackHolder(FluidRegistries.VODKA_SOURCE, 100)).build())
                .with(Tags.Items.CROPS_SUGAR_CANE).with(Tags.Items.CROPS_SUGAR_CANE).with(Tags.Items.CROPS_SUGAR_CANE)
                .withFluid(FluidTags.WATER, 1000)
                .cook(600)
                .build()
                .unlockedBy("has_sugar_cane", this.has(Tags.Items.CROPS_SUGAR_CANE))
                .save(output, getName("rum_sugar_cane"));
        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.DEFERRED_FLUID, new DeferredFluidStackHolder(FluidRegistries.VODKA_SOURCE, 100)).build())
                .with(Items.SUGAR, Items.SUGAR, Items.SUGAR).with(Items.SUGAR, Items.SUGAR, Items.SUGAR)
                .withFluid(FluidTags.WATER, 1000)
                .cook(600)
                .build()
                .unlockedBy("has_potato", this.has(Items.POTATO))
                .save(output, getName("rum_sugar"));
        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.DEFERRED_FLUID, new DeferredFluidStackHolder(FluidRegistries.VODKA_SOURCE, 250)).build())
                .with(Tags.Items.DRINKS_HONEY)
                .withFluid(FluidTags.WATER, 1000)
                .cook(600)
                .build()
                .unlockedBy("has_potato", this.has(Items.POTATO))
                .save(output, getName("rum_honey"));
//        getBuilder()
//                .result(ItemRegistries.CONTENT_HOLDER)
//                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.DEFERRED_FLUID, new DeferredFluidStackHolder(FluidRegistries.VODKA_SOURCE_FLUID_BLOCK, 250)).build())
//                .withFluid(Tags.Fluids.HONEY)
//                .withFluid(FluidTags.WATER, 1000)
//                .cook(600)
//                .build()
//                .unlockedBy("has_potato", this.has(Items.POTATO))
//                .save(output, getName("rum"));
        getBuilder()
                .result(ItemRegistries.CONTENT_HOLDER)
                .patch(DataComponentPatch.builder().set(DataComponentTypeRegistries.DEFERRED_FLUID, new DeferredFluidStackHolder(FluidRegistries.VODKA_SOURCE, 100)).build())
                .with(Items.CACTUS).with(Items.CACTUS).with(Items.CACTUS)
                .withFluid(FluidTags.WATER, 1000)
                .cook(600)
                .build()
                .unlockedBy("has_cactus", this.has(Items.CACTUS))
                .save(output, getName("tequila"));

    }

    public static String getName(String name) {
        return ShakenStir.asResource(name).toString();
    }

    private Builder getBuilder() {
        return new Builder();
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new DistillerRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "distiller_recipe";
        }
    }

    class Builder extends AbstractBuilder<Builder> {
        private Supplier<Item> result;
        private int resultAmount = 1;
        private DataComponentPatch patch = DataComponentPatch.EMPTY;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private FluidIngredient fluidIngredient;
        private int cookingTime = 200;

        public Builder() {}

        public Builder(Builder builder) {
            this.result = builder.result;
            this.resultAmount = builder.resultAmount;
            this.patch = builder.patch;
            this.ingredients.addAll(builder.ingredients);
            this.fluidIngredient = builder.fluidIngredient;
            this.cookingTime = builder.cookingTime;
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
            this.fluidIngredient = FluidIngredient.of(fluids.getOrThrow(tag), 250);
            return this;
        }

        public Builder withFluid(TagKey<Fluid> tag, int amount) {
            this.fluidIngredient = FluidIngredient.of(fluids.getOrThrow(tag), amount);
            return this;
        }

        public Builder with(FluidIngredient fluidIngredient) {
            this.fluidIngredient = fluidIngredient;
            return this;
        }

        public Builder with(DeferredHolder<Fluid, FlowingFluid> fluid, int amount) {
            this.fluidIngredient = FluidIngredient.of(fluid, amount);
            return this;
        }

        public Builder withFluid(DeferredHolder<Fluid, FlowingFluid> fluid, int amount) {
            this.fluidIngredient = FluidIngredient.of(fluid, amount);
            return this;
        }

        public Builder cook(int cookingTime) {
            this.cookingTime = cookingTime;
            return this;
        }

        public DistillerRecipeBuilder build() {
            return new DistillerRecipeBuilder(
                    new ItemStackTemplate(result.get(), resultAmount, patch),
                    ingredients,
                    fluidIngredient,
                    cookingTime,
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

    class MultiBuilder extends AbstractBuilder<MultiBuilder> {
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

        public MultiBuilder withFluid(DeferredHolder<Fluid, FlowingFluid> fluid, int amount) {
            builders.forEach(builder -> builder.withFluid(fluid, amount));
            return this;
        }

        public MultiBuilder cook(int cookingTime) {
            builders.forEach(builder -> builder.cook(cookingTime));
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
        public abstract Self withFluid(DeferredHolder<Fluid, FlowingFluid> fluid, int amount);
        public abstract Self cook(int cookingTime);
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

    static class WarpedId {
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
