package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.ShakenStir;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.trigger.SimpleTrigger;
import io.github.hawah.shakenstir.content.trigger.TriggerRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.tags.SnsItemTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.function.Consumer;

public class ModAdvancementGenerator implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {

        Advancement.Builder.advancement()
                .display(
                    new ItemStackTemplate(ItemRegistries.SHAKER),
                    LangData.ADVANCEMENT_SHAKE_ROOT_TITLE.get(),
                    LangData.ADVANCEMENT_SHAKE_ROOT_DESC.get(),
                    Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"),
                    AdvancementType.GOAL,
                    true,
                    true,
                    false
                ).addCriterion(
                        "has_shaker",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistries.SHAKER)
                ).save(output, ShakenStir.asResource("shake/root"));

        Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder("shakenstir:shake/root"))
                .display(
                        new ItemStackTemplate(ItemRegistries.SHAKER, 1, DataComponentPatch.builder().set(DataComponentTypeRegistries.HAS_CUP, false).build()),
                        LangData.ADVANCEMENT_SHAKE_BUBBLE_TITLE.get(),
                        LangData.ADVANCEMENT_SHAKE_BUBBLE_DESC.get(),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                ).addCriterion(
                        "shake",
                        TriggerRegistries.SHAKE_BUBBLE_EXPLODE.get().createCriterion(new SimpleTrigger.Instance())
                ).save(output, ShakenStir.asResource("shake/bubble_explode"));

        Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder("shakenstir:shake/root"))
                .display(
                    new ItemStackTemplate(ItemRegistries.SHAKER_LID),
                    LangData.ADVANCEMENT_SHAKE_SHAKER_OVERTURN_TITLE.get(),
                    LangData.ADVANCEMENT_SHAKE_SHAKER_OVERTURN_DESC.get(),
                    null,
                    AdvancementType.GOAL,
                    true,
                    true,
                    false
                ).addCriterion(
                    "shaker_overturn",
                    TriggerRegistries.SHAKER_OVERTURN.get().createCriterion(new SimpleTrigger.Instance())
                ).save(output, ShakenStir.asResource("shake/shaker_overturn"));



        Advancement.Builder.advancement().display(
                new ItemStackTemplate(ItemRegistries.BOTTLE),
                LangData.ADVANCEMENT_DRINK_ROOT_TITLE.get(),
                LangData.ADVANCEMENT_DRINK_ROOT_DESC.get(),
                Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"),
                AdvancementType.GOAL,
                true,
                true,
                false
        ).addCriterion(
                "has_spirit",
                InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), SnsItemTags.SPIRIT))
        ).save(output, ShakenStir.asResource("drink/root"));

        Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder("shakenstir:drink/root"))
                .display(
                        new ItemStackTemplate(ItemRegistries.WHISKY),
                        LangData.ADVANCEMENT_DRINK_FIRST_TITLE.get(),
                        LangData.ADVANCEMENT_DRINK_FIRST_DESC.get(),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                ).addCriterion(
                        "first_drunk",
                        TriggerRegistries.FIRST_DRUNK.get().createCriterion(new SimpleTrigger.Instance())
                ).save(output, ShakenStir.asResource("drink/first_drunk"));
    }
}
