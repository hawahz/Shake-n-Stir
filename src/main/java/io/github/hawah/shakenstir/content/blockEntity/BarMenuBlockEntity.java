package io.github.hawah.shakenstir.content.blockEntity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.lib.util.MutablePair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BarMenuBlockEntity extends AutoUpdateBlockEntity {

    public static final Codec<MutablePair<SnsRecipeHolder, PriceAndCount>> RECIPIES_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SnsRecipeHolder.CODEC.fieldOf("Recipe").forGetter(MutablePair<SnsRecipeHolder, PriceAndCount>::left),
            PriceAndCount.CODEC.fieldOf("counts").forGetter(MutablePair<SnsRecipeHolder, PriceAndCount>::right)
    ).apply(instance, MutablePair::of));

    public static final StreamCodec<RegistryFriendlyByteBuf, MutablePair<SnsRecipeHolder, PriceAndCount>> RECIPES_STREAM_CODEC = StreamCodec.composite(
            SnsRecipeHolder.STREAM_CODEC, MutablePair::left,
            PriceAndCount.STREAM_CODEC, MutablePair::right,
            MutablePair::of
    );

    public static final Codec<List<MutablePair<SnsRecipeHolder, PriceAndCount>>> LIST_RECIPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RECIPIES_CODEC.listOf().fieldOf("Recipes").forGetter((l) -> l)
    ).apply(instance, ArrayList::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, List<MutablePair<SnsRecipeHolder, PriceAndCount>>> LIST_RECIPES_STREAM_CODEC = StreamCodec.composite(
            RECIPES_STREAM_CODEC.apply(ByteBufCodecs.list()), (l) -> l,
            ArrayList::new
    );

    public BarMenuBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.BAR_MENU_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    private UUID placerId = null;
    public List<MutablePair<SnsRecipeHolder, PriceAndCount>> recipes = new ArrayList<>();
    public boolean dirty = true;

    // Temp
    public List<FormattedText> content = new ArrayList<>();

    public void setRecipePrice(int index, int price) {
        recipes.get(index).right().price = price;
        setRecipeDirty();
    }

    public void setRecipeCount(int index, int count) {
        recipes.get(index).right().count = count;
        setRecipeDirty();
    }

    public void setRecipeItem(int index, ItemStack item) {
        if (item.isEmpty()) {
            recipes.get(index).right().item = ItemResource.EMPTY;
        } else {
            recipes.get(index).right().item = ItemResource.of(item);
        }
        setRecipeDirty();
    }

    public void addRecipe(SnsRecipeHolder recipe) {
        recipes.add(new MutablePair<>(recipe, new PriceAndCount(0, 0, ItemResource.EMPTY)));
        setRecipeDirty();
    }

    public void setPriceAndCount(int index, PriceAndCount priceAndCount) {
        if (priceAndCount.item == null || priceAndCount.item.isEmpty()) {
            priceAndCount.item = ItemResource.EMPTY;
        }
        recipes.get(index).setRight(priceAndCount);
        setRecipeDirty();
    }

    public void setRecipe(int index, SnsRecipeHolder recipeHolder) {
        recipes.get(index).setLeft(recipeHolder);
        setRecipeDirty();
    }

    public void setRecipeDirty() {
        dirty = true;
    }

    private final List<ItemStack> cachedRecipeCosts = new ArrayList<>();

    public List<ItemStack> getRecipeCosts() {
        if (!dirty) {
            return cachedRecipeCosts;
        }
        cachedRecipeCosts.clear();
        for (MutablePair<SnsRecipeHolder, PriceAndCount> recipe : recipes) {
            if (recipe.right().count > 0) {
                boolean found = false;
                for (ItemStack cachedRecipeCost : cachedRecipeCosts) {
                    ItemStack cost = cachedRecipeCost.copy();
                    if (recipe.right().item.test(itemStack -> ItemStack.isSameItemSameComponents(cost, itemStack))) {
                        cost.grow(recipe.right().count);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    cachedRecipeCosts.add(recipe.right().item.toStack(recipe.right().count));
                }
            }
        }
        return cachedRecipeCosts;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        placerId = input.read("PlacerId", UUIDUtil.CODEC).orElse(null);
        recipes.clear();
        input.read("Recipes", LIST_RECIPE_CODEC).ifPresent(recipes::addAll);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (placerId != null) {
            output.store("PlacerId", UUIDUtil.CODEC, placerId);
        }
        output.store("Recipes", LIST_RECIPE_CODEC, recipes);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        placerId = components.get(DataComponentTypeRegistries.PLACER);
        this.recipes = components.getOrDefault(DataComponentTypeRegistries.RECIPES_DATA, new ArrayList<>());
    }

    public UUID getPlacerId() {
        return placerId;
    }

    public static class PriceAndCount {
        public int price = 0;
        public int count = 0;
        public ItemResource item;
        public static final Codec<PriceAndCount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("price").forGetter(PriceAndCount::getPrice),
                Codec.INT.fieldOf("count").forGetter(PriceAndCount::getCount),
                ItemResource.OPTIONAL_CODEC.fieldOf("item").forGetter(PriceAndCount::getItem)
        ).apply(instance, PriceAndCount::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PriceAndCount> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, PriceAndCount::getPrice,
                ByteBufCodecs.INT, PriceAndCount::getCount,
                ItemResource.STREAM_CODEC, PriceAndCount::getItem,
                PriceAndCount::new
        );

        public PriceAndCount(int price, int count, ItemResource item) {
            this.price = price;
            this.count = count;
            this.item = item != null ? item : ItemResource.EMPTY;
        }

        private int getPrice() {
            return price;
        }

        private int getCount() {
            return count;
        }

        private ItemResource getItem() {
            return item;
        }

        public PriceAndCount copy() {
            return new PriceAndCount(price, count, ItemResource.of(item.toStack()));
        }
    }
}
