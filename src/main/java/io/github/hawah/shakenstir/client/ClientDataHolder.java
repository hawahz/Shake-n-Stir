package io.github.hawah.shakenstir.client;

import io.github.hawah.shakenstir.content.block.Shake;
import io.github.hawah.shakenstir.lib.client.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.intellij.lang.annotations.MagicConstant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientDataHolder {

    public static void tick() {
        Picker.tick();
    }

    public static class Picker {
        public static final int FLAG_BLOCK_POS = 1;
        public static final int FLAG_DIRECTION = 2;
        public static final int FLAG_BLOCK_STATE = 4;
        @MagicConstant(flags = {FLAG_BLOCK_POS, FLAG_DIRECTION, FLAG_BLOCK_STATE})
        public @interface CachedFlags {}
        public static @CachedFlags int cachedFlags = 0;
        public static @Nullable BlockPos cachedPos = null;
        public static @Nullable Direction cachedDirection = null;
        public static @Nullable BlockState cachedBlockState = null;
        
        public static HitResult hitResult() {
            return Minecraft.getInstance().hitResult;
        }
        public static @Nullable BlockPos pos() {
            if ((cachedFlags & FLAG_BLOCK_POS) != 0) {
                return cachedPos;
            }
            cachedFlags |= FLAG_BLOCK_POS;
            if (hitResult() == null) {
                cachedPos = null;
                return null;
            }
            return cachedPos = hitResult() instanceof BlockHitResult blockHitResult ?
                    blockHitResult.getBlockPos() :
                    BlockPos.containing(hitResult().getLocation());
        }

        public static @Nullable Vec3 location() {
            if (hitResult() == null) {
                return null;
            }
            return hitResult().getLocation();
        }

        public static @Nullable Direction direction() {
            if (hitResult() == null)
                return null;
            return hitResult() instanceof BlockHitResult blockHitResult ?
                    blockHitResult.getDirection() :
                    Direction.fromYRot(hitResult().getLocation().y);
        }

        public static @Nonnull HitResult.Type type() {
            if (hitResult() == null)
                return HitResult.Type.MISS;
            return hitResult().getType();
        }

        public static Optional<BlockState> blockState() {
            if ((cachedFlags & FLAG_BLOCK_STATE) != 0) {
                return Optional.ofNullable(cachedBlockState);
            }
            cachedFlags |= FLAG_BLOCK_STATE;
            if (hitResult() == null) {
                cachedBlockState = null;
                return Optional.empty();
            }
            cachedBlockState = pos() != null && Minecraft.getInstance().level != null && hitResult().getType().equals(HitResult.Type.BLOCK)?
                    Minecraft.getInstance().level.getBlockState(pos()):
                    null;
            return Optional.ofNullable(cachedBlockState);
        }

        public static Optional<Block> block() {
            return blockState().map(BlockBehaviour.BlockStateBase::getBlock);
        }

        public static void tick() {
            clearCache();
        }

        public static void clearCache() {
            cachedFlags = 0;
            cachedBlockState = null;
            cachedDirection = null;
            cachedPos = null;
        }
    }

    public static boolean shouldModifyView() {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().screen != null) {
            return false;
        }
        return Picker.block().isPresent() && Picker.block().get() instanceof Shake && KeyBinding.hasAltDown();
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        tick();
    }
}
