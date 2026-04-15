package com.ohyeah.ohyeahmod.client.sound;

import com.ohyeah.ohyeahmod.OhYeah;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 驱动 ClientSoundManager 的每一帧状态更新。
 */
@EventBusSubscriber(modid = OhYeah.MODID, value = Dist.CLIENT)
public final class SoundTickDriver {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientSoundManager.update();
    }

    private SoundTickDriver() {}
}
