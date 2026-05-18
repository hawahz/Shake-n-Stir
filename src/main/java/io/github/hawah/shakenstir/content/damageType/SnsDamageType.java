package io.github.hawah.shakenstir.content.damageType;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class SnsDamageType {
    public static final ResourceKey<DamageType> PARALYSIS =
            ResourceKey.create(Registries.DAMAGE_TYPE, ShakenStir.asResource("paralysis"));
}
