package io.github.hawah.shakenstir.foundation.event;

import java.util.List;

/**
 * SNS 事件系统的公共 API 入口。
 * <p>
 * 提供静态方法用于广播事件、注册处理器和查询事件系统状态。
 * 所有方法都委托给底层的 {@link SnsEventBus} 实现。
 *
 * <h3>零运行时扫描</h3>
 * 处理器类通过在类上标注 {@link RegisterEvent} 在编译期被发现。
 * 构建时生成的 SPI 文件在初始化时被 {@link EventHandlerLoader} 加载，
 * 消除了运行时的类路径扫描。
 *
 * <h3>基本用法</h3>
 * <pre>{@code
 * // 1. 定义事件类
 * @SnsRegisterEvent
 * public class PlayerJumpEvent extends AbstractSnsEvent {
 *     private final Player player;
 *     public PlayerJumpEvent(Player player) { this.player = player; }
 *     public Player getPlayer() { return player; }
 * }
 *
 * // 2. 定义处理器类
 * @RegisterEvent        // ← 编译期发现
 * public class MyEventHandlers {
 *     @EventHandler    // ← 运行时注册
 *     public static void onPlayerJump(PlayerJumpEvent event) {
 *         if (event.getPlayer().isSneaking()) {
 *             event.setCanceled(true);
 *         }
 *     }
 * }
 *
 * // 3. 广播事件
 * PlayerJumpEvent event = new PlayerJumpEvent(player);
 * SnsEvents.post(event);
 * if (event.isCanceled()) {
 *     // 事件被取消，跳过原始行为
 * }
 * }</pre>
 *
 * @see SnsEventBus
 * @see IEvent
 * @see SnsRegisterEvent
 * @see EventHandler
 * @see RegisterEvent
 */
// TODO: 人工审查 | 2026-06-29 | Claude Code | 类型:重构
// 概述: (1) 新增 register(Class) 方法 (委托 SnsEventBus.registerHandlerClass)。
//        (2) 新增 getHandlerCount(), getHandlerInfo() 方法 (替代旧命名)。
//        (3) getSubscriberCount(), getSubscriberInfo() 标记 @Deprecated 向后兼容。
//        (4) Javadoc 更新为 SPI 加载流程说明。
// 涉及: 新增 register(), getHandlerCount(), getHandlerInfo(); 旧方法 @Deprecated
// 原状: 仅有 post(), isInitialized(), getRegisteredEventTypeCount(), getSubscriberCount(), getSubscriberInfo()
@SuppressWarnings("ALL")
public final class SnsEvents {
    private SnsEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 广播事件到所有匹配的处理器。
     * <p>
     * 事件必须实现 {@link IEvent} 接口，且事件类（或其祖先类型）
     * 须被 {@link SnsRegisterEvent} 标注，否则抛出 {@link IllegalArgumentException}。
     * <p>
     * 处理器按优先级从高到低依次执行。如果事件实现了 {@link ICancelable}，
     * 处理器可以通过 {@link ICancelable#setCanceled(boolean)} 取消事件。
     *
     * @param <T>   事件类型（必须实现 {@link IEvent}）
     * @param event 要广播的事件对象
     * @return 传入的事件对象（便于链式调用和检查取消状态）
     * @throws IllegalArgumentException 如果事件未实现 IEvent 或未注册
     */
    public static <T> T post(T event) {
        return SnsEventBus.post(event);
    }

    /**
     * 注册一个处理器类，扫描其中所有标注 {@link EventHandler}
     * 的静态方法。
     * <p>
     * 通常不需要手动调用——处理器类会通过 SPI 文件在初始化时自动注册。
     *
     * @param clazz 包含处理器方法的类
     * @return 成功注册的处理器方法数量
     */
    public static int register(Class<?> clazz) {
        return SnsEventBus.registerHandlerClass(clazz);
    }

    /**
     * 检查事件总线是否已完成初始化。
     */
    public static boolean isInitialized() {
        return SnsEventBus.isInitialized();
    }

    /**
     * 获取已注册的事件类型数量。
     */
    public static int getRegisteredEventTypeCount() {
        return SnsEventBus.getRegisteredEventTypeCount();
    }

    /**
     * 获取已注册的处理器总数。
     */
    public static int getHandlerCount() {
        return SnsEventBus.getHandlerCount();
    }

    /**
     * @deprecated 使用 {@link #getHandlerCount()}
     */
    @Deprecated
    public static int getSubscriberCount() {
        return SnsEventBus.getSubscriberCount();
    }

    /**
     * 获取指定事件类型的所有处理器信息（用于调试）。
     *
     * @param eventType 事件类型
     * @return 处理器信息列表，每项格式为 "PRIORITY: ClassName.methodName()"
     */
    public static List<String> getHandlerInfo(Class<?> eventType) {
        return SnsEventBus.getHandlerInfo(eventType);
    }

    /**
     * @deprecated 使用 {@link #getHandlerInfo(Class)}
     */
    @Deprecated
    public static List<String> getSubscriberInfo(Class<?> eventType) {
        return SnsEventBus.getSubscriberInfo(eventType);
    }
}
