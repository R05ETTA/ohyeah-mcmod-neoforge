package com.ohyeah.ohyeahmod.sound.network;

import com.ohyeah.ohyeahmod.OhYeah;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = OhYeah.MODID)
public final class SoundNetworking {
    private SoundNetworking() {
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(EntitySoundPayload.TYPE, EntitySoundPayload.STREAM_CODEC, EntitySoundPayloadHandler::handle);
    }
}
