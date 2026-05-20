package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.content.damageType.SnsDamageType;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.datapack.cocktaileType.CocktailTypes;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static io.github.hawah.shakenstir.ShakenStir.MODID;

public class ModEnUsLangProvider extends LanguageProvider {

    public ModEnUsLangProvider(PackOutput output) {
        super(output, MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ItemRegistries.GIN.get(), "Gin");
        add(ItemRegistries.WHISKY.get(), "Whisky");
        add(ItemRegistries.VODKA.get(), "Vodka");
        add(ItemRegistries.TEQUILA.get(), "Tequila");
        add(ItemRegistries.RUM.get(), "Rum");
        add(ItemRegistries.BRANDY.get(), "Brandy");
        add(ItemRegistries.BUBBLE.get(), "Bubble");
        add(ItemRegistries.CONTENT_HOLDER.get(), "Shake Content Holder");
        add(ItemRegistries.LEMON.get(), "Lemon");
        add(ItemRegistries.LEMON_SLICE.get(), "Lemon Slice");
        add(ItemRegistries.SOBERING_TEA.get(), "Sobering Tea");
        add(ItemRegistries.SHAKE.get(), "Shake");
        add(ItemRegistries.SHAKE_CUP.get(), "Shake Cup");
        add("itemGroup.shakenstir", "Shake n Stir");
        add("itemGroup." + MODID + ".tab", "Shake n Stir");
        add(ItemRegistries.ICE_CUBE.get(), "Ice Cube");
        add(ItemRegistries.LONG_DRINK_GLASSWARE.get(), "Long Drink Glassware");
        add(ItemRegistries.SHORT_DRINK_GLASSWARE.get(), "Short Drink Glassware");
//        add(FluidRegistries.GIN_SOURCE_FLUID_BLOCK.getRegisteredName(), "");
        add(FluidTypeRegistries.GIN_FLUID_TYPE.get().getDescriptionId(), "Gin");
        add(FluidTypeRegistries.WHISKY_FLUID_TYPE.get().getDescriptionId(), "Whisky");
        add(FluidTypeRegistries.VODKA_FLUID_TYPE.get().getDescriptionId(), "Vodka");
        add(FluidTypeRegistries.TEQUILA_FLUID_TYPE.get().getDescriptionId(), "Tequila");
        add(FluidTypeRegistries.RUM_FLUID_TYPE.get().getDescriptionId(), "Rum");
        add(FluidTypeRegistries.BRANDY_FLUID_TYPE.get().getDescriptionId(), "Brandy");

        add(String.valueOf(CocktailTypes.SOUR_VALUE.translationKey()    )   , "%s %s Sour"    );
        add(String.valueOf(CocktailTypes.COCKTAIL_VALUE.translationKey())   , "%s %s Cocktail");
        add(String.valueOf(CocktailTypes.HIGHBALL_VALUE.translationKey())   , "%s %s Colada"  );
        add(String.valueOf(CocktailTypes.TONIC_VALUE.translationKey()   )   , "%s %s Fizz"    );
        add(String.valueOf(CocktailTypes.COLADA_VALUE.translationKey()  )   , "%s Highball"   );
        add(String.valueOf(CocktailTypes.FIZZ_VALUE.translationKey()    )   , "%s %s Tonic"   );

        add(MobEffectRegistries.DRUNK.get().getDescriptionId(), "Drunk");
        add(MobEffectRegistries.FALL_DOWN.get().getDescriptionId(), "Fall Down");
        add(MobEffectRegistries.LEMON.get().getDescriptionId(), "Lemon");
        add(MobEffectRegistries.PARALYSIS.get().getDescriptionId(), "Paralysis");

        add("death.attack." + SnsDamageType.PARALYSIS.identifier(), "When %s reacted, he has already dead.");
        add("death.attack." + SnsDamageType.PARALYSIS.identifier() + ".player", "When %s reacted, he has already been killed by %s");
        add("death.attack." + SnsDamageType.PARALYSIS.identifier() + ".item", "When %s reacted, he has already dead.");

        add(FluidTypeRegistries.BUBBLE_FLUID_TYPE.get().getDescriptionId(), "Bubble");
        genLang(this);
    }

    public static void genLang(LanguageProvider pvd) {
        for (LangData lang : LangData.values()) {
            pvd.add(lang.key, lang.def);
        }
    }

    public void add(ResourceKey<DamageType> damageType, String def) {
        add("death.attack." + damageType.identifier(), def);
    }
    public void add(ResourceKey<DamageType> damageType, String fromPlayer, String def) {
        add("death.attack." + damageType.identifier(), def);
        add("death.attack." + damageType.identifier() + ".player", fromPlayer);
    }

    public void add(ResourceKey<DamageType> damageType, String fromPlayer, String fromItem, String def) {
        add("death.attack." + damageType.identifier(), def);
        add("death.attack." + damageType.identifier() + ".player", fromPlayer);
        add("death.attack." + damageType.identifier() + ".item", fromItem);
    }
}
