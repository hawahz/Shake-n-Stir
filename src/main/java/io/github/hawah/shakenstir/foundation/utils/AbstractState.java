package io.github.hawah.shakenstir.foundation.utils;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractState<Self extends AbstractState<Self>> {

    private Self self() {
        return (Self) this;
    }

    Map<String, Self> connectionsOut = new HashMap<>();

    public long startTimeStampMs;
    public long endTimeStampMs;
    private boolean isActive = false;

    public AbstractState() {
    }

    public void registerConnection(String phase, Self state) {
        connectionsOut.put(phase, state);
    }

    public final void enter(long timeStampMs) {
        this.startTimeStampMs = timeStampMs;
        isActive = true;
        onEnter();
    }

    protected abstract void onEnter();

    public final void exit(long timeStampMs) {
        this.endTimeStampMs = timeStampMs;
        isActive = false;
        onExit();
    }

    protected abstract void onExit();

    public abstract void update(long timeStampMs);

    public boolean isActive() {
        return isActive;
    }

    public Self transferTo(String phase) {
        return connectionsOut.getOrDefault(phase, self());
    }

}
