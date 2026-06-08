package io.github.hawah.shakenstir.content.entity.ai.memory;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MemoryEntitiesHolder<T extends LivingEntity> {
    private static final MemoryEntitiesHolder<LivingEntity> EMPTY = new MemoryEntitiesHolder<>();
    private final List<T> ignoredEntities;
    private final Predicate<T> lineOfSightTest;

    public MemoryEntitiesHolder() {
        this.ignoredEntities = List.of();
        this.lineOfSightTest = ignored -> false;
    }

    public MemoryEntitiesHolder(ServerLevel level, LivingEntity body, List<T> livingEntities) {
        this.ignoredEntities = livingEntities;
        Object2BooleanOpenHashMap<T> cache = new Object2BooleanOpenHashMap<>(livingEntities.size());
        Predicate<LivingEntity> targetTest = targetEntity -> Sensor.isEntityTargetable(level, body, targetEntity);
        this.lineOfSightTest = otherEntity -> cache.computeIfAbsent(otherEntity, targetTest);
    }

    public static MemoryEntitiesHolder<LivingEntity> empty() {
        return EMPTY;
    }

    @VisibleForDebug
    public List<T> nearbyEntities() {
        return this.ignoredEntities;
    }

    public Optional<T> findClosest(Predicate<T> filter) {
        for (T nearbyEntity : this.ignoredEntities) {
            if (filter.test(nearbyEntity) && this.lineOfSightTest.test(nearbyEntity)) {
                return Optional.of(nearbyEntity);
            }
        }

        return Optional.empty();
    }

    public Iterable<T> findAll(Predicate<T> filter) {
        return Iterables.filter(this.ignoredEntities, entity -> filter.test(entity) && this.lineOfSightTest.test(entity));
    }

    public Stream<T> find(Predicate<T> filter) {
        return this.ignoredEntities.stream().filter(entity -> filter.test(entity) && this.lineOfSightTest.test(entity));
    }

    public boolean contains(T targetEntity) {
        return this.ignoredEntities.contains(targetEntity) && this.lineOfSightTest.test(targetEntity);
    }

    public boolean contains(Predicate<T> filter) {
        for (T nearbyEntity : this.ignoredEntities) {
            if (filter.test(nearbyEntity) && this.lineOfSightTest.test(nearbyEntity)) {
                return true;
            }
        }

        return false;
    }
}