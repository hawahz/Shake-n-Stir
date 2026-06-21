package io.github.hawah.shakenstir.client.gui;

import io.github.hawah.shakenstir.content.dialogue.Condition;
import io.github.hawah.shakenstir.content.dialogue.ConditionType;
import io.github.hawah.shakenstir.content.dialogue.DialogueData;
import io.github.hawah.shakenstir.content.dialogue.DialogueEntry;
import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.foundation.networking.ServerboundBartenderDialogueUpdatePacket;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import io.github.hawah.shakenstir.lib.networking.Networking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
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

    // ===================== Widgets =====================
    private EditBox txtCondValue;
    private EditBox txtDialogueText;
    private Button btnCondType;
    private Button btnCondOp;

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
        super(Component.literal("Dialogue Editor"));
        this.entity = entity;
        this.editingData = entity.getDialogueData();
        this.dataReceived = !editingData.isEmpty();
    }

    // ===================== 初始化 =====================

    @Override
    protected void init() {
        setTextureSize(WIN_WIDTH, WIN_HEIGHT);
        super.init();

        computeLayout();

        int btnY = guiTop + WIN_HEIGHT - 25;

        // -- 条件编辑控件 --
        btnCondType = Button.builder(getCondTypeLabel(), btn -> cycleCondType())
                .pos(guiLeft + EDIT_PANEL_X + 5, condEditorY).size(80, 16).build();
        addSortedRenderWidget(btnCondType);

        btnCondOp = Button.builder(getCondOpLabel(), btn -> cycleCondOp())
                .pos(guiLeft + EDIT_PANEL_X + 90, condEditorY).size(45, 16).build();
        addSortedRenderWidget(btnCondOp);

        txtCondValue = new EditBox(font, guiLeft + EDIT_PANEL_X + 140, condEditorY, 40, 16, Component.literal("val"));
        txtCondValue.setMaxLength(32);
        addSortedRenderWidget(txtCondValue);

        int condBtnY = condEditorY + 18;
        Button btnAddCond = Button.builder(Component.literal("+Cond"), btn -> addCondition())
                .pos(guiLeft + EDIT_PANEL_X + 5, condBtnY).size(50, 16).build();
        addSortedRenderWidget(btnAddCond);

        Button btnDelCond = Button.builder(Component.literal("-Cond"), btn -> deleteCondition())
                .pos(guiLeft + EDIT_PANEL_X + 60, condBtnY).size(50, 16).build();
        addSortedRenderWidget(btnDelCond);

        Button btnEditCond = Button.builder(Component.literal("Apply"), btn -> commitConditionEdit())
                .pos(guiLeft + EDIT_PANEL_X + 120, condBtnY).size(55, 16).build();
        addSortedRenderWidget(btnEditCond);

        // -- 文本编辑控件 --
        txtDialogueText = new EditBox(font, guiLeft + EDIT_PANEL_X + 5, textEditorY, 175, 16, Component.literal("text"));
        txtDialogueText.setMaxLength(128);
        addSortedRenderWidget(txtDialogueText);

        int textBtnY = textEditorY + 18;
        Button btnAddText = Button.builder(Component.literal("+Text"), btn -> addText())
                .pos(guiLeft + EDIT_PANEL_X + 5, textBtnY).size(50, 16).build();
        addSortedRenderWidget(btnAddText);

        Button btnDelText = Button.builder(Component.literal("-Text"), btn -> deleteText())
                .pos(guiLeft + EDIT_PANEL_X + 60, textBtnY).size(50, 16).build();
        addSortedRenderWidget(btnDelText);

        Button btnEditText = Button.builder(Component.literal("Apply"), btn -> commitTextEdit())
                .pos(guiLeft + EDIT_PANEL_X + 120, textBtnY).size(55, 16).build();
        addSortedRenderWidget(btnEditText);

        // -- 底栏按钮 --
        Button btnSave = Button.builder(Component.literal("Save"), btn -> saveAndClose())
                .pos(guiLeft + WIN_WIDTH - 60, btnY).size(50, 20).build();
        addSortedRenderWidget(btnSave);

        Button btnCopy = Button.builder(Component.literal("Copy"), btn -> copyToClipboard())
                .pos(guiLeft + EDIT_PANEL_X + 60, btnY).size(50, 20).build();
        addSortedRenderWidget(btnCopy);

        Button btnPaste = Button.builder(Component.literal("Paste"), btn -> pasteFromClipboard())
                .pos(guiLeft + EDIT_PANEL_X + 115, btnY).size(50, 20).build();
        addSortedRenderWidget(btnPaste);

        Button btnAddEntry = Button.builder(Component.literal("+ Entry"), btn -> addNewEntry())
                .pos(guiLeft + 5, btnY).size(60, 20).build();
        addSortedRenderWidget(btnAddEntry);

        Button btnDelEntry = Button.builder(Component.literal("- Entry"), btn -> deleteSelectedEntry())
                .pos(guiLeft + 70, btnY).size(60, 20).build();
        addSortedRenderWidget(btnDelEntry);

        finishRegister();
        refreshAllEditFields();
    }

    /**
     * 预计算所有区域的边界。
     */
    private void computeLayout() {
        // 条目列表
        entryListX = guiLeft + 5;
        entryListY = guiTop + 24;
        entryListW = ENTRY_LIST_WIDTH - 10;
        entryListH = WIN_HEIGHT - 56;
        entryListVisible = entryListH / ROW_HEIGHT;

        // 条件列表
        condListX = guiLeft + EDIT_PANEL_X + 5;
        condListY = guiTop + 42;
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

    @Override
    public void tick() {
        super.tick();
        if (!dataReceived) {
            DialogueData current = entity.getDialogueData();
            if (current != null) {
                this.editingData = current;
                this.dataReceived = true;
                this.selectedEntryIndex = -1;
                this.selectedCondIndex = -1;
                this.selectedTextIndex = -1;
                refreshAllEditFields();
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
        // 第一条对话文本截断
        if (!entry.texts().isEmpty()) {
            String firstText = entry.texts().get(0).getString();
            sb.append(truncateText(firstText, MAX_PREVIEW_CHARS - sb.length()));
        }
        if (sb.length() > 0) {
            g.textWithBackdrop(font, Component.literal(sb.toString()), x, y, PREVIEW_ALPHA, PREVIEW_COLOR);
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
            g.textWithBackdrop(font, Component.literal(condPreview), condListX + 4, y + 1, PREVIEW_ALPHA, PREVIEW_COLOR);
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
            // 预览文字: "#N 截断文本..."
            String raw = texts.get(i).getString();
            String textPreview = "#" + i + " " + truncateText(raw, MAX_PREVIEW_CHARS);
            g.textWithBackdrop(font, Component.literal(textPreview), textListX + 4, y + 1, PREVIEW_ALPHA, PREVIEW_COLOR);
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
        return Math.max(min, Math.min(max, value));
    }

    // ===================== 选择操作 =====================

    private void selectEntry(int index) {
        if (index == selectedEntryIndex) return;
        selectedEntryIndex = index;
        selectedCondIndex = -1;
        selectedTextIndex = -1;
        condListScroll = 0;
        textListScroll = 0;
        refreshAllEditFields();
    }

    private void selectCondition(int index) {
        if (index == selectedCondIndex) return;
        selectedCondIndex = index;
        selectedTextIndex = -1;
        refreshAllEditFields();
    }

    private void selectText(int index) {
        if (index == selectedTextIndex) return;
        selectedTextIndex = index;
        selectedCondIndex = -1;
        refreshAllEditFields();
    }

    // ===================== 编辑字段刷新 =====================

    private void refreshAllEditFields() {
        if (selectedEntryIndex < 0 || selectedEntryIndex >= editingData.size()) {
            txtCondValue.setValue("");
            txtDialogueText.setValue("");
            updateCondButtonLabels();
            return;
        }

        DialogueEntry entry = editingData.getEntries().get(selectedEntryIndex);

        if (selectedCondIndex >= 0 && selectedCondIndex < entry.conditions().size()) {
            Condition cond = entry.conditions().get(selectedCondIndex);
            txtCondValue.setValue(cond.value());
            editingCondTypeIdx = cond.type().ordinal();
            editingCondOpIdx = getOpIndex(cond);
        } else {
            txtCondValue.setValue("");
        }
        updateCondButtonLabels();

        if (selectedTextIndex >= 0 && selectedTextIndex < entry.texts().size()) {
            txtDialogueText.setValue(entry.texts().get(selectedTextIndex).getString());
        } else {
            txtDialogueText.setValue("");
        }
    }

    private void updateCondButtonLabels() {
        btnCondType.setMessage(getCondTypeLabel());
        btnCondOp.setMessage(getCondOpLabel());
    }

    private Component getCondTypeLabel() {
        if (editingCondTypeIdx < 0 || editingCondTypeIdx >= ConditionType.values().length)
            return Component.literal("Type: ?");
        return Component.literal("Type: " + ConditionType.values()[editingCondTypeIdx].getSerializedName());
    }

    private Component getCondOpLabel() {
        return Component.literal("Op: " + getOpString(editingCondOpIdx));
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

    private static String[] getOpArrayForType(ConditionType type) {
        return switch (type) {
            case NEARBY_PLAYERS -> OPS_NUMERIC;
            case INTERACTION_HISTORY -> OPS_SINGLE;
            default -> OPS_STRING;
        };
    }

    // ===================== 条件编辑操作 =====================

    private void cycleCondType() {
        editingCondTypeIdx = (editingCondTypeIdx + 1) % ConditionType.values().length;
        editingCondOpIdx = 0;
        updateCondButtonLabels();
    }

    private void cycleCondOp() {
        String[] ops = getCurrentOpArray();
        editingCondOpIdx = (editingCondOpIdx + 1) % ops.length;
        updateCondButtonLabels();
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
        dirty = true;
    }

    private void replaceEntry(DialogueEntry newEntry) {
        List<DialogueEntry> mutable = new ArrayList<>(editingData.getEntries());
        mutable.set(selectedEntryIndex, newEntry);
        editingData = new DialogueData(mutable);
    }

    // ===================== 复制/粘贴 =====================

    private void copyToClipboard() {
        clipboard = editingData;
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.sendSystemMessage(
                    Component.literal("Copied " + editingData.size() + " dialogue entries")
            );
        }
    }

    private void pasteFromClipboard() {
        if (clipboard == null) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.literal("Clipboard is empty!"));
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
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.sendSystemMessage(
                    Component.literal("Pasted " + editingData.size() + " dialogue entries")
            );
        }
    }

    // ===================== 保存与关闭 =====================

    private void saveAndClose() {
        entity.setDialogueData(editingData);
        Networking.sendToServer(new ServerboundBartenderDialogueUpdatePacket(entity.getId(), editingData));
        dirty = false;
        onClose();
    }

    @Override
    public void onClose() {
        if (dirty && minecraft != null && minecraft.player != null) {
            minecraft.player.sendSystemMessage(
                    Component.literal("Unsaved changes. Use 'Save' to apply.")
            );
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
