package com.ohyeah.ohyeahmod;

import com.ohyeah.ohyeahmod.entity.logic.SleepWakeGameplayCoordinator;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

@EventBusSubscriber(modid = OhYeah.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class ModEvents {
    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.updateLevel()) {
            player.getSleepingPos().ifPresent(pos -> {
                SleepWakeGameplayCoordinator.trySpawnAfterWake(player, pos);
            });
        }
    }
}
