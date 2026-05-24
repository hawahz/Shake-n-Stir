package io.github.hawah.shakenstir.client.render.item.thirdPerson;

import io.github.hawah.shakenstir.client.ClientSharedShakeParams;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.ShakeItem;
import io.github.hawah.shakenstir.foundation.utils.ShakeAnimationAccessor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ThirdPersonArmFixer {

    public static void onModifyModelPose(HumanoidRenderState state, HumanoidModel<?> model) {
        if (!(state instanceof AvatarRenderState avatarRenderState)) {
            return;
        }
        if (state.getMainHandItemStack().getItem() instanceof ShakeItem && state.isUsingItem) {
            float ticksUsingItem = state.ticksUsingItem;
            final float READY_DURATION = 10;
            final float TRANSIT_DURATION = 4;
            if (ticksUsingItem < READY_DURATION) {
                ((ShakeAnimationAccessor) model).shakeNStir$getReadyAnimation().apply((long) (ticksUsingItem / READY_DURATION * 1000), 1.0F);
                return;
            }
            int id = avatarRenderState.id;
            double x = 1-ClientSharedShakeParams.x(id);
            float y = (float) -(ClientSharedShakeParams.y(id) - 2)/4;
            double process = (x + 1) / 3;
            if (ticksUsingItem < READY_DURATION + TRANSIT_DURATION) {
                process = Mth.lerp(Ease.outSine((ticksUsingItem - READY_DURATION)/TRANSIT_DURATION), 0, process);
            }
            ((ShakeAnimationAccessor) model).shakeNStir$getShakeAnimation().apply((long) (process * 1100), y);
            ((ShakeAnimationAccessor) model).shakeNStir$getShakeUpperAnimation().apply((long) (process * 1100), 1-y);
        }
    }

    public static boolean shouldApplyArmSwing(HumanoidRenderState state,
                                              ModelPart part,
                                              float ageInTicks,
                                              float multiplier,
                                              HumanoidArm arm) {
        return !state.getMainHandItemStack().is(ItemRegistries.SHAKE) || !state.isUsingItem;
    }
}
