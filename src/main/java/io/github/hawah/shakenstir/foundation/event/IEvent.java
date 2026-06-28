package io.github.hawah.shakenstir.foundation.event;

/**
 * SNS 事件系统的标记接口。
 * <p>
 * 所有事件类型必须实现此接口。事件总线在 {@link SnsEvents#post(Object)} 时
 * 会校验事件对象是否实现了 {@code IEvent}，未实现的调用将被拒绝。
 *
 * <pre>{@code
 * @SnsRegisterEvent
 * public class PlayerJumpEvent extends AbstractSnsEvent {
 *     // AbstractSnsEvent already implements IEvent
 * }
 *
 * // Or directly:
 * @SnsRegisterEvent
 * public class MyEvent implements IEvent, ICancelable {
 *     ...
 * }
 * }</pre>
 *
 * @see AbstractSnsEvent
 * @see ICancelable
 * @see SnsRegisterEvent
 */
// TODO: 人工审查 | 2026-06-29 | Claude Code | 类型:新文件
// 概述: 创建 IEvent 标记接口，作为 SNS 事件系统的类型门。所有事件类必须实现此接口。
//        SnsEventBus.post() 在派发前检查 event instanceof IEvent，不通过则抛异常。
// 涉及: SnsEventBus.post() 新增 IEvent 类型检查
// 原状: 无 (新文件) — 此前仅靠 @SnsRegisterEvent 注解标记事件类，无接口类型约束
public interface IEvent {
}
