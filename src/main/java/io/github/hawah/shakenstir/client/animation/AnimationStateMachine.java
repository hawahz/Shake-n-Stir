package io.github.hawah.shakenstir.client.animation;

import io.github.hawah.shakenstir.client.render.entity.IStateProvider;
import io.github.hawah.shakenstir.foundation.utils.StateMachine;

public class AnimationStateMachine extends StateMachine<AnimationState> {
    public AnimationStateMachine() {
    }

    @Override
    public void transfer(String state) {
        super.transfer(state);
        if (previousState != null) {
            currentState.prevStateFadeOutMs = previousState.fadeOutMs;
            previousState.nextStateFadeInMs = currentState.fadeInMs;
        }
    }


    public void setupAnim(IStateProvider state) {
        transfer(state.state());
        if (previousState != null) {
        }
    }

    @Override
    public void update() {
        if (previousState != null) {
            previousState.update(getTimeMs());
        }
        super.update();
    }

    @Override
    public long getTimeMs() {
        return System.currentTimeMillis();
    }
}
