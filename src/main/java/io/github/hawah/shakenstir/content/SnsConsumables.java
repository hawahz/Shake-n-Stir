package io.github.hawah.shakenstir.content;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.foundation.datapack.ConsumableDescs;
import net.minecraft.core.HolderLookup;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 模组自定义的 {@link Consumable} 定义，以及工具提示描述查找。
 * <p>
 * Consumable → 描述键的映射由 {@code shakenstir:consumable_desc}
 * 数据包注册表驱动（内置条目参见 {@link ConsumableDescs}）。
 */
// TODO: 人工审查 - 2026-06-27 - 移除硬编码 CONSUMABLE_IDS Map，改为委托 ConsumableDescs 数据包驱动查找；getDescription 新增 HolderLookup.Provider 参数以支持 datapack registry
public class SnsConsumables {
    public static final Consumable MINT = Consumables.defaultFood()
            .consumeSeconds(0.8F)
            .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
                    new MobEffectInstance(MobEffectRegistries.PARALYSIS, 10)
            )))
            .build();

    /**
     * 获取消耗品的工具提示描述组件。
     * 以下情况返回空列表：
     * <ul>
     *   <li>消耗品未在数据包注册表或内置映射中注册</li>
     *   <li>当前语言中没有对应的本地化翻译</li>
     * </ul>
     * 这样可以确保对于未翻译该描述的语言，工具提示静默跳过。
     *
     * @param consumable 要描述的消耗品
     * @param registries 工具提示上下文的注册表访问器（可为 null）
     */
    public static List<Component> getDescription(Consumable consumable, @Nullable HolderLookup.Provider registries) {
        String id = ConsumableDescs.getDescriptionKey(registries, consumable);
        if (id == null) return List.of();
        String key = ShakenStir.MODID + ".consumable." + id;
        if (Language.getInstance().has(key)) {
            return List.of(Component.translatable(key));
        }
        return List.of();
    }

    /**
     * 便利重载：始终使用内置映射（不查询数据包注册表）。
     */
    public static List<Component> getDescription(Consumable consumable) {
        return getDescription(consumable, null);
    }
}
