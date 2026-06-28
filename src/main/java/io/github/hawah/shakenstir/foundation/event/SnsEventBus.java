package io.github.hawah.shakenstir.foundation.event;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * SNS 事件系统的核心事件总线。
 * <p>
 * 负责事件类型的注册、处理器类的注册与扫描、以及事件的派发。
 * 通过 {@link SnsEvents} 提供的静态方法对外暴露功能。
 * <p>
 * <b>零运行时类路径扫描：</b>
 * 处理器类的发现由构建阶段生成的 SPI 文件驱动
 * ({@code META-INF/services/io.github.hawah.shakenstir.foundation.event.RegisterEvent})，
 * 通过 {@link EventHandlerLoader} 在初始化时加载。
 * <p>
 * <b>端感知（Side-aware）：</b>
 * 事件类型和处理器均支持端限制：
 * <ul>
 *   <li>{@link Side#CLIENT} 事件仅在物理客户端注册，服务端自动忽略</li>
 *   <li>{@link Side#SERVER} 事件仅在物理服务端注册，客户端自动忽略</li>
 *   <li>{@link Side#COMMON} 事件在两端均可注册</li>
 * </ul>
 *
 * @see IEvent
 * @see SnsRegisterEvent
 * @see EventHandler
 * @see RegisterEvent
 */
public final class SnsEventBus {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 事件类型 → 按优先级排序的处理器列表。
     */
    private static final Map<Class<?>, List<Handler>> HANDLERS = new ConcurrentHashMap<>();

    /**
     * 已注册的事件类型集合（被 {@link SnsRegisterEvent} 标注且端匹配的类）。
     */
    private static final Set<Class<?>> REGISTERED_EVENT_TYPES = ConcurrentHashMap.newKeySet();

    /**
     * 是否已初始化。
     */
    private static volatile boolean initialized = false;

    private SnsEventBus() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== 初始化 ====================

    /**
     * 初始化事件总线，通过 SPI 文件加载所有编译期发现的处理器类。
     * <p>
     * 事件类和处理器的端({@link Side})会被检查：与当前物理端不匹配的会被静默忽略。
     * 该方法只能被调用一次，重复调用会被忽略并记录警告。
     *
     * @param ignored 参数已废弃——SPI 加载不需要包名。保留签名以保证向后兼容。
     */
    @SuppressWarnings("unused")
    public static synchronized void initialize(String ignored) {
        if (initialized) {
            LOGGER.warn("SnsEventBus already initialized, skipping duplicate call.");
            return;
        }

        LOGGER.info("Initializing SnsEventBus (SPI-based, physical side: {})", Side.getPhysicalDist());
        long startTime = System.currentTimeMillis();

        int handlerClasses = EventHandlerLoader.loadAndRegister();

        initialized = true;
        long elapsed = System.currentTimeMillis() - startTime;
        LOGGER.info("SnsEventBus initialized: {} event types, {} handlers, {} handler classes ({}ms)",
                REGISTERED_EVENT_TYPES.size(), getHandlerCount(), handlerClasses, elapsed);
    }

    // ==================== 事件类型注册 ====================

    /**
     * 注册一个事件类型。
     * <p>
     * 如果类没有 {@link SnsRegisterEvent} 注解，注册会被拒绝。
     * 如果事件指定的 {@link Side} 与当前物理端不匹配，注册会被静默忽略。
     *
     * @param eventClass 事件类
     * @return {@code true} 如果事件类型被成功注册，{@code false} 如果因端不匹配而被跳过
     */
    public static boolean registerEventType(Class<?> eventClass) {
        SnsRegisterEvent annotation = eventClass.getAnnotation(SnsRegisterEvent.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    "Class " + eventClass.getName() + " is not annotated with @SnsRegisterEvent");
        }

        Side eventSide = annotation.value();
        if (!eventSide.isValidForCurrentSide()) {
            LOGGER.debug("Skipping event type {} (side: {} is not valid for current physical side: {})",
                    eventClass.getSimpleName(), eventSide, Side.getPhysicalDist());
            return false;
        }

        REGISTERED_EVENT_TYPES.add(eventClass);
        LOGGER.debug("Registered event type: {} (side: {})", eventClass.getSimpleName(), eventSide);
        return true;
    }

    /**
     * 检查给定的类是否是已注册的事件类型。
     * <p>
     * 会沿类层次结构向上查找：只要事件类本身或其任意父类/接口被注册，即视为已注册。
     */
    public static boolean isRegisteredEventType(Class<?> eventClass) {
        if (REGISTERED_EVENT_TYPES.contains(eventClass)) {
            return true;
        }
        Class<?> superClass = eventClass.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            if (REGISTERED_EVENT_TYPES.contains(superClass)) {
                return true;
            }
            superClass = superClass.getSuperclass();
        }
        for (Class<?> iface : eventClass.getInterfaces()) {
            if (REGISTERED_EVENT_TYPES.contains(iface)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 处理器类注册 ====================

    /**
     * 注册一个处理器类中所有标注了 {@link EventHandler}
     * <p>
     * 这是运行时 SPI 加载流程的核心入口。
     *
     * @param clazz 包含处理器方法的类
     * @return 成功注册的处理器方法数量
     */
    public static int registerHandlerClass(Class<?> clazz) {
        return registerHandlerMethods(clazz).registered;
    }

    /**
     * 注册一个处理器类（别名，等同于 {@link #registerHandlerClass(Class)}）。
     */
    public static int register(Class<?> clazz) {
        return registerHandlerClass(clazz);
    }

    /**
     * 处理结果：已注册和已跳过的处理器数量。
     */
    private record HandlerResult(int registered, int skipped) {
    }

    /**
     * 扫描指定类中的所有静态方法，注册被 {@link EventHandler}
     * <p>
     * 端感知检查：
     * <ol>
     *   <li>检查方法注解的 {@code side()} 是否与当前物理端兼容</li>
     *   <li>检查事件类型的 {@link SnsRegisterEvent#value()} 是否与当前物理端兼容</li>
     * </ol>
     */
    private static HandlerResult registerHandlerMethods(Class<?> clazz) {
        int registered = 0;
        int skipped = 0;
        Method[] methods;
        try {
            methods = clazz.getDeclaredMethods();
        } catch (NoClassDefFoundError e) {
            LOGGER.debug("Cannot access methods of class {}", clazz.getName());
            return new HandlerResult(0, 0);
        }

        for (Method method : methods) {
            // Support both @EventHandler (new) and @SnsEvent (legacy)
            HandlerMeta meta = extractHandlerMeta(method, clazz);
            if (meta == null) continue;

            // 验证方法签名
            String error = validateHandlerMethod(method);
            if (error != null) {
                LOGGER.error("Invalid handler method: {}.{} - {}",
                        clazz.getSimpleName(), method.getName(), error);
                continue;
            }

            // ---- 端感知检查 #1：处理器自身指定的端 ----
            if (!meta.side().isValidForCurrentSide()) {
                LOGGER.debug("Skipping handler {}.{}() — side '{}' not valid for physical side '{}'",
                        clazz.getSimpleName(), method.getName(), meta.side(), Side.getPhysicalDist());
                skipped++;
                continue;
            }

            // 获取事件参数类型
            Class<?> eventType = method.getParameterTypes()[0];

            // ---- 自动注册事件类型（如果带 @SnsRegisterEvent） ----
            SnsRegisterEvent eventAnnotation = eventType.getAnnotation(SnsRegisterEvent.class);
            if (eventAnnotation != null) {
                // 端感知检查 #2：事件类型指定的端
                if (!eventAnnotation.value().isValidForCurrentSide()) {
                    LOGGER.debug("Skipping handler {}.{}() — event type '{}' side '{}' not valid for '{}'",
                            clazz.getSimpleName(), method.getName(),
                            eventType.getSimpleName(), eventAnnotation.value(), Side.getPhysicalDist());
                    skipped++;
                    continue;
                }
                // 自动注册事件类型（幂等操作）
                REGISTERED_EVENT_TYPES.add(eventType);
            }

            // 确保方法可访问
            method.setAccessible(true);

            // 创建处理器
            Handler handler = new Handler(
                    meta.priority(),
                    clazz.getSimpleName() + "." + method.getName() + "()",
                    event -> {
                        try {
                            method.invoke(null, event);
                        } catch (Exception e) {
                            LOGGER.error("Error invoking handler {} for event {}",
                                    clazz.getSimpleName() + "." + method.getName(),
                                    event.getClass().getSimpleName(), e);
                        }
                    }
            );

            registerHandler(eventType, handler);
            registered++;
        }

        if (registered > 0 || skipped > 0) {
            LOGGER.debug("Scanned {}: {} registered, {} skipped",
                    clazz.getSimpleName(), registered, skipped);
        }
        return new HandlerResult(registered, skipped);
    }

    /**
     * 提取处理器元数据，同时支持 {@link EventHandler}
     */
    private static HandlerMeta extractHandlerMeta(Method method, Class<?> clazz) {
        EventHandler eh = method.getAnnotation(EventHandler.class);
        if (eh != null) {
            return new HandlerMeta(eh.priority(), eh.side());
        }
        return null;
    }

    private record HandlerMeta(EventPriority priority, Side side) {
    }

    /**
     * 验证处理器方法签名。
     */
    private static String validateHandlerMethod(Method method) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers)) return "method must be public";
        if (!Modifier.isStatic(modifiers)) return "method must be static";
        if (method.getReturnType() != void.class) return "method must return void";
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1)
            return "method must have exactly one parameter (the event), but has " + paramTypes.length;
        return null;
    }

    /**
     * 向指定事件类型注册一个处理器。
     */
    private static void registerHandler(Class<?> eventType, Handler handler) {
        List<Handler> list = HANDLERS.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
        list.add(handler);
        list.sort(Comparator.comparingInt(h -> -h.priority().ordinal()));
    }

    // ==================== 事件派发 ====================

    /**
     * 广播事件到所有匹配的处理器。
     * <p>
     * 派发规则：
     * <ol>
     *   <li>事件必须实现 {@link IEvent} 接口</li>
     *   <li>事件类或其祖先类型须被 {@link SnsRegisterEvent} 标注并注册</li>
     *   <li>沿类层次结构向上查找处理器，包括父类和接口</li>
     *   <li>处理器按优先级从高到低依次执行</li>
     *   <li>单个处理器的异常不会影响其他处理器</li>
     * </ol>
     *
     * @param <T>   事件类型（必须实现 {@link IEvent}）
     * @param event 事件对象
     * @return 传入的事件对象（便于链式调用）
     * @throws IllegalArgumentException 如果事件类未实现 IEvent 或未注册
     */
    public static <T> T post(T event) {
        Class<?> eventClass = event.getClass();

        // 类型门：必须是 IEvent
        if (!(event instanceof IEvent)) {
            throw new IllegalArgumentException(
                    "Event class '" + eventClass.getName() + "' does not implement IEvent. " +
                            "All event types must implement IEvent.");
        }

        // 验证事件类型是否已注册
        if (!isRegisteredEventType(eventClass)) {
            throw new IllegalArgumentException(
                    "Event class '" + eventClass.getName() + "' is not registered in the SNS event system. " +
                            "Annotate the class (or a superclass/interface) with @SnsRegisterEvent.");
        }

        // 收集已派发过的事件类型，避免重复派发
        Set<Class<?>> dispatched = new HashSet<>();

        // 沿父类链向上派发
        Class<?> current = eventClass;
        while (current != null && current != Object.class) {
            dispatchTo(current, event, dispatched);
            current = current.getSuperclass();
        }

        // 派发到接口
        for (Class<?> iface : getAllInterfaces(eventClass)) {
            dispatchTo(iface, event, dispatched);
        }

        return event;
    }

    private static <T> void dispatchTo(Class<?> eventType, T event, Set<Class<?>> dispatched) {
        if (!dispatched.add(eventType)) return;
        List<Handler> handlers = HANDLERS.get(eventType);
        if (handlers == null || handlers.isEmpty()) return;
        for (Handler handler : handlers) {
            try {
                handler.consumer().accept(event);
            } catch (Exception e) {
                LOGGER.error("Error dispatching event '{}' to handler '{}'",
                        event.getClass().getSimpleName(), handler.source(), e);
            }
        }
    }

    private static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        collectInterfaces(clazz, interfaces);
        return interfaces;
    }

    private static void collectInterfaces(Class<?> clazz, Set<Class<?>> result) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Class<?> iface : current.getInterfaces()) {
                if (result.add(iface)) {
                    collectInterfaces(iface, result);
                }
            }
            current = current.getSuperclass();
        }
    }

    // ==================== 查询与调试 ====================

    public static int getRegisteredEventTypeCount() {
        return REGISTERED_EVENT_TYPES.size();
    }

    public static int getHandlerCount() {
        return HANDLERS.values().stream().mapToInt(List::size).sum();
    }

    /**
     * @deprecated 使用 {@link #getHandlerCount()}
     */
    @Deprecated
    public static int getSubscriberCount() {
        return getHandlerCount();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static List<String> getHandlerInfo(Class<?> eventType) {
        List<Handler> handlers = HANDLERS.get(eventType);
        if (handlers == null || handlers.isEmpty()) return List.of();
        return handlers.stream().map(h -> h.priority().name() + ": " + h.source()).toList();
    }

    /**
     * @deprecated 使用 {@link #getHandlerInfo(Class)}
     */
    @Deprecated
    public static List<String> getSubscriberInfo(Class<?> eventType) {
        return getHandlerInfo(eventType);
    }

    // ==================== 内部类型 ====================

    private record Handler(
            EventPriority priority,
            String source,
            Consumer<Object> consumer
    ) {
    }
}
