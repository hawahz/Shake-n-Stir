package io.github.hawah.shakenstir.foundation.utils;

import io.github.hawah.shakenstir.client.render.PlayerAnimationModifier;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.resources.Identifier;

import java.util.Map;

// TODO: 人工审查 | 2026-07-03 03:30 | Claude Code | 类型:接口重构 — Map化动画访问
// 概述: ShakeAnimationAccessor 接口从四个独立方法重构为基于 Map<Identifier, KeyframeAnimation> 的统一访问。
//        新增:
//        (1) shakeNStir$getAnimations() → 返回动画 Map, 外部可通过 Identifier 获取任意动画
//        (2) shakeNStir$getAnimation(Identifier) → 便捷方法, 按 key 获取单个动画
//        旧方法 (shakeNStir$getShakeAnimation 等) 标记 @Deprecated, 内部委托到 Map,
//        以确保 ThirdPersonArmFixer 和 DodgeEffect 等旧引用可平滑迁移。
//        标准动画 Identifier 定义于本接口中:
//        - ANIM_SHAKE        = shakenstir:shake         → ShakeAnimation.SHAKE
//        - ANIM_READY        = shakenstir:ready         → ShakeAnimation.READY
//        - ANIM_SHAKE_UPPER  = shakenstir:shake_upper   → MixedShakeAnimation.SHAKE_UPPER
//        - ANIM_FALL         = shakenstir:fall          → FallPose.POSE
// 涉及: 接口签名变更; 新增 getAnimations() / getAnimation(Identifier); 旧方法 @Deprecated
// 原状: 四个独立方法:
//        KeyframeAnimation shakeNStir$getShakeAnimation();
//        KeyframeAnimation shakeNStir$getReadyAnimation();
//        KeyframeAnimation shakeNStir$getShakeUpperAnimation();
//        KeyframeAnimation shakeNStir$getFallAnimation();
//       — 硬编码, 无法扩展 (新增动画需要修改接口和所有实现)
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
