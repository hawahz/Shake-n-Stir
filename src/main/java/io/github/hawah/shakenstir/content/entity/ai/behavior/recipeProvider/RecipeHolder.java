package io.github.hawah.shakenstir.content.entity.ai.behavior.recipeProvider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.util.SerializeHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.BiConsumer;

public record RecipeHolder(
        Type recipe,
        List<ItemStack> requiredItems,
        List<FluidStack> requiredFluids,
        ItemStack result
) {

    public static final Codec<RecipeHolder> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            SerializeHelper.ofEnum(Type.class).fieldOf("recipe").forGetter(RecipeHolder::recipe),
            ItemStack.CODEC.listOf().fieldOf("required_items").forGetter(RecipeHolder::requiredItems),
            FluidStack.CODEC.listOf().fieldOf("required_fluids").forGetter(RecipeHolder::requiredFluids),
            ItemStack.CODEC.fieldOf("result_factory").forGetter(RecipeHolder::result)
    ).apply(inst, RecipeHolder::new));

    public enum Type {
        SHAKE(GlasswareBlockEntity::pourProduct),
        STIR((g, i) -> {})
        ;
        private final BiConsumer<GlasswareBlockEntity, ItemStack> action;

        Type(BiConsumer<GlasswareBlockEntity, ItemStack> action) {
            this.action = action;
        }

        public void apply(GlasswareBlockEntity glassware, ItemStack itemStack) {
            action.accept(glassware, itemStack);
        }
    }
}
