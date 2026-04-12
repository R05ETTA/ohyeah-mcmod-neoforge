package com.ohyeah.ohyeahmod.client.sound;

import com.ohyeah.ohyeahmod.OhYeah;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = OhYeah.MODID, value = Dist.CLIENT)
public final class ClientSoundTicker {
    private ClientSoundTicker() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientSoundPlaybackService.tick();
    }
}
