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
public interface IEvent {
}
