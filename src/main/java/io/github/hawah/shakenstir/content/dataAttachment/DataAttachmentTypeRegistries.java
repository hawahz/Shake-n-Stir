package io.github.hawah.shakenstir.content.dataAttachment;

import io.github.hawah.shakenstir.ShakenStir;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class DataAttachmentTypeRegistries {
    public static final DeferredRegister<AttachmentType<?>> DATA_ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ShakenStir.MODID);

    public static final Supplier<AttachmentType<DeferredDamageAttachment>> DEFERRED_DEAD = DATA_ATTACHMENT_TYPES.register(
            "dead", () -> AttachmentType.serializable(DeferredDamageAttachment::empty).build());

    public static void register(IEventBus eventBus) {
        DATA_ATTACHMENT_TYPES.register(eventBus);
    }
}
