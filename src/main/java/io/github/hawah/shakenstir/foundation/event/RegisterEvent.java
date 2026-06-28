package io.github.hawah.shakenstir.foundation.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在包含事件处理器方法的类上，用于编译期自动发现。
 * <p>
 * 此注解的 Retention 为 {@link RetentionPolicy#CLASS}——它仅存在于 class 文件中供
 * 构建时扫描使用，<b>不会保留到运行时</b>。运行时通过构建阶段生成的 SPI 文件
 * ({@code META-INF/services/io.github.hawah.shakenstir.foundation.event.RegisterEvent})
 * 来发现标记的类。
 * <p>
 * 被标记的类中的具体处理器方法需标注 {@link EventHandler}。
 *
 * <pre>{@code
 * @RegisterEvent
 * public class MyEventHandlers {
 *     @EventHandler
 *     public static void onPlayerJump(PlayerJumpEvent event) {
 *         // handle event
 *     }
 * }
 * }</pre>
 *
 * @see EventHandler
 * @see EventHandlerLoader
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RegisterEvent {
}
