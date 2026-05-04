package io.github.hawah.shakenstir.lib.client.render;

import io.github.hawah.shakenstir.ShakenStirClient;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unchecked")
public abstract class PositionedElement<Self extends PositionedElement<Self>> extends ColoredElement<Self>  {
    protected Vec3 oPos = Vec3.ZERO;
    protected Vec3 visualPos = Vec3.ZERO;
    protected Vec3 actualPos =  Vec3.ZERO;

    public Self setPositions(Vec3 p0) {
        actualPos = p0;
        return (Self) this;
    }

    @Override
    public void tick() {
        super.tick();
        oPos = visualPos;
        visualPos = visualPos.lerp(actualPos, deltaTicks.orElse(ShakenStirClient.ANI_DELTAF));
    }

    @Override
    public boolean finish() {
        if (!super.finish())
            return false;
        visualPos = actualPos;
        oPos = visualPos;
        return true;
    }
}
