package io.github.hawah.shakenstir.foundation.tags;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class SnsDamageTags {
    public static final TagKey<DamageType> PARALYSIS_DEADLY_PREVENTION = create("paralysis_deadly_prevention");

    private static TagKey<DamageType> create(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, ShakenStir.asResource(name));
    }

}
