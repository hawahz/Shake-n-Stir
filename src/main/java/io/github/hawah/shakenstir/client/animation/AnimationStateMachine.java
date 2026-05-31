package io.github.hawah.shakenstir.client.animation;

import io.github.hawah.shakenstir.client.render.entity.IStateProvider;
import io.github.hawah.shakenstir.foundation.utils.StateMachine;
import io.github.hawah.shakenstir.lib.client.utils.AnimationTickHolder;
import net.minecraft.client.animation.KeyframeAnimation;

import java.util.List;

public class AnimationStateMachine extends StateMachine<AnimationState> {
    public AnimationStateMachine() {
    }

    @Override
    public boolean transfer(String state) {
        if (!super.transfer(state))
            return false;
        if (previousState != null) {
            currentState.prevStateFadeOutMs = previousState.fadeOutMs;
            previousState.nextStateFadeInMs = currentState.fadeInMs;
        }
        return true;
    }


    public void setupAnim(List<KeyframeAnimation> animation, IStateProvider provider) {
        transfer(provider.state());
        update();
        if (previousState != null) {
            previousState.apply(animation);
        }
        currentState.apply(animation);
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
        return (long) (AnimationTickHolder.getRenderTime() * 50);
    }
}
