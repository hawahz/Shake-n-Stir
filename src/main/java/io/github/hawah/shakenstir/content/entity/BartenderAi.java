package io.github.hawah.shakenstir.content.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import io.github.hawah.shakenstir.Config;
import io.github.hawah.shakenstir.content.dataAttachment.DataAttachmentTypeRegistries;
import io.github.hawah.shakenstir.content.entity.ai.activity.Activities;
import io.github.hawah.shakenstir.content.entity.ai.behavior.*;
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
                BehaviorPackage.getWorkIdlePackage(),
                ImmutableSet.of(Pair.of(Memories.BAR_MEMORY.get(), MemoryStatus.VALUE_PRESENT))
        );
    }

    static ActivityData<BartenderEntity> initWorkActivity() {
        return ActivityData.create(
                Activity.WORK,
                BehaviorPackage.getWorkPackage(),
                ImmutableSet.of(
                        Pair.of(Memories.BAR_MEMORY.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    static ActivityData<BartenderEntity> initIdleFrontActivity() {
        return ActivityData.create(
                Activities.IDLE_FRONT.get(),
                BehaviorPackage.getIdleFrontPackage(),
                ImmutableSet.of(
                        Pair.of(Memories.BAR_MEMORY.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(Memories.IDLING.get(), MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    static ActivityData<BartenderEntity> initProductActivity() {
        return ActivityData.create(
                Activities.PRODUCT.get(),
                BehaviorPackage.getProductPackage(),
                ImmutableSet.of(
                        Pair.of(Memories.BAR_MEMORY.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(Memories.RECIPE.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    protected static List<ActivityData<BartenderEntity>> getActivities(BartenderEntity bartender) {
        return List.of(
                initCoreActivity(),
                initIdleActivity(),
                initWorkIdleActivity(),
                initWorkActivity(),
                initProductActivity(),
                initIdleFrontActivity()
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
                    Pair.of(1, new MoveToTargetSink()),
                    Pair.of(1, HideItemInHand.create())
            );
        }


        /**
         * IDLE 状态为最初的状态，在该状态下，酒保会：
         * 随机游动；尝试将玩家，猫设置为看着的目标；站着不动。
         * 当酒保拥有工作区域之后，则进入状态 WORK_IDLE
         * Almost Done
         */
        public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super BartenderEntity>>> getIdlePackage() {
            return ImmutableList.of(
                    getFullLookBehavior(),
                    Pair.of(3, SetEntityLookTarget.create(EntityType.PLAYER, 8.0F)),
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

        /**
         * WORK_IDLE 状态意为酒保在有工作区域，但没有玩家时，会：
         * 尝试找到玩家，并将玩家设置为交互对象，如果成功则会进入到WORK状态；
         * 尝试将放置的菜单收起；
         * 返回工作区域。
         * TODO 添加一些其他随机动画，属于一个Activity
         */

        public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super BartenderEntity>>> getWorkIdlePackage() {
            return ImmutableList.of(
                    getFullLookBehavior(),
                    Pair.of(0, SetLookAndInteractNew.create(EntityType.PLAYER, 5)),
                    Pair.of(3, new RunOne<>(
                            ImmutableMap.of(
                                    Memories.IDLING.get(), MemoryStatus.VALUE_ABSENT
                            ),
                            ImmutableList.of(
                                    Pair.of(new DoNothing(30, 60), 2),
                                    Pair.of(BarRandomStroll.create(0.5F), 1)
                    )
                    )),
                    Pair.of(0, CollapseMenu.create()),
                    Pair.of(4, FindAndTraceToBar.create(0.5F)),
                    Pair.of(5, new RunOne<>(
                            ImmutableList.of(
                                    Pair.of(FindAndTraceToBarCorner.create(0.5F), 1),
                                    Pair.of(new DoNothing(30, 60), 3)
                            ))
                    )
            );
        }

        /**
         * WORK 状态为酒保在有工作区域，有没被IGNORED的玩家，也就是顾客时，会：
         * 检查记忆中的顾客是否是可行的，如果不是可行的，则返回到WORK_IDLE状态；
         *
         * 先尝试找到存有菜单的容器；
         * 然后从容器当中取出菜单；
         * 走到吧台区域内离玩家最近的位置
         *
         * 尝试放置菜单；
         * 在没有注视目标，但有交互对象的时候，看向最近的玩家；
         *
         * 当菜单发出信号时，会存储选择的配方，已经配方对应的顾客，然后转到PRODUCT状态
         */

        public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super BartenderEntity>>> getWorkPackage() {
            return ImmutableList.of(
                    Pair.of(6, PutMenu.create()),
                    Pair.of(5, ApproachingCustomer.create()),
                    Pair.of(0, TargetValidationChecker.create(8)),
                    Pair.of(0, CheckMenuValid.create()),
                    Pair.of(
                            4,
                            new RunOne<>(
                                    ImmutableMap.of(
                                            MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT,
                                            MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT
                                    ),
                                    ImmutableList.of(
                                            Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1),
                                            Pair.of(new DoNothing(30, 60), 1)
                                    )
                            )
                    )
            );
        }

        /**
         * PRODUCT 状态为酒保有当前需要生产的配方时，会：
         * 开始执行配方内容，Shake or Stir；
         * 配方执行结束后，产物加入到Memory里面，移除配方并把配方的顾客加入到记忆当中；
         * 根据配方检索的顾客找到最近的吧台，上菜，将玩家设为IGNORED，直到玩家和酒保交互之后才会重新注意；
         * 如果配方对应的玩家已经不在可交互范围内了，则触发动画，继续下一个配方；
         * 上菜后移除产物和配方的Memory，检索剩余工作，如果已经清空，那么结束PRODUCT，返回WORK状态。
         */
        public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super BartenderEntity>>> getProductPackage() {
            return ImmutableList.of(
                    Pair.of(0, new CollectShakeIngredient()),
                    Pair.of(0, new BartenderProduct())
            );
        }

        public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super BartenderEntity>>> getIdleFrontPackage() {
            return ImmutableList.of(
                    Pair.of(0, SetLookAndInteractNew.create(EntityType.PLAYER, 5)),
                    Pair.of(4, new RunOne<>(
                            ImmutableList.of(
                                    Pair.of(new StartIdleFront(), 1),
                                    Pair.of(new StartIdleBack(), 1)
                            ))
                    )
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
        bartender.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activities.PRODUCT.get(), Activity.WORK, Activities.IDLE_FRONT.get(), Activities.WORK_IDLE.get(), Activity.IDLE));
        if (Config.Common.DEBUG_MODE.get()) {
            bartender.setData(DataAttachmentTypeRegistries.BRAIN_STATE.get(), bartender.getBrain().getActiveNonCoreActivity().map(Activity::getName).orElse("Null"));
        }
    }
}
