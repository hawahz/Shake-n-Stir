package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.item.MenuItem;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class PutMenu {
    public static <T extends Mob> OneShot<T> create() {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.present(Memories.BAR_MEMORY.get()),
                                i.absent(Memories.MENU.get()),
                                i.present(MemoryModuleType.INTERACTION_TARGET),
                                i.present(MemoryModuleType.VISITED_BLOCK_POSITIONS) // 可用的吧台方块
                        )
                        .apply(
                                i,
                                (barMemory, menu, interactionTarget, visitedBlockPositions) -> (level, body, timestamp) -> {
                                    if (isHoldingMenu(body)) {
                                        List<GlobalPos> list = i.get(visitedBlockPositions).stream().sorted(
                                                (pos1, pos2) -> (int) (pos1.pos().distSqr(i.get(interactionTarget).blockPosition()) - pos2.pos().distSqr(i.get(interactionTarget).blockPosition()))
                                        ).toList();
                                        ItemStack item = body.getMainHandItem();
                                        BlockItem blockItem = (BlockItem) item.getItem();
                                        for (GlobalPos globalPos : list) {
                                            BlockHitResult blockHitResult = new BlockHitResult(globalPos.pos().above().getBottomCenter(), Direction.UP, globalPos.pos(), false);
                                            if (blockItem.place(new BlockPlaceContext(level, null, InteractionHand.MAIN_HAND, item, blockHitResult)).consumesAction()) {
                                                menu.set(globalPos);
                                                return true;
                                            }
                                        }
                                    }
                                    return false;
                                }
                        )
        );
    }

    private static boolean isHoldingMenu(Mob body) {
        return body.getMainHandItem().getItem() instanceof MenuItem;
    }
}
