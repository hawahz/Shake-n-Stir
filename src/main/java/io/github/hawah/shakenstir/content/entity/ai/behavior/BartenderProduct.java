package io.github.hawah.shakenstir.content.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.BarData;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.data.SnsRecipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;

/**
 * 驱动酒保完成完整的调酒流程：
 * 走近顾客 → 摇晃 → 等待取杯 → 倒酒 → 装饰 → 结束。
 *
 * <h3>状态机流转</h3>
 * <pre>
 *   ApproachingState  —  走向顾客附近的吧台位置
 *        │
 *   TurnForShakeState  —  注视顾客 {@value #LOOKAT_DURATION} tick，然后装备摇酒壶
 *        │
 *   ShakingState  —  播放摇晃动画，持续 {@value #SHAKING_DURATION} tick
 *        │
 *   WaitForGlasswareState  —  等待外部 AI 找到所需玻璃器皿（或超时）
 *        │
 *   PouringState  —  装备杯子 → 定位吧台空位 → 走近 → 放置 → 倒酒 → 应用配方
 *        │
 *   DecoratingState  —  逐一放置装饰品（水果片、小伞等），每次间隔等待
 *        │
 *   EndState  —  终态；触发 {@link #timedOut} → {@link #stop}
 * </pre>
 *
 * <p>每个状态是一个自包含的内部类，拥有自己的等待计时器和转换逻辑。
 * 每次计时等待都用一个具名 {@link WaitTimer} 表达，"正在等什么"一目了然。</p>
 */
public class BartenderProduct extends Behavior<BartenderEntity> {

    // =========================================================================
    // 常量
    // =========================================================================

    /** 摇晃动画播放结束后等待的 tick 数。 */
    public static final int SHAKING_DURATION = 100;
    /** 放置每个装饰品之间的基础间隔 tick 数。 */
    public static final int DECORATING_DURATION = 10;
    /** 装备短饮杯后到可以放置之间的冷却 tick 数。 */
    public static final int POURING_DURATION = 20;
    /** 放置杯子后到应用配方之间的等待 tick 数。 */
    public static final int POURING_CD = 20;
    /** 开始摇晃前注视顾客的 tick 数。 */
    public static final long LOOKAT_DURATION = 40L;
    /** "玻璃器皿已找到"后到开始走去倒酒之间的稳定等待 tick 数。 */
    public static final int PREPARE_WALK_DURATION = 20;
    /** 等待外部取物 AI 找到玻璃器皿的最长时间（5 分钟）。 */
    public static final long GLASSWARE_FIND_TIMEOUT = 5 * 20 * 60;

    // =========================================================================
    // WaitTimer — 显式、具名的倒计时
    // =========================================================================

    /**
     * 小型倒计时辅助类，让状态机中每处等待都写成
     * {@code timer.start(now, 时长)} … {@code timer.isDone(now)}
     * 的形式，而非隐晦的 {@code 某字段 = timestamp + 魔数} 比较。
     */
    private static final class WaitTimer {
        private long endTime = -1;

        /** 从 {@code now} 开始等待 {@code ticks} 刻。 */
        void start(long now, int ticks) {
            this.endTime = now + ticks;
        }

        /** 开始等待，在基础时长上附加已计算好的抖动值（单位 tick）。 */
        void startWithJitter(long now, int baseTicks, int jitter) {
            this.endTime = now + baseTicks + jitter;
        }

        /** 计时器是否已启动且已到达截止时间？ */
        boolean isDone(long now) {
            return endTime > 0 && now >= endTime;
        }

        /** 计时器是否正在倒计时中？ */
        boolean isActive() {
            return endTime > 0;
        }

        /** 取消计时器，使 {@link #isDone} / {@link #isActive} 返回 false。 */
        void reset() {
            endTime = -1;
        }
    }

    // =========================================================================
    // BartenderState — 抽象状态契约
    // =========================================================================

    /**
     * 调酒流程中单个步骤的基类。
     * 每个具体状态拥有自己的数据、计时器和转换规则。
     */
    private abstract class BartenderState {

        /** 此状态成为活跃状态时调用一次。 */
        abstract void enter(ServerLevel level, BartenderEntity body, long timestamp);

        /** 此状态活跃期间每个服务端 tick 调用。 */
        abstract void tick(ServerLevel level, BartenderEntity body, long timestamp);

        /** 离开此状态转入下一状态时调用。默认空操作。 */
        void exit(ServerLevel level, BartenderEntity body, long timestamp) {}

        /** 此状态已完成、应当转换时返回 {@code true}。 */
        abstract boolean isComplete(ServerLevel level, BartenderEntity body, long timestamp);

        /** 返回下一状态。仅在 {@link #isComplete} 返回 true 后调用。 */
        abstract BartenderState nextState(ServerLevel level, BartenderEntity body);
    }

    private BartenderState currentState;

    // =========================================================================
    // 共享数据
    // =========================================================================

    /** 供 {@link #timedOut} 使用，以在 {@link EndState} 期间触发 {@link #stop}。 */
    private long endTime = -1;

    /** 玻璃器皿查找超时 — 在 ShakingState 启动，在 WaitForGlasswareState 检查。 */
    private final WaitTimer glasswareFindTimeout = new WaitTimer();

    /** 正在准备的配方。在 {@link #start} 中设置一次，被多个状态读取。 */
    private SnsRecipeHolder recipeHolder;

    // =========================================================================
    // ApproachingState — 走近顾客
    // =========================================================================

    /**
     * 让酒保走到 {@code bartenderArea} 中靠近顾客的位置
     *（距离顾客 2.5 格以内，但至少 1 格以外）。
     *
     * <p><b>等待条件：</b>酒保距离目标位置 2.0 格以内。</p>
     * <p><b>下一状态：</b>{@link TurnForShakeState}</p>
     */
    private class ApproachingState extends BartenderState {
        private BlockPos target;
        private boolean completed;

        @Override
        void enter(ServerLevel level, BartenderEntity body, long timestamp) {
            target = null;
            completed = false;
        }

        @Override
        void tick(ServerLevel level, BartenderEntity body, long timestamp) {
            if (target != null && body.distanceToSqr(target.getCenter()) < 4.0) { // 2.0²
                completed = true;
                return;
            }
            findOrWalkToTarget(body);
        }

        /**
         * 首次调用：找到 {@code bartenderArea} 中离顾客最近的合适位置。
         * 后续调用：持续走向该位置。
         */
        private void findOrWalkToTarget(BartenderEntity body) {
            if (target == null) {
                BarData data = body.getBrain()
                        .getMemory(Memories.BAR_MEMORY.get())
                        .orElseThrow();
                if (data.dimension() != body.level().dimension()) {
                    return; // 维度不对 — 无法导航
                }
                LivingEntity customer = body.getBrain()
                        .getMemory(MemoryModuleType.INTERACTION_TARGET)
                        .orElseThrow();
                if (body.distanceTo(customer) <= 2) {
                    return; // 已足够近，无需找吧台位置
                }
                data.bartenderArea().stream()
                        .filter(pos -> pos.closerThan(customer.blockPosition(), 2.5)
                                && !pos.closerThan(customer.blockPosition(), 1))
                        .min(Comparator.comparing(pos -> pos.distToCenterSqr(body.position())))
                        .ifPresent(pos -> target = pos);
            }

            if (target != null) {
                BehaviorUtils.setWalkAndLookTargetMemories(body, target, 0.5F, 0);
            }
        }

        @Override
        boolean isComplete(ServerLevel level, BartenderEntity body, long timestamp) {
            return completed;
        }

        @Override
        BartenderState nextState(ServerLevel level, BartenderEntity body) {
            return new TurnForShakeState();
        }
    }

    // =========================================================================
    // TurnForShakeState — 转向准备摇晃
    // =========================================================================

    /**
     * 让酒保在摇晃前凝视顾客一个节拍。
     *
     * <p><b>等待：</b>持续注视 {@value #LOOKAT_DURATION} tick。</p>
     * <p><b>退出时：</b>将摇酒壶装备到主手。</p>
     * <p><b>下一状态：</b>{@link ShakingState}</p>
     */
    private class TurnForShakeState extends BartenderState {
        private final WaitTimer lookTimer = new WaitTimer();

        @Override
        void enter(ServerLevel level, BartenderEntity body, long timestamp) {
            lookTimer.start(timestamp, (int) LOOKAT_DURATION);
        }

        @Override
        void tick(ServerLevel level, BartenderEntity body, long timestamp) {
            body.getBrain()
                    .getMemory(MemoryModuleType.INTERACTION_TARGET)
                    .ifPresent(target -> {
                        Vec3 lookAt = target.getEyePosition()
                                .subtract(body.getEyePosition())
                                .yRot(Mth.PI * 0.4F)
                                .add(body.position());
                        body.getLookControl().setLookAt(lookAt);
                        body.setYBodyRot(body.yHeadRot);
                    });
        }

        @Override
        void exit(ServerLevel level, BartenderEntity body, long timestamp) {
            body.setItemInHand(InteractionHand.MAIN_HAND, ItemRegistries.SHAKER.toStack());
        }

        @Override
        boolean isComplete(ServerLevel level, BartenderEntity body, long timestamp) {
            return lookTimer.isDone(timestamp);
        }

        @Override
        BartenderState nextState(ServerLevel level, BartenderEntity body) {
            return new ShakingState();
        }
    }

    // =========================================================================
    // ShakingState — 摇晃
    // =========================================================================

    /**
     * 播放完整的摇晃动画。
     *
     * <p><b>等待：</b>动画进入 {@code SHAKING} 子状态后 {@value #SHAKING_DURATION} tick
     *（不计入 {@code READY_TO_SHAKE} 的起手阶段）。</p>
     * <p><b>退出时：</b>卸下摇酒壶，将待寻物品列表写入记忆，
     * 并启动 5 分钟的玻璃器皿查找时钟。</p>
     * <p><b>下一状态：</b>{@link WaitForGlasswareState}</p>
     */
    private class ShakingState extends BartenderState {
        private final WaitTimer shakeTimer = new WaitTimer();
        private boolean timerStarted;

        @Override
        void enter(ServerLevel level, BartenderEntity body, long timestamp) {
            timerStarted = false;
            if (!body.isShaking()) {
                body.startShaking();
            }
        }

        @Override
        void tick(ServerLevel level, BartenderEntity body, long timestamp) {
            if (!timerStarted && body.getState() == BartenderEntity.AnimState.SHAKING) {
                shakeTimer.start(timestamp, SHAKING_DURATION);
                timerStarted = true;
            }
        }

        @Override
        void exit(ServerLevel level, BartenderEntity body, long timestamp) {
            body.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            body.setState(BartenderEntity.AnimState.DEFAULT);
            if (recipeHolder != null) {
                body.getBrain().setMemory(
                        Memories.ITEM_TO_FIND.get(),
                        recipeHolder.getItemToFind());
            }
            glasswareFindTimeout.start(timestamp, (int) GLASSWARE_FIND_TIMEOUT);
        }

        @Override
        boolean isComplete(ServerLevel level, BartenderEntity body, long timestamp) {
            return timerStarted && shakeTimer.isDone(timestamp);
        }

        @Override
        BartenderState nextState(ServerLevel level, BartenderEntity body) {
            return new WaitForGlasswareState();
        }
    }

    // =========================================================================
    // WaitForGlasswareState — 等待玻璃器皿
    // =========================================================================

    /**
     * 空闲等待外部取物 AI（{@code BartenderFindItem}）收集到所需玻璃器皿，
     * 以 {@code ITEM_TO_FIND} 记忆被清除为信号。
     *
     * <p><b>等待：</b>{@code ITEM_TO_FIND} 不存在 <em>且</em>
     * {@value #PREPARE_WALK_DURATION} tick 稳定等待。</p>
     * <p><b>超时：</b>超过 {@value #GLASSWARE_FIND_TIMEOUT} tick（5 分钟）后
     * 放弃并转入 {@link EndState}。</p>
     * <p><b>下一状态：</b>{@link PouringState}（成功）或 {@link EndState}（超时）</p>
     */
    private class WaitForGlasswareState extends BartenderState {
        private final WaitTimer prepareTimer = new WaitTimer();
        private boolean timedOut;

        @Override
        void enter(ServerLevel level, BartenderEntity body, long timestamp) {
            prepareTimer.reset();
            timedOut = false;
        }

        @Override
        void tick(ServerLevel level, BartenderEntity body, long timestamp) {
            if (glasswareFindTimeout.isDone(timestamp)) {
                timedOut = true;
                return;
            }

            if (body.getBrain().checkMemory(Memories.ITEM_TO_FIND.get(), MemoryStatus.VALUE_ABSENT)) {
                if (!prepareTimer.isActive()) {
                    prepareTimer.start(timestamp, PREPARE_WALK_DURATION);
                }
                // 否则：仍在稳定等待中
            } else {
                prepareTimer.reset(); // 玻璃器皿尚未找到 — 重置稳定计时器
            }
        }

        @Override
        boolean isComplete(ServerLevel level, BartenderEntity body, long timestamp) {
            return timedOut || prepareTimer.isDone(timestamp);
        }

        @Override
        BartenderState nextState(ServerLevel level, BartenderEntity body) {
            return timedOut ? new EndState() : new PouringState();
        }
    }

    // =========================================================================
    // PouringState — 倒酒
    // =========================================================================

    /**
     * 将饮品倒入吧台上的杯子中。
     *
     * <p>内部阶段（由子枚举驱动）：</p>
     * <ol>
     *   <li><b>EQUIP</b> — 从背包中找到玻璃器皿，转为短饮杯</li>
     *   <li><b>LOCATE</b> — 在吧台上找到靠近顾客的空位</li>
     *   <li><b>APPROACH</b> — 在装备冷却期间走向该位置</li>
     *   <li><b>PLACE</b> — 将玻璃器皿方块放置在吧台边缘</li>
     *   <li><b>WAIT_POUR</b> — 短暂暂停以表现倒酒过程</li>
     *   <li><b>APPLY</b> — 将配方结果应用到玻璃器皿方块实体</li>
     * </ol>
     *
     * <p><b>下一状态：</b>{@link DecoratingState}（正常）或 {@link EndState}
     *（无可用玻璃器皿时）</p>
     */
    private class PouringState extends BartenderState {

        enum Phase { EQUIP, LOCATE, APPROACH, PLACE, WAIT_POUR, APPLY }

        private Phase phase = Phase.EQUIP;
        private final WaitTimer equipCooldown = new WaitTimer(); // POURING_DURATION
        private final WaitTimer pourCooldown  = new WaitTimer(); // POURING_CD
        private BlockPos placePos;
        private boolean completed;

        @Override
        void enter(ServerLevel level, BartenderEntity body, long timestamp) {
            phase = Phase.EQUIP;
            placePos = null;
            completed = false;
            equipCooldown.reset();
            pourCooldown.reset();
        }

        @Override
        void tick(ServerLevel level, BartenderEntity body, long timestamp) {
            switch (phase) {
                case EQUIP     -> tickEquip(level, body, timestamp);
                case LOCATE    -> tickLocate(level, body, timestamp);
                case APPROACH  -> tickApproach(level, body, timestamp);
                case PLACE     -> tickPlace(level, body, timestamp);
                case WAIT_POUR -> tickWaitPour(level, body, timestamp);
                case APPLY     -> tickApply(level, body, timestamp);
            }
        }

        // -- EQUIP：装备杯子 --------------------------------------------------

        /** 在背包中找到 {@link GlasswareItem}，装备其短饮杯版本。 */
        private void tickEquip(ServerLevel level, BartenderEntity body, long timestamp) {
            int glasswareIdx = -1;
            NonNullList<ItemStack> inv = body.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.get(i).getItem() instanceof GlasswareItem) {
                    glasswareIdx = i;
                    break;
                }
            }
            if (glasswareIdx < 0) {
                completed = true; // 无可用玻璃器皿 → 中止进入 EndState
                return;
            }
            equipCooldown.start(timestamp, POURING_DURATION);
            ItemStack shortGlass = GlasswareItem.getShortGlass(
                    ShakenStir.asResource(recipeHolder.holderGlass()));
            body.setItemInHand(InteractionHand.MAIN_HAND, shortGlass);
            body.setInventorySlot(glasswareIdx, ItemStack.EMPTY);
            phase = Phase.LOCATE;
        }

        // -- LOCATE：寻找放置位置 ---------------------------------------------

        /** 扫描吧台方块列表，找到空位以放置玻璃器皿。 */
        private void tickLocate(ServerLevel level, BartenderEntity body, long timestamp) {
            body.getBrain().getMemory(Memories.BAR_MEMORY.get()).flatMap(barData ->
                    barData.barCounter().stream()
                            .filter(pos -> level.getBlockState(pos).isEmpty())
                            .min(Comparator.comparing(bp -> body.getBrain()
                                    .getMemory(MemoryModuleType.INTERACTION_TARGET)
                                    .orElse(body)
                                    .distanceToSqr(bp.getCenter()))))
                    .ifPresent(pos -> placePos = pos);
            if (placePos != null) {
                phase = Phase.APPROACH;
            }
        }

        // -- APPROACH：走向放置位置 -------------------------------------------

        /** 在装备冷却倒计时期间走向选定的放置位置。 */
        private void tickApproach(ServerLevel level, BartenderEntity body, long timestamp) {
            if (equipCooldown.isDone(timestamp)) {
                phase = Phase.PLACE;
                return;
            }
            BehaviorUtils.setWalkAndLookTargetMemories(body, placePos.below(), 0.5F, 2);
            body.getLookControl().setLookAt(placePos.getCenter());
        }

        // -- PLACE：放置杯子 -------------------------------------------------

        /**
         * 将玻璃器皿方块放置在吧台最靠近酒保的边缘，
         * 使用 {@link FakePlayer} 作为方块放置上下文。
         */
        private void tickPlace(ServerLevel level, BartenderEntity body, long timestamp) {
            ItemStack itemInHand = body.getItemInHand(InteractionHand.MAIN_HAND);

            // 选择吧台边缘最靠近酒保的放置位置
            Vec3 bodyPos = body.position();
            Vec3 center = placePos.getCenter();
            double dx = bodyPos.x() - center.x();
            double dz = bodyPos.z() - center.z();
            float edgeInset = 0.05F;

            double posX, posZ;
            if (Math.abs(dx) > Math.abs(dz)) {
                posX = dx > 0 ? placePos.getX() + 1.0 - edgeInset : placePos.getX() + edgeInset;
                posZ = placePos.getZ() + level.getRandom().nextFloat();
            } else {
                posX = placePos.getX() + level.getRandom().nextFloat();
                posZ = dz > 0 ? placePos.getZ() + 1.0 - edgeInset : placePos.getZ() + edgeInset;
            }

            Vec3 hitPos = new Vec3(posX, placePos.getY() + 1.0, posZ);
            itemInHand.set(DataComponentTypeRegistries.GLASSWARE_ROTATION, body.getYRot() + 45);

            UseOnContext ctx = new UseOnContext(
                    level,
                    new FakePlayer(level, new GameProfile(UUID.randomUUID(), "bartender")),
                    InteractionHand.MAIN_HAND,
                    itemInHand,
                    new BlockHitResult(hitPos, Direction.UP, placePos, false));
            ((GlasswareItem) itemInHand.getItem()).useOn(ctx);

            body.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            body.getBrain().setMemory(
                    Memories.MEMORY_GLASSWARE.get(),
                    new GlobalPos(level.dimension(), placePos));
            pourCooldown.start(timestamp, POURING_CD);
            phase = Phase.WAIT_POUR;
        }

        // -- WAIT_POUR：等待倒酒 ----------------------------------------------

        /** 短暂暂停，表现倒酒过程。 */
        private void tickWaitPour(ServerLevel level, BartenderEntity body, long timestamp) {
            if (pourCooldown.isDone(timestamp)) {
                phase = Phase.APPLY;
            }
        }

        // -- APPLY：应用配方 --------------------------------------------------

        /** 将配方结果应用到已放置的玻璃器皿方块实体。 */
        private void tickApply(ServerLevel level, BartenderEntity body, long timestamp) {
            body.getBrain().getMemory(Memories.MEMORY_GLASSWARE.get()).ifPresent(glassware -> {
                if (level.getBlockEntity(glassware.pos()) instanceof GlasswareBlockEntity be) {
                    recipeHolder.recipe().apply(be, recipeHolder.result());
                }
            });
            body.swing(InteractionHand.MAIN_HAND);
            completed = true;
        }

        @Override
        boolean isComplete(ServerLevel level, BartenderEntity body, long timestamp) {
            return completed;
        }

        @Override
        BartenderState nextState(ServerLevel level, BartenderEntity body) {
            return new DecoratingState();
        }
    }

    // =========================================================================
    // DecoratingState — 装饰
    // =========================================================================

    /**
     * 将装饰品（水果片、小伞等）逐一放置到倒好的玻璃器皿上。
     *
     * <p>遍历 {@link SnsRecipeHolder#decorations()}：
     * 装备装饰品 → 等待 {@value #DECORATING_DURATION} + 随机抖动 →
     * 插入玻璃器皿 → 从列表移除 → 重复。</p>
     *
     * <p>所有装饰品放置完成后，玻璃器皿被推到最终上桌位置，状态完成。</p>
     *
     * <p><b>下一状态：</b>{@link EndState}</p>
     */
    private class DecoratingState extends BartenderState {
        private final List<GlasswareBlockEntity.Decoration> decorations = new ArrayList<>();
        private final WaitTimer decoTimer = new WaitTimer();
        private boolean completed;

        @Override
        void enter(ServerLevel level, BartenderEntity body, long timestamp) {
            decorations.clear();
            completed = false;
            decoTimer.reset();
        }

        @Override
        void tick(ServerLevel level, BartenderEntity body, long timestamp) {
            // --- 步骤 1：加载装饰品列表（仅首次） ---------------------------
            if (decorations.isEmpty() && body.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                decorations.addAll(recipeHolder.decorations());
                if (decorations.isEmpty()) {
                    pushGlasswareToCounter(level, body);
                    completed = true;
                    return; // 配方无装饰品 — 直接完成
                }
            }

            // --- 步骤 2：手上为空则装备下一个装饰品 ---------------------------
            if (body.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                GlasswareBlockEntity.Decoration deco = decorations.getFirst();
                if (deco.itemStack().isEmpty()) {
                    return; // 无效装饰品 — 等待（不应发生）
                }
                NonNullList<ItemStack> inventory = body.getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.get(i);
                    if (stack.is(deco.itemStack().getItem())) {
                        body.setItemInHand(InteractionHand.MAIN_HAND, deco.itemStack().copy());
                        body.setInventorySlot(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }

            // --- 步骤 3：等待装饰计时器 ---------------------------------------
            if (!decoTimer.isActive()) {
                decoTimer.startWithJitter(timestamp, DECORATING_DURATION, level.getRandom().nextInt(3));
            }
            if (!decoTimer.isDone(timestamp)) {
                return; // 仍在等待
            }

            // --- 步骤 4：应用装饰品 -------------------------------------------
            decoTimer.startWithJitter(timestamp, DECORATING_DURATION, level.getRandom().nextInt(3));
            body.getBrain().getMemory(Memories.MEMORY_GLASSWARE.get()).ifPresent(glassware -> {
                if (level.getBlockEntity(glassware.pos()) instanceof GlasswareBlockEntity be) {
                    be.insertDecoration(decorations.getFirst());
                }
            });
            body.swing(InteractionHand.MAIN_HAND);

            if (!decorations.isEmpty()) {
                decorations.removeFirst();
            }
            body.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
            if (body.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                body.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }

            // --- 步骤 5：全部完成？将玻璃器皿推到上桌位置 ---------------------
            if (decorations.isEmpty()) {
                pushGlasswareToCounter(level, body);
                completed = true;
            }
        }

        @Override
        boolean isComplete(ServerLevel level, BartenderEntity body, long timestamp) {
            return completed;
        }

        @Override
        BartenderState nextState(ServerLevel level, BartenderEntity body) {
            return new EndState();
        }
    }

    // =========================================================================
    // EndState — 终态
    // =========================================================================

    /**
     * 终态。设置 {@link #endTime} 以使 {@link #timedOut} 返回 {@code true}，
     * Minecraft 的 brain 系统随后会调用 {@link #stop} 进行清理。
     */
    private class EndState extends BartenderState {

        @Override
        void enter(ServerLevel level, BartenderEntity body, long timestamp) {
            endTime = timestamp;
        }

        @Override
        void tick(ServerLevel level, BartenderEntity body, long timestamp) {
            // 空闲 — 等待 timedOut() 触发清理
        }

        @Override
        boolean isComplete(ServerLevel level, BartenderEntity body, long timestamp) {
            return false; // 终态；由 timedOut() 驱动生命周期结束
        }

        @Override
        BartenderState nextState(ServerLevel level, BartenderEntity body) {
            return null;
        }
    }

    // =========================================================================
    // pushGlasswareToCounter — 共享辅助方法
    // =========================================================================

    /**
     * 将完成的玻璃器皿推到吧台上的最终上桌位置。
     * 通过统计各水平轴上吧台方块数量决定推动方向，与 {@code PutMenu} 的逻辑一致。
     * 同时触发 {@code PLEASE} 动画。
     */
    private void pushGlasswareToCounter(ServerLevel level, BartenderEntity body) {
        body.getBrain().getMemory(Memories.MEMORY_GLASSWARE.get()).ifPresent(globalPos -> {
            if (!level.dimension().equals(globalPos.dimension())) {
                return;
            }

            BlockPos pos = globalPos.pos();
            if (!(level.getBlockEntity(pos) instanceof GlasswareBlockEntity be)) {
                return;
            }

            // 通过统计各轴吧台方块数量决定推动方向
            BlockPos below = pos.below();
            int northSouthRank = 0;
            int eastWestRank = 0;
            for (Direction dir : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
                if (level.getBlockState(below.relative(dir)).is(BlockRegistries.BAR_COUNTER_BLOCK)) {
                    eastWestRank += Math.abs(dir.getStepZ());
                    northSouthRank += Math.abs(dir.getStepX());
                }
            }

            Vec3 blockCenter = Vec3.atCenterOf(pos);
            double dx = blockCenter.x() - body.getX();
            double dz = blockCenter.z() - body.getZ();

            float localX, localZ;
            if (northSouthRank >= eastWestRank) {
                localX = 0.5F;
                localZ = dz > 0 ? 1.0F : 0.0F;
            } else {
                localX = dx > 0 ? 1.0F : 0.0F;
                localZ = 0.5F;
            }

            body.setState(BartenderEntity.AnimState.PLEASE);
            be.moveTo(localX, localZ);
        });
    }

    // =========================================================================
    // Behavior 生命周期
    // =========================================================================

    public BartenderProduct() {
        super(ImmutableMap.of(
                Memories.RECIPE.get(), MemoryStatus.VALUE_PRESENT,
                Memories.RECIPE_READY.get(), MemoryStatus.VALUE_PRESENT,
                Memories.MEMORY_GLASSWARE.get(), MemoryStatus.REGISTERED));
    }

    @Override
    protected void start(ServerLevel level, BartenderEntity body, long timestamp) {
        endTime = -1;
        glasswareFindTimeout.reset();
        recipeHolder = null;

        body.getBrain().getMemory(Memories.RECIPE.get())
                .ifPresent(recipe -> recipeHolder = recipe);

        setState(new ApproachingState());
        currentState.enter(level, body, timestamp);
    }

    @Override
    protected void tick(ServerLevel level, BartenderEntity body, long timestamp) {
        if (currentState == null) return;

        currentState.tick(level, body, timestamp);
        tryTransition(level, body, timestamp);
    }

    /**
     * 若当前状态报告已完成，执行其退出逻辑，创建下一状态并进入 —
     * 全部在同一 tick 内完成。
     */
    private void tryTransition(ServerLevel level, BartenderEntity body, long timestamp) {
        if (!currentState.isComplete(level, body, timestamp)) return;

        currentState.exit(level, body, timestamp);
        BartenderState next = currentState.nextState(level, body);
        setState(next != null ? next : new EndState());
        currentState.enter(level, body, timestamp);
    }

    private void setState(BartenderState state) {
        this.currentState = state;
    }

    @Override
    protected void stop(ServerLevel level, BartenderEntity body, long timestamp) {
        // 从背包中消耗配方原料
        body.getBrain().eraseMemory(Memories.RECIPE_READY.get());
        body.getBrain().getMemory(Memories.RECIPE.get()).ifPresent(recipe -> {
            NonNullList<ItemStack> inventory = body.getInventory();

            // 消耗物品原料
            for (ItemStack requiredItem : recipe.requiredItems()) {
                int remainingCount = requiredItem.getCount();
                for (int i = 0; i < inventory.size() && remainingCount > 0; i++) {
                    ItemStack slot = inventory.get(i);
                    if (slot.isEmpty()) continue;
                    if (ItemStack.isSameItemSameComponents(slot, requiredItem)) {
                        int toConsume = Math.min(slot.getCount(), remainingCount);
                        slot.shrink(toConsume);
                        remainingCount -= toConsume;
                        if (slot.isEmpty()) {
                            inventory.set(i, ItemStack.EMPTY);
                        }
                    }
                }
            }

            // 消耗流体原料
            for (FluidStack requiredFluid : recipe.requiredFluids()) {
                int remainingAmount = requiredFluid.getAmount();
                for (int i = 0; i < inventory.size() && remainingAmount > 0; i++) {
                    ItemStack slot = inventory.get(i);
                    if (slot.isEmpty()) continue;

                    SpiritContent spiritContent = slot.getOrDefault(
                            DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY);
                    if (spiritContent.isEmpty()) continue;

                    FluidStack fluidInSlot = spiritContent.fluidStack();
                    if (!fluidInSlot.is(requiredFluid.getFluid())) continue;

                    int toConsume = Math.min(fluidInSlot.getAmount(), remainingAmount);
                    if (toConsume > 0) {
                        FluidStack newFluid = fluidInSlot.copyWithAmount(
                                fluidInSlot.getAmount() - toConsume);
                        slot.set(DataComponentTypeRegistries.SPIRIT_CONTENT,
                                new SpiritContent(newFluid));
                        remainingAmount -= toConsume;
                        if (newFluid.isEmpty()) {
                            inventory.set(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
        });

        body.setState(BartenderEntity.AnimState.DEFAULT);
        body.getBrain().eraseMemory(Memories.RECIPE.get());
        body.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    @Override
    protected boolean timedOut(long timestamp) {
        return endTime > 0 && timestamp > endTime && currentState instanceof EndState;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, BartenderEntity body, long timestamp) {
        return true;
    }

    @Override
    public String debugString() {
        String stateName = currentState != null
                ? currentState.getClass().getSimpleName()
                : "NULL";
        return "BartenderProduct[" + stateName + "]";
    }
}
