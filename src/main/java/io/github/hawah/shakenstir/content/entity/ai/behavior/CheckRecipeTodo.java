package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.foundation.data.SnsRecipeStack;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.entity.ai.memory.MemoryEntitiesHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.ArrayList;
import java.util.List;

public class CheckRecipeTodo {
    public static OneShot<BartenderEntity> create() {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.present(Memories.BAR_MEMORY.get()),
                                i.present(Memories.RECIPES_TODO.get()),
                                i.present(MemoryModuleType.INTERACTION_TARGET),
                                i.registered(Memories.IGNORED_ENTITIES.get()),
                                i.absent(Memories.RECIPE.get())
                        )
                        .apply(
                                i,
                                (_, recipeTodo, interactionTarget, ignore, recipe) -> (level, body, _) -> {
                                    List<SnsRecipeStack> snsRecipeStacks = new ArrayList<>(i.get(recipeTodo));
                                    if (!snsRecipeStacks.isEmpty()){
                                        recipe.set(snsRecipeStacks.getFirst().holder());
                                        snsRecipeStacks.getFirst().shrink();
                                        if (snsRecipeStacks.getFirst().isEmpty()) {
                                            snsRecipeStacks.removeFirst();
                                        }
                                        if (snsRecipeStacks.isEmpty()) {
                                            recipeTodo.erase();
                                            List<LivingEntity> entities = new ArrayList<>(i.tryGet(ignore).map(MemoryEntitiesHolder::nearbyEntities).orElse(List.of()));
                                            entities.add(i.get(interactionTarget));
                                            ignore.set(new MemoryEntitiesHolder<>(level, body, entities));
                                            interactionTarget.erase();
                                        } else {
                                            recipeTodo.set(snsRecipeStacks);
                                        }
                                    } else {
                                        recipeTodo.erase();
                                        List<LivingEntity> entities = new ArrayList<>(i.tryGet(ignore).map(MemoryEntitiesHolder::nearbyEntities).orElse(List.of()));
                                        entities.add(i.get(interactionTarget));
                                        ignore.set(new MemoryEntitiesHolder<>(level, body, entities));
                                        interactionTarget.erase();
                                    }
                                    return false;
                                }
                        )
        );
    }
}
