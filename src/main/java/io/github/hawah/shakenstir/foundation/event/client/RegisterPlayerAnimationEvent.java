package io.github.hawah.shakenstir.foundation.event.client;

import io.github.hawah.shakenstir.foundation.event.AbstractSnsEvent;
import io.github.hawah.shakenstir.foundation.event.Side;
import io.github.hawah.shakenstir.foundation.event.SnsRegisterEvent;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SnsRegisterEvent(Side.CLIENT)
public class RegisterPlayerAnimationEvent extends AbstractSnsEvent {
    private final ModelPart root;
    private final Map<Identifier, KeyframeAnimation> animations;

    public RegisterPlayerAnimationEvent(ModelPart root) {
        this.root = root;
        this.animations = new HashMap<>();
    }

    /**
     * 向动画 Map 中注册一个动画条目。
     * <p>
     * 使用 {@link Identifier} 作为 key，后续可通过相同的 Identifier 获取对应动画。
     * 如果已有相同 key 的动画，会被覆盖（后注册的优先）。
     *
     * @param id        动画标识符，推荐使用 {@code shakenstir:xxx} 命名空间
     * @param animation 已烘焙的 KeyframeAnimation 实例
     */
    public void register(Identifier id, KeyframeAnimation animation) {
        animations.put(id, animation);
    }

    /**
     * 获取模型根部件，供处理器调用 {@code AnimationDefinition.bake(root)} 烘焙动画。
     */
    public ModelPart getRoot() {
        return root;
    }

    /**
     * 获取所有已注册动画的只读视图。
     */
    public Map<Identifier, KeyframeAnimation> getAnimations() {
        return Collections.unmodifiableMap(animations);
    }

    /**
     * 获取内部可变 Map（仅供 Mixin 在事件处理完成后使用）。
     */
    public Map<Identifier, KeyframeAnimation> getAnimationsInternal() {
        return animations;
    }
}
