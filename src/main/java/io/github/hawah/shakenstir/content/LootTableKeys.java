package io.github.hawah.shakenstir.content;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("SameParameterValue")
public class LootTableKeys {
    private static final Set<ResourceKey<LootTable>> LOCATIONS = new HashSet<>();
    private static final Set<ResourceKey<LootTable>> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);
    public static final ResourceKey<LootTable> HARVEST_MINT_PLANT = register("harvest/mint_plant");


    private static ResourceKey<LootTable> register(String location) {
        return register(ResourceKey.create(Registries.LOOT_TABLE, ShakenStir.asResource(location)));
    }

    private static ResourceKey<LootTable> register(ResourceKey<LootTable> location) {
        if (LOCATIONS.add(location)) {
            return location;
        } else {
            throw new IllegalArgumentException(location.identifier() + " is already a registered built-in loot table");
        }
    }
    public static Set<ResourceKey<LootTable>> all() {
        return IMMUTABLE_LOCATIONS;
    }
}
