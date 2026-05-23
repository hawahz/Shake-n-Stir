package io.github.hawah.shakenstir.content.blockEntity;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.recipe.DistillerRecipe;
import io.github.hawah.shakenstir.content.recipe.DistillerRecipeInput;
import io.github.hawah.shakenstir.content.recipe.RecipeTypeRegistries;
import io.github.hawah.shakenstir.util.FluidStackWithSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DistillerBlockEntity extends BlockEntity implements ItemOwner {
    public static final int MAX_INPUT_ITEMS = 16;
    public static final int MAX_INPUT_FLUID_CAPACITY = 4000;
    public static final int MAX_PRODUCT_FLUID_CAPACITY = 4000;

    public float animationHeight = 0;
    public float oAnimationHeight = 0;

    public float liquidAnimationHeight = 0;
    public float oLiquidAnimationHeight = 0;

    private int burnTicks = 0;
    @Nullable
    private DistillerRecipe currentRecipe = null;
    private int recipeProgress = 0;

    final InputItemHandler inputItemHandler = new InputItemHandler();
    final InputFluidHandler inputFluidHandler = new InputFluidHandler();
    final ProductFluidHandler productHandler = new ProductFluidHandler();
    final FuelItemHandler fuelHandler = new FuelItemHandler();

    public DistillerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityRegistries.DISTILLER_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    // ---- Direct BE methods ----

    public boolean insertItem(ItemStack stack, Player player) {
        if (stack.isEmpty()) return false;
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = inputItemHandler.insert(0, ItemResource.of(stack), 1, tx);
            if (inserted > 0) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                    if (stack.getCraftingRemainder() != null) {
                        player.addItem(stack.getCraftingRemainder().create());
                    }
                }
                level().playSound(null, worldPosition, SoundEvents.BONE_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    public ItemStack popItem() {
        try (Transaction tx = Transaction.openRoot()) {
            for (int i = MAX_INPUT_ITEMS - 1; i >= 0; i--) {
                ItemResource resource = inputItemHandler.getResource(i);
                if (!resource.isEmpty()) {
                    int extracted = inputItemHandler.extract(i, resource, 1, tx);
                    if (extracted > 0) {
                        return resource.toStack(extracted);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean insertFluid(FluidStack fluid, boolean isCreative) {
        if (fluid.isEmpty()) return false;
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = inputFluidHandler.insert(0, FluidResource.of(fluid), fluid.getAmount(), tx);
            if (inserted > 0) {
                if (!isCreative) {
                    fluid.shrink(inserted);
                }
                level().playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                markChanged();
                return true;
            }
        }
        return false;
    }

    public boolean insertFuel(ItemStack stack, Player player) {
        // TODO 燃料Tags
        if (!stack.is(ItemTags.FURNACE_MINECART_FUEL)) {
            return false;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            return stack.is(ItemTags.FURNACE_MINECART_FUEL);
        }
        int fuelValue = stack.getBurnTime(null, serverLevel.fuelValues());
        if (fuelValue <= 0) return false;
        burnTicks += fuelValue;
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        serverLevel.playSound(null, worldPosition, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        markChanged();
        return true;
    }

    int extractProductInternal(FluidResource resource, int amount, TransactionContext tx) {
        if (!resource.matches(productHandler.fluid)) return 0;
        int extracted = Math.min(productHandler.fluid.getAmount(), amount);
        productHandler.fluid.shrink(extracted);
        markChanged();
        return extracted;
    }

    // ---- Render getters ----

    public NonNullList<ItemStack> getInputItems() {
        NonNullList<ItemStack> copy = NonNullList.withSize(MAX_INPUT_ITEMS, ItemStack.EMPTY);
        for (int i = 0; i < MAX_INPUT_ITEMS; i++) {
            copy.set(i, inputItemHandler.items.get(i).copy());
        }
        return copy;
    }

    public FluidStack getInputFluid() {
        return inputFluidHandler.fluid.copy();
    }

    public ResourceHandler<FluidResource> getInputFluidHandler() {
        return inputFluidHandler;
    }

    public FluidStack getProduct() {
        return productHandler.fluid.copy();
    }

    public int getBurnTicks() {
        return burnTicks;
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    public int getMaxProgress() {
        return currentRecipe != null ? currentRecipe.cookingTime() : 0;
    }

    // ---- Tick ----

    public static void serverTick(Level level, BlockPos pos, BlockState state, DistillerBlockEntity be) {
        if (be.burnTicks > 0) {
            be.burnTicks--;
        }

        if (be.burnTicks > 0) {
            List<ItemStack> nonEmptyItems = new ArrayList<>();
            for (int i = 0; i < be.inputItemHandler.items.size(); i++) {
                ItemStack stack = be.inputItemHandler.items.get(i);
                if (!stack.isEmpty()) {
                    nonEmptyItems.add(stack.copy());
                }
            }
            DistillerRecipeInput input = new DistillerRecipeInput(nonEmptyItems, be.inputFluidHandler.fluid.copy());

            RecipeManager recipeManager = level.getServer().getRecipeManager();
            Optional<net.minecraft.world.item.crafting.RecipeHolder<DistillerRecipe>> optionalRecipe =
                    recipeManager.getRecipeFor(RecipeTypeRegistries.DISTILLER_RECIPE.get(), input, level);

            if (optionalRecipe.isPresent()) {
                DistillerRecipe recipe = optionalRecipe.get().value();
                if (recipe.equals(be.currentRecipe)) {
                    be.recipeProgress++;
                } else {
                    be.currentRecipe = recipe;
                    be.recipeProgress = 0;
                }

                if (be.recipeProgress >= recipe.cookingTime()) {
                    craft(be, recipe);
                    be.currentRecipe = null;
                    be.recipeProgress = 0;
                }
            } else {
                be.currentRecipe = null;
                be.recipeProgress = 0;
            }
        } else {
            be.currentRecipe = null;
            be.recipeProgress = 0;
        }

        be.markChanged();
    }

    private static void craft(DistillerBlockEntity be, DistillerRecipe recipe) {
        List<ItemStack> remaining = new ArrayList<>();
        for (int i = 0; i < be.inputItemHandler.items.size(); i++) {
            ItemStack stack = be.inputItemHandler.items.get(i);
            if (!stack.isEmpty()) {
                remaining.add(stack);
            }
        }

        for (Ingredient ingredient : recipe.inputItems()) {
            Optional<ItemStack> matched = remaining.stream()
                    .filter(ingredient)
                    .findFirst();
            matched.ifPresent(stack -> {
                stack.shrink(1);
                remaining.remove(stack);
            });
        }

        be.inputFluidHandler.fluid.shrink(recipe.inputFluid().amount());

        ItemStack resultStack = recipe.result().create();
        SpiritContent content = resultStack.getOrDefault(DataComponentTypeRegistries.SPIRIT_CONTENT, SpiritContent.EMPTY);
        be.productHandler.fluid = content.fluidStack().copy();
    }

    public static void animationTick(Level level, BlockPos pos, BlockState state, DistillerBlockEntity be) {

        if (level.isClientSide()) {
            be.oAnimationHeight = be.animationHeight;
            be.animationHeight = Mth.lerp(ShakenStirClient.ANI_DELTAF * 0.3F, be.animationHeight, be.burnTicks > 0 ? 1.0F : 0.0F);

            float targetHeight = be.inputFluidHandler.fluid.amount() / (float) MAX_INPUT_FLUID_CAPACITY;
            be.oLiquidAnimationHeight = be.liquidAnimationHeight;
            be.liquidAnimationHeight = Mth.lerp(ShakenStirClient.ANI_DELTAF * 0.3F, be.liquidAnimationHeight, targetHeight);
            RandomSource random = level.getRandom();
            if (be.burnTicks > 0 && !be.inputFluidHandler.fluid.isEmpty() && random.nextFloat() < 0.11F) {
                for (int i = 0; i < random.nextInt(2) + 2; i++) {
                    CampfireBlock.makeParticles(level, pos.above(), false, false);
                }
            }
        }
    }

    // ---- Save / Load / Sync ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inputItemHandler.items);
        inputFluidHandler.fluid = loadFluid(input, "InputFluid");
        productHandler.fluid = loadFluid(input, "Product");
        burnTicks = input.getInt("BurnTicks").orElse(0);
        recipeProgress = input.getInt("RecipeProgress").orElse(0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inputItemHandler.items, true);
        saveFluid(output, "InputFluid", inputFluidHandler.fluid);
        saveFluid(output, "Product", productHandler.fluid);
        output.putInt("BurnTicks", burnTicks);
        output.putInt("RecipeProgress", recipeProgress);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LogUtils.getLogger())) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            tag = output.buildResult();
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void markChanged() {
        setChanged();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.players().forEach(
                    player -> player.connection.send(getUpdatePacket())
            );
            //noinspection UnstableApiUsage
            net.neoforged.neoforge.attachment.AttachmentSync.syncBlockEntityUpdates(this, serverLevel.players());
        }
    }

    private static void saveFluid(ValueOutput output, String key, FluidStack fluid) {
        ValueOutput.TypedOutputList<FluidStackWithSlot> list = output.list(key, FluidStackWithSlot.CODEC);
        if (!fluid.isEmpty()) {
            list.add(new FluidStackWithSlot(0, fluid));
        }
    }

    private static FluidStack loadFluid(ValueInput input, String key) {
        for (FluidStackWithSlot item : input.listOrEmpty(key, FluidStackWithSlot.CODEC)) {
            if (item.slot() == 0) {
                return item.stack();
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public Level level() {
        assert getLevel() != null;
        return getLevel();
    }

    @Override
    public Vec3 position() {
        return worldPosition.getCenter();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return 0;
    }

    // ---- ResourceHandler inner classes ----

    class InputItemHandler implements ResourceHandler<ItemResource> {
        final NonNullList<ItemStack> items = NonNullList.withSize(MAX_INPUT_ITEMS, ItemStack.EMPTY);

        @Override
        public int size() {
            return MAX_INPUT_ITEMS;
        }

        @Override
        public ItemResource getResource(int index) {
            if (index >= size() || index < 0) return ItemResource.EMPTY;
            return ItemResource.of(items.get(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            if (index >= size() || index < 0) return 0L;
            return items.get(index).isEmpty() ? 0L : 1L;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            if (index >= size() || index < 0) return 0L;
            return 1L;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index >= 0 && index < size();
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (amount == 0) return 0;
            for (int i = 0; i < MAX_INPUT_ITEMS; i++) {
                if (items.get(i).isEmpty()) {
                    items.set(i, resource.toStack());
                    markChanged();
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (amount == 0) return 0;
            for (int i = MAX_INPUT_ITEMS - 1; i >= 0; i--) {
                if (ItemResource.of(items.get(i)).equals(resource)) {
                    items.set(i, ItemStack.EMPTY);
                    markChanged();
                    return 1;
                }
                if (!items.get(i).isEmpty()) {
                    return 0;
                }
            }
            return 0;
        }
    }

    class InputFluidHandler implements ResourceHandler<FluidResource> {
        FluidStack fluid = FluidStack.EMPTY;

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int index) {
            if (index != 0) return FluidResource.EMPTY;
            return FluidResource.of(fluid);
        }

        @Override
        public long getAmountAsLong(int index) {
            if (index != 0) return 0L;
            return fluid.getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            if (index != 0) return 0L;
            return MAX_INPUT_FLUID_CAPACITY;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return index == 0 && (fluid.isEmpty() || resource.matches(fluid));
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (amount == 0 || index != 0) return 0;
            if (!fluid.isEmpty() && !resource.matches(fluid)) return 0;
            int space = MAX_INPUT_FLUID_CAPACITY - fluid.getAmount();
            int toInsert = Math.min(amount, space);
            if (toInsert <= 0) return 0;
            if (fluid.isEmpty()) {
                fluid = resource.toStack(toInsert);
            } else {
                fluid.grow(toInsert);
            }
            markChanged();
            return toInsert;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (amount == 0 || index != 0) return 0;
            if (!resource.matches(fluid)) return 0;
            int extracted = Math.min(fluid.getAmount(), amount);
            fluid.shrink(extracted);
            markChanged();
            return extracted;
        }
    }

    class ProductFluidHandler implements ResourceHandler<FluidResource> {
        FluidStack fluid = FluidStack.EMPTY;

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int index) {
            if (index != 0) return FluidResource.EMPTY;
            return FluidResource.of(fluid);
        }

        @Override
        public long getAmountAsLong(int index) {
            if (index != 0) return 0L;
            return fluid.getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            if (index != 0) return 0L;
            return MAX_PRODUCT_FLUID_CAPACITY;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return index == 0;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0; // extract-only from external perspective
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return extractProductInternal(resource, amount, transaction);
        }
    }

    class FuelItemHandler implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return 0L;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return 1L;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return index == 0 && resource.toStack().getBurnTime(null, level().getServer().fuelValues()) > 0;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            int burnTime = resource.toStack().getBurnTime(null, level().getServer().fuelValues());
            if (burnTime <= 0) return 0;
            burnTicks += burnTime;
            markChanged();
            return 1;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }
}