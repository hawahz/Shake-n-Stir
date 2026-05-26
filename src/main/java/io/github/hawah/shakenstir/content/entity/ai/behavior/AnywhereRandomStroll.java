package io.github.hawah.shakenstir.content.entity.ai.behavior;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class AnywhereRandomStroll {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;

    public static OneShot<PathfinderMob> create(float speedModifier) {
        return create(speedModifier, 10, 7);
    }

    public static OneShot<PathfinderMob> create(float speedModifier, int maxXyDist, int maxYDist) {
        return BehaviorBuilder.create(
                i -> i.group(i.absent(MemoryModuleType.WALK_TARGET))
                        .apply(
                                i,
                                walkTarget -> (level, body, timestamp) -> {
                                    Vec3 landPos = LandRandomPos.getPos(body, maxXyDist, maxYDist);

                                    walkTarget.setOrErase(Optional.ofNullable(landPos).map(pos -> new WalkTarget(pos, speedModifier, 0)));
                                    return true;
                                }
                        )
        );
    }
}
