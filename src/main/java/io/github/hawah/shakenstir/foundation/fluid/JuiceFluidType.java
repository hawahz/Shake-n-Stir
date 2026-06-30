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

@SuppressWarnings("unused")
public class JuiceFluidType extends FluidType implements TintColorGetter{
    // 定义了源source的纹理图片，流动的纹理图片，以及流体覆盖层的图片（指的是颜色，例如水的蓝色纹理，岩浆的红色纹理，你可以到原版对应的位置看看是什么图片就知道了）
    private final Identifier stillTexture;
    private final Identifier flowingTexture;
    private final Identifier overlayTexture;
    // 流体的着色颜色
    private final int tintColor;
    // 从流体中看外面的雾的颜色
    private final Vector3f fogColor;

    public JuiceFluidType(final Identifier stillTexture, final Identifier flowingTexture, final Identifier overlayTexture,
                          final int tintColor, final Vector3f fogColor, final Properties properties) {
        super(properties);
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.overlayTexture = overlayTexture;
        this.tintColor = tintColor;
        this.fogColor = fogColor;
    }

    // 对应的get函数
    public Identifier getStillTexture() {
        return stillTexture;
    }

    public Identifier getFlowingTexture() {
        return flowingTexture;
    }

    public int getTintColor() {
        return tintColor;
    }

    public Identifier getOverlayTexture() {
        return overlayTexture;
    }

    public Vector3f getFogColor() {
        return fogColor;
    }

    public ItemStack getFruit(FluidStack stack) {
        return stack.getOrDefault(DataComponentTypeRegistries.FRUIT_DATA, SingleItemComponent.EMPTY).itemStack();
    }

    @Override
    public @NonNull Component getDescription(@NonNull FluidStack stack) {
        ItemStack fruit = getFruit(stack);
        return fruit.isEmpty()? Component.empty().append(super.getDescription(stack)).withColor(getTintColor()): Component.empty().append(fruit.getHoverName()).append(super.getDescription(stack)).withColor(getTintColor());
    }

    // 对于我们的几个纹理，如果如果想生效的话，就需要重写这个方法，在对于的方法将我们的RL的资源定位的图片返回。
//    @Override
//    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
//        consumer.accept(new IClientFluidTypeExtensions() {
//            @Override
//            public Identifier getStillTexture() {
//                return stillTexture;
//            }
//
//            @Override
//            public Identifier getFlowingTexture() {
//                return flowingTexture;
//            }
//
//            @Override
//            public @Nullable Identifier getOverlayTexture() {
//                return overlayTexture;
//            }
//
//            @Override
//            public int getTintColor() {
//                return tintColor;
//            }
//
//            // 修改从流体中看雾的颜色
//            @Override
//            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
//                                                    int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
//                return fogColor;
//            }
//            // 液体中的能见度 或者 说雾的范围
//            @Override
//            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick,
//                                        float nearDistance, float farDistance, FogShape shape) {
//                RenderSystem.setShaderFogStart(1f);
//                RenderSystem.setShaderFogEnd(6f); // distance when the fog starts
//            }
//        });
//    }
}
