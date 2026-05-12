package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.item.PriorityBlockItem;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

public class GlasswareItem extends PriorityBlockItem {
    public GlasswareItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext placeContext) {
        if (!Direction.UP.equals(placeContext.getClickedFace())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return super.place(placeContext);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult interactionResult = super.useOn(context);
        context.getItemInHand().remove(DataComponentTypeRegistries.GLASSWARE_POSITION);
        context.getItemInHand().remove(DataComponentTypeRegistries.GLASSWARE_ROTATION);
        return interactionResult;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext placeContext, BlockState placementState) {
        if (!Direction.UP.equals(placeContext.getClickedFace())) {
            return false;
        }
        if (super.placeBlock(placeContext, placementState)) {
            Vec3 clickLocation = placeContext.getClickLocation().subtract(placeContext.getClickedPos().getX(), placeContext.getClickedPos().getY(), placeContext.getClickedPos().getZ());
            Vector2f localPos = new Vector2f((float) clickLocation.x(), (float) clickLocation.z());
            final float DEAD_ZONE = 0.25F;
            localPos.set(
                    Mth.clamp(localPos.x(), DEAD_ZONE, 1 - DEAD_ZONE),
                    Mth.clamp(localPos.y(), DEAD_ZONE, 1 - DEAD_ZONE)
            );
            placeContext.getItemInHand().set(DataComponentTypeRegistries.GLASSWARE_POSITION, localPos);
            assert placeContext.getPlayer() != null;
            placeContext.getItemInHand().set(DataComponentTypeRegistries.GLASSWARE_ROTATION, placeContext.getPlayer().getYRot());
            return true;
        };
        return false;
    }
}
