package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Optional;

public class FindAndTraceToBar {
    public static OneShot<PathfinderMob> create(float speedModifier) {
        return create(speedModifier, 10, 7);
    }

    public static OneShot<PathfinderMob> create(float speedModifier, int maxXyDist, int maxYDist) {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.absent(MemoryModuleType.WALK_TARGET),
                                i.present(Memories.BAR_MEMORY.get())
                        )
                        .apply(
                                i,
                                (walkTarget, barMemory) -> (level, body, timestamp) -> {
                                    BoundingBox boundingBox = i.get(barMemory);
                                    if (boundingBox.isInside(body.blockPosition())) {
                                        return false;
                                    }
                                    BlockPos.MutableBlockPos mutable = boundingBox.getCenter().mutable();
//                                    mutable.setY(boundingBox.minY());


                                    walkTarget.setOrErase(Optional.of(mutable.getCenter()).map(pos -> new WalkTarget(pos, speedModifier, 0)));
                                    return true;
                                }
                        )
        );
    }
}
