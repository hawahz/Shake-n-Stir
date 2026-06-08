package io.github.hawah.shakenstir.client.animation;

import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;


public final class ShakeAnimationState extends AnimationState{

    private final BartenderEntity entity;

    public ShakeAnimationState(int animIndex, BartenderEntity entity) {
        super(animIndex);
        this.entity = entity;
    }

    long lastSoundMillis = -1;

    @Override
    public void update(long timeStampMs) {
        if (isClosed) {
            return;
        }
        super.update(timeStampMs);
        float soundV = millisSinceStart % (0.6667F * 1000);
        boolean playSound = false;
        if (soundV > 0.1667F*1000 && soundV < 0.33 * 1000 && timeStampMs - lastSoundMillis > 100) {
            lastSoundMillis = timeStampMs;
            playSound = true;
        }
        if (soundV > 0.5 * 1000 && timeStampMs - lastSoundMillis > 100) {
            lastSoundMillis = timeStampMs;
            playSound = true;
        }
        if (playSound && entity != null && isActive()) {
            Minecraft.getInstance().getSoundManager().play(
                    new SimpleSoundInstance(
                            SoundEvents.ARMOR_EQUIP_IRON.value(),
                            SoundSource.PLAYERS,
                            1.2F,
                            20,
                            entity.level().getRandom(),
                            entity.blockPosition()
                    )
            );

            Minecraft.getInstance().getSoundManager().playDelayed(
                    new SimpleSoundInstance(
                            SoundEvents.GLASS_BREAK,
                            SoundSource.PLAYERS,
                            (1.2F - 0) * 0.2F,
                            1.5F,
                            entity.level().getRandom(),
                            entity.blockPosition()
                    ),
                    1
            );
        }
    }
}
