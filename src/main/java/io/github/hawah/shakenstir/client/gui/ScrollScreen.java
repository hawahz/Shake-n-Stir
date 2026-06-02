package io.github.hawah.shakenstir.client.gui;

import com.mojang.logging.LogUtils;
import io.github.hawah.shakenstir.client.RecipeSyncData;
import io.github.hawah.shakenstir.content.data.SnsRecipeHolder;
import io.github.hawah.shakenstir.content.dataComponent.DataComponentTypeRegistries;
import io.github.hawah.shakenstir.content.dataComponent.SpiritContent;
import io.github.hawah.shakenstir.content.fluid.FluidRegistries;
import io.github.hawah.shakenstir.content.item.ItemRegistries;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.networking.ClientboundSyncRecipeData;
import io.github.hawah.shakenstir.foundation.recipeRecord.ServerRecipeHelper;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import io.github.hawah.shakenstir.lib.client.gui.element.TextureButton;
import io.github.hawah.shakenstir.util.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class ScrollScreen extends BaseScreen {

    public static final Textures BACKGROUND = Textures.SCROLL_BKG;

    private int scrollPtr = 0;
    private int pageRange = 0;
    private TextureButton left;
    private TextureButton right;
    private EditBox nameField;
    private String suggestion = "";

    private final ItemStack itemStack;

    public ScrollScreen(ItemStack itemStack) {
        super(Component.empty());
        this.itemStack = itemStack;
    }

    public void changeRecipes(Object[] objects) {
        pageRange = RecipeSyncData.recipes.size();
        nextPage();
        prevPage();
    }

    @Override
    protected void init() {

        ClientboundSyncRecipeData.SIGNAL.bind(this, this::changeRecipes);

        setTextureSize(BACKGROUND.getWidth(), BACKGROUND.getHeight());
        guiLeft += 20;
        super.init();
        int x = guiLeft;
        int y = guiTop + 2;


        nameField = new EditBox(font, x + 55, y + 100, 106, 10, CommonComponents.EMPTY);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(false);
        nameField.setResponder(text -> {
            if (text.isEmpty()) {
                nameField.setSuggestion(this.suggestion);
            } else {
                nameField.setSuggestion("");
            }
        });
//        nameField.setMaxLength(14);
        nameField.setFocused(true);
        setFocused(nameField);
        addRenderableWidget(nameField);
        Runnable EMPTY = () -> {
        };
        TextureButton check = TextureButton.builder(
                x + 166,
                y + 96,
                Textures.CHECK.getWidth(),
                Textures.CHECK.getHeight(),
                Component.empty(),
                () -> {
                    if (RecipeSyncData.recipes.size() > this.scrollPtr && !RecipeSyncData.recipes.isEmpty()) {
                        itemStack.set(DataComponentTypeRegistries.RECIPE_HOLDER, RecipeSyncData.recipes.get(this.scrollPtr).named(nameField.getValue().isEmpty()? suggestion: nameField.getValue()));
                    }
                    Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);
                    this.onClose();
                }
        )
                .texture(Textures.CHECK.getResource())
                .normalUV(Textures.CHECK.getStartX(), Textures.CHECK.getStartY())
                .hoverUV(Textures.CHECK.builder().variant(1).getStartX(), Textures.CHECK.builder().variant(1).getStartY())
                .hoverSize(Textures.CHECK.getWidth(), Textures.CHECK.getHeight())
                .build();

        addRenderableWidget(check);

        left = TextureButton.builder(
                x + 20,
                y + 60,//40,
                Textures.LEFT_ARROW.getWidth(),
                Textures.LEFT_ARROW.getHeight(),
                Component.empty(),
                this::prevPage
        )
                .texture(Textures.LEFT_ARROW.getResource())
                .normalUV(Textures.LEFT_ARROW.getStartX(), Textures.LEFT_ARROW.getStartY())
                .hoverUV(Textures.LEFT_ARROW.builder().variant(1).getStartX(), Textures.LEFT_ARROW.builder().variant(1).getStartY())
                .hoverSize(Textures.LEFT_ARROW.getWidth(), Textures.LEFT_ARROW.getHeight())
                .inactiveUV(Textures.LEFT_ARROW.builder().variant(2).getStartX(), Textures.LEFT_ARROW.builder().variant(2).getStartY())
                .inactiveSize(Textures.LEFT_ARROW.getWidth(), Textures.LEFT_ARROW.getHeight())
                .build();

        addRenderableWidget(left);
        right = TextureButton.builder(
                x + 194,
                y + 60,
                Textures.RIGHT_ARROW.getWidth(),
                Textures.RIGHT_ARROW.getHeight(),
                Component.empty(),
                this::nextPage
        )
                .texture(Textures.RIGHT_ARROW.getResource())
                .normalUV(Textures.RIGHT_ARROW.getStartX(), Textures.RIGHT_ARROW.getStartY())
                .hoverUV(Textures.RIGHT_ARROW.builder().variant(1).getStartX(), Textures.RIGHT_ARROW.builder().variant(1).getStartY())
                .hoverSize(Textures.RIGHT_ARROW.getWidth(), Textures.RIGHT_ARROW.getHeight())
                .inactiveUV(Textures.RIGHT_ARROW.builder().variant(2).getStartX(), Textures.RIGHT_ARROW.builder().variant(2).getStartY())
                .inactiveSize(Textures.RIGHT_ARROW.getWidth(), Textures.RIGHT_ARROW.getHeight())
                .build();

        addRenderableWidget(right);

        nextPage();
        prevPage();
    }

    @Override
    protected void renderWindowPre(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, float partialTick) {
        BaseScreen.blit(
                graphicsExtractor,
                BACKGROUND.getResource(),
                guiLeft,
                guiTop,
                BACKGROUND.getStartX(),
                BACKGROUND.getStartY(),
                BACKGROUND.getWidth(),
                BACKGROUND.getHeight()
        );
        super.renderWindowPre(graphicsExtractor, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderWindowPost(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.renderWindowPost(graphicsExtractor, mouseX, mouseY, partialTick);

        if (pageRange == 0) {
            graphicsExtractor.text(font, "0/0", guiLeft + 14, guiTop + 103, 0xffe8e6da);
            return;
        }

        graphicsExtractor.text(font, (scrollPtr + 1) + "/" + pageRange, guiLeft + 14, guiTop + 103, 0xffe8e6da);

        SnsRecipeHolder snsRecipeHolder = RecipeSyncData.recipes.get(scrollPtr);
        int itemSize = snsRecipeHolder.requiredItems().size();
        int size = itemSize + snsRecipeHolder.requiredFluids().size();
        final int DISTANCE = 10;
        int leftX = width/2 - size * 16 / 2 - (size - 1) * DISTANCE / 2;

        for (int i = 0; i < itemSize; i++) {
            ItemStack itemStack = snsRecipeHolder.requiredItems().get(i);
            itemStackWithTooltip(font, graphicsExtractor, leftX + i*(16+DISTANCE), guiTop + 24, mouseX, mouseY, itemStack);
        }
        for (int i = 0; i < snsRecipeHolder.requiredFluids().size(); i++) {
            FluidStack fluidStack = snsRecipeHolder.requiredFluids().get(i);
            ItemStack stack;
            if (fluidStack.is(FluidRegistries.GIN_SOURCE_FLUID_BLOCK)) {
                stack = ItemRegistries.GIN.toStack();
                stack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(fluidStack));
            }
            else if (fluidStack.is(FluidRegistries.BRANDY_SOURCE_FLUID_BLOCK)) {
                stack = ItemRegistries.BRANDY.toStack();
                stack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(fluidStack));
            }
            else if (fluidStack.is(FluidRegistries.VODKA_SOURCE_FLUID_BLOCK)) {
                stack = ItemRegistries.VODKA.toStack();
                stack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(fluidStack));
            }
            else if (fluidStack.is(FluidRegistries.WHISKY_SOURCE_FLUID_BLOCK)) {
                stack = ItemRegistries.WHISKY.toStack();
                stack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(fluidStack));
            }
            else if (fluidStack.is(FluidRegistries.TEQUILA_SOURCE_FLUID_BLOCK)) {
                stack = ItemRegistries.TEQUILA.toStack();
                stack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(fluidStack));
            }
            else if (fluidStack.is(FluidRegistries.RUM_SOURCE_FLUID_BLOCK)) {
                stack = ItemRegistries.RUM.toStack();
                stack.set(DataComponentTypeRegistries.SPIRIT_CONTENT, new SpiritContent(fluidStack));
            }
            else {
                stack = ItemRegistries.BOTTLE.toStack();
            }
            itemStackWithTooltip(font, graphicsExtractor, leftX + (itemSize + i)*(16+DISTANCE), guiTop + 24, mouseX, mouseY, stack);
        }

        ItemStack result = ServerRecipeHelper.getShakerProductFromItemHolder(snsRecipeHolder.result());

        itemStackWithTooltip(font, graphicsExtractor, width/2 - 8, guiTop + 70, mouseX, mouseY, result);

        graphicsExtractor.itemDecorations(font, ItemRegistries.CONTENT_HOLDER.toStack(), guiLeft + 106, guiTop + 47, String.valueOf(snsRecipeHolder.shakeTimes()));
        if (mouseX >= guiLeft + 106 && mouseY >= guiTop + 47 && mouseX < guiLeft + 106 + 16 && mouseY < guiTop + 47 + 16){
            graphicsExtractor.setTooltipForNextFrame(
                    font,
                    List.of(LangData.TOOLTIP_SCROLL_SHAKER_SHAKE_TIME.get(Component.literal(String.valueOf(snsRecipeHolder.shakeTimes())).withStyle(ChatFormatting.GOLD)).getVisualOrderText()),
                    mouseX,
                    mouseY + font.lineHeight
            );
        }
    }

    public void nextPage() {
        scrollPtr++;
        scrollPtr = Mth.clamp(scrollPtr, 0, pageRange - 1);
        if (scrollPtr + 1 >=  pageRange) {
            right.active = false;
        }
        left.active = !(scrollPtr - 1 < 0);
        pageChanged();
    }

    public void prevPage() {
        scrollPtr--;
        scrollPtr = Mth.clamp(scrollPtr, 0, pageRange - 1);
        if (scrollPtr - 1 < 0) {
            left.active = false;
        }
        right.active = !(scrollPtr + 1 >=  pageRange);
        pageChanged();
    }

    public void pageChanged() {
        if (pageRange == 0) {
            nameField.setSuggestion("");
            return;
        }
        try {
            SnsRecipeHolder snsRecipeHolder = RecipeSyncData.recipes.get(Mth.clamp(scrollPtr, 0, pageRange - 1));
            Component component = snsRecipeHolder.result().get(DataComponents.ITEM_NAME);
            nameField.setSuggestion(suggestion = component.getString());
        } catch (RuntimeException e) {
            LogUtils.getLogger().error("Error while changing page", e);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientboundSyncRecipeData.SIGNAL.unbind(this);
    }
}
