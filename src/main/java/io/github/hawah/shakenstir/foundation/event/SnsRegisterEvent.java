package io.github.hawah.shakenstir.foundation.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在事件类上，用于将该事件类注册到 SNS 事件系统中。
 * <p>
 * 只有被此注解标注的事件类才能通过 {@link SnsEvents#post(Object)} 进行广播。
 * 未标注此注解的事件类在 post 时会抛出 {@link IllegalArgumentException}。
 * <p>
 * 通过 {@link #value()} 可以指定事件的有效端：
 * <ul>
 *   <li>{@link Side#COMMON} — 两端通用（默认）</li>
 *   <li>{@link Side#CLIENT} — 仅物理客户端，服务端自动忽略，避免 {@link NoClassDefFoundError}</li>
 *   <li>{@link Side#SERVER} — 仅物理服务端，客户端自动忽略</li>
 * </ul>
 *
 * <pre>{@code
 * // 客户端专用事件
 * @SnsRegisterEvent(side = Side.CLIENT)
 * public class RenderTickEvent extends AbstractSnsEvent { ... }
 *
 * // 服务端专用事件
 * @SnsRegisterEvent(side = Side.SERVER)
 * public class PlayerDataSaveEvent extends AbstractSnsEvent { ... }
 *
 * // 通用事件
 * @SnsRegisterEvent
 * public class PlayerJumpEvent extends AbstractSnsEvent { ... }
 * }</pre>
 *
 * @see SnsEvents
 * @see Side
 */
// TODO: 人工审查 | 2026-06-29 | IDE Linter + Claude Code | 类型:参数改名
// 概述: IDE Linter 将 side() 参数改为 value() (Java 注解惯例: 唯一参数用 value)。
//        所有引用 annotation.side() 的代码已同步更新为 annotation.value()。
//        ⚠ Javadoc 示例中仍使用 side=Side.CLIENT 旧语法，需人工更新示例。
// 涉及: SnsEventBus.registerEventType(), SnsEventBus.registerHandlerMethods()
// 原状: @interface SnsRegisterEvent { Side side() default Side.COMMON; }
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SnsRegisterEvent {
    /**
     * 事件所在端。默认为 {@link Side#COMMON}（两端通用）。
     * <p>
     * 如果指定端与当前运行时的物理端不匹配，事件类型和对应订阅者会被静默忽略。
     *
     * @return 事件有效端
     */
    Side value() default Side.COMMON;
}
