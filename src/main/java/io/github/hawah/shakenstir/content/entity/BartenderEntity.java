package io.github.hawah.shakenstir.content.entity;

import com.mojang.logging.LogUtils;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.Config;
import io.github.hawah.shakenstir.client.animation.AnimationState;
import io.github.hawah.shakenstir.client.animation.AnimationStateMachine;
import io.github.hawah.shakenstir.client.animation.ShakeAnimationState;
import io.github.hawah.shakenstir.client.gui.DialogueEditorScreen;
import io.github.hawah.shakenstir.client.render.entity.BartenderModel;
import io.github.hawah.shakenstir.content.blockEntity.BarMenuBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dialogue.DialogueData;
import io.github.hawah.shakenstir.content.dialogue.DialogueManager;
import io.github.hawah.shakenstir.content.entity.ai.activity.Activities;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.MenuItem;
import io.github.hawah.shakenstir.foundation.data.SnsRecipeStack;
import io.github.hawah.shakenstir.foundation.networking.ClientboundBartenderDialogueSyncPacket;
import io.github.hawah.shakenstir.foundation.networking.ClientboundBartenderSpeakPacket;
import io.github.hawah.shakenstir.foundation.networking.ServerboundBartenderDialogueRequestPacket;
import io.github.hawah.shakenstir.foundation.networking.ServerboundBartenderSpeakAnnouncePacket;
import io.github.hawah.shakenstir.lib.client.gui.ScreenOpener;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked", "resource"})
public class BartenderEntity extends AbstractInventoryMob implements OwnableEntity {

    public static final EntityDataAccessor<Integer> DATA_ACCESSOR =
            SynchedEntityData.defineId(
                    // 实体的类
                    BartenderEntity.class,
                    // 实体数据访问器 (EntityDataAccessor) 类型
                    EntityDataSerializers.INT
            );
    public static final EntityDataAccessor<Integer> ANIM_ACCESSOR =
            SynchedEntityData.defineId(
                    // 实体的类
                    BartenderEntity.class,
                    // 实体数据访问器 (EntityDataAccessor) 类型
                    EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<OptionalInt> DATA_SHOULDER_PARROT_RIGHT = SynchedEntityData.defineId(
            BartenderEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT
    );
    private static final EntityDataAccessor<OptionalInt> DATA_SHOULDER_PARROT_LEFT = SynchedEntityData.defineId(
            BartenderEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT
    );
    protected static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(
            BartenderEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE
    );
    public static final EntityDataAccessor<Boolean> DATA_HAS_QUEUED_SPEAK = SynchedEntityData.defineId(
            BartenderEntity.class, EntityDataSerializers.BOOLEAN
    );

    public static final Brain.Provider<BartenderEntity> BRAIN_PROVIDER = Brain.provider(
            BartenderAi.getSensors(),
            BartenderAi::getActivities
    );

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(24, ItemStack.EMPTY);
    public float readyShakeAmount = 0;
    public float readyShakeAmountO = 0;

    private Component speakingComponent = null;
    private int speakingRemainingTicks = 0;
    private final LinkedList<QueuedMessage> queuedSpeaks = new LinkedList<>();

    /** 对话数据 (Dialogue Data) - 存储所有对话条目及其条件配置 */
    private DialogueData dialogueData = DialogueData.EMPTY;
    /** 已播放索引追踪器 (Played Index Tracker) - 用于无重复播放机制 */
    private final DialogueManager.PlayedTracker dialoguePlayedTracker = new DialogueManager.PlayedTracker();
    /** 已交互过的玩家 UUID 集合 - 用于判断交互历史条件 */
    private final Set<UUID> interactedPlayers = new HashSet<>();

    public BartenderEntity(EntityType<BartenderEntity> type, Level level) {
        super(type, level);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        if (level.isClientSide()) {
            initStateMachine();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    @Override
    protected Brain<BartenderEntity> makeBrain(Brain.Packed packedBrain) {
        return BRAIN_PROVIDER.makeBrain(this, packedBrain);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACCESSOR, 0);
        builder.define(ANIM_ACCESSOR, 0);
        builder.define(DATA_SHOULDER_PARROT_RIGHT, OptionalInt.empty());
        builder.define(DATA_SHOULDER_PARROT_LEFT, OptionalInt.empty());
        builder.define(DATA_OWNERUUID_ID, Optional.empty());
        builder.define(DATA_HAS_QUEUED_SPEAK, false);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return super.hurtServer(level, source, damage);
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }


    public Parrot.@Nullable Variant getParrotVariantOnShoulder(boolean left) {
        return (left ? this.getShoulderParrotLeft() : this.getShoulderParrotRight()).orElse(null);
    }

    public Optional<Parrot.Variant> getShoulderParrotLeft() {
        return convertParrotVariant(this.entityData.get(DATA_SHOULDER_PARROT_LEFT));
    }

    public Optional<Parrot.Variant> getShoulderParrotRight() {
        return convertParrotVariant(this.entityData.get(DATA_SHOULDER_PARROT_RIGHT));
    }

    public void setShoulderParrotRight(Optional<Parrot.Variant> variant) {
        this.entityData.set(DATA_SHOULDER_PARROT_RIGHT, convertParrotVariant(variant));
    }

    public void setShoulderParrotLeft(Optional<Parrot.Variant> variant) {
        this.entityData.set(DATA_SHOULDER_PARROT_LEFT, convertParrotVariant(variant));
    }

    public boolean alertCustomerOrdered(Player customer) {
        if (getBrain().getActiveNonCoreActivity().map(activity -> Activities.PRODUCT.get().equals(activity)).orElse(false)) {
            return false;
        }
        if (getBrain().checkMemory(Memories.RECIPES_TODO.get(), MemoryStatus.VALUE_PRESENT)) {
            return false;
        }
        getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, customer);
        this.getBrain().getMemory(Memories.MENU.get()).ifPresent(
                menu -> {
                    if (!level().dimension().equals(menu.dimension())) {
                        return;
                    }
                    if (!(level().getBlockEntity(menu.pos()) instanceof BarMenuBlockEntity blockEntity)) {
                        this.getBrain().eraseMemory(Memories.MENU.get());
                        return;
                    }
                    if (this.getBrain().checkMemory(Memories.RECIPE.get(), MemoryStatus.VALUE_PRESENT)) {
                        return;
                    }
                    List<SnsRecipeStack> recipes = new ArrayList<>(blockEntity.recipes
                            .stream()
                            .filter(item -> item.right().count > 0)
                            .map(
                                    recipe -> new SnsRecipeStack(recipe.left(), recipe.right().count)
                            )
                            .toList());
                    if (!recipes.isEmpty()){
                        getBrain().setMemory(Memories.RECIPE.get(), recipes.getFirst().holder());
                        recipes.getFirst().shrink();
                        if (recipes.getFirst().isEmpty()) {
                            recipes.removeFirst();
                        }
                        if (!recipes.isEmpty()) {
                            getBrain().setMemory(Memories.RECIPES_TODO.get(), recipes);
                        }
                    }
                    for (int i = 0; i < blockEntity.recipes.size(); i++) {
                        blockEntity.setRecipeCount(i, 0);
                    }
                }
        );
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.updateSwingTime();
    }

    @Override
    public Brain<BartenderEntity> getBrain() {
        return (Brain<BartenderEntity>) super.getBrain();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("bartenderBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        profiler.pop();
        BartenderAi.updateActivity(this);
    }

    public AnimState getState() {
        return AnimState.from(this.getEntityData().get(ANIM_ACCESSOR));
    }

    public void setState(AnimState state) {
        this.entityData.set(ANIM_ACCESSOR, state.ordinal());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return super.createNavigation(level);
    }

    private static Optional<Parrot.Variant> convertParrotVariant(OptionalInt variant) {
        return variant.isPresent() ? Optional.of(Parrot.Variant.byId(variant.getAsInt())) : Optional.empty();
    }

    protected static OptionalInt convertParrotVariant(Optional<Parrot.Variant> variant) {
        return variant.<OptionalInt>map(v -> OptionalInt.of(v.getId())).orElse(OptionalInt.empty());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        ContainerHelper.loadAllItems(input, inventory);
        // 加载对话数据
        loadDialogueData(input);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ContainerHelper.saveAllItems(output, inventory);
        // 保存对话数据
        saveDialogueData(output);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (getOwner() != null && player.is(getOwner())) {
            if (itemInHand.has(DataComponentTypeRegistries.BAR_AREA)) {
                player.sendOverlayMessage(Component.literal("Set work area success"));
                Optional.ofNullable(itemInHand.get(DataComponentTypeRegistries.BAR_AREA)).ifPresent(barArea -> {
                    // TODO Reachable Prediction
                    if (barArea.area().getCenter().distManhattan(this.blockPosition()) < 200 && level().dimension().equals(barArea.dimension())) {
                        this.getBrain().setMemory(Memories.BAR_MEMORY.get(), BarAreaHelper.calculateBarData(barArea.area(), level()));
                    }
                });
                return InteractionResult.SUCCESS;
            } else if (itemInHand.is(ItemRegistries.MENU)) {
                for (int i = 0; i < getInventory().size(); i++) {
                    if (getInventorySlot(i).getItem() instanceof MenuItem) {
                        ItemEntity itemEntity = new ItemEntity(level(), getX(), getY(), getZ(), getInventorySlot(i));
                        level().addFreshEntity(itemEntity);
                        setInventorySlot(i, ItemStack.EMPTY);
                    }
                }
                int count = itemInHand.getCount();
                this.insertItem(itemInHand);
                if (player.isCreative()) {
                    itemInHand.setCount(count);
                }
                return InteractionResult.SUCCESS;
            } else if (itemInHand.has(DataComponentTypeRegistries.DIALOGUE)) {
                if (level().isClientSide()) {
                    // 客户端：请求服务端同步对话数据后打开编辑器
                    Networking.sendToServer(new ServerboundBartenderDialogueRequestPacket(getId()));
                    ScreenOpener.open(new DialogueEditorScreen(BartenderEntity.this));
                }
                markPlayerInteracted(player.getUUID());
                return InteractionResult.SUCCESS;
            }
        }
        if (itemInHand.is(Items.STICK) && getOwner() == null) {
            setOwner(player);
            player.sendOverlayMessage(Component.literal("Now you are the owner"));
        } else if (player.isShiftKeyDown() && Config.Common.DEBUG_MODE.get()) {
            StringBuilder sb = new StringBuilder();
            for (ItemStack itemStack : inventory) {
                if (itemStack.has(DataComponentTypeRegistries.SPIRIT_CONTENT)) {
                    sb.append(itemStack.getDisplayName().getString())
                            .append(" ")
                            .append(itemStack.get(DataComponentTypeRegistries.SPIRIT_CONTENT).fluidStack().amount())
                            .append(" mB\n");
                } else {
                    sb.append(itemStack.getDisplayName().getString())
                            .append("\n");
                }
            }
            sb.append("Inventory: ")
                    .append(getItemInHand(InteractionHand.MAIN_HAND));
            player.sendSystemMessage(Component.literal(sb.toString()));
        }else {
//            alertCustomerOrdered(player);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    public void setMainHandItem(ItemStack itemStack) {
        if (!this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            this.insertItem(this.getItemInHand(InteractionHand.MAIN_HAND));
        }
        this.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
    }

    public void setMainHandItemAndShrink(ItemStack itemStack) {
        this.setMainHandItem(itemStack.copyAndClear());
    }

    public void tryGetOnHand(Holder<Item> item) {
        this.getInventory().stream()
                .filter(itemStack -> itemStack.is(item))
                .findFirst()
                .ifPresent(this::setMainHandItemAndShrink);
    }

    public void tryGetOnHand(Item item) {
        this.getInventory().stream()
                .filter(itemStack -> itemStack.is(item))
                .findFirst()
                .ifPresent(this::setMainHandItemAndShrink);
    }
    public AnimationStateMachine animationStateMachine;

    private void initStateMachine() {
        animationStateMachine = new AnimationStateMachine();
        AnimationState idle = new AnimationState(BartenderModel.DEFAULT);
        AnimationState ready = new AnimationState(BartenderModel.READY).fadeInMs(200).fadeOutMs(250);
        AnimationState shake = new ShakeAnimationState(BartenderModel.SHAKE, this).fadeInMs(250).fadeOutMs(500);
        AnimationState idleFront = new AnimationState(BartenderModel.IDLE_FRONT).fadeInMs(500).fadeOutMs(250);
        AnimationState idleBack = new AnimationState(BartenderModel.IDLE_BACK).fadeInMs(500).fadeOutMs(250);
        AnimationState please = new AnimationState(BartenderModel.PLEASE).fadeInMs(500).fadeOutMs(500);
        idle.registerConnection("readyToShake", ready);
        ready.registerConnection("shake", shake);
        shake.registerConnection("idle", idle);
        idle.registerConnection("idleFront", idleFront);
        idle.registerConnection("idleBack", idleBack);
        idleFront.registerConnection("idle", idle);
        idleBack.registerConnection("idle", idle);
        idle.registerConnection("please", please);
        please.registerConnection("idle", idle);

        animationStateMachine.state = "idle";
        animationStateMachine.start(idle);
    }

    @Override
    public void tick() {
        super.tick();
        updateReady();
        // Speaking countdown
        if (speakingRemainingTicks > 0) {
            speakingRemainingTicks--;
            if (speakingRemainingTicks <= 0) {
                speakingComponent = null;
                speakingRemainingTicks = 0;
                // Dequeue next message if available
                tryDequeueNext();
            }
        }
    }

    float pleaseAmount = 0;

    public void updateReady() {
        this.readyShakeAmountO = this.readyShakeAmount;
        if (this.getState().equals(AnimState.READY_TO_SHAKE)) {
            this.readyShakeAmount = this.readyShakeAmount + 1 / 20F;
            if (this.readyShakeAmount > 1) {
                this.setState(AnimState.SHAKING);
            }
        } else {
            this.readyShakeAmount = 0;
        }

        if (this.getState().equals(AnimState.PLEASE)) {
            this.pleaseAmount = this.pleaseAmount + 1/20F;
            if (this.pleaseAmount > 1) {
                this.setState(AnimState.DEFAULT);
            }
        } else {
            this.pleaseAmount = 0;
        }
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(owner).map(EntityReference::of));
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        PlayerTeam ownTeam = super.getTeam();
        if (ownTeam != null) {
            return ownTeam;
        } else {
            LivingEntity owner = this.getRootOwner();
            if (owner != null) {
                return owner.getTeam();
            }
        }
        return null;
    }

    //TODO
    public boolean isShaking() {
        return getState().equals(AnimState.SHAKING) || getState().equals(AnimState.READY_TO_SHAKE);
    }

    public void startShaking() {
        setState(AnimState.READY_TO_SHAKE);
    }

    /**
     * 获取当前正在显示的对话组件 (Component)，如果未显示则返回 null。
     */
    public @Nullable Component getSpeakingComponent() {
        return speakingComponent;
    }

    /**
     * 获取当前对话气泡的剩余显示刻数。
     */
    public int getSpeakingRemainingTicks() {
        return speakingRemainingTicks;
    }

    /**
     * 是否有下一条消息在队列中等待（通过实体数据同步）。
     */
    public boolean hasQueuedSpeak() {
        return entityData.get(DATA_HAS_QUEUED_SPEAK);
    }

    /**
     * 将 DATA_HAS_QUEUED_SPEAK 实体数据与实际的队列状态同步。
     * 必须在任何 queuedSpeaks 队列变更后调用。
     */
    private void syncQueueState() {
        boolean hasQueued = !queuedSpeaks.isEmpty();
        if (entityData.get(DATA_HAS_QUEUED_SPEAK) != hasQueued) {
            entityData.set(DATA_HAS_QUEUED_SPEAK, hasQueued);
        }
    }

    /**
     * 立即设置当前正在显示的对话消息，如果是服务端则广播给所有追踪客户端。
     */
    private void setSpeakingImmediate(Component message, int remainingTicks, boolean broadcast) {
        this.speakingComponent = message;
        this.speakingRemainingTicks = Math.max(0, remainingTicks);
        if (broadcast && !level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(
                    this,
                    new ClientboundBartenderSpeakPacket(getId(), message, remainingTicks)
            );
        }
    }

    /**
     * 尝试从队列中出队下一条消息。如果当前没有正在显示的消息且队列非空，
     * 则弹出队首消息并显示（如果是服务端则广播）。
     */
    private void tryDequeueNext() {
        if (speakingRemainingTicks <= 0 && !queuedSpeaks.isEmpty()) {
            QueuedMessage next = queuedSpeaks.pollFirst();
            if (next != null) {
                boolean broadcast = !level().isClientSide();
                setSpeakingImmediate(next.message(), next.remainingTicks(), broadcast);
            }
            syncQueueState();
        }
    }

    /**
     * 通用入口：显示对话气泡。自动根据当前逻辑端委托到 speakClient（客户端）或 speakServer（服务端）。
     * 使用覆盖模式（清空队列，立即替换当前消息）。
     *
     * @param message        要显示的组件 (Chat Component)
     * @param remainingTicks 消息应显示的刻数
     */
    public void speak(Component message, int remainingTicks) {
        speak(message, remainingTicks, false);
    }

    /**
     * 通用入口：显示对话气泡。自动根据当前逻辑端委托到 speakClient（客户端）或 speakServer（服务端）。
     *
     * @param message        要显示的组件 (Chat Component)
     * @param remainingTicks 消息应显示的刻数
     * @param enqueue        如果为 true，追加到队列尾部；如果为 false，立即覆盖并清空队列
     */
    public void speak(Component message, int remainingTicks, boolean enqueue) {
        if (level().isClientSide()) {
            speakClient(message, false, remainingTicks, enqueue);
        } else {
            speakServer(message, remainingTicks, enqueue);
        }
    }

    /**
     * 仅客户端。在本地客户端显示此实体上方的对话气泡（覆盖模式：清空队列，立即替换当前消息）。
     * 如果 announce 为 true，还会发送 C2S 包让服务端广播给所有玩家。
     *
     * @param message        要显示的组件 (Chat Component)
     * @param announce       如果为 true，请求服务端将此消息广播给所有追踪客户端
     * @param remainingTicks 消息应显示的刻数
     */
    public void speakClient(Component message, boolean announce, int remainingTicks) {
        speakClient(message, announce, remainingTicks, false);
    }

    /**
     * 仅客户端。在本地客户端显示此实体上方的对话气泡。
     * 如果 announce 为 true，还会发送 C2S 包让服务端广播给所有玩家。
     *
     * @param message        要显示的组件 (Chat Component)
     * @param announce       如果为 true，请求服务端将此消息广播给所有追踪客户端
     * @param remainingTicks 消息应显示的刻数
     * @param enqueue        如果为 true，追加到队列尾部；如果为 false，立即覆盖并清空队列
     */
    public void speakClient(Component message, boolean announce, int remainingTicks, boolean enqueue) {
        if (enqueue) {
            queuedSpeaks.add(new QueuedMessage(message, remainingTicks));
            syncQueueState();
            if (speakingRemainingTicks <= 0) {
                tryDequeueNext();
            }
            if (announce && level().isClientSide()) {
                Networking.sendToServer(new ServerboundBartenderSpeakAnnouncePacket(getId(), message, remainingTicks, true));
            }
        } else {
            queuedSpeaks.clear();
            syncQueueState();
            setSpeakingImmediate(message, remainingTicks, false);
            if (announce && level().isClientSide()) {
                Networking.sendToServer(new ServerboundBartenderSpeakAnnouncePacket(getId(), message, remainingTicks, false));
            }
        }
    }

    /**
     * 仅服务端。向所有当前追踪此实体的玩家广播对话气泡（覆盖模式：清空队列，立即替换当前消息）。
     *
     * @param message        要显示的组件 (Chat Component)
     * @param remainingTicks 消息应显示的刻数
     */
    public void speakServer(Component message, int remainingTicks) {
        speakServer(message, remainingTicks, false);
    }

    /**
     * 仅服务端。向所有当前追踪此实体的玩家广播对话气泡。
     *
     * @param message        要显示的组件 (Chat Component)
     * @param remainingTicks 消息应显示的刻数
     * @param enqueue        如果为 true，追加到队列尾部；如果为 false，立即覆盖并清空队列
     */
    public void speakServer(Component message, int remainingTicks, boolean enqueue) {
        if (level().isClientSide()) {
            return;
        }
        if (enqueue) {
            queuedSpeaks.add(new QueuedMessage(message, remainingTicks));
            syncQueueState();
            if (speakingRemainingTicks <= 0) {
                tryDequeueNext();
            }
            // 排队的消息不会立即广播；它将在出队时广播
        } else {
            queuedSpeaks.clear();
            syncQueueState();
            setSpeakingImmediate(message, remainingTicks, true);
        }
    }

    /**
     * 仅服务端。仅向指定的玩家列表发送对话气泡（覆盖模式：清空队列，立即替换当前消息）。
     *
     * @param message        要显示的组件 (Chat Component)
     * @param targets        应该看到消息的玩家列表
     * @param remainingTicks 消息应显示的刻数
     */
    public void speakServer(Component message, List<Player> targets, int remainingTicks) {
        speakServer(message, targets, remainingTicks, false);
    }

    /**
     * 仅服务端。仅向指定的玩家列表发送对话气泡。
     *
     * @param message        要显示的组件 (Chat Component)
     * @param targets        应该看到消息的玩家列表
     * @param remainingTicks 消息应显示的刻数
     * @param enqueue        如果为 true，追加到队列尾部；如果为 false，立即覆盖并清空队列
     */
    public void speakServer(Component message, List<Player> targets, int remainingTicks, boolean enqueue) {
        if (level().isClientSide()) {
            return;
        }
        if (enqueue) {
            queuedSpeaks.add(new QueuedMessage(message, remainingTicks));
            syncQueueState();
            if (speakingRemainingTicks <= 0) {
                tryDequeueNext();
            }
        } else {
            queuedSpeaks.clear();
            syncQueueState();
            this.speakingComponent = message;
            this.speakingRemainingTicks = Math.max(0, remainingTicks);
            ClientboundBartenderSpeakPacket packet = new ClientboundBartenderSpeakPacket(getId(), message, remainingTicks);
            for (Player player : targets) {
                if (player instanceof ServerPlayer serverPlayer) {
                    Networking.sendToPlayer(packet, serverPlayer);
                }
            }
        }
    }

    // ========================
    //  对话数据 (Dialogue Data) 存取与管理
    // ========================

    /**
     * 获取当前对话数据 (Dialogue Data)。
     */
    public DialogueData getDialogueData() {
        return dialogueData;
    }

    /**
     * 设置对话数据 (Dialogue Data)。
     * 在服务端设置后会自动持久化；在客户端设置则仅用于编辑预览。
     */
    public void setDialogueData(DialogueData data) {
        this.dialogueData = data;
    }

    /**
     * 获取已播放索引追踪器 (Played Index Tracker)。
     */
    public DialogueManager.PlayedTracker getDialoguePlayedTracker() {
        return dialoguePlayedTracker;
    }

    /**
     * 记录玩家与酒保发生过交互。
     */
    public void markPlayerInteracted(UUID playerUUID) {
        interactedPlayers.add(playerUUID);
    }

    /**
     * 检查指定玩家是否曾与该酒保交互过。
     */
    public boolean hasInteractedWith(UUID playerUUID) {
        return interactedPlayers.contains(playerUUID);
    }

    /**
     * 获取已交互玩家集合。
     */
    public Set<UUID> getInteractedPlayers() {
        return Collections.unmodifiableSet(interactedPlayers);
    }

    /**
     * 服务端评估对话条件并触发对话气泡显示。
     * 向指定玩家发送匹配的对话（如果没有匹配则不做任何事）。
     *
     * @param player 触发对话的玩家
     * @param durationTicks 对话气泡显示的刻数
     * @return 是否成功匹配并发送了对话
     */
    public boolean evaluateAndSpeak(Player player, int durationTicks) {
        if (level().isClientSide() || !(level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (dialogueData.isEmpty()) {
            return false;
        }

        Component result = DialogueManager.selectDialogue(
                dialogueData, serverLevel, this, player, dialoguePlayedTracker
        );

        if (result != null) {
            speakServer(result, durationTicks);
            return true;
        }
        return false;
    }

    /**
     * 向服务端请求同步对话数据到客户端。
     * 由客户端在打开 DialogueEditorScreen 时调用。
     */
    public void requestDialogueSyncFromServer() {
        // 此方法在客户端调用时发 C2S 请求包；
        // 实际同步通过交互时触发的 S2C 包完成。
        // 目前通过 mobInteract 中直接请求同步。
    }

    /**
     * 从 NBT (ValueInput) 加载对话数据。
     */
    private void loadDialogueData(ValueInput input) {
        String raw = input.getString("DialogueData").orElse("");
        if (!raw.isEmpty()) {
            try {
                var json = com.google.gson.JsonParser.parseString(raw);
                var result = DialogueData.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json);
                this.dialogueData = result.resultOrPartial(err ->
                        com.mojang.logging.LogUtils.getLogger().error("Failed to load dialogue data: {}", err)
                ).orElse(DialogueData.EMPTY);
            } catch (Exception e) {
                com.mojang.logging.LogUtils.getLogger().error("Failed to parse dialogue data JSON", e);
                this.dialogueData = DialogueData.EMPTY;
            }
        }

        // 加载已播放索引
        int playedCount = input.getInt("DialoguePlayedCount").orElse(0);
        Map<UUID, BitSet> loadedPlayed = new HashMap<>();
        for (int i = 0; i < playedCount; i++) {
            String key = input.getString("DialoguePlayedKey_" + i).orElse("");
            String bits = input.getString("DialoguePlayedBits_" + i).orElse("");
            if (!key.isEmpty() && !bits.isEmpty()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    byte[] bytes = Base64.getDecoder().decode(bits);
                    loadedPlayed.put(uuid, BitSet.valueOf(bytes));
                } catch (Exception ignored) {
                }
            }
        }
        dialoguePlayedTracker.loadFromRaw(loadedPlayed);

        // 加载已交互玩家
        int interactedCount = input.getInt("DialogueInteractedCount").orElse(0);
        interactedPlayers.clear();
        for (int i = 0; i < interactedCount; i++) {
            String uuidStr = input.getString("DialogueInteracted_" + i).orElse("");
            if (!uuidStr.isEmpty()) {
                try {
                    interactedPlayers.add(UUID.fromString(uuidStr));
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 保存对话数据到 NBT (ValueOutput)。
     */
    private void saveDialogueData(ValueOutput output) {
        var result = DialogueData.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, dialogueData);
        String raw = result.resultOrPartial(err ->
                com.mojang.logging.LogUtils.getLogger().error("Failed to serialize dialogue data: {}", err)
        ).map(com.google.gson.JsonElement::toString).orElse("");
        output.putString("DialogueData", raw);

        // 保存已播放索引
        Map<UUID, BitSet> playedData = dialoguePlayedTracker.getRawData();
        output.putInt("DialoguePlayedCount", playedData.size());
        int idx = 0;
        for (var entry : playedData.entrySet()) {
            output.putString("DialoguePlayedKey_" + idx, entry.getKey().toString());
            byte[] bytes = entry.getValue().toByteArray();
            output.putString("DialoguePlayedBits_" + idx, Base64.getEncoder().encodeToString(bytes));
            idx++;
        }

        // 保存已交互玩家
        output.putInt("DialogueInteractedCount", interactedPlayers.size());
        int interactedIdx = 0;
        for (UUID uuid : interactedPlayers) {
            output.putString("DialogueInteracted_" + interactedIdx, uuid.toString());
            interactedIdx++;
        }
    }

    /**
     * 排队的对话消息及其显示时长。
     */
    public record QueuedMessage(Component message, int remainingTicks) {}

    public enum AnimState implements StringRepresentable {
        DEFAULT("idle"),
        READY_TO_SHAKE("readyToShake"),
        SHAKING("shake"),
        IDLE_FRONT("idleFront"),
        IDLE_BACK("idleBack"),
        PLEASE("please")
        ;

        private final String name;

        AnimState(String name) {
            this.name = name;
        }

        public static AnimState from(int i) {
            if (i < 0 || i >= values().length) {
                LogUtils.getLogger().error("Invalid animation state");
                return DEFAULT;
            }
            return AnimState.values()[i];
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
