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
import io.github.hawah.shakenstir.content.entity.ai.activity.Activities;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.MenuItem;
import io.github.hawah.shakenstir.foundation.data.SnsRecipeStack;
import io.github.hawah.shakenstir.foundation.networking.ClientboundBartenderSpeakPacket;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked", "resource"})
public class BartenderEntity extends AbstractInventoryMob implements OwnableEntity {

    public static final EntityDataAccessor<Integer> DATA_ACCESSOR =
            SynchedEntityData.defineId(
                    // The class of the entity.
                    BartenderEntity.class,
                    // The entity data accessor type.
                    EntityDataSerializers.INT
            );
    public static final EntityDataAccessor<Integer> ANIM_ACCESSOR =
            SynchedEntityData.defineId(
                    // The class of the entity.
                    BartenderEntity.class,
                    // The entity data accessor type.
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

    public static final Brain.Provider<BartenderEntity> BRAIN_PROVIDER = Brain.provider(
            BartenderAi.getSensors(),
            BartenderAi::getActivities
    );

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(24, ItemStack.EMPTY);
    public float readyShakeAmount = 0;
    public float readyShakeAmountO = 0;

    private Component speakingComponent = null;
    private int speakingRemainingTicks = 0;

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
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ContainerHelper.saveAllItems(output, inventory);
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
                    new Runnable() {
                        @Override
                        public void run() {
                            ScreenOpener.open(new DialogueEditorScreen(BartenderEntity.this));
                        }
                    }.run();
                }
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
     * Get the current speaking Component, or null if not speaking.
     */
    public @Nullable Component getSpeakingComponent() {
        return speakingComponent;
    }

    /**
     * Get the remaining display ticks for the current speech bubble.
     */
    public int getSpeakingRemainingTicks() {
        return speakingRemainingTicks;
    }

    /**
     * Client-side only. Display a speech bubble above this entity on the local client.
     * If announce is true, also sends a C2S packet so the server can broadcast to all players.
     *
     * @param message       the Component (Chat Component) to display
     * @param announce      if true, request the server to broadcast this message to all tracking clients
     * @param remainingTicks how many ticks the message should remain visible
     */
    public void speakClient(Component message, boolean announce, int remainingTicks) {
        this.speakingComponent = message;
        this.speakingRemainingTicks = Math.max(0, remainingTicks);
        if (announce && level().isClientSide()) {
            Networking.sendToServer(new ServerboundBartenderSpeakAnnouncePacket(getId(), message, remainingTicks));
        }
    }

    /**
     * Server-side only. Broadcasts a speech bubble to ALL players currently tracking this entity.
     *
     * @param message       the Component (Chat Component) to display
     * @param remainingTicks how many ticks the message should remain visible
     */
    public void speakServer(Component message, int remainingTicks) {
        if (level().isClientSide()) {
            return;
        }
        this.speakingComponent = message;
        this.speakingRemainingTicks = Math.max(0, remainingTicks);
        PacketDistributor.sendToPlayersTrackingEntity(
                this,
                new ClientboundBartenderSpeakPacket(getId(), message, remainingTicks)
        );
    }

    /**
     * Server-side only. Sends a speech bubble to a specific list of players only.
     *
     * @param message       the Component (Chat Component) to display
     * @param targets       the list of players who should see the message
     * @param remainingTicks how many ticks the message should remain visible
     */
    public void speakServer(Component message, List<Player> targets, int remainingTicks) {
        if (level().isClientSide()) {
            return;
        }
        this.speakingComponent = message;
        this.speakingRemainingTicks = Math.max(0, remainingTicks);
        ClientboundBartenderSpeakPacket packet = new ClientboundBartenderSpeakPacket(getId(), message, remainingTicks);
        for (Player player : targets) {
            if (player instanceof ServerPlayer serverPlayer) {
                Networking.sendToPlayer(packet, serverPlayer);
            }
        }
    }

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
