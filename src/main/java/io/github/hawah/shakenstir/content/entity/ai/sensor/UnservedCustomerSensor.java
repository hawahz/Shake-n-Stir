package io.github.hawah.shakenstir.content.entity.ai.sensor;

import com.google.common.collect.ImmutableSet;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.entity.ai.memory.MemoryEntitiesHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;

public class UnservedCustomerSensor extends Sensor<BartenderEntity> {
    @Override
    protected void doTick(ServerLevel level, BartenderEntity body) {
        body.getBrain()
                .getMemory(MemoryModuleType.NEAREST_PLAYERS).ifPresent(
                        nearestVisibleLivingEntities -> {
                            List<Player> unserved = nearestVisibleLivingEntities.stream().filter(
                                    living -> body.getBrain().getMemory(Memories.IGNORED_ENTITIES.get()).map(ignored -> !ignored.contains(living)).orElse(true)
                            ).toList();
                            body.getBrain().setMemory(Memories.UNSERVED_CUSTOMER.get(), new MemoryEntitiesHolder<>(level, body, unserved));
                        }
                );
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS);
    }
}
