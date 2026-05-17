package io.github.hawah.shakenstir.content.recipe;

public interface IScoreSortedRecipe<T> {
    int score(T recipeInput);
}
