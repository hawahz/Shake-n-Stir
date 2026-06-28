package io.github.hawah.shakenstir.foundation.event;

/**
 * SNS 事件系统的优先级枚举。
 * <p>
 * 订阅者按优先级从高到低依次执行：
 * {@link #HIGHEST} → {@link #HIGH} → {@link #NORMAL} → {@link #LOW} → {@link #LOWEST}
 *
 */
// TODO: 人工审查 | 2026-06-29 | IDE Linter | 类型:微调
// 概述: IDE Linter 移除了 Javadoc 中的 @see SnsEvent 引用 (因 SnsEvent.java 已被删除)。
//        此枚举现被 @EventHandler 和 @SnsEvent 共同引用 (SnsEventBus 兼容双注解)。
// 涉及: Javadoc @see 标签调整
// 原状: * @see SnsEvent  (该行被 Linter 删除)
public enum EventPriority {
    /**
     * 最高优先级，最先执行。
     */
    HIGHEST,
    /**
     * 高优先级。
     */
    HIGH,
    /**
     * 普通优先级（默认值）。
     */
    NORMAL,
    /**
     * 低优先级。
     */
    LOW,
    /**
     * 最低优先级，最后执行。
     */
    LOWEST
}
