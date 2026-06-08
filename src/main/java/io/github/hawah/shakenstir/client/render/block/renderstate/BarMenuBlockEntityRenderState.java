package io.github.hawah.shakenstir.client.render.block.renderstate;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;

public class BarMenuBlockEntityRenderState extends BlockEntityRenderState {
    public Direction facing;
    public final List<FormattedText> content = new ArrayList<>();
    public final List<ItemClusterRenderState> displayItems = new ArrayList<>();
    public float spin = 0;
    public List<Integer> displayItemCounts = new ArrayList<>();
}
