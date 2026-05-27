package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.entity.ai.memory.BarData;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.util.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.Comparator;
import java.util.Optional;

public class ApproachingCustomer {
    public static OneShot<PathfinderMob> create() {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.present(MemoryModuleType.INTERACTION_TARGET),
                                i.present(Memories.BAR_DATA.get()),
                                i.absent(MemoryModuleType.WALK_TARGET)
                        )
                        .apply(
                                i,
                                (interactionTarget, barData, walkTarget) -> (level, body, timestamp) -> {
                                    BarData data = i.get(barData);
                                    if (data.dimension() != level.dimension()) {
                                        return false;
                                    }
                                    LivingEntity customer = i.get(interactionTarget);
                                    if (body.distanceTo(customer) <= 2) {
                                        return true;
                                    }
                                    Optional<BlockPos> target = data.bartenderArea()
                                            .stream()
                                            .filter(pos ->
                                                    pos.closerThan(customer.blockPosition(), 2.5) &&
                                                            !pos.closerThan(customer.blockPosition(), 1))
                                            .min(Comparator.comparing(pos -> pos.distToCenterSqr(body.position())));
                                    walkTarget.setOrErase(target.map(pos -> new WalkTarget(pos, 0.5F, 0)));
                                    target.ifPresent(pos -> Debug.drawBlock(pos, level));
                                    return true;
                                }
                        )
        );
    }
}
