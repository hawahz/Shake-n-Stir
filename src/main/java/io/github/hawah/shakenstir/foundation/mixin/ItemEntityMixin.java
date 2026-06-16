package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.content.item.StackedMintItem;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Final
    @Shadow
    private static EntityDataAccessor<ItemStack> DATA_ITEM;
    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
    public void shakeNStir$setItem(ItemStack itemStack, CallbackInfo ci) {
        if (itemStack.getItem() instanceof StackedMintItem) {
            ci.cancel();
            // TODO
            ((ItemEntity) (Object) this).getEntityData().set(DATA_ITEM, itemStack);
        }
    }
}
