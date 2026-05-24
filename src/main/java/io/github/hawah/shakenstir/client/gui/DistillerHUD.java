package io.github.hawah.shakenstir.client.gui;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.client.ClientDataHolder;
import io.github.hawah.shakenstir.client.clientTooltip.ClientProgressBarTooltip;
import io.github.hawah.shakenstir.client.clientTooltip.ItemTooltipWithCount;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.block.Distiller;
import io.github.hawah.shakenstir.content.blockEntity.DistillerBlockEntity;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix3x2fStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DistillerHUD extends AbstractBlockTargetHUD {

    float fadeInProgress = 0;

    @Override
    protected Block block() {
        return BlockRegistries.DISTILLER.get();
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!isVisible()) {
            fadeInProgress = 0;
            return;
        }

        fadeInProgress = Mth.lerp(ShakenStirClient.ANI_DELTAF * deltaTracker.getGameTimeDeltaTicks(), fadeInProgress, 1);

        BlockState state = ClientDataHolder.Picker.blockState().get();
        BlockPos pos = ClientDataHolder.Picker.pos();
        if (pos != null && (Minecraft.getInstance().level.getBlockEntity(Distiller.findSource(state, pos)) instanceof DistillerBlockEntity blockEntity)){
            List<ClientTooltipComponent> components = new ArrayList<>();
            switch (state.getValue(Distiller.PART)) {
                case UPPER -> {

                    components.add(ClientTooltipComponent.create(LangData.TOOLTIP_DISTILLER_CONTENT.get().getVisualOrderText()));
                    Map<Item, Integer> items = new HashMap<>();
                    blockEntity.getInputItems().forEach(itemStack ->
                            items.merge(
                                    itemStack.getItem(),
                                    itemStack.getCount(),
                                    Integer::sum
                            ));
                    for (ItemStack inputItem : items.entrySet().stream().map(entry -> new ItemStack(entry.getKey(), entry.getValue())).toList()) {
                        if (inputItem.isEmpty()) {
                            continue;
                        }
                        components.add(new ItemTooltipWithCount(inputItem, 0, 0));
                    }
                    float progress = blockEntity.getRecipeProgress() / (float) blockEntity.getMaxProgress();
                    final int PROGRESS_BAR_WIDTH = 25;
                    components.add(new ClientProgressBarTooltip(progress, PROGRESS_BAR_WIDTH, 0, 5));
//                    components.add(new ClientUnicodeProgressBarTooltip(progress, PROGRESS_BAR_WIDTH/2, 0, 0));
                }
                case LOWER -> {
                    int burnTicks = blockEntity.getBurnTicks();
                    components.add(ClientTooltipComponent.create(LangData.TOOLTIP_DISTILLER_BURNING_TIME.get(Component.literal(String.valueOf(burnTicks / 20)).withStyle(ChatFormatting.WHITE)).getVisualOrderText()));
                }
                case PIPE -> {
                    FluidStack product = blockEntity.getProduct();
                    if (!product.isEmpty()) {
                        new SpiritContent(product).addToTooltip(
                                Item.TooltipContext.EMPTY,
                                (component) -> components.add(ClientTooltipComponent.create(component.getVisualOrderText())),
                                TooltipFlag.NORMAL,
                                product
                        );
                    }
                }
            }
            if (components.isEmpty()) {
                fadeInProgress = 0;
                return;
            }
            Matrix3x2fStack pose = guiGraphics.pose();
            pose.pushMatrix();
            pose.translate( (1- fadeInProgress) * 6, 0);
            ARGBTooltipRenderUtil.tooltip(
                    guiGraphics,
                    Minecraft.getInstance().font,
                    components,
                    guiGraphics.guiWidth() / 2,
                    guiGraphics.guiHeight() / 2,
                    DefaultTooltipPositioner.INSTANCE,
                    null,
                    0xFFFFFF | (int)(200 * fadeInProgress) << 24
            );
            pose.popMatrix();
        }

    }
}
