package io.github.hawah.shakenstir.content.item;

import io.github.hawah.shakenstir.client.gui.ScrollScreen;
import io.github.hawah.shakenstir.content.block.BlockRegistries;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.foundation.networking.ClientboundSyncRecipeData;
import io.github.hawah.shakenstir.foundation.recipeRecord.ServerRecipeHelper;
import io.github.hawah.shakenstir.lib.client.gui.ScreenOpener;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;

public class RecipeScroll extends BlockItem {
    public RecipeScroll(Properties properties) {
        super(BlockRegistries.RECIPE_SCROLL_BLOCK.get(), properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext placeContext) {
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).has(DataComponentTypeRegistries.RECIPE_HOLDER)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            ScreenOpener.open(new ScrollScreen(player.getItemInHand(hand)));
            player.swing(hand);
        } else if (ServerRecipeHelper.recipes.containsKey(player.getUUID())) {
            Networking.sendToPlayer(new ClientboundSyncRecipeData(ServerRecipeHelper.recipes.get(player.getUUID()).stream().toList()), (ServerPlayer) player);
        }
        return super.use(level, player, hand);
    }
}
