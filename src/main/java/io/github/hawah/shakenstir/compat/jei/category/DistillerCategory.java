package io.github.hawah.shakenstir.compat.jei.category;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.ShakenStir;
import org.slf4j.Logger;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.fluid.FluidConstants;
import io.github.hawah.shakenstir.foundation.recipe.DistillerRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DistillerCategory implements IRecipeCategory<DistillerRecipe> {

    // TODO: 人工审查 - 2026-09-03 - 新增静态 Logger 字段,替换原内联 LOGGER 调用。
    //  原代码每次写日志都会重新解析调用类并创建 Logger,改为类级静态常量后仅创建一次。
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Identifier UID = ShakenStir.asResource("distiller");
    public static final IRecipeType<DistillerRecipe> TYPE = IRecipeType.create(UID, DistillerRecipe.class);

    @Override
    public IRecipeType<DistillerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Distiller");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DistillerRecipe recipe, IFocusGroup focuses) {
        List<FluidStack> fluidStacks = recipe.inputFluid().getPredicates();
        builder.addInputSlot(0, 0)
                .addIngredients(NeoForgeTypes.FLUID_STACK, fluidStacks)
                .setFluidRenderer(FluidConstants.DISTILLER_MAX_INPUT_FLUID_CAPACITY, true, 16, 48);
        List<Ingredient> inputItems = recipe.inputItems();
        for (int i = 0; i < inputItems.size(); i++) {
            Ingredient ingredient = inputItems.get(i);
            builder.addInputSlot(32, i * 16)
                    .add(ingredient);
        }
        var holder = recipe.result().get(DataComponentTypeRegistries.DEFERRED_FLUID);
        if (holder == null) {
            LOGGER.error("Distiller recipe has no fluid result");
            return;
        }
        FluidStack fluidStack = holder.toFluidStack();
        builder.addInputSlot(64, 0)
                .add(fluidStack.getFluid(), fluidStack.amount())
                .setFluidRenderer(FluidConstants.BLOCK_VOLUMN/2, false, 16, 48);
    }
}
