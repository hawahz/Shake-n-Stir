package io.github.hawah.shakenstir.foundation.recipeRecord;

import com.google.common.collect.EvictingQueue;
import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.item.GlasswareItem;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.util.Paths;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

@EventBusSubscriber
public class ServerRecipeHelper {

    public static final int MAX_RECIPES = 10;

    public static ConcurrentHashMap<UUID, EvictingQueue<SnsRecipeHolder>> recipes = new ConcurrentHashMap<>();
    public static void writeRecipe(Player player, List<ItemStack> itemStacks, List<FluidStack> fluidStacks, ItemStack result, SnsRecipeHolder.Type type, int shakeTimes) {
        recipes.compute(player.getUUID(), (_, queue) -> {
            queue = queue == null ? EvictingQueue.create(MAX_RECIPES) : queue;
            queue.add(new SnsRecipeHolder(type, itemStacks, fluidStacks, shakeTimes, result));
            return queue;
        });
    }
    
    public static ItemStack getShakerProductFromItemHolder(ItemStack result) {
        if (!result.is(ItemRegistries.CONTENT_HOLDER)) {
            return ItemStack.EMPTY;
        }
        ItemStack martiniGlass = GlasswareItem.getMartiniGlass();
        DataComponentMap components = result.getComponents();
        if (components.has(DataComponents.DYED_COLOR)) {
            martiniGlass.set(DataComponents.DYED_COLOR, components.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0)));
            martiniGlass.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.DYED_COLOR, true));
        }
        if (components.has(DataComponents.ITEM_NAME)) {
            martiniGlass.set(DataComponents.ITEM_NAME, components.get(DataComponents.ITEM_NAME));
        }
        if (components.has(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY)) {
            martiniGlass.set(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY, components.get(DataComponentTypeRegistries.SHAKE_PRODUCT_QUALITY));
        }
        if (components.has(DataComponentTypeRegistries.DRINK_DATA)) {
            martiniGlass.set(DataComponentTypeRegistries.DRINK_DATA, components.get(DataComponentTypeRegistries.DRINK_DATA));
        }
        return martiniGlass;
    }

    @SubscribeEvent
    public static void onPlayerExited(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        EvictingQueue<SnsRecipeHolder> snsRecipeHolders = recipes.remove(player.getUUID());
        String playerName = player.getName().getString();
        if (player.level() instanceof ServerLevel serverLevel){
            Path dir = Paths.getPlayerDataPath(serverLevel, (ServerPlayer) player).resolve("recipe");
            Path path = dir.resolve("recipes").toAbsolutePath();
            CompoundTag data = new CompoundTag();
            data.store(SnsRecipeHolder.CODEC.listOf().fieldOf("recipeHolder"), snsRecipeHolders.stream().toList());

            try {
                Files.createDirectories(dir);
                try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE)) {
                    NbtIo.writeCompressed(data, out);
                }
            } catch (IOException e) {
                LogUtils.getLogger().error("Occurred Error when saving structure.", e);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerEntered(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel serverLevel){
            Path dir = Paths.getPlayerDataPath(serverLevel, (ServerPlayer) player).resolve("recipe");
            Path path = dir.resolve("recipes").toAbsolutePath();
            if (Files.exists(path)) {
                try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
                        new GZIPInputStream(Files.newInputStream(path, StandardOpenOption.READ))))) {
                    CompoundTag nbt = NbtIo.read(stream, NbtAccounter.create(0x20000000L));
                    nbt.read("recipeHolder", SnsRecipeHolder.CODEC.listOf()).ifPresent(
                            snsRecipeHolders -> {
                                EvictingQueue<SnsRecipeHolder> evictingQueue = EvictingQueue.create(MAX_RECIPES);
                                evictingQueue.addAll(snsRecipeHolders);
                                recipes.put(player.getUUID(), evictingQueue);
                            }
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
