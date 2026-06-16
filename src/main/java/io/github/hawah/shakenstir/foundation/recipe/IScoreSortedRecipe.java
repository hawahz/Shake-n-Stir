package io.github.hawah.shakenstir.foundation.recipe;

public interface IScoreSortedRecipe<T> {
    int score(T recipeInput);
}
