package io.github.hawah.shakenstir.client.render.block.renderstate;

import io.github.hawah.shakenstir.content.blockEntity.DistillerBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

public class DistillerBlockEntityRenderState extends BlockEntityRenderState {
    public final @Nullable ItemStackRenderState[] items = new ItemStackRenderState[DistillerBlockEntity.MAX_INPUT_ITEMS];
    public FluidStack inputFluid = FluidStack.EMPTY;
    public FluidStack product = FluidStack.EMPTY;
    public Direction facing;
    public int burnTicks;
    public int recipeProgress;
    public int maxProgress;
    public float animationHeight;
    public float liquidHeight;
}