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
