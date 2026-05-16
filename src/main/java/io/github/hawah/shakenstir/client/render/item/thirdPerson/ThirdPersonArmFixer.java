package io.github.hawah.shakenstir.client.render.item.thirdPerson;

import io.github.hawah.shakenstir.client.ClientSharedShakeParams;
import io.github.hawah.shakenstir.content.item.ShakeItem;
import io.github.hawah.shakenstir.util.Result;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ThirdPersonArmFixer {

    public static List<BiFunction<AvatarRenderState, ModelPart, Result>> rightArmModifiers = new ArrayList<>();
    public static List<BiFunction<AvatarRenderState, ModelPart, Result>> leftArmModifiers = new ArrayList<>();
    public static void onRenderRightArm(ModelPart rightArm, HumanoidRenderState state) {
        if (!(state instanceof AvatarRenderState avatarRenderState)) {
            return;
        }
        for (BiFunction<AvatarRenderState, ModelPart, Result> rightArmModifier : rightArmModifiers) {
            Result result = rightArmModifier.apply(avatarRenderState, rightArm);
            if (result.cancelled()) {
                return;
            }
        }
    }
    public static void onRenderLeftArm(ModelPart leftArm, HumanoidRenderState state) {
        if (!(state instanceof AvatarRenderState avatarRenderState)) {
            return;
        }
        for (BiFunction<AvatarRenderState, ModelPart, Result> leftArmModifier : leftArmModifiers) {
            Result result = leftArmModifier.apply(avatarRenderState, leftArm);
            if (result.cancelled()) {
                return;
            }
        }
    }

    static {
        rightArmModifiers.add(
                ThirdPersonArmFixer::modifyShakeItemRight
        );
        leftArmModifiers.add(
                ThirdPersonArmFixer::modifyShakeItemLeft
        );
    }

    private static Result modifyShakeItemRight(AvatarRenderState state, ModelPart arm) {
        if (state.getMainHandItemStack().getItem() instanceof ShakeItem && state.isUsingItem) {
            int id = state.id;
            double x = 1-ClientSharedShakeParams.x(id);
            arm.xRot = (float) Math.toRadians(-150 + x * 20);
            arm.yRot = (float) Math.toRadians(-20);
            arm.y = 2f;
            arm.x = (float) (-5f + (x - 2f)/3);
            return new Result(true);
        }
        return Result.empty();
    }
    private static Result modifyShakeItemLeft(AvatarRenderState state, ModelPart arm) {
        if (state.getMainHandItemStack().getItem() instanceof ShakeItem && state.isUsingItem) {
            int id = state.id;
            double x = 1-ClientSharedShakeParams.x(id);
            arm.xRot = (float) Math.toRadians(-120 + x * 18);
            double newX = (-x + 2)/3.5 * 2;
            arm.yRot = (float) Math.toRadians(5 + newX * 15);
            arm.y = 0;
            arm.yScale = 1.3f;
            arm.x -= (float) ((x - 2f)/3);
            return new Result(true);
        }
        return Result.empty();
    }
}
