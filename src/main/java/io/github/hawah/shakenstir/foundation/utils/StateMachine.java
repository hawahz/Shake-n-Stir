package io.github.hawah.shakenstir.foundation.utils;

import javax.annotation.Nullable;

public abstract class StateMachine<T extends AbstractState<T>> {
    public static final String ROOT = "<root>";
    public String state = ROOT;
    public T currentState = null;
    public @Nullable T previousState = null;

    public void start(T startState) {
        currentState = startState;
        currentState.enter(getTimeMs());
    }

    public void update() {
        currentState.update(getTimeMs());
    }

    public abstract long getTimeMs();

    public void transfer(String state) {
        T nextState;
        if ((nextState = currentState.transferTo(state)).equals(currentState)) {
            return;
        }
        previousState = currentState;
        currentState = nextState;
        previousState.exit(getTimeMs());
        currentState.enter(getTimeMs());
    }
}
