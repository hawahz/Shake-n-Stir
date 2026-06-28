package io.github.hawah.shakenstir.foundation.event;

/**
 * SNS 事件系统的抽象基类。
 * <p>
 * 提供内置的 {@link ICancelable} 实现，简化可取消事件的编写。
 * 子类需要添加 {@link SnsRegisterEvent} 注解才能被事件系统识别。
 *
 * <pre>{@code
 * @SnsRegisterEvent
 * public class MyEvent extends AbstractSnsEvent {
 *     private final Player player;
 *
 *     public MyEvent(Player player) {
 *         this.player = player;
 *     }
 *
 *     public Player getPlayer() { return player; }
 * }
 * }</pre>
 */
// TODO: 人工审查 | 2026-06-29 | Claude Code | 类型:接口变更
// 概述: AbstractSnsEvent 新增 implements IEvent，以满足事件总线的 IEvent 类型门要求。
//        所有继承 AbstractSnsEvent 的事件类自动通过 post() 时的 IEvent 检查。
// 涉及: AbstractSnsEvent 类签名新增 IEvent 接口
// 原状: public abstract class AbstractSnsEvent implements ICancelable
//       (仅实现 ICancelable，未实现 IEvent)
public abstract class AbstractSnsEvent implements IEvent, ICancelable {
    private boolean canceled = false;

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
