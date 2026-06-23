package io.github.hawah.shakenstir.client.gui;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.github.hawah.shakenstir.content.dialogue.Condition;
import io.github.hawah.shakenstir.content.dialogue.ConditionType;
import io.github.hawah.shakenstir.content.dialogue.DialogueData;
import io.github.hawah.shakenstir.content.dialogue.DialogueEntry;
import io.github.hawah.shakenstir.content.dialogue.DialogueEventType;
import io.github.hawah.shakenstir.content.dialogue.DialogueTriggerMode;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.foundation.datagen.lang.LangData;
import io.github.hawah.shakenstir.foundation.networking.ServerboundBartenderDialogueUpdatePacket;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import io.github.hawah.shakenstir.lib.networking.Networking;
import io.github.hawah.shakenstir.util.Paths;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话编辑器界面 (Dialogue Editor Screen)，支持对 DialogueData -> DialogueEntry -> Condition / Text
 * 三层嵌套数据结构的逐层选择与完整编辑。
 *
 * <p>交互功能：
 * <ul>
 *     <li>左侧条目列表：点击选中条目，滚动浏览，高亮显示，含条件摘要+文本截断预览</li>
 *     <li>右侧条件列表：点击选中条件，显示 [type] [op] [value] 预览，循环切换类型/操作符，编辑值</li>
 *     <li>右侧文本列表：点击选中文本条目，显示截断文本预览，编辑内容</li>
 *     <li>底栏：条目增删、复制/粘贴、保存</li>
 * </ul>
 */
@ParametersAreNonnullByDefault
public class DialogueEditorScreen extends BaseScreen {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ===================== 布局常量 =====================
    private static final int WIN_WIDTH = 400;
    private static final int WIN_HEIGHT = 300;
    private static final int ENTRY_LIST_WIDTH = 160;
    private static final int EDIT_PANEL_X = ENTRY_LIST_WIDTH + 10;
    private static final int EDIT_PANEL_WIDTH = WIN_WIDTH - EDIT_PANEL_X - 10;
    private static final int LINE_HEIGHT = 14;
    private static final int ROW_HEIGHT = 16;

    // 颜色常量
    private static final int BG_DARK = 0xFF_2C2C2C;
    private static final int BG_MID = 0xFF_3C3C3C;
    private static final int BG_LIGHT = 0xFF_4A4A4A;
    private static final int BG_SELECTED = 0xFF_336633;
    private static final int BG_SUB_SELECTED = 0xFF_556633;
    private static final int LINE_COLOR = 0xFF_555555;
    private static final int TEXT_COLOR = 0xFF_CCCCCC;
    private static final int HEADER_COLOR = 0xFF_FFEE88;
    private static final int LABEL_COLOR = 0xFF_AAAAAA;
    /** 预览文字颜色 - 较暗的灰色，与选中高亮色区分 */
    private static final int PREVIEW_COLOR = 0xFF_8A8A8A;
    /** 预览文字 alpha */
    private static final int PREVIEW_ALPHA = 230;
    /** 预览文字最大字符数 */
    private static final int MAX_PREVIEW_CHARS = 28;

    // ===================== 数据 =====================
    private final BartenderEntity entity;
    private DialogueData editingData = DialogueData.EMPTY;
    private int selectedEntryIndex = -1;
    private boolean dataReceived = false;
    private boolean dirty = false;
    private static DialogueData clipboard = null;

    // ===================== 滚动状态 =====================
    private int entryListScroll = 0;
    private int condListScroll = 0;
    private int textListScroll = 0;

    // ===================== 子项选中状态 =====================
    /** 当前选中的条件索引（-1 表示未选中） */
    private int selectedCondIndex = -1;
    /** 当前选中的文本索引（-1 表示未选中） */
    private int selectedTextIndex = -1;

    // ===================== 条件编辑暂存状态 =====================
    private int editingCondTypeIdx = 0;
    private int editingCondOpIdx = 0;
    // TODO: 人工审查 - 2026-06-23 - 新增触发模式/事件类型编辑暂存状态 + 天气值索引
    private int editingTriggerModeIdx = 0;
    private int editingEventTypeIdx = 0;
    /** 天气值循环索引（0=clear, 1=rain, 2=thunder） */
    private int editingWeatherIdx = 0;

    // ===================== 帮助面板状态 =====================
    private boolean helpVisible = false;

    // ===================== Widgets =====================
    private EditBox txtCondValue;
    private EditBox txtDialogueText;
    private EditBox txtFrequency;
    private Button btnCondType;
    private Button btnCondOp;
    // TODO: 人工审查 - 2026-06-22 - 新增导出功能控件：文件名输入框 + 导出按钮
    private EditBox txtExportFilename;
    private Button btnExport;
    // TODO: 人工审查 - 2026-06-23 - 新增触发模式/事件类型/天气值控件
    private Button btnTriggerMode;
    private Button btnEventType;
    private Button btnWeatherValue;
    // 条件编辑区按钮引用（用于禁用逻辑）
    private Button btnAddCond;
    private Button btnDelCond;
    private Button btnEditCond;
    // 文本编辑区按钮引用（用于禁用逻辑）
    private Button btnAddText;
    private Button btnDelText;
    private Button btnEditText;
    // 条目元数据按钮引用（用于禁用逻辑）
    private Button btnApplyFreq;
    /** 条件类型切换按钮引用（用于在天气类型时动态显示天气下拉） */
    private Button btnCondTypeRef;

    // ===================== 滚动文本动画 =====================
    /** 滚动动画计时（每 tick +1），用于溢出文本的左右滚动 */
    private int scrollAnimTick = 0;

    // ===================== 区域边界（每次 init/渲染计算） =====================

    /** 条目列表区域 */
    private int entryListX, entryListY, entryListW, entryListH;
    private int entryListVisible;
    /** 条件列表区域 */
    private int condListX, condListY, condListW, condListH;
    private int condListVisible;
    private int condEditorY;
    /** 文本列表区域 */
    private int textListX, textListY, textListW, textListH;
    private int textListVisible;
    private int textEditorY;

    public DialogueEditorScreen(BartenderEntity entity) {
        super(LangData.GUI_DIALOGUE_EDITOR_TITLE.get());
        this.entity = entity;
        this.editingData = entity.getDialogueData();
        this.dataReceived = !editingData.isEmpty();
    }

    // ===================== 初始化 =====================

    // TODO: 人工审查 - 2026-06-23 - init() 新增触发模式/事件类型/天气值按钮，保存按钮引用用于禁用逻辑
    @Override
    protected void init() {
        setTextureSize(WIN_WIDTH, WIN_HEIGHT);
        super.init();

        computeLayout();

        int btnY = guiTop + WIN_HEIGHT - 25;
        int BUTTON_HEIGHT = 16;

        // -- 触发模式 / 事件类型行（condListY 上方） --
        int trigRowY = condListY - 18;
        btnTriggerMode = Button.builder(getTriggerModeLabel(), btn -> cycleTriggerMode())
                .pos(guiLeft + EDIT_PANEL_X + 5, trigRowY).size(85, BUTTON_HEIGHT).build();
        addSortedRenderWidget(btnTriggerMode);

        btnEventType = Button.builder(getEventTypeLabel(), btn -> cycleEventType())
                .pos(guiLeft + EDIT_PANEL_X + 95, trigRowY).size(95, BUTTON_HEIGHT).build();
        addSortedRenderWidget(btnEventType);

        // -- 条目元数据控件（频率） --
        txtFrequency = new EditBox(font, guiLeft + EDIT_PANEL_X + 5, condEditorY - 20, 40, BUTTON_HEIGHT,
                LangData.GUI_DIALOGUE_EDITOR_FREQ.get());
        txtFrequency.setMaxLength(4);
        txtFrequency.setFilter(s -> s.matches("[0-9]*")); // 仅数字
        addSortedRenderWidget(txtFrequency);

        btnApplyFreq = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_SET_FREQ.get(), btn -> commitFrequency())
                .pos(guiLeft + EDIT_PANEL_X + 50, condEditorY - 20).size(50, BUTTON_HEIGHT).build();
        addSortedRenderWidget(btnApplyFreq);

        // -- 帮助按钮 --
        Button btnHelp = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_HELP.get(), btn -> { helpVisible = !helpVisible; })
                .pos(guiLeft + EDIT_PANEL_X + 110, condEditorY - 20).size(30, BUTTON_HEIGHT).build();
        addSortedRenderWidget(btnHelp);

        // -- 条件编辑控件 --
        btnCondType = Button.builder(getCondTypeLabel(), btn -> cycleCondType())
                .pos(guiLeft + EDIT_PANEL_X + 5, condEditorY).size(80, BUTTON_HEIGHT).build();
        btnCondTypeRef = btnCondType;
        addSortedRenderWidget(btnCondType);

        // TODO: 人工审查 - 2026-06-23 - 运算符下拉选择按钮：宽度 55px 适配 is_not，显示 "▼" 指示下拉行为
        btnCondOp = Button.builder(getCondOpLabel(), btn -> cycleCondOp())
                .pos(guiLeft + EDIT_PANEL_X + 90, condEditorY).size(55, BUTTON_HEIGHT).build();
        addSortedRenderWidget(btnCondOp);

        txtCondValue = new EditBox(font, guiLeft + EDIT_PANEL_X + 150, condEditorY, 40, BUTTON_HEIGHT,
                LangData.GUI_DIALOGUE_EDITOR_COND_VAL.get());
        txtCondValue.setMaxLength(32);
        addSortedRenderWidget(txtCondValue);

        // 天气值循环按钮（初始隐藏，仅当 WEATHER 类型选中时显示）
        btnWeatherValue = Button.builder(getWeatherValueLabel(), btn -> cycleWeatherValue())
                .pos(guiLeft + EDIT_PANEL_X + 150, condEditorY).size(50, BUTTON_HEIGHT).build();
        btnWeatherValue.visible = false;
        addSortedRenderWidget(btnWeatherValue);

        int condBtnY = condEditorY + 18;
        btnAddCond = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_ADD_COND.get(), btn -> addCondition())
                .pos(guiLeft + EDIT_PANEL_X + 5, condBtnY)
                .size(50, BUTTON_HEIGHT)
                .build();
        addSortedRenderWidget(btnAddCond);

        btnDelCond = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_DEL_COND.get(), btn -> deleteCondition())
                .pos(guiLeft + EDIT_PANEL_X + 60, condBtnY)
                .size(50, BUTTON_HEIGHT)
                .build();
        addSortedRenderWidget(btnDelCond);

        btnEditCond = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_APPLY.get(), btn -> commitConditionEdit())
                .pos(guiLeft + EDIT_PANEL_X + 120, condBtnY)
                .size(55, BUTTON_HEIGHT)
                .build();
        addSortedRenderWidget(btnEditCond);

        // -- 文本编辑控件 --
        txtDialogueText = new EditBox(font, guiLeft + EDIT_PANEL_X + 5, textEditorY, 175, BUTTON_HEIGHT,
                LangData.GUI_DIALOGUE_EDITOR_TEXT.get());
        txtDialogueText.setMaxLength(128);
        addSortedRenderWidget(txtDialogueText);

        int textBtnY = textEditorY + 18;
        btnAddText = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_ADD_TEXT.get(), _ -> addText())
                .pos(guiLeft + EDIT_PANEL_X + 5, textBtnY)
                .size(50, BUTTON_HEIGHT)
                .build();
        addSortedRenderWidget(btnAddText);

        btnDelText = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_DEL_TEXT.get(), _ -> deleteText())
                .pos(guiLeft + EDIT_PANEL_X + 60, textBtnY)
                .size(50, BUTTON_HEIGHT)
                .build();
        addSortedRenderWidget(btnDelText);

        btnEditText = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_APPLY.get(), _ -> commitTextEdit())
                .pos(guiLeft + EDIT_PANEL_X + 120, textBtnY)
                .size(55, BUTTON_HEIGHT)
                .build();
        addSortedRenderWidget(btnEditText);

        // -- 底栏按钮 --
        Button btnSave = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_SAVE.get(), _ -> saveAndClose())
                .pos(guiLeft + WIN_WIDTH - 60, btnY)
                .size(50, 20)
                .build();
        addSortedRenderWidget(btnSave);

        Button btnCopy = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_COPY.get(), _ -> copyToClipboard())
                .pos(guiLeft + EDIT_PANEL_X + 60, btnY)
                .size(50, 20).build();
        addSortedRenderWidget(btnCopy);

        Button btnPaste = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_PASTE.get(), _ -> pasteFromClipboard())
                .pos(guiLeft + EDIT_PANEL_X + 115, btnY)
                .size(50, 20)
                .build();
        addSortedRenderWidget(btnPaste);

        Button btnAddEntry = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_ADD_ENTRY.get(), _ -> addNewEntry())
                .pos(guiLeft + 5, btnY)
                .size(60, 20)
                .build();
        addSortedRenderWidget(btnAddEntry);

        Button btnDelEntry = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_DEL_ENTRY.get(), _ -> deleteSelectedEntry())
                .pos(guiLeft + 70, btnY)
                .size(60, 20)
                .build();
        addSortedRenderWidget(btnDelEntry);

        // -- 导出控件（底栏左侧） --
        txtExportFilename = new EditBox(font, guiLeft + 135, btnY, 60, 20,
                LangData.GUI_DIALOGUE_EDITOR_EXPORT_FILENAME.get());
        txtExportFilename.setMaxLength(64);
        txtExportFilename.setHint(LangData.GUI_DIALOGUE_EDITOR_EXPORT_FILENAME.get());
        addSortedRenderWidget(txtExportFilename);

        btnExport = Button.builder(LangData.GUI_DIALOGUE_EDITOR_BTN_EXPORT.get(), _ -> exportToFile())
                .pos(guiLeft + 197, btnY).size(32, 20).build();
        addSortedRenderWidget(btnExport);

        finishRegister();
        refreshAllEditFields();
        updateDisableStates();
    }

    /**
     * 预计算所有区域的边界。
     */
    // TODO: 人工审查 - 2026-06-23 - 布局下移 18px 为触发模式/事件类型行腾出空间
    private void computeLayout() {
        // 条目列表
        entryListX = guiLeft + 5;
        entryListY = guiTop + 24;
        entryListW = ENTRY_LIST_WIDTH - 10;
        entryListH = WIN_HEIGHT - 56;
        entryListVisible = entryListH / ROW_HEIGHT;

        // 条件列表（下移 18px 以容纳触发模式/事件类型行）
        condListX = guiLeft + EDIT_PANEL_X + 5;
        condListY = guiTop + 60;
        condListW = EDIT_PANEL_WIDTH - 10;
        condListH = 4 * ROW_HEIGHT; // 最多显示 4 行
        condListVisible = condListH / ROW_HEIGHT;
        condEditorY = condListY + condListH + 4;

        // 文本列表
        textListY = condEditorY + 38; // 条件编辑器按钮行之后
        textListX = condListX;
        textListW = condListW;
        textListH = 4 * ROW_HEIGHT;
        textListVisible = textListH / ROW_HEIGHT;
        textEditorY = textListY + textListH + 4;
    }

    // ===================== 每帧轮询 =====================

    // TODO: 人工审查 - 2026-06-23 - tick() 新增 scrollAnimTick 递增
    @Override
    public void tick() {
        super.tick();
        scrollAnimTick++;
        if (!dataReceived) {
            DialogueData current = entity.getDialogueData();
            if (current != null) {
                this.editingData = current;
                this.dataReceived = true;
                this.selectedEntryIndex = -1;
                this.selectedCondIndex = -1;
                this.selectedTextIndex = -1;
                refreshAllEditFields();
                updateDisableStates();
            }
        }
    }

    // ===================== 渲染 =====================

    @Override
    protected void renderWindowPre(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // -- 整体背景 --
        fillRect(g, guiLeft, guiTop, guiLeft + WIN_WIDTH, guiTop + WIN_HEIGHT, BG_DARK);
        fillRect(g, guiLeft + 1, guiTop + 1, guiLeft + WIN_WIDTH - 1, guiTop + WIN_HEIGHT - 1, BG_MID);

        // -- 标题栏 --
        fillRect(g, guiLeft, guiTop, guiLeft + WIN_WIDTH, guiTop + 16, BG_LIGHT);
        g.horizontalLine(guiLeft, guiLeft + WIN_WIDTH, guiTop + 16, LINE_COLOR);

        g.text(
                minecraft.font,
                LangData.GUI_DIALOGUE_EDITOR_TITLE.get(),
                guiLeft + 6, guiTop + 8 - minecraft.font.lineHeight/2,
                0xFFFFFFFF
        );

        // -- 左侧/右侧分隔线 --
        int divX = guiLeft + ENTRY_LIST_WIDTH;
        fillRect(g, divX, guiTop + 16, divX + 2, guiTop + WIN_HEIGHT - 32, LINE_COLOR);

        // -- 条目列表标题 --
        fillRect(g, entryListX, guiTop + 18, entryListX + entryListW, guiTop + 22, BG_LIGHT);

        // -- 绘制三个列表 --
        drawEntryList(g);
        drawConditionList(g);
        drawTextList(g);

        // -- 加载状态 --
        if (!dataReceived) {
            fillRect(g, guiLeft + EDIT_PANEL_X + 5, guiTop + 40,
                    guiLeft + EDIT_PANEL_X + 105, guiTop + 56, 0x88_000000);
        }

        // -- 占位符帮助提示 --
        if (helpVisible) {
            drawPlaceholderHelp(g, mouseX, mouseY);
        }
    }

    // ===================== 占位符帮助面板 =====================

    /** 缓存的帮助文本列表，避免每帧重新构建 */
    private static List<Component> cachedHelpLines = null;

    private static List<Component> getHelpLines() {
        if (cachedHelpLines == null) {
            cachedHelpLines = List.of(
                    LangData.GUI_DIALOGUE_HELP_TITLE.get().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    LangData.GUI_DIALOGUE_HELP_PLAYER_NAME.get(),
                    LangData.GUI_DIALOGUE_HELP_RECIPE_NAME.get(),
                    LangData.GUI_DIALOGUE_HELP_SEARCH_ITEM.get(),
                    LangData.GUI_DIALOGUE_HELP_SEARCH_TICKS.get(),
                    LangData.GUI_DIALOGUE_HELP_ACTIVITY.get(),
                    LangData.GUI_DIALOGUE_HELP_BR.get()
            );
        }
        return cachedHelpLines;
    }

    private void drawPlaceholderHelp(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.setTooltipForNextFrame(font, getHelpLines(), java.util.Optional.empty(),
                guiLeft + EDIT_PANEL_X + 5, guiTop + 35);
    }

    // ===================== 条目列表渲染 =====================

    private void drawEntryList(GuiGraphicsExtractor g) {
        List<DialogueEntry> entries = editingData.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            int ri = i - entryListScroll;
            if (ri < 0 || ri >= entryListVisible) continue;
            int y = entryListY + ri * ROW_HEIGHT;
            boolean sel = (i == selectedEntryIndex);
            int bg = sel ? BG_SELECTED : (i % 2 == 0 ? BG_MID : BG_LIGHT);
            fillRect(g, entryListX, y, entryListX + entryListW, y + ROW_HEIGHT - 2, bg);
            // 选中边框
            if (sel) {
                g.horizontalLine(entryListX, entryListX + entryListW, y, 0xFF_88CC88);
                g.horizontalLine(entryListX, entryListX + entryListW, y + ROW_HEIGHT - 2, 0xFF_88CC88);
            }
            // 预览文字：条件摘要 + 首条对话截断
            drawEntryPreview(g, entries.get(i), entryListX + 3, y + 1);
        }
    }

    /**
     * 绘制条目的预览文本：条件摘要（前2个条件的紧凑表示） + 第一条对话文本截断。
     */
    // TODO: 人工审查 - 2026-06-23 - drawEntryPreview 使用滚动文本替代截断
    private void drawEntryPreview(GuiGraphicsExtractor g, DialogueEntry entry, int x, int y) {
        StringBuilder sb = new StringBuilder();
        // 条件摘要
        List<Condition> conds = entry.conditions();
        if (!conds.isEmpty()) {
            sb.append('[');
            int maxConds = Math.min(2, conds.size());
            for (int ci = 0; ci < maxConds; ci++) {
                if (ci > 0) sb.append(',');
                sb.append(conds.get(ci).type().getSerializedName());
                sb.append('=');
                sb.append(conds.get(ci).value());
            }
            if (conds.size() > 2) sb.append(",..");
            sb.append("] ");
        }
        // 第一条对话文本
        if (!entry.texts().isEmpty()) {
            String firstText = entry.texts().getFirst().getString();
            sb.append(firstText);
        }
        if (!sb.isEmpty()) {
            drawScrollTextWithBackdrop(g, sb.toString(), x, y,
                    entryListW - 8, PREVIEW_ALPHA, PREVIEW_COLOR);
        }
    }

    // ===================== 条件列表渲染 =====================

    private void drawConditionList(GuiGraphicsExtractor g) {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) return;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        List<Condition> conds = entry.conditions();

        // 标题分隔线
        g.horizontalLine(condListX, condListX + condListW, condListY - 4, LINE_COLOR);

        for (int i = 0; i < conds.size(); i++) {
            int ri = i - condListScroll;
            if (ri < 0 || ri >= condListVisible) continue;
            int y = condListY + ri * ROW_HEIGHT;
            boolean sel = (i == selectedCondIndex);
            int bg = sel ? BG_SUB_SELECTED : BG_MID;
            fillRect(g, condListX, y, condListX + condListW, y + ROW_HEIGHT - 1, bg);
            if (sel) {
                g.horizontalLine(condListX, condListX + condListW, y, 0xFF_88CC88);
                g.horizontalLine(condListX, condListX + condListW, y + ROW_HEIGHT - 1, 0xFF_88CC88);
            }
            // 预览文字: "#N [type] [op] [value]"
            Condition cond = conds.get(i);
            String condPreview = "#" + i + " " + cond.type().getSerializedName()
                    + " " + cond.operator() + " \"" + cond.value() + "\"";
            // TODO: 人工审查 - 2026-06-23 - 条件列表使用滚动文本
            drawScrollTextWithBackdrop(g, condPreview, condListX + 4, y + 1,
                    condListW - 8, PREVIEW_ALPHA, PREVIEW_COLOR);
        }
        // 底部线
        g.horizontalLine(condListX, condListX + condListW, condListY + condListH, LINE_COLOR);
    }

    // ===================== 文本列表渲染 =====================

    private void drawTextList(GuiGraphicsExtractor g) {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) return;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        List<Component> texts = entry.texts();

        // 标题分隔线
        g.horizontalLine(textListX, textListX + textListW, textListY - 4, LINE_COLOR);

        for (int i = 0; i < texts.size(); i++) {
            int ri = i - textListScroll;
            if (ri < 0 || ri >= textListVisible) continue;
            int y = textListY + ri * ROW_HEIGHT;
            boolean sel = (i == selectedTextIndex);
            int bg = sel ? BG_SUB_SELECTED : BG_MID;
            fillRect(g, textListX, y, textListX + textListW, y + ROW_HEIGHT - 1, bg);
            if (sel) {
                g.horizontalLine(textListX, textListX + textListW, y, 0xFF_88CC88);
                g.horizontalLine(textListX, textListX + textListW, y + ROW_HEIGHT - 1, 0xFF_88CC88);
            }
            // 预览文字: "#N 文本内容..."
            String raw = texts.get(i).getString();
            String textPreview = "#" + i + " " + raw;
            // TODO: 人工审查 - 2026-06-23 - 文本列表使用滚动文本
            drawScrollTextWithBackdrop(g, textPreview, textListX + 4, y + 1,
                    textListW - 8, PREVIEW_ALPHA, PREVIEW_COLOR);
        }
        // 底部线
        g.horizontalLine(textListX, textListX + textListW, textListY + textListH, LINE_COLOR);
    }

    // ===================== 工具方法 =====================

    private static void fillRect(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
        for (int y = y1; y < y2; y++) {
            g.horizontalLine(x1, x2, y, color);
        }
    }

    /**
     * 截断文本，超过最大长度则添加 "..."。
     */
    private static String truncateText(String text, int maxLen) {
        if (text.length() <= maxLen) return text;
        return text.substring(0, Math.max(1, maxLen - 3)) + "...";
    }

    // TODO: 人工审查 - 2026-06-23 - 新增滚动文本绘制方法，替代截断省略号

    /**
     * 绘制可滚动的文本（Draw Scrollable Text）。
     * 当文本宽度超过 maxWidth 时，文本将平滑左右滚动以显示完整内容；
     * 当文本宽度不超过 maxWidth 时，正常绘制。
     *
     * @param g        图形上下文
     * @param text     要绘制的文本
     * @param x        起始 X 坐标
     * @param y        起始 Y 坐标
     * @param maxWidth 最大绘制宽度
     * @param color    文本颜色
     */
    // TODO: 人工审查 - 2026-06-23 - GuiGraphicsExtractor API 验证 - 使用字符截断替代 scissor
    private void drawScrollText(GuiGraphicsExtractor g, String text, int x, int y, int maxWidth, int color) {
        int textWidth = font.width(text);
        if (textWidth <= maxWidth) {
            g.text(font, Component.literal(text), x, y, color);
            return;
        }
        int overflow = textWidth - maxWidth;
        int cycleLen = overflow + 40;
        int pos = scrollAnimTick % (cycleLen * 2);
        int offset;
        if (pos < 30) {
            offset = 0;
        } else if (pos < cycleLen) {
            offset = pos - 30;
        } else if (pos < cycleLen + 30) {
            offset = overflow;
        } else {
            offset = overflow - (pos - cycleLen - 30);
        }
        // 通过计算字符宽度找到可见部分的起始字符
        int charOffset = 0;
        int accumulatedWidth = 0;
        while (charOffset < text.length() && accumulatedWidth < offset) {
            accumulatedWidth += font.width(text.substring(charOffset, charOffset + 1));
            charOffset++;
        }
        // 找到可见部分的结束字符
        int visibleEnd = charOffset;
        int visibleWidth = 0;
        while (visibleEnd < text.length() && visibleWidth < maxWidth) {
            visibleWidth += font.width(text.substring(visibleEnd, visibleEnd + 1));
            visibleEnd++;
        }
        String visible = text.substring(charOffset, Math.min(visibleEnd, text.length()));
        g.text(font, Component.literal(visible), x, y, color);
    }

    /**
     * 绘制带背幕的可滚动文本（Draw Scrollable Text with Backdrop）。
     */
    private void drawScrollTextWithBackdrop(GuiGraphicsExtractor g, String text, int x, int y, int maxWidth,
                                            int alpha, int color) {
        int textWidth = font.width(text);
        if (textWidth <= maxWidth) {
            g.textWithBackdrop(font, Component.literal(text), x, y, alpha, color);
            return;
        }
        int overflow = textWidth - maxWidth;
        int cycleLen = overflow + 40;
        int pos = scrollAnimTick % (cycleLen * 2);
        int offset;
        if (pos < 30) {
            offset = 0;
        } else if (pos < cycleLen) {
            offset = pos - 30;
        } else if (pos < cycleLen + 30) {
            offset = overflow;
        } else {
            offset = overflow - (pos - cycleLen - 30);
        }
        // 通过计算字符宽度找到可见部分的起始和结束字符
        int charOffset = 0;
        int accumulatedWidth = 0;
        while (charOffset < text.length() && accumulatedWidth < offset) {
            accumulatedWidth += font.width(text.substring(charOffset, charOffset + 1));
            charOffset++;
        }
        int visibleEnd = charOffset;
        int visibleWidth = 0;
        while (visibleEnd < text.length() && visibleWidth < maxWidth) {
            visibleWidth += font.width(text.substring(visibleEnd, visibleEnd + 1));
            visibleEnd++;
        }
        String visible = text.substring(charOffset, Math.min(visibleEnd, text.length()));
        g.textWithBackdrop(font, Component.literal(visible), x, y, alpha, color);
    }

    // ===================== 鼠标交互 =====================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        int mx = (int) event.x();
        int my = (int) event.y();

        // 1. 条目列表点击
        if (clickInRect(mx, my, entryListX, entryListY, entryListW, entryListH)) {
            int relY = my - entryListY;
            int idx = relY / ROW_HEIGHT + entryListScroll;
            if (idx >= 0 && idx < editingData.size()) {
                selectEntry(idx);
                return true;
            }
        }

        // 2. 条件列表点击
        if (selectedEntryIndex >= 0
                && clickInRect(mx, my, condListX, condListY, condListW, condListH)) {
            int relY = my - condListY;
            int idx = relY / ROW_HEIGHT + condListScroll;
            DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
            if (idx >= 0 && idx < entry.conditions().size()) {
                selectCondition(idx);
                return true;
            }
        }

        // 3. 文本列表点击
        if (selectedEntryIndex >= 0
                && clickInRect(mx, my, textListX, textListY, textListW, textListH)) {
            int relY = my - textListY;
            int idx = relY / ROW_HEIGHT + textListScroll;
            DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
            if (idx >= 0 && idx < entry.texts().size()) {
                selectText(idx);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (super.mouseScrolled(x, y, scrollX, scrollY)) return true;

        int mx = (int) x;
        int my = (int) y;
        int delta = (int) scrollY;

        if (clickInRect(mx, my, entryListX, entryListY, entryListW, entryListH)) {
            int maxScroll = Math.max(0, editingData.size() - entryListVisible);
            entryListScroll = clamp(entryListScroll - delta, 0, maxScroll);
            return true;
        }

        if (selectedEntryIndex >= 0
                && clickInRect(mx, my, condListX, condListY, condListW, condListH)) {
            DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
            int maxScroll = Math.max(0, entry.conditions().size() - condListVisible);
            condListScroll = clamp(condListScroll - delta, 0, maxScroll);
            return true;
        }

        if (selectedEntryIndex >= 0
                && clickInRect(mx, my, textListX, textListY, textListW, textListH)) {
            DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
            int maxScroll = Math.max(0, entry.texts().size() - textListVisible);
            textListScroll = clamp(textListScroll - delta, 0, maxScroll);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private static boolean clickInRect(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    private static int clamp(int value, int min, int max) {
        return Math.clamp(value, min, max);
    }

    // ===================== 选择操作 =====================

    // TODO: 人工审查 - 2026-06-23 - 选择方法新增 updateDisableStates() 调用
    private void selectEntry(int index) {
        if (index == selectedEntryIndex) return;
        selectedEntryIndex = index;
        selectedCondIndex = -1;
        selectedTextIndex = -1;
        condListScroll = 0;
        textListScroll = 0;
        refreshAllEditFields();
        updateDisableStates();
    }

    private void selectCondition(int index) {
        if (index == selectedCondIndex) return;
        selectedCondIndex = index;
        selectedTextIndex = -1;
        refreshAllEditFields();
        updateDisableStates();
    }

    private void selectText(int index) {
        if (index == selectedTextIndex) return;
        selectedTextIndex = index;
        selectedCondIndex = -1;
        refreshAllEditFields();
        updateDisableStates();
    }

    // ===================== 编辑字段刷新 =====================

    // TODO: 人工审查 - 2026-06-23 - refreshAllEditFields 新增触发模式/事件类型/天气值的刷新，含动态控件可见性
    private void refreshAllEditFields() {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) {
            txtCondValue.setValue("");
            txtDialogueText.setValue("");
            txtFrequency.setValue("");
            updateCondButtonLabels();
            updateTriggerModeLabel();
            updateEventTypeLabel();
            updateWeatherVisibility();
            updateDisableStates();
            return;
        }

        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);

        // 频率
        txtFrequency.setValue(String.valueOf(entry.frequency()));

        // 触发模式 / 事件类型
        editingTriggerModeIdx = entry.triggerMode().ordinal();
        editingEventTypeIdx = entry.eventType().ordinal();
        updateTriggerModeLabel();
        updateEventTypeLabel();

        if (selectedCondIndex >= 0 && selectedCondIndex < entry.conditions().size()) {
            Condition cond = entry.conditions().get(selectedCondIndex);
            txtCondValue.setValue(cond.value());
            editingCondTypeIdx = cond.type().ordinal();
            editingCondOpIdx = getOpIndex(cond);
            // 如果是天气类型，同步天气值索引
            if (cond.type() == ConditionType.WEATHER) {
                editingWeatherIdx = getWeatherIndex(cond.value());
            }
        } else {
            txtCondValue.setValue("");
        }
        updateCondButtonLabels();
        updateWeatherButtonLabel();
        updateWeatherVisibility();

        if (selectedTextIndex >= 0 && selectedTextIndex < entry.texts().size()) {
            txtDialogueText.setValue(entry.texts().get(selectedTextIndex).getString());
        } else {
            txtDialogueText.setValue("");
        }

        updateDisableStates();
    }

    // TODO: 人工审查 - 2026-06-23 - 新增禁用状态更新方法
    /** 根据当前选中状态更新所有控件的禁用/启用状态 */
    private void updateDisableStates() {
        boolean hasEntry = selectedEntryIndex >= 0 && selectedEntryIndex < editingData.size();
        boolean hasCond = hasEntry && selectedCondIndex >= 0;
        boolean hasText = hasEntry && selectedTextIndex >= 0;

        // 触发模式/事件类型按钮：需要选中条目
        btnTriggerMode.active = hasEntry;
        btnEventType.active = hasEntry;

        // 频率编辑控件：需要选中条目
        txtFrequency.setEditable(hasEntry);
        btnApplyFreq.active = hasEntry;

        // 条件编辑控件：需要选中条件；单运算符类型（如 INTERACTION_HISTORY）仅显示不可切换
        btnCondType.active = hasCond;
        btnCondOp.active = hasCond && getCurrentOpArray().length > 1;
        txtCondValue.setEditable(hasCond);
        btnWeatherValue.active = hasCond;
        // 条件增删按钮始终可用（只要有条目选中）
        btnAddCond.active = hasEntry;
        btnDelCond.active = hasCond;
        btnEditCond.active = hasCond;

        // 文本编辑控件：需要选中文本
        txtDialogueText.setEditable(hasText);
        btnAddText.active = hasEntry;
        btnDelText.active = hasText;
        btnEditText.active = hasText;
    }

    private void updateCondButtonLabels() {
        btnCondType.setMessage(getCondTypeLabel());
        btnCondOp.setMessage(getCondOpLabel());
    }

    private Component getCondTypeLabel() {
        if (editingCondTypeIdx < 0 || editingCondTypeIdx >= ConditionType.values().length)
            return LangData.GUI_DIALOGUE_EDITOR_COND_TYPE_UNKNOWN.get();
        return LangData.GUI_DIALOGUE_EDITOR_COND_TYPE.get(
                ConditionType.values()[editingCondTypeIdx].getSerializedName());
    }

    // TODO: 人工审查 - 2026-06-23 - 运算符标签改用 "▼" 指示下拉行为，直接显示运算符符号
    private Component getCondOpLabel() {
        String op = getOpString(editingCondOpIdx);
        return Component.literal(op + " ▼");
    }

    // ===================== 操作符辅助方法 =====================

    private static final String[] OPS_NUMERIC = {">=", "<=", "==", ">", "<"};
    private static final String[] OPS_STRING = {"is", "is_not"};
    private static final String[] OPS_SINGLE = {"is"};

    private String getOpString(int idx) {
        String[] ops = getCurrentOpArray();
        return idx >= 0 && idx < ops.length ? ops[idx] : "?";
    }

    private int getOpIndex(Condition cond) {
        String[] ops = getOpArrayForType(cond.type());
        for (int i = 0; i < ops.length; i++) {
            if (ops[i].equals(cond.operator())) return i;
        }
        return 0;
    }

    private String[] getCurrentOpArray() {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) return OPS_STRING;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        if (selectedCondIndex < 0 || selectedCondIndex >= entry.conditions().size()) return OPS_STRING;
        return getOpArrayForType(entry.conditions().get(selectedCondIndex).type());
    }

    // TODO: 人工审查 - 2026-06-23 - NEARBY_PLAYERS/SEARCH_TIME 改为数值不等式运算符：确认运算符数组正确
    private static String[] getOpArrayForType(ConditionType type) {
        return switch (type) {
            case NEARBY_PLAYERS, SEARCH_TIME -> OPS_NUMERIC;
            case INTERACTION_HISTORY -> OPS_SINGLE;
            default -> OPS_STRING;
        };
    }

    // ===================== 条件编辑操作 =====================

    // TODO: 人工审查 - 2026-06-23 - cycleCondType 新增天气值可见性切换
    private void cycleCondType() {
        editingCondTypeIdx = (editingCondTypeIdx + 1) % ConditionType.values().length;
        editingCondOpIdx = 0;
        updateCondButtonLabels();
        updateWeatherVisibility();
    }

    private void cycleCondOp() {
        String[] ops = getCurrentOpArray();
        editingCondOpIdx = (editingCondOpIdx + 1) % ops.length;
        updateCondButtonLabels();
    }

    // TODO: 人工审查 - 2026-06-23 - 新增触发模式/事件类型/天气值循环方法

    /** 循环切换触发模式 */
    private void cycleTriggerMode() {
        if (selectedEntryIndex < 0) return;
        editingTriggerModeIdx = (editingTriggerModeIdx + 1) % DialogueTriggerMode.values().length;
        // 切换触发模式时，将事件类型重置为第一个有效类型
        if (DialogueTriggerMode.values()[editingTriggerModeIdx] == DialogueTriggerMode.POLLING) {
            editingEventTypeIdx = DialogueEventType.NONE.ordinal();
        }
        updateTriggerModeLabel();
        updateEventTypeLabel();
        commitTriggerModeAndEventType();
    }

    /** 循环切换事件类型 */
    private void cycleEventType() {
        if (selectedEntryIndex < 0) return;
        editingEventTypeIdx = (editingEventTypeIdx + 1) % DialogueEventType.values().length;
        updateEventTypeLabel();
        commitTriggerModeAndEventType();
    }

    /** 循环切换天气值（clear → rain → thunder → clear） */
    private void cycleWeatherValue() {
        editingWeatherIdx = (editingWeatherIdx + 1) % WEATHER_VALUES.length;
        updateWeatherButtonLabel();
        // 自动将天气值写入条件值输入框
        txtCondValue.setValue(WEATHER_VALUES[editingWeatherIdx]);
    }

    /** 天气可选值列表 */
    private static final String[] WEATHER_VALUES = {"clear", "rain", "thunder"};

    /** 根据天气字符串获取索引 */
    private static int getWeatherIndex(String weatherValue) {
        for (int i = 0; i < WEATHER_VALUES.length; i++) {
            if (WEATHER_VALUES[i].equalsIgnoreCase(weatherValue)) return i;
        }
        return 0;
    }

    /** 根据当前条件类型切换天气按钮和文本输入框的可见性 */
    // TODO: 人工审查 - 2026-06-23 - NEARBY_PLAYERS/SEARCH_TIME 改为数值不等式运算符：数字输入过滤
    private void updateWeatherVisibility() {
        boolean isWeather = (editingCondTypeIdx >= 0 && editingCondTypeIdx < ConditionType.values().length
                && ConditionType.values()[editingCondTypeIdx] == ConditionType.WEATHER);
        btnWeatherValue.visible = isWeather;
        txtCondValue.visible = !isWeather;
        // 同步更新输入过滤：数值类型仅接受整数
        updateCondValueFilter();
    }

    /**
     * 根据当前条件类型动态设置值输入框的过滤规则。
     * 数值类条件（NEARBY_PLAYERS、SEARCH_TIME）仅接受整数输入（支持负号前缀）；
     * 其他类型不设过滤，允许自由文本输入。
     */
    private void updateCondValueFilter() {
        if (editingCondTypeIdx < 0 || editingCondTypeIdx >= ConditionType.values().length) {
            txtCondValue.setFilter(s -> true);
            return;
        }
        ConditionType currentType = ConditionType.values()[editingCondTypeIdx];
        if (currentType == ConditionType.NEARBY_PLAYERS || currentType == ConditionType.SEARCH_TIME) {
            // 仅接受整数输入（可选负号前缀）
            txtCondValue.setFilter(s -> s.isEmpty() || s.matches("-?[0-9]*"));
        } else {
            // 其他类型：无过滤
            txtCondValue.setFilter(s -> true);
        }
    }

    /** 将触发模式和事件类型的编辑状态提交到当前选中的条目 */
    private void commitTriggerModeAndEventType() {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) return;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        DialogueTriggerMode mode = DialogueTriggerMode.values()[editingTriggerModeIdx];
        DialogueEventType eventType = DialogueEventType.values()[editingEventTypeIdx];
        if (entry.triggerMode() == mode && entry.eventType() == eventType) return;
        replaceEntryMeta(new DialogueEntry(entry.id(), entry.conditions(), entry.texts(),
                entry.frequency(), mode, eventType));
        dirty = true;
    }

    // ===================== 触发模式/事件类型/天气值标签方法 =====================

    private void updateTriggerModeLabel() {
        btnTriggerMode.setMessage(getTriggerModeLabel());
    }

    private void updateEventTypeLabel() {
        btnEventType.setMessage(getEventTypeLabel());
    }

    private void updateWeatherButtonLabel() {
        btnWeatherValue.setMessage(getWeatherValueLabel());
    }

    private Component getTriggerModeLabel() {
        if (editingTriggerModeIdx < 0 || editingTriggerModeIdx >= DialogueTriggerMode.values().length)
            return Component.literal("Mode: ?");
        return Component.literal("Mode: " + DialogueTriggerMode.values()[editingTriggerModeIdx].getSerializedName());
    }

    private Component getEventTypeLabel() {
        if (editingEventTypeIdx < 0 || editingEventTypeIdx >= DialogueEventType.values().length)
            return Component.literal("Event: ?");
        return Component.literal("Event: " + DialogueEventType.values()[editingEventTypeIdx].getSerializedName());
    }

    private Component getWeatherValueLabel() {
        if (editingWeatherIdx < 0 || editingWeatherIdx >= WEATHER_VALUES.length)
            return Component.literal("?");
        return Component.literal(WEATHER_VALUES[editingWeatherIdx]);
    }

    private void addCondition() {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) return;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        Condition newCond = new Condition(ConditionType.WEATHER, "is", "clear");
        List<Condition> mutableConds = new ArrayList<>(entry.conditions());
        mutableConds.add(newCond);
        replaceEntry(new DialogueEntry(entry.id(), mutableConds, entry.texts()));
        selectedCondIndex = mutableConds.size() - 1;
        selectedTextIndex = -1;
        condListScroll = Math.max(0, mutableConds.size() - condListVisible);
        refreshAllEditFields();
        dirty = true;
    }

    private void deleteCondition() {
        if (selectedEntryIndex < 0 || selectedCondIndex < 0) return;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        if (selectedCondIndex >= entry.conditions().size()) return;
        List<Condition> mutableConds = new ArrayList<>(entry.conditions());
        mutableConds.remove(selectedCondIndex);
        replaceEntry(new DialogueEntry(entry.id(), mutableConds, entry.texts()));
        if (mutableConds.isEmpty()) {
            selectedCondIndex = -1;
        } else {
            selectedCondIndex = Math.min(selectedCondIndex, mutableConds.size() - 1);
        }
        refreshAllEditFields();
        dirty = true;
    }

    private void commitConditionEdit() {
        if (selectedEntryIndex < 0 || selectedCondIndex < 0) return;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        if (selectedCondIndex >= entry.conditions().size()) return;

        ConditionType type = ConditionType.values()[editingCondTypeIdx];
        String op = getOpString(editingCondOpIdx);
        String val = txtCondValue.getValue();

        List<Condition> mutableConds = new ArrayList<>(entry.conditions());
        mutableConds.set(selectedCondIndex, new Condition(type, op, val));
        replaceEntry(new DialogueEntry(entry.id(), mutableConds, entry.texts()));
        refreshAllEditFields();
        dirty = true;
    }

    // ===================== 文本编辑操作 =====================

    private void addText() {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) return;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        List<Component> mutableTexts = new ArrayList<>(entry.texts());
        mutableTexts.add(Component.literal("New dialogue text"));
        replaceEntry(new DialogueEntry(entry.id(), entry.conditions(), mutableTexts));
        selectedTextIndex = mutableTexts.size() - 1;
        selectedCondIndex = -1;
        textListScroll = Math.max(0, mutableTexts.size() - textListVisible);
        refreshAllEditFields();
        dirty = true;
    }

    private void deleteText() {
        if (selectedEntryIndex < 0 || selectedTextIndex < 0) return;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        if (selectedTextIndex >= entry.texts().size()) return;
        List<Component> mutableTexts = new ArrayList<>(entry.texts());
        mutableTexts.remove(selectedTextIndex);
        replaceEntry(new DialogueEntry(entry.id(), entry.conditions(), mutableTexts));
        if (mutableTexts.isEmpty()) {
            selectedTextIndex = -1;
        } else {
            selectedTextIndex = Math.min(selectedTextIndex, mutableTexts.size() - 1);
        }
        refreshAllEditFields();
        dirty = true;
    }

    private void commitTextEdit() {
        if (selectedEntryIndex < 0 || selectedTextIndex < 0) return;
        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
        if (selectedTextIndex >= entry.texts().size()) return;

        List<Component> mutableTexts = new ArrayList<>(entry.texts());
        mutableTexts.set(selectedTextIndex, Component.literal(txtDialogueText.getValue()));
        replaceEntry(new DialogueEntry(entry.id(), entry.conditions(), mutableTexts));
        refreshAllEditFields();
        dirty = true;
    }

    // ===================== 条目元数据编辑 =====================

    /** 提交频率修改 */
    // TODO: 人工审查 - 2026-06-23 - commitFrequency 保留 triggerMode/eventType
    private void commitFrequency() {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) return;
        try {
            int newFreq = Integer.parseInt(txtFrequency.getValue());
            if (newFreq <= 0) newFreq = 1;
            DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);
            List<DialogueEntry> mutable = new ArrayList<>(editingData.getEntries());
            mutable.set(selectedEntryIndex, new DialogueEntry(entry.id(), entry.conditions(), entry.texts(),
                    newFreq, entry.triggerMode(), entry.eventType()));
            editingData = new DialogueData(mutable);
            refreshAllEditFields();
            updateDisableStates();
            dirty = true;
        } catch (NumberFormatException ignored) {
        }
    }

    // ===================== 条目操作 =====================

    private void addNewEntry() {
        DialogueEntry newEntry = DialogueEntry.create(
                List.of(new Condition(ConditionType.WEATHER, "is", "clear")),
                List.of(Component.literal("Hello, welcome!"))
        );
        List<DialogueEntry> mutable = new ArrayList<>(editingData.getEntries());
        mutable.add(newEntry);
        editingData = new DialogueData(mutable);
        selectedEntryIndex = mutable.size() - 1;
        selectedCondIndex = 0;
        selectedTextIndex = 0;
        condListScroll = 0;
        textListScroll = 0;
        entryListScroll = Math.max(0, mutable.size() - entryListVisible);
        refreshAllEditFields();
        updateDisableStates();
        dirty = true;
    }

    private void deleteSelectedEntry() {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) return;
        List<DialogueEntry> mutable = new ArrayList<>(editingData.getEntries());
        mutable.remove(selectedEntryIndex);
        editingData = new DialogueData(mutable);
        if (mutable.isEmpty()) {
            selectedEntryIndex = -1;
            selectedCondIndex = -1;
            selectedTextIndex = -1;
        } else {
            selectedEntryIndex = Math.min(selectedEntryIndex, mutable.size() - 1);
            selectedCondIndex = 0;
            selectedTextIndex = 0;
        }
        refreshAllEditFields();
        updateDisableStates();
        dirty = true;
    }

    // TODO: 人工审查 - 2026-06-23 - replaceEntry 保留触发模式/事件类型
    private void replaceEntry(DialogueEntry newEntry) {
        List<DialogueEntry> mutable = new ArrayList<>(editingData.getEntries());
        DialogueEntry old = mutable.get(selectedEntryIndex);
        mutable.set(selectedEntryIndex, new DialogueEntry(newEntry.id(), newEntry.conditions(), newEntry.texts(),
                old.frequency(), old.triggerMode(), old.eventType()));
        editingData = new DialogueData(mutable);
    }

    /**
     * 替换条目的元数据（频率、触发模式、事件类型），保留条件和文本不变。
     */
    private void replaceEntryMeta(DialogueEntry newEntry) {
        List<DialogueEntry> mutable = new ArrayList<>(editingData.getEntries());
        mutable.set(selectedEntryIndex, newEntry);
        editingData = new DialogueData(mutable);
    }

    // ===================== 复制/粘贴 =====================

    private void copyToClipboard() {
        clipboard = editingData;
        if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(
                    LangData.GUI_DIALOGUE_EDITOR_MSG_COPIED.get(String.valueOf(editingData.size()))
            );
        }
    }

    private void pasteFromClipboard() {
        if (clipboard == null) {
            if (minecraft.player != null) {
                minecraft.player.sendSystemMessage(LangData.GUI_DIALOGUE_EDITOR_MSG_CLIPBOARD_EMPTY.get());
            }
            return;
        }
        editingData = clipboard;
        selectedEntryIndex = editingData.size() > 0 ? 0 : -1;
        selectedCondIndex = selectedEntryIndex >= 0 ? 0 : -1;
        selectedTextIndex = selectedEntryIndex >= 0 ? 0 : -1;
        condListScroll = 0;
        textListScroll = 0;
        entryListScroll = 0;
        dirty = true;
        refreshAllEditFields();
        updateDisableStates();
        if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(
                    LangData.GUI_DIALOGUE_EDITOR_MSG_PASTED.get(String.valueOf(editingData.size()))
            );
        }
    }

    // ===================== 保存与关闭 =====================

    // TODO: 人工审查 - 2026-06-22 - 新增导出到文件方法，序列化 editingData 为 JSON 并保存到 CONVERSATION_DIR
    /**
     * 将当前编辑的对话数据导出为 JSON 文件，保存到 {@code ./shakenstir/bartender/conversation/} 目录。
     * 文件名从 {@code txtExportFilename} 获取，为空则使用 "untitled"。
     */
    private void exportToFile() {
        String rawName = txtExportFilename.getValue().trim();
        String fileName = rawName.isEmpty() ? "untitled" : rawName;

        try {
            java.nio.file.Path dir = Paths.CONVERSATION_DIR;
            Files.createDirectories(dir);

            java.nio.file.Path filePath = dir.resolve(fileName + ".json");

            var encodeResult = DialogueData.CODEC.encodeStart(JsonOps.INSTANCE, editingData);
            JsonElement json = encodeResult.resultOrPartial(err ->
                    LOGGER.error("Failed to encode DialogueData for export: {}", err)
            ).orElse(null);

            if (json == null) {
                if (minecraft.player != null) {
                    minecraft.player.sendSystemMessage(
                            LangData.GUI_DIALOGUE_EDITOR_MSG_EXPORT_FAILED.get("Serialization error")
                    );
                }
                return;
            }

            Files.writeString(filePath, json.toString());

            LOGGER.info("Exported dialogue data to: {}", filePath);
            if (minecraft.player != null) {
                minecraft.player.sendSystemMessage(
                        LangData.GUI_DIALOGUE_EDITOR_MSG_EXPORT_SUCCESS.get(fileName + ".json")
                );
            }
        } catch (IOException e) {
            LOGGER.error("Failed to export dialogue data", e);
            if (minecraft.player != null) {
                minecraft.player.sendSystemMessage(
                        LangData.GUI_DIALOGUE_EDITOR_MSG_EXPORT_FAILED.get(e.getMessage())
                );
            }
        }
    }

    private void saveAndClose() {
        entity.setDialogueData(editingData);
        Networking.sendToServer(new ServerboundBartenderDialogueUpdatePacket(entity.getId(), editingData));
        dirty = false;
        onClose();
    }

    @Override
    public void onClose() {
        if (dirty && minecraft.player != null) {
            minecraft.player.sendSystemMessage(LangData.GUI_DIALOGUE_EDITOR_MSG_UNSAVED.get());
        }
        super.onClose();
    }
}
