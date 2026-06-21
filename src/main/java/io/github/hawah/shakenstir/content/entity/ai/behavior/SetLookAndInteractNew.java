package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;

public class SetLookAndInteractNew {
    public static BehaviorControl<BartenderEntity> create(EntityType<?> type, int interactionRange) {
        int interactionRangeSqr = interactionRange * interactionRange;
        return BehaviorBuilder.create(
                i -> i.group(
                                i.registered(MemoryModuleType.LOOK_TARGET),
                                i.registered(Memories.IGNORED_ENTITIES.get()),
                                i.absent(MemoryModuleType.INTERACTION_TARGET),
                                i.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                        )
                        .apply(
                                i,
                                (lookTarget, ignoredEntity, interactionTarget, nearestEntities) -> (_, body, _) -> {
                                    Optional<LivingEntity> closest = i.get(nearestEntities)
                                            .findClosest(e ->
                                                    !i.tryGet(ignoredEntity)
                                                            .map(ignored -> ignored.contains(e))
                                                            .orElse(false) &&
                                                    e.distanceToSqr(body) <= interactionRangeSqr &&
                                                    e.is(type)
                                            );
                                    Optional<LivingEntity> closestFallback = Optional.empty();
                                    if (closest.isEmpty()) {
                                        closestFallback = i.get(nearestEntities)
                                                .findClosest(e -> e.distanceToSqr(body) <= interactionRangeSqr && e.is(type));
                                        if (closestFallback.isEmpty()) {
                                            return false;
                                        }
                                    }
                                    LivingEntity closestEntity = closest.orElse(closestFallback.orElse(null));
                                    interactionTarget.set(closestEntity);
                                    lookTarget.set(new EntityTracker(closestEntity, true));
//                                    if (closestEntity instanceof Player player) {
//                                        body.speakServer(Component.literal("Hello ").append(player.getDisplayName()), List.of(player), 20 * 1);
//                                        body.speakServer(Component.literal("Do you want a cup of drink?"), List.of(player), 20 * 3, true);
//                                    }
                                    return true;
                                }
                        )
        );
    }
}
