package io.github.hawah.shakenstir.foundation.recipeRecord;

import com.google.common.collect.EvictingQueue;
import io.github.hawah.shakenstir.content.entity.ai.behavior.recipeProvider.SnsRecipeHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class ServerRecipeWriter {

    public static final int MAX_RECIPES = 10;

    public static ConcurrentHashMap<UUID, EvictingQueue<SnsRecipeHolder>> recipes = new ConcurrentHashMap<>();
    public static void writeRecipe(Player player, List<ItemStack> itemStacks, List<FluidStack> fluidStacks, ItemStack result, SnsRecipeHolder.Type type) {
        recipes.compute(player.getUUID(), (_, queue) -> {
            queue = queue == null ? EvictingQueue.create(MAX_RECIPES) : queue;
            queue.add(new SnsRecipeHolder(type, itemStacks, fluidStacks, result));
            return queue;
        });
    }

    @SubscribeEvent
    public static void onPlayerExited(PlayerEvent.PlayerLoggedOutEvent event) {
        recipes.remove(event.getEntity().getUUID());
    }
}
