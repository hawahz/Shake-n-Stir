package io.github.hawah.shakenstir.content.entity.ai.behavior;

import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.item.ItemStack;

public class HideItemInHand {
    public static OneShot<BartenderEntity> create() {
        return BehaviorBuilder.create(
                i -> i.group(
                                i.present(Memories.BAR_MEMORY.get()),
                                i.present(Memories.MENU.get())
                        )
                        .apply(
                                i,
                                (barMemory, menu) -> (level, body, timestamp) -> {
                                    ItemStack itemInHand = body.getItemInHand(InteractionHand.MAIN_HAND);
                                    if (itemInHand.isEmpty()) {
                                        return true;
                                    }
                                    if (body.insertItem(itemInHand)) {
                                        body.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                        return true;
                                    }
                                    return false;
                                }
                        )
        );
    }
}
