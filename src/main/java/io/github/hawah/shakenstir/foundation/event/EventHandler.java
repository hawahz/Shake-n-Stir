package io.github.hawah.shakenstir.foundation.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在静态方法上，用于将该方法注册为事件处理器。
 * <p>
 * 此注解的 Retention 为 {@link RetentionPolicy#RUNTIME}，事件总线在
 * {@link SnsEventBus#registerHandlerClass(Class)} 时通过反射扫描并注册。
 * <p>
 * 被标注的方法必须满足以下条件：
 * <ul>
 *   <li>必须是 {@code public static} 方法</li>
 *   <li>返回值必须是 {@code void}</li>
 *   <li>有且仅有一个参数，该参数类型必须是实现了 {@link IEvent} 的事件类</li>
 * </ul>
 *
 * <pre>{@code
 * @RegisterEvent
 * public class MyEventHandlers {
 *     @EventHandler(priority = EventPriority.HIGH)
 *     public static void onPlayerJump(PlayerJumpEvent event) {
 *         event.setCanceled(true);
 *     }
 *
 *     @EventHandler(side = Side.CLIENT)
 *     public static void onRenderOverlay(PlayerJumpEvent event) {
 *         // client-only handling
 *     }
 * }
 * }</pre>
 *
 * @see RegisterEvent
 * @see EventPriority
 * @see Side
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    /**
     * 处理器的优先级。默认为 {@link EventPriority#NORMAL}。
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * 处理器所在端。默认为 {@link Side#COMMON}（两端通用）。
     */
    Side side() default Side.COMMON;
}
