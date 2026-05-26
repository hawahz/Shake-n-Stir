package io.github.hawah.shakenstir.client.render.block.renderstate;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;

public class BarMenuBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing;
    public List<FormattedText> content = new ArrayList<>();
}
