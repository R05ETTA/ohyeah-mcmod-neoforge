package com.ohyeah.ohyeahmod;

import com.ohyeah.ohyeahmod.client.ClientEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 客户端专属入口点 (Client Entry Point)。
 * 在此初始化仅在客户端运行的代码（如渲染器、屏幕 UI 等），避免在服务端触发崩溃。
 */
@Mod(value = OhYeah.MODID, dist = Dist.CLIENT)
public class OhYeahClient {
    
    public OhYeahClient(IEventBus modEventBus, ModContainer container) {
        // [注册配置界面]
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // [注册生命周期事件]
        modEventBus.addListener(this::onClientSetup);
        
        // [注册客户端渲染器事件]
        // NeoForge 1.21+ 规范：放弃使用 @EventBusSubscriber(bus=MOD)，直接在总线上绑定方法
        modEventBus.addListener(ClientEntityRenderers::onRegisterLayerDefinitions);
        modEventBus.addListener(ClientEntityRenderers::onRegisterRenderers);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // 客户端初始化逻辑 (入队执行非线程安全操作)
        event.enqueueWork(() -> {
            // ...
        });
    }
}
