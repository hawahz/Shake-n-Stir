package io.github.hawah.shakenstir.content.fluid;

import io.github.hawah.shakenstir.ShakenStir;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FluidRegistries {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ShakenStir.MODID);
    // 注册对应流体的source和flow，使用NeoForge提供的BaseFlowingFluid来注册
    // 其中source和flow都需要填入一个参数，这个参数是流体的属性,在下面定义
    public static DeferredHolder<Fluid, FlowingFluid> GIN_SOURCE_FLUID_BLOCK =   FLUIDS.register("gin_fluid", () ->                     new BaseFlowingFluid.Source(FluidRegistries.    GIN_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> GIN_FLOWING_FLUID_BLOCK =  FLUIDS.register("gin_fluid_flow", () ->                new BaseFlowingFluid.Flowing(FluidRegistries.   GIN_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> VODKA_SOURCE_FLUID_BLOCK =     FLUIDS.register("vodka_fluid", () ->               new BaseFlowingFluid.Source(FluidRegistries.    VODKA_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> VODKA_FLOWING_FLUID_BLOCK =    FLUIDS.register("vodka_fluid_flow", () ->          new BaseFlowingFluid.Flowing(FluidRegistries.   VODKA_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> WHISKY_SOURCE_FLUID_BLOCK =   FLUIDS.register("whiskey_fluid", () ->              new BaseFlowingFluid.Source(FluidRegistries.    WHISKY_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> WHISKY_FLOWING_FLUID_BLOCK =  FLUIDS.register("whiskey_fluid_flow", () ->         new BaseFlowingFluid.Flowing(FluidRegistries.   WHISKY_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> BRANDY_SOURCE_FLUID_BLOCK =    FLUIDS.register("branky_fluid", () ->              new BaseFlowingFluid.Source(FluidRegistries.    BRANDY_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> BRANDY_FLOWING_FLUID_BLOCK =   FLUIDS.register("branky_fluid_flow", () ->         new BaseFlowingFluid.Flowing(FluidRegistries.   BRANDY_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> RUM_SOURCE_FLUID_BLOCK =    FLUIDS.register("rum_fluid", () ->                    new BaseFlowingFluid.Source(FluidRegistries.    RUM_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> RUM_FLOWING_FLUID_BLOCK =   FLUIDS.register("rum_fluid_flow", () ->               new BaseFlowingFluid.Flowing(FluidRegistries.   RUM_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> TEQUILA_SOURCE_FLUID_BLOCK =   FLUIDS.register("tequila_fluid", () ->             new BaseFlowingFluid.Source(FluidRegistries.    TEQUILA_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> TEQUILA_FLOWING_FLUID_BLOCK =  FLUIDS.register("tequila_fluid_flow", () ->        new BaseFlowingFluid.Flowing(FluidRegistries.   TEQUILA_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> BUBBLE_SOURCE_FLUID_BLOCK =   FLUIDS.register("bubble_fluid", () ->               new BaseFlowingFluid.Source(FluidRegistries.    BUBBLE_PROPERTIES));
    public static DeferredHolder<Fluid, FlowingFluid> BUBBLE_FLOWING_FLUID_BLOCK =  FLUIDS.register("bubble_fluid_flow", () ->          new BaseFlowingFluid.Flowing(FluidRegistries.   BUBBLE_PROPERTIES));

    // 定义流体的属性
    // 这个流体的属性要传入的内容比较多，我们挨个介绍，我们使用了BaseFlowingFluid的Properties内部类创建对应的Properties，其中第一个参数是对应的流体的类体类型FluidType，然后第二个参数是对应的source流体，第三个参数是flow流体，都是我们刚刚写过的，看起来比较绕，大家自己理清下关系。
    // 通过bucket这个设置流体和对应的流体桶的绑定，等会我们注册这个bucketitem
    // 通过block绑定对应的流体和方块的绑定，这个方块等会我们注册。
    // slopeFindDistance寻找斜坡的距离
    // levelDecreasePerBlock 每个方块流体的减少量。
    // 后两个数据是用于流体的流动的，主要是斜坡时候优先流，不会扩散。
    // 以及流体最多能流多远，例如原版的水是8格
    // 可以自己调试这几个数值试试，也可以去wiki看看具体的含义。
    private static final BaseFlowingFluid.Properties GIN_PROPERTIES = new BaseFlowingFluid.Properties(
            FluidTypeRegistries.GIN_FLUID_TYPE,
            FluidRegistries.GIN_SOURCE_FLUID_BLOCK,
            FluidRegistries.GIN_FLOWING_FLUID_BLOCK
    );//.bucket(ModItems.MY_FLUID_BUCKET)
//            .slopeFindDistance(2)
//            .levelDecreasePerBlock(2)
//            .block(BlockRegistries.GIN_LIQUID);
    private static final BaseFlowingFluid.Properties WHISKY_PROPERTIES = new BaseFlowingFluid.Properties(
            FluidTypeRegistries.WHISKY_FLUID_TYPE,
            FluidRegistries.    WHISKY_SOURCE_FLUID_BLOCK,
            FluidRegistries.    WHISKY_FLOWING_FLUID_BLOCK
    );//.bucket(ModItems.MY_FLUID_BUCKET)
//            .slopeFindDistance(2)
//            .levelDecreasePerBlock(2)
//            .block(BlockRegistries.GIN_LIQUID);
    private static final BaseFlowingFluid.Properties RUM_PROPERTIES = new BaseFlowingFluid.Properties(
            FluidTypeRegistries.RUM_FLUID_TYPE,
            FluidRegistries.    RUM_SOURCE_FLUID_BLOCK,
            FluidRegistries.    RUM_FLOWING_FLUID_BLOCK
    );//.bucket(ModItems.MY_FLUID_BUCKET)
//            .slopeFindDistance(2)
//            .levelDecreasePerBlock(2)
//            .block(BlockRegistries.GIN_LIQUID);
    private static final BaseFlowingFluid.Properties BRANDY_PROPERTIES = new BaseFlowingFluid.Properties(
            FluidTypeRegistries.BRANDY_FLUID_TYPE,
            FluidRegistries.    BRANDY_SOURCE_FLUID_BLOCK,
            FluidRegistries.    BRANDY_FLOWING_FLUID_BLOCK
    );//.bucket(ModItems.MY_FLUID_BUCKET)
//            .slopeFindDistance(2)
//            .levelDecreasePerBlock(2)
//            .block(BlockRegistries.GIN_LIQUID);
    private static final BaseFlowingFluid.Properties VODKA_PROPERTIES = new BaseFlowingFluid.Properties(
            FluidTypeRegistries.VODKA_FLUID_TYPE,
            FluidRegistries.    VODKA_SOURCE_FLUID_BLOCK,
            FluidRegistries.    VODKA_FLOWING_FLUID_BLOCK
    );//.bucket(ModItems.MY_FLUID_BUCKET)
//            .slopeFindDistance(2)
//            .levelDecreasePerBlock(2)
//            .block(BlockRegistries.GIN_LIQUID);
    private static final BaseFlowingFluid.Properties TEQUILA_PROPERTIES = new BaseFlowingFluid.Properties(
            FluidTypeRegistries.TEQUILA_FLUID_TYPE,
            FluidRegistries.    TEQUILA_SOURCE_FLUID_BLOCK,
            FluidRegistries.    TEQUILA_FLOWING_FLUID_BLOCK
    );//.bucket(ModItems.MY_FLUID_BUCKET)
//            .slopeFindDistance(2)
//            .levelDecreasePerBlock(2)
//            .block(BlockRegistries.GIN_LIQUID);
    private static final BaseFlowingFluid.Properties BUBBLE_PROPERTIES = new BaseFlowingFluid.Properties(
            FluidTypeRegistries.BUBBLE_FLUID_TYPE,
            FluidRegistries.    BUBBLE_SOURCE_FLUID_BLOCK,
            FluidRegistries.    BUBBLE_FLOWING_FLUID_BLOCK
    );//.bucket(ModItems.MY_FLUID_BUCKET)
//            .slopeFindDistance(2)
//            .levelDecreasePerBlock(2)
//            .block(BlockRegistries.GIN_LIQUID);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
