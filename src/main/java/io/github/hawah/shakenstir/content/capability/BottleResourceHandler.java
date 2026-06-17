package io.github.hawah.shakenstir.content.capability;

import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Objects;

public final class BottleResourceHandler extends ItemAccessResourceHandler<FluidResource> {

    public static final int BOTTLE_VOLUME = FluidType.BUCKET_VOLUME / (LayeredCauldronBlock.LEVEL.getPossibleValues().size() + 1);

    public BottleResourceHandler(ItemAccess itemAccess) {
        super(itemAccess, 1);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        PotionContents potion;
        if (accessResource.getItem() instanceof PotionItem) {
            potion = accessResource.get(DataComponents.POTION_CONTENTS);
            if (potion == null) {
                return FluidResource.EMPTY;
            }
            if (potion.is(Potions.WATER)) {
                return FluidResource.of(Fluids.WATER);
            }
        }
        if (accessResource.is(Items.HONEY_BOTTLE)) {
            return FluidResource.of(FluidRegistries.HONEY_SOURCE);
        }
        return FluidResource.EMPTY;

    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        var resource = getResourceFrom(accessResource, index);
        return resource.isEmpty() ? 0 : BOTTLE_VOLUME;
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        if (newAmount == 0) {
            return ItemResource.of(Items.GLASS_BOTTLE);
        } else if (newAmount != BOTTLE_VOLUME) {
            return ItemResource.EMPTY;
        } else {
            FluidStack newStack = newResource.toStack(newAmount);
            if (newStack.is(FluidTags.WATER)) {
                ItemStack stack = Items.POTION.getDefaultInstance();
                stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
                return ItemResource.of(stack);
            } else if (newStack.is(Tags.Fluids.HONEY)) {
                return ItemResource.of(Items.HONEY_BOTTLE);
            }
            return ItemResource.EMPTY;
        }
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return BOTTLE_VOLUME;
    }
}

