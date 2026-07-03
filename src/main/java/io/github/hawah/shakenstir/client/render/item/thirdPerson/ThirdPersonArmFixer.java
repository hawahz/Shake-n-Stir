package io.github.hawah.shakenstir.client.render.item.thirdPerson;

import io.github.hawah.shakenstir.client.render.entity.BartenderRenderState;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;

public class ThirdPersonArmFixer {
    public static boolean shouldApplyArmSwing(HumanoidRenderState state,
                                              ModelPart part,
                                              float ageInTicks,
                                              float multiplier,
                                              HumanoidArm arm) {
        return !(state.getMainHandItemStack().is(ItemRegistries.SHAKER) && state.isUsingItem)
                && !(state instanceof BartenderRenderState bartenderRenderState
                && bartenderRenderState.animState != BartenderEntity.AnimState.DEFAULT);
    }
}
