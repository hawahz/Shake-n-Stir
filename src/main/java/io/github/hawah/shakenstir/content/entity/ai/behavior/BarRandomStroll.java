package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.util.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.List;
import java.util.Optional;

public class BarRandomStroll {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public static OneShot<PathfinderMob> create(float speedModifier) {
        return create(speedModifier, 10, 7);
    }

    public static OneShot<PathfinderMob> create(float speedModifier, int maxXyDist, int maxYDist) {
        return BehaviorBuilder.create(
                i -> i.group(
                        i.absent(MemoryModuleType.WALK_TARGET),
                        i.present(Memories.BAR_DATA.get())
                        )
                        .apply(
                                i,
                                (walkTarget, barMemory) -> (level, body, timestamp) -> {
                                    List<BlockPos> validPos = i.get(barMemory).bartenderArea();
                                    Optional<BlockPos> target = Optional.ofNullable(validPos.get(level.getRandom().nextInt(validPos.size())));
                                    walkTarget.setOrErase(target.map(pos -> new WalkTarget(pos, speedModifier, 0)));
                                    target.ifPresent(pos -> Debug.drawBlock(pos, level));
                                    return true;
                                }
                        )
        );
    }
}
