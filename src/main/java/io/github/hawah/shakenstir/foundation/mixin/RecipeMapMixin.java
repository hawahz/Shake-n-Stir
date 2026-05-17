package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.content.recipe.IScoreSortedRecipe;
import io.github.hawah.shakenstir.content.recipe.RecipeTypeRegistries;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.stream.Stream;

@Mixin(RecipeMap.class)
public abstract class RecipeMapMixin {

    @Shadow
    public abstract  <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type);

    @Inject(method = "getRecipesFor", at = @At("HEAD"), cancellable = true)
    public <I extends RecipeInput, T extends Recipe<I>> void getRecipesFor(RecipeType<T> type, I container, Level level, CallbackInfoReturnable<Stream<RecipeHolder<T>>> cir) {
        if (type.equals(RecipeTypeRegistries.STIR_RECIPE) || type.equals(RecipeTypeRegistries.SHAKE_RECIPE)) {
            cir.cancel();
            cir.setReturnValue(
                    container.isEmpty() ?
                            Stream.empty() :
                            this.byType(type)
                                    .stream()
                                    .filter(r -> r.value().matches(container, level))
                                    .map(r -> Pair.of(r, ((IScoreSortedRecipe<I>) r.value()).score(container)))
                                    .sorted((a, b) -> Integer.compare(b.second(), a.second()))
                                    .map(Pair::first)
            );
        }
    }
}
