package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.content.entity.EntityTypeRegistries;
import net.minecraft.world.item.SpawnEggItem;

public class BartenderSpawner extends SpawnEggItem {
    public BartenderSpawner(Properties properties) {
        super(properties.spawnEgg(EntityTypeRegistries.BARTENDER.get()));
    }
}
