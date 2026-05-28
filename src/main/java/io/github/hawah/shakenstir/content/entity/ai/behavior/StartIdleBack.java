package io.github.hawah.shakenstir.content.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class StartIdleBack extends Behavior<BartenderEntity> {

    public long startTime = 0;
    public Direction lookDirection;
    public BlockPos atPos;

    public StartIdleBack() {
        super(ImmutableMap.of(
                Memories.BAR_DATA.get(), MemoryStatus.VALUE_PRESENT,
                Memories.IDLING.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ), 1000);
    }

    @Override
    protected void start(ServerLevel level, BartenderEntity body, long timestamp) {
        super.start(level, body, timestamp);
        startTime = timestamp;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(body.blockPosition().relative(direction)).is(BlockRegistries.BAR_COUNTER_BLOCK)) {
                lookDirection = direction.getOpposite();
                break;
            }
        }
    }



    @Override
    protected void tick(ServerLevel level, BartenderEntity body, long timestamp) {
        super.tick(level, body, timestamp);
        Vec3 lookAt = body.position().add(lookDirection.getUnitVec3());
        if (timestamp - startTime < 20){
            body.getLookControl().setLookAt(lookAt);
            body.setYBodyRot(body.yHeadRot);
        } else {
            if (atPos == null) {
                atPos = body.blockPosition();
            }
            body.setState(BartenderEntity.AnimState.IDLE_BACK);
        }

    }

    @Override
    protected void stop(ServerLevel level, BartenderEntity body, long timestamp) {
        super.stop(level, body, timestamp);
        body.getBrain().eraseMemory(Memories.IDLING.get());
        body.setState(BartenderEntity.AnimState.DEFAULT);
        atPos = null;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, BartenderEntity body, long timestamp) {
        return this.lookDirection != null &&
                (atPos == null || body.blockPosition().getBottomCenter().distanceTo(atPos.getBottomCenter()) < 0.5) &&
                body.getBrain().checkMemory(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_ABSENT);
    }

    @Override
    protected boolean timedOut(long timestamp) {
        return super.timedOut(timestamp);
    }
}
