package io.github.hawah.shakenstir.content.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class ServePlayer extends Behavior<BartenderEntity> {
    public ServePlayer() {
        super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected void start(ServerLevel level, BartenderEntity body, long timestamp) {
        super.start(level, body, timestamp);
    }

    @Override
    protected void stop(ServerLevel level, BartenderEntity body, long timestamp) {
        super.stop(level, body, timestamp);
    }

    @Override
    protected void tick(ServerLevel level, BartenderEntity body, long timestamp) {
        super.tick(level, body, timestamp);
    }
}
