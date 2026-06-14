package io.github.hawah.shakenstir.client.gui;

import io.github.hawah.shakenstir.content.entity.BartenderEntity;
import io.github.hawah.shakenstir.lib.client.gui.BaseScreen;
import net.minecraft.network.chat.Component;

/**
 * TODO
 * 设置对话逻辑和触发内容
 * 触发方式：
 * 开始shake
 * 找东西
 * 找不到东西
 * 碰到玩家
 * 结束
 *
 * 条件判断：
 * 天气
 * 是否首次碰到玩家
 * 当前制作配方
 *
 * 玩家对话选项：
 * 玩家询问
 */
public class DialogueEditorScreen extends BaseScreen {

    private final BartenderEntity entity;

    public DialogueEditorScreen(BartenderEntity entity) {
        super(Component.empty());
        this.entity = entity;
    }
}
