package io.github.hawah.shakenstir.compat.jei.category;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.SnsCreativeTab;
import io.github.hawah.shakenstir.foundation.fluid.FluidConstants;
import io.github.hawah.shakenstir.foundation.recipe.ingredient.FluidIngredient;
import io.github.hawah.shakenstir.foundation.recipe.shake.ShakeRecipe;
import io.github.hawah.shakenstir.util.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ShakeCategory implements IRecipeCategory<ShakeRecipe> {

    public static final Identifier UID = ShakenStir.asResource("shaker");
    public static final IRecipeType<ShakeRecipe> SHAKE_TYPE = IRecipeType.create(UID, ShakeRecipe.class);

    public static final int SHAKER_OFFSET_X = 64;
    public static final int SHAKER_OFFSET_Y = 32;


    @Override
    public IRecipeType<ShakeRecipe> getRecipeType() {
        return SHAKE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Shaker");
    }



    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public int getHeight() {
        return 120;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return null;
    }

    @Override
    public void draw(ShakeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Textures.SHAKE_HUD_INSIDE.blit(guiGraphics, SHAKER_OFFSET_X, SHAKER_OFFSET_Y);
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ShakeRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> inputItems = recipe.inputItems();
        for (int i = 0; i < inputItems.size(); i++) {
            Ingredient ingredient = inputItems.get(i);
            builder.addInputSlot(32, i * 16 + SHAKER_OFFSET_Y)
                    .add(ingredient);
        }

        List<List<FluidStack>> flui = new ArrayList<>();
        for (FluidIngredient inputFluid : recipe.inputFluids()) {
            List<FluidStack> fluidStacks = BuiltInRegistries.FLUID.stream()
                    .filter(f -> inputFluid.match(new FluidStack(f, 1)) != 0)
                    .map(f -> new FluidStack(f, inputFluid.amount()))
                    .sorted(Comparator.comparingInt(FluidStack::amount))
                    .toList();
            flui.add(fluidStacks);
        }

        int height = 77;

        for (List<FluidStack> fluidIngredients : flui) {
            FluidStack visibleStack = fluidIngredients.getFirst();
            int curHeight = FluidConstants.SHAKER_TOOLTIP_VISUAL_FULL_VOLUMN * visibleStack.amount() / FluidConstants.SHAKER_MAX_FLUID_CAPACITY;
            height -= curHeight;
            builder.addSlot(RecipeIngredientRole.INPUT, 8 + SHAKER_OFFSET_X, height + SHAKER_OFFSET_Y)
                    .addIngredients(NeoForgeTypes.FLUID_STACK, fluidIngredients)
                    .setFluidRenderer(
                            visibleStack.amount(),
                            false,
                            Textures.SHAKE_HUD_INSIDE.getWidth() - 16,
                            curHeight
                    );
        }

        List<ItemStack> list = SnsCreativeTab.SHAKENSTIR_TAB_BARTENDING.get()
                .getDisplayItems()
                .stream()
                .filter(itemStack -> itemStack.is(ItemRegistries.SHORT_DRINK_GLASSWARE))
                .map(ItemStack::copy)
                .peek(itemStack -> itemStack.set(
                        DataComponents.ITEM_NAME,
                        Optional.ofNullable(recipe.result().create().get(DataComponentTypeRegistries.COCKTAIL_TYPE))
                                .map(type -> type.translate(List.of(), List.of()))
                                .orElse(Component.literal("???"))
                ))
                .toList();

        builder.addOutputSlot(getWidth()-16, 0)
                .addIngredients(VanillaTypes.ITEM_STACK, list)
        ;
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY)
                .setOverlay(new IDrawable() {
                    @Override
                    public int getWidth() {
                        return Textures.SHAKE_HUD_OUTSIDE.getWidth();
                    }

                    @Override
                    public int getHeight() {
                        return Textures.SHAKE_HUD_OUTSIDE.getHeight();
                    }

                    @Override
                    public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
                        Textures.SHAKE_HUD_OUTSIDE.blit(guiGraphics, xOffset, yOffset);
                        Font font = Minecraft.getInstance().font;
                        String text = "Shake";
                        guiGraphics.text(
                                font,
                                text,
                                xOffset + Textures.SHAKE_HUD_OUTSIDE.getWidth()/2 - font.width(text)/2,
                                yOffset - font.lineHeight*2,
                                -1
                        );
                    }
                }, SHAKER_OFFSET_X, SHAKER_OFFSET_Y);
    }


}
