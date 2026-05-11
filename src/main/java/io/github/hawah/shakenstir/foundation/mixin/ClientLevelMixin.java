package io.github.hawah.shakenstir.foundation.mixin;

import io.github.hawah.shakenstir.client.GlasswareRaycast;
import io.github.hawah.shakenstir.content.block.Glassware;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Redirect(
            method = "addDestroyBlockEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
            )
    )
    private VoxelShape redirectGetShape(BlockState blockState, BlockGetter blockGetter, BlockPos pos) {
        if (blockState.getBlock() instanceof Glassware && blockGetter.getBlockEntity(pos) instanceof GlasswareBlockEntity blockEntity) {
            return Optional.ofNullable(GlasswareRaycast.getShape()).orElse(Shapes.empty());
        }
        return blockState.getShape(blockGetter, pos);
    }
}
