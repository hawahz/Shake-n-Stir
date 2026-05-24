package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.recipe.SpiritBottleSpecialRecipe;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.MISC, ItemRegistries.BOTTLE)
                .pattern("g g")
                .pattern("g g")
                .pattern("ggg")
                .define('g', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_glass", has(Tags.Items.GLASS_BLOCKS))
                .save(output);

        SpecialRecipeBuilder.special(
                        () -> new SpiritBottleSpecialRecipe(
                                Ingredient.of(ItemRegistries.BOTTLE),
                                Ingredient.of(Items.GREEN_DYE),
                                new ItemStackTemplate(ItemRegistries.GIN)
                        )
                )
                .save(output, "gin_from_bottle");

        SpecialRecipeBuilder.special(
                        () -> new SpiritBottleSpecialRecipe(
                                Ingredient.of(ItemRegistries.BOTTLE),
                                Ingredient.of(Items.ORANGE_DYE),
                                new ItemStackTemplate(ItemRegistries.WHISKY)
                        )
                )
                .save(output, "whisky_from_bottle");

        SpecialRecipeBuilder.special(
                        () -> new SpiritBottleSpecialRecipe(
                                Ingredient.of(ItemRegistries.BOTTLE),
                                Ingredient.of(Items.GRAY_DYE),
                                new ItemStackTemplate(ItemRegistries.VODKA)
                        )
                )
                .save(output, "vodka_from_bottle");

        SpecialRecipeBuilder.special(
                        () -> new SpiritBottleSpecialRecipe(
                                Ingredient.of(ItemRegistries.BOTTLE),
                                Ingredient.of(Items.YELLOW_DYE),
                                new ItemStackTemplate(ItemRegistries.RUM)
                        )
                )
                .save(output, "rum_from_bottle");

        SpecialRecipeBuilder.special(
                        () -> new SpiritBottleSpecialRecipe(
                                Ingredient.of(ItemRegistries.BOTTLE),
                                Ingredient.of(Items.LIME_DYE),
                                new ItemStackTemplate(ItemRegistries.TEQUILA)
                        )
                )
                .save(output, "tequila_from_bottle");

        SpecialRecipeBuilder.special(
                        () -> new SpiritBottleSpecialRecipe(
                                Ingredient.of(ItemRegistries.BOTTLE),
                                Ingredient.of(Items.RED_DYE),
                                new ItemStackTemplate(ItemRegistries.BRANDY)
                        )
                )
                .save(output, "brandy_from_bottle");

        SpecialRecipeBuilder.special(
                        () -> new SpiritBottleSpecialRecipe(
                                Ingredient.of(ItemRegistries.BOTTLE),
                                Ingredient.of(Items.RED_DYE),
                                new ItemStackTemplate(ItemRegistries.BOTTLE)
                        )
                )
                .save(output, "bottle_from_bottle");

        shapeless(RecipeCategory.MISC, ItemRegistries.ICE_CUBE, 3)
                .requires(Items.ICE)
                .unlockedBy("has_ice", has(Items.ICE))
                .save(output, "ice_cube");

        shaped(RecipeCategory.MISC, ItemRegistries.CABINET)
                .pattern("ppp")
                .pattern("p p")
                .pattern("ppp")
                .define('p', ItemTags.PLANKS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(output, "cabinet");

        ;

        shaped(RecipeCategory.MISC, createLongDrink("collins_glass"))
                .pattern("g g")
                .pattern("g g")
                .pattern("ggg")
                .define('g', Tags.Items.GLASS_PANES)
                .unlockedBy("has_glass_pane", has(Tags.Items.GLASS_PANES))
                .save(output, "collins_glass");

        shaped(RecipeCategory.MISC, createShortDrink("martini_glass"))
                .pattern("g g")
                .pattern("ggg")
                .pattern(" g ")
                .define('g', Tags.Items.GLASS_PANES)
                .unlockedBy("has_glass_pane", has(Tags.Items.GLASS_PANES))
                .save(output, "martini_glass");

        shaped(RecipeCategory.MISC, createShortDrink("margarita_glass"))
                .pattern("g g")
                .pattern(" g ")
                .pattern("ggg")
                .define('g', Tags.Items.GLASS_PANES)
                .unlockedBy("has_glass_pane", has(Tags.Items.GLASS_PANES))
                .save(output, "margarita_glass");

        shaped(RecipeCategory.MISC, ItemRegistries.SHAKER)
                .pattern("ibi")
                .pattern("i i")
                .pattern("iii")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('b', Tags.Items.STORAGE_BLOCKS_IRON)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(output);

        shaped(RecipeCategory.MISC, ItemRegistries.SHAKER_LID)
                .pattern("ibi")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('b', Tags.Items.STORAGE_BLOCKS_IRON)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(output);

        shaped(RecipeCategory.MISC, ItemRegistries.DISTILLER)
                .pattern("bbb")
                .pattern("gos")
                .pattern("bfb")
                .define('b', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('g', Tags.Items.GLASS_BLOCKS)
                .define('o', Tags.Items.INGOTS_GOLD)
                .define('s', Items.AMETHYST_SHARD)
                .define('f', Items.BLAST_FURNACE)
                .unlockedBy("has_iron", has(Tags.Items.STORAGE_BLOCKS_IRON))
                .save(output);
    }

    public static ItemStackTemplate createShortDrink(String path) {
        return new ItemStackTemplate(ItemRegistries.SHORT_DRINK_GLASSWARE, DataComponentPatch.builder()
                .set(DataComponents.ITEM_MODEL, ShakenStir.asResource(path))
                .set(DataComponents.ITEM_NAME, LangData.getFromItem(path))
                .set(DataComponentTypeRegistries.GLASSWARE_NAME, LangData.getFromItem(path))
                .build());
    }

    public static ItemStackTemplate createLongDrink(String path) {
        return new ItemStackTemplate(ItemRegistries.LONG_DRINK_GLASSWARE, DataComponentPatch.builder()
                .set(DataComponents.ITEM_MODEL, ShakenStir.asResource(path))
                .set(DataComponents.ITEM_NAME, LangData.getFromItem(path))
                .set(DataComponentTypeRegistries.GLASSWARE_NAME, LangData.getFromItem(path))
                .build());
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
            return "sns_vanilla_recipe";
        }
    }
}
