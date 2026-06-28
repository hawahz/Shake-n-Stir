package io.github.hawah.shakenstir.foundation.event;

/**
 * 可取消事件的接口。
 * <p>
 * 事件类实现此接口后，订阅者可以调用 {@link #setCanceled(boolean)} 来取消事件。
 * 发布事件后，可以通过 {@link #isCanceled()} 检查事件是否被取消，
 * 并根据结果决定是否继续执行原始行为。
 *
 * <pre>{@code
 * @SnsRegisterEvent
 * public class MyEvent implements ICancelable {
 *     private boolean canceled = false;
 *
 *     @Override
 *     public boolean isCanceled() { return canceled; }
 *
 *     @Override
 *     public void setCanceled(boolean canceled) { this.canceled = canceled; }
 * }
 * }</pre>
 */
// TODO: 人工审查 | 2026-06-29 | Claude Code | 类型:新文件
// 概述: 创建 ICancelable 接口，提供事件取消机制。事件类实现此接口后可被处理器取消。
//        与 AbstractSnsEvent 配合使用 (AbstractSnsEvent 提供默认实现)。
// 涉及: SnsEvents.post() 后检查 event.isCanceled() 决定是否执行原始行为
// 原状: 无 (新文件) — 此前无事件取消机制
public interface ICancelable {
    /**
     * 检查事件是否已被取消。
     *
     * @return {@code true} 如果事件已被取消
     */
    boolean isCanceled();

    /**
     * 设置事件的取消状态。
     *
     * @param canceled {@code true} 则取消事件
     */
    void setCanceled(boolean canceled);
}
