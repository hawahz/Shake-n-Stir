package io.github.hawah.shakenstir.client.hanlder;

import io.github.hawah.shakenstir.lib.client.handler.IHandler;

public abstract class ActiveTriggerHandler implements IHandler {

    protected boolean wasActive = false;

    @Override
    public final void tick() {
        if (!isActive()) {
            if (wasActive) {
                wasActive = false;
                onStop();
            }
            return;
        }
        if (!wasActive) {
            onStart();
            wasActive = true;
        }
        onTick();
    }

    public abstract void onTick();

    public final boolean wasActive() {
        return wasActive;
    }

    public void onStart() {

    }

    public void onStop() {

    }
}
