package io.github.hawah.shakenstir.content.dataAttachment;

import com.mojang.serialization.Codec;
import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class DataAttachmentTypeRegistries {
    public static final DeferredRegister<AttachmentType<?>> DATA_ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ShakenStir.MODID);

    public static final Supplier<AttachmentType<DeferredDamageAttachment>> DEFERRED_DEAD = DATA_ATTACHMENT_TYPES.register(
            "deferred_dead", () -> AttachmentType.serializable(DeferredDamageAttachment::empty).build());
    public static final Supplier<AttachmentType<Integer>> FALL_DOWN = DATA_ATTACHMENT_TYPES.register(
            "falldown", () -> AttachmentType.builder(() -> -1).serialize(Codec.INT.fieldOf("sns_falldown")).build());
    public static final Supplier<AttachmentType<String>> BRAIN_STATE = DATA_ATTACHMENT_TYPES.register(
            "falldistance", () -> AttachmentType.builder(() -> "").serialize(Codec.string(0, 128).fieldOf("brain_state")).sync(ByteBufCodecs.stringUtf8(128)).build());

    public static void register(IEventBus eventBus) {
        DATA_ATTACHMENT_TYPES.register(eventBus);
    }
}
