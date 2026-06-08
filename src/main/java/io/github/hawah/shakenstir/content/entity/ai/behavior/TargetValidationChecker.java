package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TargetValidationChecker {
    public static BehaviorControl<LivingEntity> create(int interactionRange) {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.present(MemoryModuleType.INTERACTION_TARGET),
                                i.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES),
                                i.absent(Memories.RECIPE.get()),
                                i.absent(Memories.RECIPES_TODO.get())
                        )
                        .apply(
                                i,
                                (interactionTarget, nearestEntities, _, _) -> (level, body, timestamp) -> {
                                    LivingEntity livingEntity = i.get(interactionTarget);
                                    if (!i.get(nearestEntities).contains(livingEntity) || livingEntity.distanceTo(body) > interactionRange) {
                                        interactionTarget.erase();
                                        return true;
                                    }
                                    return false;
                                }
                        )
        );
    }
}
