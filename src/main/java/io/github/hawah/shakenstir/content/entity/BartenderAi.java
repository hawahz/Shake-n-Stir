package io.github.hawah.shakenstir.content.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import io.github.hawah.shakenstir.content.entity.ai.activity.Activities;
import io.github.hawah.shakenstir.content.entity.ai.behavior.AnywhereRandomStroll;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;

public class BartenderAi {

    public static final float DEFAULT_SPEED_MODIFIER = 0.5F;

    static ActivityData<BartenderEntity> initCoreActivity() {
        return ActivityData.create(Activity.CORE, BehaviorPackage.getCorePackage());
    }

    static ActivityData<BartenderEntity> initIdleActivity() {
        return ActivityData.create(
                Activity.IDLE,
                BehaviorPackage.getIdlePackage()
        );
    }

    static ActivityData<BartenderEntity> initWorkIdleActivity() {
        return ActivityData.create(
                Activities.WORK_IDLE.get(),
                BehaviorPackage.getIdlePackage(),
                ImmutableSet.of(Pair.of(Memories.BAR_MEMORY.get(), MemoryStatus.VALUE_PRESENT))
        );
    }

    static ActivityData<BartenderEntity> initWorkingActivity() {
        return ActivityData.create(
                Activity.WORK,
                ImmutableList.of(),
                ImmutableSet.of(
                        Pair.of(Memories.BAR_MEMORY.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    protected static List<ActivityData<BartenderEntity>> getActivities(BartenderEntity bartender) {
        return List.of(
                initCoreActivity(),
                initIdleActivity(),
                initWorkIdleActivity(),
                initWorkingActivity()
        );
    }

    protected static List<SensorType<? extends Sensor<? super BartenderEntity>>> getSensors() {
        return List.of(
                SensorType.NEAREST_PLAYERS,
                SensorType.NEAREST_LIVING_ENTITIES
        );
    }

    private static class BehaviorPackage {

        public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super BartenderEntity>>> getCorePackage() {
            return ImmutableList.of(
                    Pair.of(0, new Swim<>(0.8F)),
                    Pair.of(0, new LookAtTargetSink(45, 90)),
                    Pair.of(1, new MoveToTargetSink())
            );
        }

        public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super BartenderEntity>>> getIdlePackage() {
            return ImmutableList.of(
                    getFullLookBehavior(),
                    Pair.of(3, SetLookAndInteract.create(EntityType.PLAYER, 4)),
                    Pair.of(
                            2,
                            new RunOne<>(
                                    ImmutableList.of(
                                            Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1),
                                            Pair.of(AnywhereRandomStroll.create(0.5F), 1),
                                            Pair.of(SetWalkTargetFromLookTarget.create(DEFAULT_SPEED_MODIFIER, 2), 1),
                                            Pair.of(new DoNothing(30, 60), 1)
                                    )
                            )
                    ),
                    Pair.of(
                            99,
                            new RunOne<>(
                                    ImmutableList.of(
                                            Pair.of(new DoNothing(30, 60), 1)
                                    )
                            )
                    )
            );
        }

        public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super BartenderEntity>>> getWorkIdlePackage() {
            return ImmutableList.of(
                    getFullLookBehavior(),
                    Pair.of(3, SetLookAndInteract.create(EntityType.PLAYER, 4))
            );
        }
        private static Pair<Integer, BehaviorControl<LivingEntity>> getFullLookBehavior() {
            return Pair.of(
                    5,
                    new RunOne<>(
                            ImmutableList.of(
                                    Pair.of(SetEntityLookTarget.create(EntityType.CAT, 8.0F), 8),
                                    Pair.of(SetEntityLookTarget.create(EntityType.WOLF, 8.0F), 8),
                                    Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 4),
                                    Pair.of(SetEntityLookTarget.create(MobCategory.CREATURE, 8.0F), 1),
                                    Pair.of(SetEntityLookTarget.create(MobCategory.WATER_CREATURE, 8.0F), 1),
                                    Pair.of(SetEntityLookTarget.create(MobCategory.AXOLOTLS, 8.0F), 1),
                                    Pair.of(SetEntityLookTarget.create(MobCategory.UNDERGROUND_WATER_CREATURE, 8.0F), 1),
                                    Pair.of(new DoNothing(30, 60), 2)
                            )
                    )
            );
        }
    }

    public static void updateActivity(BartenderEntity bartender) {
        bartender.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.WORK, Activities.WORK_IDLE.get(), Activity.IDLE));
    }
}
