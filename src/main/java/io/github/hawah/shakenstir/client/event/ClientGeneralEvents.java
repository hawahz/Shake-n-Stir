package io.github.hawah.shakenstir.client.event;

import io.github.hawah.shakenstir.ShakenStirClient;
import io.github.hawah.shakenstir.foundation.event.SnsEventBus;
import io.github.hawah.shakenstir.lib.client.render.outliner.Outliner;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import static io.github.hawah.shakenstir.client.event.MC.getLevel;
import static io.github.hawah.shakenstir.client.event.MC.getPlayer;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientGeneralEvents {

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        if (getLevel() == null) {
            return;
        }
        ShakenStirClient.GLASSWARE_HANDLER.tick();
        ShakenStirClient.SHAKE_HANDLER.tick();
        ShakenStirClient.CABINET_HUD.tick();
        ShakenStirClient.BAR_BUILDER_HANDLER.tick();
        ShakenStirClient.MENU_HUD.tick();
        Outliner.tick();
        if (getPlayer() == null) {
            return;
        }
    }

    // TODO: 人工审查 | 2026-06-29 | IDE Linter | 类型:初始化迁移
// 概述: SnsEventBus.initialize() 被 Linter 从 ShakenStir 构造函数移至此处 (客户端初始化点)。
//        初始化通过 SPI 加载 (EventHandlerLoader)，参数 "io.github.hawah.shakenstir" 现已废弃。
// 涉及: onClientSetup() 新增 initialize 调用
// 原状: 此调用原在 ShakenStir 构造函数中 (第一行); 此处原有 initialize 调用 (冗余保护, 现为主入口)
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        SnsEventBus.initialize("io.github.hawah.shakenstir");
    }

    public static final Identifier HOTBAR_SELECTION_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_selection");

}
