package io.github.hawah.shakenstir.client.render.block.renderstate;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class CabinetBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing;
    public final List<Either<BlockModelRenderState, ItemStackRenderState>> renderStateEither = new ArrayList<>();
}
