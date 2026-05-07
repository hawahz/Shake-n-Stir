package io.github.hawah.shakenstir.lib.client.render.toolkit;

import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Animation<T> {
    private final Function<T, T> modifier;
    private final LerpFunction<T> lerp;
    private final List<Value<T>> values = new ArrayList<>();

    private T value = null;
    private double timelineLength = 0;
    private double offset = 0;
    private boolean cycle = false;
    private boolean rewind = false;
    private final AnimationPlayer player;

    public Animation(Function<T, T> modifier, LerpFunction<T> lerp, T initialValue, AnimationPlayer player) {
        this.modifier = modifier;
        this.lerp = lerp;
        this.player = player;
        setValue(initialValue);
    }

    public Animation<T> cycle(boolean flag) {
        this.cycle = flag;
        return this;
    }

    public Animation<T> rewind(boolean flag) {
        this.rewind = flag;
        return this;
    }

    public Animation<T> offset(double offset) {
        this.offset = offset;
        player.expand(timelineLength + offset);
        return this;
    }

    public void setValue(T value) {
        this.value = modifier.apply(value);
    }

    public T getValue() {
        return value;
    }

    public double getTimelineLength() {
        return timelineLength;
    }

    public Value<T> addKeyFrame(double tickTime, T value) {
        Value<T> element = Value.of(tickTime, value);

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).time() > tickTime) {
                values.add(i, element);
                timelineLength = Math.max(timelineLength, tickTime);
                player.expand(timelineLength + offset);
                return element;
            }
        }

        values.add(element);
        timelineLength = Math.max(timelineLength, tickTime);
        player.expand(timelineLength + offset);
        return element;
    }

    public T value(double tickTime) {
        if (values.isEmpty()) {
            return value;
        }

        // 只有一个关键帧时，直接返回它
        if (values.size() == 1) {
            setValue(values.getFirst().value());
            return value;
        }

        // 没有有效时间长度时，避免取模/除零
        if (timelineLength <= 0) {
            setValue(values.getFirst().value());
            return value;
        }

        double time = tickTime - offset;

        // 偏移前：保持第一帧
        if (time <= 0) {
            setValue(values.getFirst().value());
            return value;
        }

        // 处理循环/往返
        if (rewind) {
            double period = timelineLength * 2.0;
            time %= period;
            if (time < 0) time += period;
            if (time > timelineLength) {
                time = period - time;
            }
        } else if (cycle) {
            time %= timelineLength;
            if (time < 0) time += timelineLength;
        } else {
            // 非循环：超过末尾时保持最后一帧
            if (time >= timelineLength) {
                setValue(values.getLast().value());
                return value;
            }
        }

        // 早于第一关键帧：固定第一帧
        if (time <= values.getFirst().time()) {
            setValue(values.getFirst().value());
            return value;
        }

        // 晚于最后关键帧：固定最后帧
        if (time >= values.getLast().time()) {
            setValue(values.getLast().value());
            return value;
        }

        // 二分查找所在区间 [i, i+1]
        int left = 0;
        int right = values.size() - 2;
        int idx = 0;

        while (left <= right) {
            int mid = (left + right) >>> 1;
            double start = values.get(mid).time();
            double end = values.get(mid + 1).time();

            if (time < start) {
                right = mid - 1;
            } else if (time >= end) {
                left = mid + 1;
            } else {
                idx = mid;
                break;
            }
        }

        Value<T> a = values.get(idx);
        Value<T> b = values.get(idx + 1);

        double span = b.time() - a.time();
        if (span <= 0) {
            setValue(b.value());
            return value;
        }

        double phasePast = time - a.time();
        double delta = Mth.clamp(phasePast / span, 0.0, 1.0);
        delta = a.mapping().apply(delta);

        setValue(lerp.apply(a.value(), b.value(), delta));
        return value;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        if (value == null) {
            throw new IllegalStateException("Animation value is null.");
        }
        return (Class<T>) value.getClass();
    }

    public static final class Value<T> {
        private final double time;
        private final T value;
        private Function<Double, Double> mapping;

        public Value(double time, T value, Function<Double, Double> mapping) {
            this.time = time;
            this.value = value;
            this.mapping = mapping;
        }

        private Value(double time, T value) {
            this(time, value, d -> d);
        }

        static <T> Value<T> of(double time, T value) {
            return new Value<>(time, value);
        }

        public Value<T> withMapping(Function<Double, Double> mapping) {
            this.mapping = mapping;
            return this;
        }

        public double time() {
            return time;
        }

        public T value() {
            return value;
        }

        public Function<Double, Double> mapping() {
            return mapping;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Value<?>) obj;
            return Double.doubleToLongBits(this.time) == Double.doubleToLongBits(that.time) &&
                    Objects.equals(this.value, that.value) &&
                    Objects.equals(this.mapping, that.mapping);
        }

        @Override
        public int hashCode() {
            return Objects.hash(time, value, mapping);
        }

        @Override
        public String toString() {
            return "Value[" +
                    "time=" + time + ", " +
                    "value=" + value + ", " +
                    "mapping=" + mapping + ']';
        }
    }

    public interface LerpFunction<T> {
        T apply(T from, T to, double delta);
    }
}