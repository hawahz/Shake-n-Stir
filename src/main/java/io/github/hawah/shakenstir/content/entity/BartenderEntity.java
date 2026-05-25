package io.github.hawah.shakenstir.content.entity;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

public class BartenderEntity extends PathfinderMob {

    public static final EntityDataAccessor<Integer> DATA_ACCESSOR =
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

    public BartenderEntity(EntityType<BartenderEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACCESSOR, 0);
        builder.define(DATA_SHOULDER_PARROT_RIGHT, OptionalInt.empty());
        builder.define(DATA_SHOULDER_PARROT_LEFT, OptionalInt.empty());
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

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new MoveToBlockGoal(this, 1, 16) {
            @Override
            protected boolean isValidTarget(LevelReader level, BlockPos pos) {
                return level.getBlockState(pos).is(BlockRegistries.SHAKE_BLOCK);
            }
        });
    }

    @Override
    public void aiStep() {
        super.aiStep();
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
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
    }
}
