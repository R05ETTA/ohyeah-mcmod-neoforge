package com.ohyeah.ohyeahmod.entity.logic;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.config.ModConfig;
import com.ohyeah.ohyeahmod.entity.tiansuluo.spawn.TiansuluoPinkScarfBedWakeSpawner;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

@EventBusSubscriber(modid = OhYeah.MODID)
public final class SleepWakeGameplayCoordinator {
    private static final Map<String, SleepWakeSpeciesHandler> HANDLERS = new LinkedHashMap<>();

    static {
        register(TiansuluoPinkScarfBedWakeSpawner.INSTANCE);
    }

    private SleepWakeGameplayCoordinator() {
    }

    private static void register(SleepWakeSpeciesHandler handler) {
        HANDLERS.put(handler.speciesId(), handler);
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!ModConfig.SLEEP_WAKE_ENABLED.get()) {
            return;
        }

        BlockPos origin = player.blockPosition();
        for (String speciesId : ModConfig.SLEEP_WAKE_SPECIES.get()) {
            SleepWakeSpeciesHandler handler = HANDLERS.get(speciesId);
            if (handler != null && handler.canSpawnAt(player, origin)) {
                handler.trySpawn(player, origin);
                return;
            }
        }
    }
}
