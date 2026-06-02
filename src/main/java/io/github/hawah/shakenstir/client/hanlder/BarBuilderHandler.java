package io.github.hawah.shakenstir.client.hanlder;

import io.github.hawah.shakenstir.content.dataComponent.BarAreaHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.networking.ServerboundHandItemDataChangedPacket;
import io.github.hawah.shakenstir.lib.client.handler.AbstractBoxHandler;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import static io.github.hawah.shakenstir.client.hanlder.PACKAGE.getPlayer;


public class BarBuilderHandler extends AbstractBoxHandler {

    protected boolean skipSelection = false;

    public BarBuilderHandler() {
        super(false);
    }

    @Override
    protected boolean onSetSecondPos(BlockPos pos) {
        return false;
    }

    @Override
    protected boolean onCheck() {
        super.onCheck();
        if (!isValidSize()) {
            return true;
        }
        BlockPos first = firstPos;
        BlockPos second = secondPos == null? (selectedPos == null? firstPos: selectedPos) : secondPos;
        BoundingBox boundingBox = BoundingBox.fromCorners(first, second);
        ItemStack item = getPlayer().getMainHandItem();
        item.set(DataComponentTypeRegistries.BAR_AREA, new BarAreaHolder(boundingBox, Minecraft.getInstance().level.dimension()));
        Networking.sendToServer(new ServerboundHandItemDataChangedPacket(getPlayer().getUUID(), InteractionHand.MAIN_HAND, item));
        getPlayer().swing(InteractionHand.MAIN_HAND);
        skipSelection = true;
        selectedPos = null;
        return true;
    }

    @Override
    protected boolean onRightClickPre() {
        if (skipSelection) {
            skipSelection = false;
        }
        return super.onRightClickPre();
    }

    @Override
    protected boolean onDelete() {
        ItemStack item = getPlayer().getMainHandItem();
        item.remove(DataComponentTypeRegistries.BAR_AREA);
        return super.onDelete();
    }

    @Override
    public void tick() {
        if (!isVisible()) {
            fadeOutline();
        }

        if (!isActive()) {
            skipSelection = true;
            discard();
            return;
        }
        BarAreaHolder barAreaData;
        if (skipSelection && (barAreaData = getPlayer().getMainHandItem().get(DataComponentTypeRegistries.BAR_AREA)) != null) {
            BoundingBox barArea = barAreaData.area();
            firstPos = new BlockPos(barArea.minX(), barArea.minY(), barArea.minZ());
            secondPos = new BlockPos(barArea.maxX(), barArea.maxY(), barArea.maxZ());
            submitOutline(0.5);
            return;
        }
        super.tick();
    }

    @Override
    public boolean isActive() {
        return getPlayer() != null &&
                getPlayer().getMainHandItem().is(ItemRegistries.RAG);
    }
}
