package io.github.hawah.shakenstir.compat.jei.category;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.resources.Identifier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SnsJeiRecipeType<T> implements IRecipeType<T> {

    private final Identifier uid;
    private final Class<T> recipeClass;

    public SnsJeiRecipeType(Identifier uid, Class<T> recipeClass) {
        this.uid = uid;
        this.recipeClass = recipeClass;
    }

    @Override
    public Identifier getUid() {
        return uid;
    }

    @Override
    public Class<T> getRecipeClass() {
        return recipeClass;
    }
}
