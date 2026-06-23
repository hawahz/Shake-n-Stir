package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.state.BlockState;

public class CollapseMenu {
    public static OneShot<BartenderEntity> create() {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.present(Memories.BAR_MEMORY.get()),
                                i.present(Memories.MENU.get()),
                                i.present(Memories.IDLE_TIME.get()),
                                i.absent(MemoryModuleType.INTERACTION_TARGET),
                                i.absent(Memories.IDLING.get())
                        )
                        .apply(
                                i,
                                (_, menu, idleTime, _, _) -> (level, body, _) -> {
                                    if (body.tickCount - i.get(idleTime) < 1200) {
                                        return false;
                                    }
                                    GlobalPos globalPos = i.get(menu);
                                    menu.erase();
                                    if (globalPos.dimension() == level.dimension()) {
                                        BlockState state = level.getBlockState(globalPos.pos());
                                        if (!state.is(BlockRegistries.BAR_MENU_BLOCK)) {
                                            return true;
                                        }
                                        level.removeBlock(globalPos.pos(), false);
                                        return true;
                                    }
                                    return false;
                                }
                        )
        );
    }
}
