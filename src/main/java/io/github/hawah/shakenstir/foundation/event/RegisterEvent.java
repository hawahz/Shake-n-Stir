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
// TODO: 人工审查 | 2026-06-29 | Claude Code | 类型:新文件
// 概述: 创建 @RegisterEvent 注解 (RetentionPolicy.CLASS)。标注在处理器类上，
//        供 Gradle generateEventSpi task 在编译期扫描源文件发现，生成 SPI 文件。
//        此注解不保留到运行时，强制走 SPI 加载路径，实现零运行时类路径扫描。
// 涉及: build.gradle generateEventSpi task, EventHandlerLoader SPI 加载
// 原状: 无 (新文件) — 此前无编译期发现机制，依赖 SnsEventBus 运行时 ClassLoader.getResources() 扫描
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RegisterEvent {
}
