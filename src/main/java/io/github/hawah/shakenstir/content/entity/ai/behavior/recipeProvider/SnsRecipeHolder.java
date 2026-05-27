package io.github.hawah.shakenstir.content.entity.ai.behavior.recipeProvider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.blockEntity.GlasswareBlockEntity;
import io.github.hawah.shakenstir.util.SerializeHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.BiConsumer;

public record SnsRecipeHolder(
        Type recipe,
        List<ItemStack> requiredItems,
        List<FluidStack> requiredFluids,
        ItemStack result
) {

    public static final Codec<SnsRecipeHolder> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            SerializeHelper.ofEnum(Type.class).fieldOf("recipe").forGetter(SnsRecipeHolder::recipe),
            ItemStack.CODEC.listOf().fieldOf("required_items").forGetter(SnsRecipeHolder::requiredItems),
            FluidStack.CODEC.listOf().fieldOf("required_fluids").forGetter(SnsRecipeHolder::requiredFluids),
            ItemStack.CODEC.fieldOf("result_factory").forGetter(SnsRecipeHolder::result)
    ).apply(inst, SnsRecipeHolder::new));

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
