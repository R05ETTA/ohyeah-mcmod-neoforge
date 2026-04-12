package com.ohyeah.ohyeahmod.entity.tiansuluo.spawn;

import com.ohyeah.ohyeahmod.config.ModConfig;
import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.entity.TiansuluoPinkScarfEntity;
import com.ohyeah.ohyeahmod.entity.logic.SleepWakeSpeciesHandler;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 粉围巾天素罗的床边生成逻辑。
 */
public final class TiansuluoPinkScarfBedWakeSpawner implements SleepWakeSpeciesHandler {
    public static final TiansuluoPinkScarfBedWakeSpawner INSTANCE = new TiansuluoPinkScarfBedWakeSpawner();

    private TiansuluoPinkScarfBedWakeSpawner() {
    }

    @Override
    public String speciesId() {
        return "tiansuluo_pink_scarf";
    }

    @Override
    public boolean canSpawnAt(ServerPlayer player, BlockPos origin) {
        return isBed(player.serverLevel(), origin);
    }

    @Override
    public void trySpawn(ServerPlayer player, BlockPos origin) {
        ServerLevel level = player.serverLevel();
        if (!isBed(level, origin)) return;

        List<BlockPos> spawnPositions = findSpawnPositions(level, origin, 3, 2);
        if (spawnPositions.size() < 2) return;

        spawnEntity(level, spawnPositions.get(0), false);
        spawnEntity(level, spawnPositions.get(1), true);
    }

    private void spawnEntity(ServerLevel level, BlockPos pos, boolean baby) {
        TiansuluoPinkScarfEntity entity = ModEntityTypes.TIANSULUO_PINK_SCARF.get().create(level);
        if (entity != null) {
            entity.moveTo(Vec3.atBottomCenterOf(pos));
            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            if (baby) {
                entity.setBaby(true);
            }
            level.addFreshEntity(entity);
        }
    }

    private List<BlockPos> findSpawnPositions(ServerLevel level, BlockPos bedPos, int radius, int needed) {
        List<BlockPos> positions = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(bedPos.offset(-radius, -1, -radius), bedPos.offset(radius, 1, radius))) {
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolid()) {
                positions.add(pos.immutable());
                if (positions.size() >= needed) break;
            }
        }
        return positions;
    }

    private boolean isBed(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof BedBlock;
    }
}
