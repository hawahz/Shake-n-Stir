package io.github.hawah.shakenstir.client.render.entity;

import io.github.hawah.shakenstir.client.animation.AnimationStateMachine;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class BartenderRenderState extends HumanoidRenderState implements IStateProvider {
    public PlayerSkin skin = DefaultPlayerSkin.getDefaultSkin();
    public float capeFlap;
    public float capeLean;
    public float capeLean2;
    public int arrowCount;
    public int stingerCount;
    public boolean isSpectator;
    public boolean showHat = true;
    public boolean showJacket = true;
    public boolean showLeftPants = true;
    public boolean showRightPants = true;
    public boolean showLeftSleeve = true;
    public boolean showRightSleeve = true;
    public boolean showCape = true;
    public float fallFlyingTimeInTicks;
    public boolean shouldApplyFlyingYRot;
    public float flyingYRot;
    public Parrot.@Nullable Variant parrotOnLeftShoulder;
    public Parrot.@Nullable Variant parrotOnRightShoulder;
    public int id;
    public boolean showExtraEars = false;
    public final ItemStackRenderState heldOnHead = new ItemStackRenderState();

    public AnimationStateMachine stateMachine;

    public BartenderEntity.AnimState animState = BartenderEntity.AnimState.DEFAULT;

    public float fallFlyingScale() {
        return Mth.clamp(this.fallFlyingTimeInTicks * this.fallFlyingTimeInTicks / 100.0F, 0.0F, 1.0F);
    }
    public String brainState = "";

    @Override
    public String state() {
        return animState.getSerializedName();
    }

    public boolean shakeInHand = false;
    public boolean shaking = false;
    public ItemStackRenderState shakerItem = new ItemStackRenderState();

    public List<ItemStackRenderState> inventory = new ArrayList<>();

    public Component speaking = null;
    public int speakingRemainingTicks = 0;
    public boolean hasQueuedSpeak = false;

}
