package io.github.hawah.shakenstir.client.animation;

import io.github.hawah.shakenstir.foundation.utils.AbstractState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.util.Mth;

import java.util.List;

public class AnimationState extends AbstractState<AnimationState> {

    public long prevStateFadeOutMs = 0;
    public long fadeInMs = 0;
    public long fadeOutMs = 0;
    public long nextStateFadeInMs = 0;

    private long curFadeInTime = 0;
    private long curFadeOutTime = 0;
    public float currentFade = 0;
    public boolean isClosed = false;

    public long millisSinceStart = 0;

    public long loop = Long.MAX_VALUE;

    public final int animIndex;

    public AnimationState(int animIndex) {
        this.animIndex = animIndex;
    }

    public AnimationState fadeTime(long fadeMs) {
        return fadeOutMs(fadeMs);
    }

    public AnimationState fadeInMs(long fadeInMs) {
        this.fadeInMs = fadeInMs;
        return this;
    }

    public AnimationState fadeOutMs(long fadeOutMs) {
        this.fadeOutMs = fadeOutMs;
        return this;
    }

    public AnimationState loop(long loop) {
        this.loop = loop;
        return this;
    }

    @Override
    protected void onEnter() {
        isClosed = false;
        curFadeInTime = fadeInMs + prevStateFadeOutMs;
    }

    @Override
    protected void onExit() {
        this.endTimeStampMs += (long) ((1 - currentFade) * fadeOutMs);
        curFadeOutTime = fadeOutMs + nextStateFadeInMs;
    }

    @Override
    public void update(long timeStampMs) {
        if (isClosed) {
            return;
        }
        if (shouldMixture()) {
            mixtureState(timeStampMs);
        }
        this.millisSinceStart = (timeStampMs - this.startTimeStampMs) % loop;
    }

    public void apply(List<KeyframeAnimation> animation) {
        if (isClosed) {
            return;
        }
        KeyframeAnimation keyframeAnimation = animation.get(animIndex);
        if (keyframeAnimation != null) {
            keyframeAnimation.apply(millisSinceStart, currentFade);
        }
    }

    public void mixtureState(long timeStampMs) {
        if (!isActive()) {
            if (curFadeOutTime == 0) {
                currentFade = 0;
                isClosed = true;
                return;
            }
            currentFade = 1 - Mth.inverseLerp(timeStampMs - endTimeStampMs, 0, curFadeOutTime);
            currentFade = Mth.clamp(currentFade, 0, 1);
            if (currentFade <= 0) {
                isClosed = true;
                currentFade = 0;
            }
            return;
        }
        if (curFadeInTime == 0) {
            currentFade = 1;
            return;
        }
        currentFade = Mth.inverseLerp(timeStampMs - startTimeStampMs, 0, curFadeInTime);
        currentFade = Mth.clamp(currentFade, 0, 1);
    }

    public boolean shouldMixture() {
        return (isActive() && currentFade < 1) || (!isActive() && currentFade > 0);
    }
}
