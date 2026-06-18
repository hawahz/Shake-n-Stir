package io.github.hawah.shakenstir.foundation.datagen;

import io.github.hawah.shakenstir.content.damageType.SnsDamageType;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.recipe.datapack.cocktaileType.CocktailTypes;
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
        add(ItemRegistries.CABINET.get(), "Cabinet");
        add(ItemRegistries.DISTILLER.get(), "Distiller");
        add(ItemRegistries.BOTTLE.get(), "Bottle");
        add(ItemRegistries.CONTENT_HOLDER.get(), "Shake Content Holder");
        add(ItemRegistries.LEMON.get(), "Lemon");
        add(ItemRegistries.LEMON_SLICE.get(), "Lemon Slice");
        add(ItemRegistries.SOBERING_TEA.get(), "Sobering Tea");
        add(ItemRegistries.SHAKER.get(), "Shaker");
        add(ItemRegistries.SHAKER_LID.get(), "Shaker Lid");
        add(ItemRegistries.DIALOGUE_EDITOR.get(), "Dialogue Editor");
        add(ItemRegistries.MINT.get(), "Mint");
        add(ItemRegistries.STACKED_MINT.get(), "Stacked Mint");
        add(ItemRegistries.SQUEEZER.get(), "Squeezer");
        add("itemGroup.shakenstir", "Shake n Stir");
        add("itemGroup." + MODID + ".tab", "Shake n Stir");
        add("itemGroup." + MODID + ".tab.bar", "Shake n Stir - Bar");
        add("itemGroup." + MODID + ".tab.bartending", "Shake n Stir - Bartending");
        add("itemGroup." + MODID + ".tab.decoration", "Shake n Stir - Decoration");
        add(ItemRegistries.ICE_CUBE.get(), "Ice Cube");
        add(ItemRegistries.LONG_DRINK_GLASSWARE.get(), "Long Drink Glassware");
        add(ItemRegistries.SHORT_DRINK_GLASSWARE.get(), "Short Drink Glassware");
        add(ItemRegistries.BAR_COUNTER.get(), "Bar Counter");
        add(ItemRegistries.MENU.get(), "Menu");
        add(ItemRegistries.RAG.get(), "Rag");
        add(ItemRegistries.RECIPE_SCROLL.get(), "Recipe Scroll");
        add(ItemRegistries.BARTENDER_SPAWNER.get(), "Bartender's Tie");
        add(ItemRegistries.BARTENDER_GLOVE.get(), "Bartender's Glove");
        add(ItemRegistries.TONIC.get(), "Tonic");
        add(ItemRegistries.BITTERS.get(), "Bitters");
        add(ItemRegistries.LEMON_TOP_LEAVES.get(), "Lemon Top Leaves");
        add(ItemRegistries.LEMON_LEAVES.get(), "Lemon Leaves");
        add(ItemRegistries.LEMON_LOG.get(), "Lemon Log");
        add(ItemRegistries.LEMON_SIDE_LEAVES.get(), "Lemon Side Leaves");
        add(ItemRegistries.LEMON_SAPLING.get(), "Lemon Sapling");
        add(ItemRegistries.MINT_SEED.get(), "Mint Seed");

//        add(FluidRegistries.GIN_SOURCE_FLUID_BLOCK.getRegisteredName(), "");
        add(FluidTypeRegistries.GIN_FLUID_TYPE.get().getDescriptionId(), "Gin");
        add(FluidTypeRegistries.WHISKY_FLUID_TYPE.get().getDescriptionId(), "Whisky");
        add(FluidTypeRegistries.VODKA_FLUID_TYPE.get().getDescriptionId(), "Vodka");
        add(FluidTypeRegistries.TEQUILA_FLUID_TYPE.get().getDescriptionId(), "Tequila");
        add(FluidTypeRegistries.RUM_FLUID_TYPE.get().getDescriptionId(), "Rum");
        add(FluidTypeRegistries.BRANDY_FLUID_TYPE.get().getDescriptionId(), "Brandy");
        add(FluidTypeRegistries.BUBBLE_FLUID_TYPE.get().getDescriptionId(), "Bubble");
        add(FluidTypeRegistries.TONIC_FLUID_TYPE.get().getDescriptionId(), "Tonic");
        add(FluidTypeRegistries.BITTERS_FLUID_TYPE.get().getDescriptionId(), "Bitters");
        add(FluidTypeRegistries.LEMONADE_FLUID_TYPE.get().getDescriptionId(), "Lemonade");
        add(FluidTypeRegistries.JUICE_FLUID_TYPE.get().getDescriptionId(), "Juice");
        add(FluidTypeRegistries.HONEY_FLUID_TYPE.get().getDescriptionId(), "Honey");
        add(FluidTypeRegistries.SUCROSE_SYRUP_FLUID_TYPE.get().getDescriptionId(), "Sucrose Syrup");

        add(String.valueOf(CocktailTypes.SOUR_VALUE.translationKey()    )   , "%s %s Sour"    );
        add(String.valueOf(CocktailTypes.COCKTAIL_VALUE.translationKey())   , "%s %s Cocktail");
        add(String.valueOf(CocktailTypes.HIGHBALL_VALUE.translationKey())   , "%s Highball"   );
        add(String.valueOf(CocktailTypes.TONIC_VALUE.translationKey()   )   , "%s %s Tonic"   );
        add(String.valueOf(CocktailTypes.COLADA_VALUE.translationKey()  )   , "%s %s Colada"  );
        add(String.valueOf(CocktailTypes.FIZZ_VALUE.translationKey()    )   , "%s %s Fizz"    );

        add(MobEffectRegistries.DRUNK.get().getDescriptionId(), "Drunk");
        add(MobEffectRegistries.FALL_DOWN.get().getDescriptionId(), "Fall Down");
        add(MobEffectRegistries.LEMON.get().getDescriptionId(), "Lemon");
        add(MobEffectRegistries.PARALYSIS.get().getDescriptionId(), "Paralysis");

        add("death.attack." + SnsDamageType.PARALYSIS.identifier(), "When %s reacted, he has already dead.");
        add("death.attack." + SnsDamageType.PARALYSIS.identifier() + ".player", "When %s reacted, he has already been killed by %s");
        add("death.attack." + SnsDamageType.PARALYSIS.identifier() + ".item", "When %s reacted, he has already dead.");

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
