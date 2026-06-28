package io.github.hawah.shakenstir.foundation.event;

/**
 * SNS 事件系统的优先级枚举。
 * <p>
 * 订阅者按优先级从高到低依次执行：
 * {@link #HIGHEST} → {@link #HIGH} → {@link #NORMAL} → {@link #LOW} → {@link #LOWEST}
 *
 */
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
