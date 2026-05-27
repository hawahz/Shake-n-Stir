package io.github.hawah.shakenstir.lib.signal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InstantSignal {
    private final ConcurrentHashMap<Object, Consumer<Object[]>> listeners = new ConcurrentHashMap<>();
    private final int argNum;
    public InstantSignal bind(Object key, Consumer<Object[]> listener) {
        listeners.put(key, listener);
        return this;
    }

    public InstantSignal unbind(Object listener) {
        listeners.remove(listener);
        return this;
    }

    public InstantSignal(int argNum) {
        this.argNum = argNum;
    }

    public void emit(Object... args) {
        if (args.length != argNum) {
            throw new IllegalArgumentException("Incorrect number of arguments");
        }
        for (Consumer<Object[]> listener : listeners.values()) {
            listener.accept(args);
        }
    }
}
