package io.github.hawah.shakenstir.lib.util;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class PartitionableTask {

    private static final List<TickTask> tasks = new ArrayList<>();
    private static boolean ticking = false;
    private static boolean dirty = false;
    public static void tick() {
        if (dirty) {
            ticking = !tasks.isEmpty();
            dirty = false;
        }
        if (!ticking) {
            return;
        }
        for (int i = tasks.size() - 1; i >= 0; i--) {
            if (tasks.get(i).tick()) {
                tasks.remove(i);
                dirty = true;
            }
        }
    }

    // ---- TaskP1 ----

    public static <P> void create(P paramInitial, TaskP1.TaskP1Like<P> task, TaskP1.TaskEndP1Like<P> taskEnd) {
        TaskP1<P> taskP1 = new TaskP1<>(paramInitial, task, taskEnd);
        dirty = true;
        tasks.add(taskP1);
    }

    public static <P> void create(P paramInitial, TaskP1.TaskP1Like<P> task) {
        create(paramInitial, task, TaskP1.TaskEndP1Like.empty());
    }

    // ---- TaskP2 ----

    public static <P1, P2> void create(P1 param1Initial, P2 param2Initial,
                                       TaskP2.TaskP2Like<P1, P2> task,
                                       TaskP2.TaskEndP2Like<P1, P2> taskEnd) {
        tasks.add(new TaskP2<>(param1Initial, param2Initial, task, taskEnd));
        dirty = true;
    }

    public static <P1, P2> void create(P1 param1Initial, P2 param2Initial,
                                       TaskP2.TaskP2Like<P1, P2> task) {
        create(param1Initial, param2Initial, task, TaskP2.TaskEndP2Like.empty());
    }

    // ---- TaskP3 ----

    public static <P1, P2, P3> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial,
                                           TaskP3.TaskP3Like<P1, P2, P3> task,
                                           TaskP3.TaskEndP3Like<P1, P2, P3> taskEnd) {
        tasks.add(new TaskP3<>(param1Initial, param2Initial, param3Initial, task, taskEnd));
        dirty = true;
    }

    public static <P1, P2, P3> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial,
                                           TaskP3.TaskP3Like<P1, P2, P3> task) {
        create(param1Initial, param2Initial, param3Initial, task, TaskP3.TaskEndP3Like.empty());
    }

    // ---- TaskP4 ----

    public static <P1, P2, P3, P4> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial,
                                               TaskP4.TaskP4Like<P1, P2, P3, P4> task,
                                               TaskP4.TaskEndP4Like<P1, P2, P3, P4> taskEnd) {
        tasks.add(new TaskP4<>(param1Initial, param2Initial, param3Initial, param4Initial, task, taskEnd));
        dirty = true;
    }

    public static <P1, P2, P3, P4> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial,
                                               TaskP4.TaskP4Like<P1, P2, P3, P4> task) {
        create(param1Initial, param2Initial, param3Initial, param4Initial, task, TaskP4.TaskEndP4Like.empty());
    }

    // ---- TaskP5 ----

    public static <P1, P2, P3, P4, P5> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial,
                                                   TaskP5.TaskP5Like<P1, P2, P3, P4, P5> task,
                                                   TaskP5.TaskEndP5Like<P1, P2, P3, P4, P5> taskEnd) {
        tasks.add(new TaskP5<>(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, task, taskEnd));
        dirty = true;
    }

    public static <P1, P2, P3, P4, P5> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial,
                                                   TaskP5.TaskP5Like<P1, P2, P3, P4, P5> task) {
        create(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, task, TaskP5.TaskEndP5Like.empty());
    }

    // ---- TaskP6 ----

    public static <P1, P2, P3, P4, P5, P6> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial, P6 param6Initial,
                                                       TaskP6.TaskP6Like<P1, P2, P3, P4, P5, P6> task,
                                                       TaskP6.TaskEndP6Like<P1, P2, P3, P4, P5, P6> taskEnd) {
        tasks.add(new TaskP6<>(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, param6Initial, task, taskEnd));
        dirty = true;
    }

    public static <P1, P2, P3, P4, P5, P6> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial, P6 param6Initial,
                                                       TaskP6.TaskP6Like<P1, P2, P3, P4, P5, P6> task) {
        create(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, param6Initial, task, TaskP6.TaskEndP6Like.empty());
    }

    // ---- TaskP7 ----

    public static <P1, P2, P3, P4, P5, P6, P7> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial, P6 param6Initial, P7 param7Initial,
                                                           TaskP7.TaskP7Like<P1, P2, P3, P4, P5, P6, P7> task,
                                                           TaskP7.TaskEndP7Like<P1, P2, P3, P4, P5, P6, P7> taskEnd) {
        tasks.add(new TaskP7<>(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, param6Initial, param7Initial, task, taskEnd));
        dirty = true;
    }

    public static <P1, P2, P3, P4, P5, P6, P7> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial, P6 param6Initial, P7 param7Initial,
                                                           TaskP7.TaskP7Like<P1, P2, P3, P4, P5, P6, P7> task) {
        create(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, param6Initial, param7Initial, task, TaskP7.TaskEndP7Like.empty());
    }

    // ---- TaskP8 ----

    public static <P1, P2, P3, P4, P5, P6, P7, P8> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial, P6 param6Initial, P7 param7Initial, P8 param8Initial,
                                                               TaskP8.TaskP8Like<P1, P2, P3, P4, P5, P6, P7, P8> task,
                                                               TaskP8.TaskEndP8Like<P1, P2, P3, P4, P5, P6, P7, P8> taskEnd) {
        tasks.add(new TaskP8<>(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, param6Initial, param7Initial, param8Initial, task, taskEnd));
        dirty = true;
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial, P6 param6Initial, P7 param7Initial, P8 param8Initial,
                                                               TaskP8.TaskP8Like<P1, P2, P3, P4, P5, P6, P7, P8> task) {
        create(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, param6Initial, param7Initial, param8Initial, task, TaskP8.TaskEndP8Like.empty());
    }

    // ---- TaskP9 ----

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial, P6 param6Initial, P7 param7Initial, P8 param8Initial, P9 param9Initial,
                                                                   TaskP9.TaskP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> task,
                                                                   TaskP9.TaskEndP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> taskEnd) {
        tasks.add(new TaskP9<>(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, param6Initial, param7Initial, param8Initial, param9Initial, task, taskEnd));
        dirty = true;
    }

    public static <P1, P2, P3, P4, P5, P6, P7, P8, P9> void create(P1 param1Initial, P2 param2Initial, P3 param3Initial, P4 param4Initial, P5 param5Initial, P6 param6Initial, P7 param7Initial, P8 param8Initial, P9 param9Initial,
                                                                   TaskP9.TaskP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> task) {
        create(param1Initial, param2Initial, param3Initial, param4Initial, param5Initial, param6Initial, param7Initial, param8Initial, param9Initial, task, TaskP9.TaskEndP9Like.empty());
    }

    interface TickTask {
        boolean tick();
    }

    // ======================== TaskP1 ========================

    public static class TaskP1<P> implements TickTask {
        private final ParamHolder<P> param;
        private final TaskP1Like<P> task;
        private final TaskEndP1Like<P> taskEnd;

        public TaskP1(P param, TaskP1Like<P> task, TaskEndP1Like<P> taskEnd) {
            this.param = new ParamHolder<>(param);
            this.task = task;
            this.taskEnd = taskEnd;
        }

        public interface TaskP1Like<P> {
            boolean tryToRun(ParamHolder<P> param);
        }
        public interface TaskEndP1Like<P> {
            void onEnd(ParamHolder<P> param);
            static <P> TaskEndP1Like<P> empty() {
                return (_) -> {};
            }
        }

        public boolean tick() {
            long startTime = System.currentTimeMillis();
            while (!task.tryToRun(param)) {
                if (System.nanoTime() - startTime > 5) {
                    return false;
                }
            }
            taskEnd.onEnd(param);
            return true;
        }
    }

    // ======================== TaskP2 ========================

    public static class TaskP2<P1, P2> implements TickTask {
        private final ParamHolder<P1> param1;
        private final ParamHolder<P2> param2;
        private final TaskP2Like<P1, P2> task;
        private final TaskEndP2Like<P1, P2> taskEnd;

        public TaskP2(P1 param1, P2 param2, TaskP2Like<P1, P2> task, TaskEndP2Like<P1, P2> taskEnd) {
            this.param1 = new ParamHolder<>(param1);
            this.param2 = new ParamHolder<>(param2);
            this.task = task;
            this.taskEnd = taskEnd;
        }

        public interface TaskP2Like<P1, P2> {
            boolean tryToRun(ParamHolder<P1> param1, ParamHolder<P2> param2);
        }
        public interface TaskEndP2Like<P1, P2> {
            void onEnd(ParamHolder<P1> param1, ParamHolder<P2> param2);
            static <P1, P2> TaskEndP2Like<P1, P2> empty() {
                return (_1, _2) -> {};
            }
        }

        public boolean tick() {
            long startTime = System.currentTimeMillis();
            while (!task.tryToRun(param1, param2)) {
                if (System.nanoTime() - startTime > 5) {
                    return false;
                }
            }
            taskEnd.onEnd(param1, param2);
            return true;
        }
    }

    // ======================== TaskP3 ========================

    public static class TaskP3<P1, P2, P3> implements TickTask {
        private final ParamHolder<P1> param1;
        private final ParamHolder<P2> param2;
        private final ParamHolder<P3> param3;
        private final TaskP3Like<P1, P2, P3> task;
        private final TaskEndP3Like<P1, P2, P3> taskEnd;

        public TaskP3(P1 param1, P2 param2, P3 param3, TaskP3Like<P1, P2, P3> task, TaskEndP3Like<P1, P2, P3> taskEnd) {
            this.param1 = new ParamHolder<>(param1);
            this.param2 = new ParamHolder<>(param2);
            this.param3 = new ParamHolder<>(param3);
            this.task = task;
            this.taskEnd = taskEnd;
        }

        public interface TaskP3Like<P1, P2, P3> {
            boolean tryToRun(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3);
        }
        public interface TaskEndP3Like<P1, P2, P3> {
            void onEnd(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3);
            static <P1, P2, P3> TaskEndP3Like<P1, P2, P3> empty() {
                return (_1, _2, _3) -> {};
            }
        }

        public boolean tick() {
            long startTime = System.currentTimeMillis();
            while (!task.tryToRun(param1, param2, param3)) {
                if (System.nanoTime() - startTime > 5) {
                    return false;
                }
            }
            taskEnd.onEnd(param1, param2, param3);
            return true;
        }
    }

    // ======================== TaskP4 ========================

    public static class TaskP4<P1, P2, P3, P4> implements TickTask {
        private final ParamHolder<P1> param1;
        private final ParamHolder<P2> param2;
        private final ParamHolder<P3> param3;
        private final ParamHolder<P4> param4;
        private final TaskP4Like<P1, P2, P3, P4> task;
        private final TaskEndP4Like<P1, P2, P3, P4> taskEnd;

        public TaskP4(P1 param1, P2 param2, P3 param3, P4 param4, TaskP4Like<P1, P2, P3, P4> task, TaskEndP4Like<P1, P2, P3, P4> taskEnd) {
            this.param1 = new ParamHolder<>(param1);
            this.param2 = new ParamHolder<>(param2);
            this.param3 = new ParamHolder<>(param3);
            this.param4 = new ParamHolder<>(param4);
            this.task = task;
            this.taskEnd = taskEnd;
        }

        public interface TaskP4Like<P1, P2, P3, P4> {
            boolean tryToRun(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4);
        }
        public interface TaskEndP4Like<P1, P2, P3, P4> {
            void onEnd(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4);
            static <P1, P2, P3, P4> TaskEndP4Like<P1, P2, P3, P4> empty() {
                return (_1, _2, _3, _4) -> {};
            }
        }

        public boolean tick() {
            long startTime = System.currentTimeMillis();
            while (!task.tryToRun(param1, param2, param3, param4)) {
                if (System.nanoTime() - startTime > 5) {
                    return false;
                }
            }
            taskEnd.onEnd(param1, param2, param3, param4);
            return true;
        }
    }

    // ======================== TaskP5 ========================

    public static class TaskP5<P1, P2, P3, P4, P5> implements TickTask {
        private final ParamHolder<P1> param1;
        private final ParamHolder<P2> param2;
        private final ParamHolder<P3> param3;
        private final ParamHolder<P4> param4;
        private final ParamHolder<P5> param5;
        private final TaskP5Like<P1, P2, P3, P4, P5> task;
        private final TaskEndP5Like<P1, P2, P3, P4, P5> taskEnd;

        public TaskP5(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, TaskP5Like<P1, P2, P3, P4, P5> task, TaskEndP5Like<P1, P2, P3, P4, P5> taskEnd) {
            this.param1 = new ParamHolder<>(param1);
            this.param2 = new ParamHolder<>(param2);
            this.param3 = new ParamHolder<>(param3);
            this.param4 = new ParamHolder<>(param4);
            this.param5 = new ParamHolder<>(param5);
            this.task = task;
            this.taskEnd = taskEnd;
        }

        public interface TaskP5Like<P1, P2, P3, P4, P5> {
            boolean tryToRun(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5);
        }
        public interface TaskEndP5Like<P1, P2, P3, P4, P5> {
            void onEnd(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5);
            static <P1, P2, P3, P4, P5> TaskEndP5Like<P1, P2, P3, P4, P5> empty() {
                return (_1, _2, _3, _4, _5) -> {};
            }
        }

        public boolean tick() {
            long startTime = System.currentTimeMillis();
            while (!task.tryToRun(param1, param2, param3, param4, param5)) {
                if (System.nanoTime() - startTime > 5) {
                    return false;
                }
            }
            taskEnd.onEnd(param1, param2, param3, param4, param5);
            return true;
        }
    }

    // ======================== TaskP6 ========================

    public static class TaskP6<P1, P2, P3, P4, P5, P6> implements TickTask {
        private final ParamHolder<P1> param1;
        private final ParamHolder<P2> param2;
        private final ParamHolder<P3> param3;
        private final ParamHolder<P4> param4;
        private final ParamHolder<P5> param5;
        private final ParamHolder<P6> param6;
        private final TaskP6Like<P1, P2, P3, P4, P5, P6> task;
        private final TaskEndP6Like<P1, P2, P3, P4, P5, P6> taskEnd;

        public TaskP6(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, TaskP6Like<P1, P2, P3, P4, P5, P6> task, TaskEndP6Like<P1, P2, P3, P4, P5, P6> taskEnd) {
            this.param1 = new ParamHolder<>(param1);
            this.param2 = new ParamHolder<>(param2);
            this.param3 = new ParamHolder<>(param3);
            this.param4 = new ParamHolder<>(param4);
            this.param5 = new ParamHolder<>(param5);
            this.param6 = new ParamHolder<>(param6);
            this.task = task;
            this.taskEnd = taskEnd;
        }

        public interface TaskP6Like<P1, P2, P3, P4, P5, P6> {
            boolean tryToRun(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5, ParamHolder<P6> param6);
        }
        public interface TaskEndP6Like<P1, P2, P3, P4, P5, P6> {
            void onEnd(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5, ParamHolder<P6> param6);
            static <P1, P2, P3, P4, P5, P6> TaskEndP6Like<P1, P2, P3, P4, P5, P6> empty() {
                return (_1, _2, _3, _4, _5, _6) -> {};
            }
        }

        public boolean tick() {
            long startTime = System.currentTimeMillis();
            while (!task.tryToRun(param1, param2, param3, param4, param5, param6)) {
                if (System.nanoTime() - startTime > 5) {
                    return false;
                }
            }
            taskEnd.onEnd(param1, param2, param3, param4, param5, param6);
            return true;
        }
    }

    // ======================== TaskP7 ========================

    public static class TaskP7<P1, P2, P3, P4, P5, P6, P7> implements TickTask {
        private final ParamHolder<P1> param1;
        private final ParamHolder<P2> param2;
        private final ParamHolder<P3> param3;
        private final ParamHolder<P4> param4;
        private final ParamHolder<P5> param5;
        private final ParamHolder<P6> param6;
        private final ParamHolder<P7> param7;
        private final TaskP7Like<P1, P2, P3, P4, P5, P6, P7> task;
        private final TaskEndP7Like<P1, P2, P3, P4, P5, P6, P7> taskEnd;

        public TaskP7(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, TaskP7Like<P1, P2, P3, P4, P5, P6, P7> task, TaskEndP7Like<P1, P2, P3, P4, P5, P6, P7> taskEnd) {
            this.param1 = new ParamHolder<>(param1);
            this.param2 = new ParamHolder<>(param2);
            this.param3 = new ParamHolder<>(param3);
            this.param4 = new ParamHolder<>(param4);
            this.param5 = new ParamHolder<>(param5);
            this.param6 = new ParamHolder<>(param6);
            this.param7 = new ParamHolder<>(param7);
            this.task = task;
            this.taskEnd = taskEnd;
        }

        public interface TaskP7Like<P1, P2, P3, P4, P5, P6, P7> {
            boolean tryToRun(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5, ParamHolder<P6> param6, ParamHolder<P7> param7);
        }
        public interface TaskEndP7Like<P1, P2, P3, P4, P5, P6, P7> {
            void onEnd(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5, ParamHolder<P6> param6, ParamHolder<P7> param7);
            static <P1, P2, P3, P4, P5, P6, P7> TaskEndP7Like<P1, P2, P3, P4, P5, P6, P7> empty() {
                return (_1, _2, _3, _4, _5, _6, _7) -> {};
            }
        }

        public boolean tick() {
            long startTime = System.currentTimeMillis();
            while (!task.tryToRun(param1, param2, param3, param4, param5, param6, param7)) {
                if (System.nanoTime() - startTime > 5) {
                    return false;
                }
            }
            taskEnd.onEnd(param1, param2, param3, param4, param5, param6, param7);
            return true;
        }
    }

    // ======================== TaskP8 ========================

    public static class TaskP8<P1, P2, P3, P4, P5, P6, P7, P8> implements TickTask {
        private final ParamHolder<P1> param1;
        private final ParamHolder<P2> param2;
        private final ParamHolder<P3> param3;
        private final ParamHolder<P4> param4;
        private final ParamHolder<P5> param5;
        private final ParamHolder<P6> param6;
        private final ParamHolder<P7> param7;
        private final ParamHolder<P8> param8;
        private final TaskP8Like<P1, P2, P3, P4, P5, P6, P7, P8> task;
        private final TaskEndP8Like<P1, P2, P3, P4, P5, P6, P7, P8> taskEnd;

        public TaskP8(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8, TaskP8Like<P1, P2, P3, P4, P5, P6, P7, P8> task, TaskEndP8Like<P1, P2, P3, P4, P5, P6, P7, P8> taskEnd) {
            this.param1 = new ParamHolder<>(param1);
            this.param2 = new ParamHolder<>(param2);
            this.param3 = new ParamHolder<>(param3);
            this.param4 = new ParamHolder<>(param4);
            this.param5 = new ParamHolder<>(param5);
            this.param6 = new ParamHolder<>(param6);
            this.param7 = new ParamHolder<>(param7);
            this.param8 = new ParamHolder<>(param8);
            this.task = task;
            this.taskEnd = taskEnd;
        }

        public interface TaskP8Like<P1, P2, P3, P4, P5, P6, P7, P8> {
            boolean tryToRun(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5, ParamHolder<P6> param6, ParamHolder<P7> param7, ParamHolder<P8> param8);
        }
        public interface TaskEndP8Like<P1, P2, P3, P4, P5, P6, P7, P8> {
            void onEnd(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5, ParamHolder<P6> param6, ParamHolder<P7> param7, ParamHolder<P8> param8);
            static <P1, P2, P3, P4, P5, P6, P7, P8> TaskEndP8Like<P1, P2, P3, P4, P5, P6, P7, P8> empty() {
                return (_1, _2, _3, _4, _5, _6, _7, _8) -> {};
            }
        }

        public boolean tick() {
            long startTime = System.currentTimeMillis();
            while (!task.tryToRun(param1, param2, param3, param4, param5, param6, param7, param8)) {
                if (System.nanoTime() - startTime > 5) {
                    return false;
                }
            }
            taskEnd.onEnd(param1, param2, param3, param4, param5, param6, param7, param8);
            return true;
        }
    }

    // ======================== TaskP9 ========================

    public static class TaskP9<P1, P2, P3, P4, P5, P6, P7, P8, P9> implements TickTask {
        private final ParamHolder<P1> param1;
        private final ParamHolder<P2> param2;
        private final ParamHolder<P3> param3;
        private final ParamHolder<P4> param4;
        private final ParamHolder<P5> param5;
        private final ParamHolder<P6> param6;
        private final ParamHolder<P7> param7;
        private final ParamHolder<P8> param8;
        private final ParamHolder<P9> param9;
        private final TaskP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> task;
        private final TaskEndP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> taskEnd;

        public TaskP9(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8, P9 param9, TaskP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> task, TaskEndP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> taskEnd) {
            this.param1 = new ParamHolder<>(param1);
            this.param2 = new ParamHolder<>(param2);
            this.param3 = new ParamHolder<>(param3);
            this.param4 = new ParamHolder<>(param4);
            this.param5 = new ParamHolder<>(param5);
            this.param6 = new ParamHolder<>(param6);
            this.param7 = new ParamHolder<>(param7);
            this.param8 = new ParamHolder<>(param8);
            this.param9 = new ParamHolder<>(param9);
            this.task = task;
            this.taskEnd = taskEnd;
        }

        public interface TaskP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> {
            boolean tryToRun(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5, ParamHolder<P6> param6, ParamHolder<P7> param7, ParamHolder<P8> param8, ParamHolder<P9> param9);
        }
        public interface TaskEndP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> {
            void onEnd(ParamHolder<P1> param1, ParamHolder<P2> param2, ParamHolder<P3> param3, ParamHolder<P4> param4, ParamHolder<P5> param5, ParamHolder<P6> param6, ParamHolder<P7> param7, ParamHolder<P8> param8, ParamHolder<P9> param9);
            static <P1, P2, P3, P4, P5, P6, P7, P8, P9> TaskEndP9Like<P1, P2, P3, P4, P5, P6, P7, P8, P9> empty() {
                return (_1, _2, _3, _4, _5, _6, _7, _8, _9) -> {};
            }
        }

        public boolean tick() {
            long startTime = System.currentTimeMillis();
            while (!task.tryToRun(param1, param2, param3, param4, param5, param6, param7, param8, param9)) {
                if (System.nanoTime() - startTime > 5) {
                    return false;
                }
            }
            taskEnd.onEnd(param1, param2, param3, param4, param5, param6, param7, param8, param9);
            return true;
        }
    }

    public static class ParamHolder<P> {
        private P param;

        public ParamHolder(P param) {
            this.param = param;
        }

        public void setParam(P param) {
            this.param = param;
        }

        public P getParam() {
            return param;
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        tick();
    }

    @SubscribeEvent
    public static void onTickServer(ServerTickEvent.Post event) {
        tick();
    }
}
