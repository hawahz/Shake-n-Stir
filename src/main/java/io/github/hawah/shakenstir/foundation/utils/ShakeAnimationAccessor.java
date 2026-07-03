package io.github.hawah.shakenstir.foundation.utils;

import io.github.hawah.shakenstir.client.render.PlayerAnimationModifier;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.resources.Identifier;

import java.util.Map;

@SuppressWarnings("ALL")
public interface ShakeAnimationAccessor {

    // ===== 新版 Map 接口 =====

    /**
     * 获取所有已注册的玩家动画 Map。
     * <p>
     * Key 为 {@link Identifier}（动画标识符），Value 为已烘焙的 {@link KeyframeAnimation}。
     * 此 Map 在 HumanoidModel 构造期间通过 {@code RegisterPlayerAnimationEvent} 事件填充。
     *
     * @return 不可变的动画 Map（由实现类保证）
     */
    Map<Identifier, KeyframeAnimation> shakeNStir$getAnimations();

    /**
     * 按 {@link Identifier} 获取指定动画。
     *
     * @param id 动画标识符，使用本接口中定义的常量（如 {@link PlayerAnimationModifier#ANIM_SHAKE}）
     * @return 对应的 KeyframeAnimation，如果未注册则返回 {@code null}
     */
    default KeyframeAnimation shakeNStir$getAnimation(Identifier id) {
        return shakeNStir$getAnimations().get(id);
    }
}
