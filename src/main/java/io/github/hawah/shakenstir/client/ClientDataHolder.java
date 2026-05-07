package io.github.hawah.shakenstir.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientDataHolder {

    public static void tick() {
        Picker.tick();
    }

    public static class Picker {
        public static HitResult hitResult = null;
        public static BlockPos pos() {
            if (hitResult == null)
                return null;
            return hitResult instanceof BlockHitResult blockHitResult ?
                    blockHitResult.getBlockPos() :
                    BlockPos.containing(hitResult.getLocation());
        }

        public static Vec3 location() {
            if (hitResult == null)
                return null;
            return hitResult.getLocation();
        }

        public static Direction direction() {
            if (hitResult == null)
                return null;
            return hitResult instanceof BlockHitResult blockHitResult ?
                    blockHitResult.getDirection() :
                    Direction.fromYRot(hitResult.getLocation().y);
        }

        public static HitResult.Type type() {
            if (hitResult == null)
                return HitResult.Type.MISS;
            return hitResult.getType();
        }

        public static Optional<BlockState> blockState() {
            if (hitResult == null) {
                return Optional.empty();
            }
            return hitResult instanceof BlockHitResult blockHitResult ?
                    Optional.of(Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos())) :
                    Optional.empty();
        }

        public static Optional<Block> block() {
            if (hitResult == null) {
                return Optional.empty();
            }
            return hitResult instanceof BlockHitResult blockHitResult ?
                    Optional.of(Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos()).getBlock()) :
                    Optional.empty();
        }

        public static void tick() {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
//            if (!(player.getMainHandItem().getItem() instanceof IPickMarkedItem)) {
//                return;
//            }

            hitResult = player.pick(4.5D, 0.0F, false);
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        tick();
    }
}
