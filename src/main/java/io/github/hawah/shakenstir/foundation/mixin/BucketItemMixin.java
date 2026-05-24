package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.content.block.Distiller;
import io.github.hawah.shakenstir.content.blockEntity.DistillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class BucketItemMixin {

    @Inject(method = "emptyContents(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
    public void emptyContents(LivingEntity user, Level level, BlockPos pos, BlockHitResult hitResult, ItemStack containerItem, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        if (block instanceof Distiller && level.getBlockEntity(Distiller.findSource(blockState, pos)) instanceof DistillerBlockEntity blockEntity) {
            var containedFluidStack = containerItem != null ? net.neoforged.neoforge.transfer.fluid.FluidUtil.getFirstStackContained(containerItem) : net.neoforged.neoforge.fluids.FluidStack.EMPTY;
            blockEntity.insertFluid(containedFluidStack, false);
            cir.setReturnValue(true);
        }
    }
}
