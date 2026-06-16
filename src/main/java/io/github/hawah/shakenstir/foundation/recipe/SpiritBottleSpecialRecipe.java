package io.github.hawah.shakenstir.foundation.recipe;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.item.SpiritBottleItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpiritBottleSpecialRecipe extends CustomRecipe {

    public static final MapCodec<SpiritBottleSpecialRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                            Ingredient.CODEC.fieldOf("bottle").forGetter(o -> o.bottle),
                            Ingredient.CODEC.fieldOf("target").forGetter(o -> o.target),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result)
                    )
                    .apply(i, SpiritBottleSpecialRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritBottleSpecialRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            o -> o.bottle,
            Ingredient.CONTENTS_STREAM_CODEC,
            o -> o.target,
            ItemStackTemplate.STREAM_CODEC,
            o -> o.result,
            SpiritBottleSpecialRecipe::new
    );
    public static final RecipeSerializer<SpiritBottleSpecialRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
    private final Ingredient bottle;
    private final Ingredient target;
    private final ItemStackTemplate result;

    public SpiritBottleSpecialRecipe(Ingredient banner, Ingredient target, ItemStackTemplate result) {
        this.bottle = banner;
        this.target = target;
        this.result = result;
    }
    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 2) {
            return false;
        }
        boolean hasDye = false;
        boolean hasBottle = false;

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack itemStack = input.getItem(slot);
            if (!itemStack.isEmpty()) {
                if (this.bottle.test(itemStack) && itemStack.getItem() instanceof SpiritBottleItem) {
                    if (hasBottle) {
                        return false;
                    }

                    hasBottle = true;
                } else {
                    if (!this.target.test(itemStack) || hasDye) {
                        return false;
                    }
                    hasDye = true;
                }
            }
        }

        return hasDye && hasBottle;

    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack itemStack = result.create();
        for (int i = 0; i < input.ingredientCount(); i++) {
            if (input.getItem(i).getItem() instanceof SpiritBottleItem) {
                itemStack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, input.getItem(i).get(DataComponentTypeRegistries.SPIRIT_CONTENT));
            }
        }
        return itemStack;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
