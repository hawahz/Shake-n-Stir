package io.github.hawah.shakenstir;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.blockEntity.BlockEntityRegistries;
import io.github.hawah.shakenstir.content.dataAttachment.DataAttachmentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.effect.MobEffectRegistries;
import io.github.hawah.shakenstir.content.entity.EntityTypeRegistries;
import io.github.hawah.shakenstir.content.entity.ai.activity.Activities;
import io.github.hawah.shakenstir.content.entity.ai.memory.Memories;
import io.github.hawah.shakenstir.content.entity.ai.sensor.Sensors;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.fluid.FluidTypeRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.content.item.SnsCreativeTab;
import io.github.hawah.shakenstir.content.recipe.RecipeTypeRegistries;
import io.github.hawah.shakenstir.content.trigger.TriggerRegistries;
import io.github.hawah.shakenstir.foundation.networking.NetworkPackets;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ShakenStir.MODID)
public class ShakenStir {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "shakenstir";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public ShakenStir(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);

        BlockRegistries.register(modEventBus);
        BlockEntityRegistries.register(modEventBus);
        DataComponentTypeRegistries.register(modEventBus);
        FluidTypeRegistries.register(modEventBus);
        FluidRegistries.register(modEventBus);
        ItemRegistries.register(modEventBus);
        RecipeTypeRegistries.register(modEventBus);
        SnsCreativeTab.register(modEventBus);
        MobEffectRegistries.register(modEventBus);
        DataAttachmentTypeRegistries.register(modEventBus);
        TriggerRegistries.register(modEventBus);
        EntityTypeRegistries.register(modEventBus);
        Sensors.register(modEventBus);
        Activities.register(modEventBus);
        Memories.register(modEventBus);
        NetworkPackets.register();

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.Server.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
    public static <T> ContextKey<T> asContextKey(String name) {
        return new ContextKey<>(Identifier.fromNamespaceAndPath(MODID, name));
    }
}
