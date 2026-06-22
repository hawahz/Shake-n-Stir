package io.github.hawah.shakenstir.foundation.fluid;

import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SingleItemComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

/**
 * 包裹可消耗物品的流体类型（ItemFluidType）。
 * <p>
 * 类似于 {@link JuiceFluidType} 包裹水果物品的机制，
 * 该流体类型通过 {@link DataComponentTypeRegistries#ITEM_CONTENT} 数据组件
 * 存储被包裹的原始物品（如药水），使得 Shaker 可以将可消耗物品以流体态的形式接受。
 * </p>
 */
@SuppressWarnings("unused")
public class ItemFluidType extends FluidType implements TintColorGetter {
    private final Identifier stillTexture;
    private final Identifier flowingTexture;
    private final Identifier overlayTexture;
    private final int tintColor;
    private final Vector3f fogColor;

    public ItemFluidType(final Identifier stillTexture, final Identifier flowingTexture, final Identifier overlayTexture,
                         final int tintColor, final Vector3f fogColor, final Properties properties) {
        super(properties);
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.overlayTexture = overlayTexture;
        this.tintColor = tintColor;
        this.fogColor = fogColor;
    }

    public Identifier getStillTexture() {
        return stillTexture;
    }

    public Identifier getFlowingTexture() {
        return flowingTexture;
    }

    @Override
    public int getTintColor() {
        return tintColor;
    }

    public Identifier getOverlayTexture() {
        return overlayTexture;
    }

    public Vector3f getFogColor() {
        return fogColor;
    }

    /**
     * 从 FluidStack 中获取被包裹的原始物品。
     * 数据存储方式与 JuiceFluidType 存储水果一致，使用 ITEM_CONTENT 数据组件。
     */
    public ItemStack getWrappedItem(FluidStack stack) {
        return stack.getOrDefault(DataComponentTypeRegistries.ITEM_CONTENT, SingleItemComponent.EMPTY).itemStack();
    }

    @Override
    public @NonNull Component getDescription(@NonNull FluidStack stack) {
        ItemStack wrapped = getWrappedItem(stack);
        if (!wrapped.isEmpty()) {
            return Component.empty().append(wrapped.getHoverName()).append(super.getDescription(stack)).withColor(getTintColor());
        }
        return super.getDescription(stack).copy().withColor(getTintColor());
    }
}
