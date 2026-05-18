package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SoberingTea extends Item {
    public SoberingTea(Properties properties) {
        super(properties.stacksTo(1)
                .food(Foods.SUSPICIOUS_STEW)
                .usingConvertsTo(Items.BOWL));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        entity.removeEffect(MobEffectRegistries.DRUNK);
        return super.finishUsingItem(itemStack, level, entity);
    }
}
