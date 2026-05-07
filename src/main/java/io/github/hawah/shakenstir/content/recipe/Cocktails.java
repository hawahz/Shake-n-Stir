package io.github.hawah.shakenstir.content.recipe;

import io.github.hawah.shakenstir.content.recipe.ingredient.ShakeIngredient;

import static io.github.hawah.shakenstir.content.recipe.ingredient.ShakeIngredient.*;

public enum Cocktails {
    SOUR(MixtureTypes.SHAKE, SPIRIT, ShakeIngredient.SOUR, SWEET),
    FIZZ(MixtureTypes.STIR, SPIRIT, ShakeIngredient.SOUR, SWEET, BUBBLES),
    COCKTAIL(MixtureTypes.SHAKE, SPIRIT, BITTER, SWEET),
//    TODDY(SPIRIT, BITTER, ShakeFluidIngredient.SOUR),
    HIGHBALL(MixtureTypes.STIR, SPIRIT, SWEET, BUBBLES),
    ;
    Cocktails(MixtureTypes mixtureType, ShakeIngredient... ingredients) {

    }
}
