package io.github.hawah.shakenstir.client.animation;

import io.github.hawah.shakenstir.foundation.utils.AbstractState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class AnimationState extends AbstractState<AnimationState> {

    public long prevStateFadeOutMs = 0;
    public long fadeInMs = 0;
    public long fadeOutMs = 0;
    public long nextStateFadeInMs = 0;

    private long curFadeInTime = 0;
    private long curFadeOutTime = 0;
    public float currentFade = 1;
    public boolean isClosed = false;

    public long millisSinceStart = 0;

    public long loop = Long.MAX_VALUE;

    protected AnimationState() {
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

    public void apply(@Nullable KeyframeAnimation animation) {
        if (animation != null) {
            animation.apply(millisSinceStart, currentFade);
        }
    }

    public void mixtureState(long timeStampMs) {
        if (!isActive()) {
            currentFade = 1 - Mth.inverseLerp(timeStampMs, endTimeStampMs, endTimeStampMs + curFadeOutTime);
            currentFade = Mth.clamp(currentFade, 0, 1);
            if (currentFade == 0) {
                isClosed = true;
            }
            return;
        }
        currentFade = Mth.inverseLerp(timeStampMs, startTimeStampMs, startTimeStampMs + curFadeInTime);
        currentFade = Mth.clamp(currentFade, 0, 1);
    }

    public boolean shouldMixture() {
        return (isActive() && currentFade < 1) || (!isActive() && currentFade > 0);
    }
}
