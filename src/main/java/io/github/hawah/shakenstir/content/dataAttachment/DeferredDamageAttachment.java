package io.github.hawah.shakenstir.content.dataAttachment;

import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public final class DeferredDamageAttachment implements ValueIOSerializable {
    private Holder<DamageType> type;
    private @Nullable UUID causingEntity;
    private @Nullable UUID directEntity;
    private @Nullable Vec3 damageSourcePosition;

    public DeferredDamageAttachment(
            Holder<DamageType> type,
            @Nullable UUID causingEntity,
            @Nullable UUID directEntity,
            @Nullable Vec3 damageSourcePosition
    ) {
        this.type = type;
        this.causingEntity = causingEntity;
        this.directEntity = directEntity;
        this.damageSourcePosition = damageSourcePosition;
    }

    public DeferredDamageAttachment(DamageSource damageSource) {
        this(
                damageSource.typeHolder(),
                damageSource.getEntity() == null ? null : damageSource.getEntity().getUUID(),
                damageSource.getDirectEntity() == null ? null : damageSource.getDirectEntity().getUUID(),
                damageSource.sourcePositionRaw()
        );
    }

    public static DeferredDamageAttachment empty() {
        return new DeferredDamageAttachment(null, null, null, null);
    }


    @Override
    public void serialize(ValueOutput output) {
        Holder<DamageType> type = type();
        UUID entity = causingEntity();
        UUID directEntity = directEntity();
        Vec3 sourcePosition = damageSourcePosition();
        output.store("DamageType", DamageType.CODEC, type);
        if (entity != null) {
            output.store("Entity", UUIDUtil.CODEC, entity);
        }
        if (directEntity != null) {
            output.store("DirectEntity", UUIDUtil.CODEC, directEntity);
        }
        if (sourcePosition != null) {
            output.store("SourcePosition", Vec3.CODEC, sourcePosition);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        this.type = input.read("DamageType", DamageType.CODEC).orElseThrow();
        causingEntity = input.read("Entity", UUIDUtil.CODEC).orElse(null);
        directEntity = input.read("DirectEntity", UUIDUtil.CODEC).orElse(null);
        damageSourcePosition = input.read("SourcePosition", Vec3.CODEC).orElse(null);
    }

    public boolean isReady() {
        return type != null;
    }

    public Holder<DamageType> type() {
        return type;
    }

    public @Nullable UUID causingEntity() {
        return causingEntity;
    }

    public @Nullable UUID directEntity() {
        return directEntity;
    }

    public @Nullable Vec3 damageSourcePosition() {
        return damageSourcePosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DeferredDamageAttachment) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.causingEntity, that.causingEntity) &&
                Objects.equals(this.directEntity, that.directEntity) &&
                Objects.equals(this.damageSourcePosition, that.damageSourcePosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, causingEntity, directEntity, damageSourcePosition);
    }

    @Override
    public String toString() {
        return "DeferredDamageAttachment[" +
                "type=" + type + ", " +
                "causingEntity=" + causingEntity + ", " +
                "directEntity=" + directEntity + ", " +
                "damageSourcePosition=" + damageSourcePosition + ']';
    }

    public Entity getCausingEntity(ServerLevel level) {
        return causingEntity() == null? null: level.getEntity(causingEntity);
    }

    public Entity getDirectEntity(ServerLevel level) {
        return directEntity() == null? null: level.getEntity(directEntity);
    }
    public DamageSource toDamageSource(ServerLevel level) {
        return new DamageSource(type, getDirectEntity(level), getCausingEntity(level), damageSourcePosition);
    }

}
