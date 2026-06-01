package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

import java.util.List;
import java.util.Optional;

public class FindAndTraceToBarCorner {
    public static OneShot<PathfinderMob> create(float speedModifier) {
        return create(speedModifier, 10, 7);
    }

    public static OneShot<PathfinderMob> create(float speedModifier, int maxXyDist, int maxYDist) {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.absent(MemoryModuleType.WALK_TARGET),
                                i.absent(Memories.IDLING.get()),
                                i.present(Memories.BAR_MEMORY.get())
                        )
                        .apply(
                                i,
                                (walkTarget, idling, barMemory) -> (level, body, timestamp) -> {
                                    List<BlockPos> bartenderArea = i.get(barMemory).bartenderArea();
                                    if (bartenderArea.isEmpty()) {
                                        return false;
                                    }
                                    List<BlockPos> validArea = bartenderArea.stream().filter(pos -> {
                                        for (Direction possibleValue : HorizontalDirectionalBlock.FACING.getPossibleValues()) {
                                            if (level.getBlockState(pos.relative(possibleValue)).is(BlockRegistries.BAR_COUNTER_BLOCK)) {
                                                return true;
                                            }
                                        }
                                        return false;
                                    }).toList();
//                                    mutable.setY(boundingBox.minY());


                                    walkTarget.setOrErase(Optional.of(validArea.get(level.getRandom().nextInt(validArea.size())))
                                            .map(pos -> new WalkTarget(pos, speedModifier, 0)));
                                    idling.set(Unit.INSTANCE);
                                    return true;
                                }
                        )
        );
    }
}
