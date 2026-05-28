package io.github.hawah.shakenstir.content.entity;

import com.mojang.logging.LogUtils;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
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

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(6, ItemStack.EMPTY);

    private float shakeAmount = 0;
    public float shakeAmountO = 0;
    public float readyShakeAmount = 0;
    public float readyShakeAmountO = 0;
    private float idleFrontAmount;
    private float idleFrontAmountO;
    private float idleBackAmount;
    private float idleBackAmountO;

    public BartenderEntity(EntityType<BartenderEntity> type, Level level) {
        super(type, level);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
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

    public void alertCustomerOrdered() {

    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Override
    public Brain<BartenderEntity> getBrain() {
        return (Brain<BartenderEntity>) super.getBrain();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("creakingBrain");
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
        if (itemInHand.has(DataComponentTypeRegistries.BAR_AREA) && getOwner() != null && player.is(getOwner())) {
            player.sendOverlayMessage(Component.literal("Set work area success"));
            Optional.ofNullable(itemInHand.get(DataComponentTypeRegistries.BAR_AREA)).ifPresent(barArea -> {
                // TODO Reachable Prediction
                if (barArea.area().getCenter().distManhattan(this.blockPosition()) < 200 && level().dimension().equals(barArea.dimension())) {
                    this.getBrain().setMemory(Memories.BAR_DATA.get(), BarAreaHelper.calculateBarData(barArea.area(), level()));
                }
            });
        }
        else if (itemInHand.is(ItemRegistries.MENU)) {
            int count = itemInHand.getCount();
            this.insertItem(itemInHand);
            if (player.isCreative()) {
                itemInHand.setCount(count);
            }
        }
        else if (itemInHand.is(Items.STICK)) {
            setOwner(player);
            player.sendOverlayMessage(Component.literal("Now you are the owner"));
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

    @Override
    public void tick() {
        super.tick();
        updateReady();
        updateShake();
        updateIdle();
    }

    public void updateIdle() {
        this.idleFrontAmountO = this.idleFrontAmount;
        this.idleBackAmountO = this.idleBackAmount;
        if (this.getState().equals(AnimState.IDLE_FRONT)) {
            this.idleFrontAmount = Mth.clamp(this.idleFrontAmount + 2 / 20F, 0 , 1);
        } else {
            this.idleFrontAmount = Mth.clamp(this.idleFrontAmount - 4 / 20F, 0 , 1);
        }

        if (this.getState().equals(AnimState.IDLE_BACK)) {
            this.idleBackAmount = Mth.clamp(this.idleBackAmount + 2 / 20F, 0 , 1);
        } else {
            this.idleBackAmount = Mth.clamp(this.idleBackAmount - 4 / 20F, 0 , 1);
        }
    }

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
    }

    public void updateShake() {
        this.shakeAmountO = this.shakeAmount;
        if (this.getState().equals(AnimState.SHAKING)) {
            this.shakeAmount = this.shakeAmount + 1/20F;
        } else {
            this.shakeAmount = 0;
        }
    }

    public float getShakeAmount(float partialTicks) {
        return Mth.lerp(partialTicks, this.shakeAmountO, this.shakeAmount);
    }

    public float getReadyShakeAmount(float partialTicks) {
        return Mth.lerp(partialTicks, this.readyShakeAmountO, this.readyShakeAmount);
    }

    public float getIdleFrontAmount(float partialTicks) {
        return Mth.lerp(partialTicks, this.idleFrontAmountO, this.idleFrontAmount);
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

    public float getIdleBackAmount(float partialTicks) {
        return Mth.lerp(partialTicks, this.idleBackAmountO, this.idleBackAmount);
    }

    public enum AnimState {
        DEFAULT,
        READY_TO_SHAKE,
        SHAKING,
        IDLE_FRONT,
        IDLE_BACK
        ;
        public static AnimState from(int i) {
            if (i < 0 || i >= values().length) {
                LogUtils.getLogger().error("Invalid animation state");
                return DEFAULT;
            }
            return AnimState.values()[i];
        }
    }
}
