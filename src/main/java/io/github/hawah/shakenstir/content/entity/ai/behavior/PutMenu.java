package io.github.hawah.shakenstir.content.entity.ai.behavior;

import com.mojang.authlib.GameProfile;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.BarData;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.MenuItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.Comparator;
import java.util.Optional;

public class PutMenu {
    public static <T extends BartenderEntity> OneShot<T> create() {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.present(Memories.BAR_MEMORY.get()),
                                i.absent(Memories.MENU.get()),
                                i.present(MemoryModuleType.INTERACTION_TARGET)
                        )
                        .apply(
                                i,
                                (barMemory, menu, interactionTarget) -> (level, body, _) -> {
                                    body.tryGetOnHand(ItemRegistries.MENU);
                                    if (isHoldingMenu(body)) {
                                        BarData barData = i.get(barMemory);
                                        LivingEntity guy = i.get(interactionTarget);
                                        Optional<BlockPos> closest = barData.barCounter().stream().min(Comparator.comparing(pos -> pos.distSqr(guy.blockPosition())));
                                        ItemStack item = body.getMainHandItem();
                                        BlockItem blockItem = (BlockItem) item.getItem();
                                        return closest.map(pos -> {
                                            BlockHitResult blockHitResult = new BlockHitResult(pos.above().getBottomCenter(), Direction.UP, pos, false);
                                            item.set(DataComponentTypeRegistries.PLACER, body.getUUID());
                                            FakePlayer fakePlayer = new FakePlayer(level, new GameProfile(body.getUUID(), body.getName().getString()));
                                            int northSouthRank = 0;
                                            int eastWestRank = 0;
                                            for (Direction dir : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
                                                BlockPos relative = pos.relative(dir);
                                                if (barData.barCounter().stream().anyMatch(relative::equals)) {
                                                    eastWestRank += Math.abs(dir.getStepZ());
                                                    northSouthRank += Math.abs(dir.getStepX());
                                                }
                                            }

                                            if (northSouthRank >= eastWestRank) {
                                                if (guy.getZ() > pos.getZ()) {
                                                    fakePlayer.setYRot(180); // 玩家朝NORTH，菜单面SOUTH
                                                } else {
                                                    fakePlayer.setYRot(0);   // 玩家朝SOUTH，菜单面NORTH
                                                }
                                            }else {
                                                if (guy.getX() > pos.getX()) {
                                                    fakePlayer.setYRot(90);  // 玩家朝WEST，菜单面EAST
                                                } else {
                                                    fakePlayer.setYRot(-90); // 玩家朝EAST，菜单面WEST
                                                }
                                            }

                                            if (blockItem.place(new BlockPlaceContext(level, fakePlayer, InteractionHand.MAIN_HAND, item, blockHitResult)).consumesAction()) {
                                                menu.set(new GlobalPos(barData.dimension(), pos));
                                                body.swing(InteractionHand.MAIN_HAND, true);
                                                item.remove(DataComponentTypeRegistries.PLACER);
                                                return true;
                                            }
                                            return false;
                                        }).orElse(false);
                                    }
                                    return false;
                                }
                        )
        );
    }

    private static boolean isHoldingMenu(Mob body) {
        return !body.getMainHandItem().isEmpty() && body.getMainHandItem().getItem() instanceof MenuItem;
    }
}
