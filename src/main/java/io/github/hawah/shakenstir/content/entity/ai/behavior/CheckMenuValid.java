package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.level.block.state.BlockState;

public class CheckMenuValid {
    public static OneShot<BartenderEntity> create() {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.present(Memories.BAR_DATA.get()),
                                i.present(Memories.MENU.get()),
                                i.absent(Memories.IDLING.get())
                        )
                        .apply(
                                i,
                                (barMemory, menu, _) -> (level, body, timestamp) -> {
                                    GlobalPos globalPos = i.<GlobalPos>get(menu);
                                    if (globalPos.dimension() == level.dimension()) {
                                        BlockState state = level.getBlockState(globalPos.pos());
                                        if (!state.is(BlockRegistries.BAR_MENU_BLOCK)) {
                                            menu.erase();
                                        }
                                        return true;
                                    }
                                    return false;
                                }
                        )
        );
    }
}
