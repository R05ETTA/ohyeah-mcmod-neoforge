package com.ohyeah.ohyeahmod.entity.tiansuluo.spawn;

import com.ohyeah.ohyeahmod.config.ModSpeciesConfigs;
import com.ohyeah.ohyeahmod.config.SpeciesConfig;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class TiansuluoPinkScarfBedWakeSpawner implements SleepWakeSpeciesHandler {
    public static final TiansuluoPinkScarfBedWakeSpawner INSTANCE = new TiansuluoPinkScarfBedWakeSpawner();

    private TiansuluoPinkScarfBedWakeSpawner() {
    }

    @Override
    public String speciesId() {
        return "tiansuluo_pink_scarf";
    }

    @Override
    public boolean shouldQueueSpawn(ServerPlayer player) {
        if (player == null) return false;
        SpeciesConfig config = ModSpeciesConfigs.pinkScarf();
        // 假设我们在 config 中有这个开关，或者直接根据全局配置
        if (player.getSleepingPos().isEmpty()) return false;
        return isBed(player.serverLevel(), player.getSleepingPos().get());
    }

    @Override
    public boolean canSpawnAt(ServerPlayer player, BlockPos bedPos) {
        return isBed(player.serverLevel(), bedPos);
    }

    @Override
    public void trySpawn(ServerPlayer player, BlockPos bedPos) {
        ServerLevel world = player.serverLevel();
        SpeciesConfig config = ModSpeciesConfigs.pinkScarf();
        
        List<BlockPos> spawnPositions = findSpawnPositions(world, bedPos, 4, 2);
        if (spawnPositions.size() < 2) return;

        TiansuluoPinkScarfEntity adult = createEntity(world, spawnPositions.get(0), false);
        TiansuluoPinkScarfEntity baby = createEntity(world, spawnPositions.get(1), true);

        if (adult != null) world.addFreshEntity(adult);
        if (baby != null) world.addFreshEntity(baby);
    }

    private static @Nullable TiansuluoPinkScarfEntity createEntity(ServerLevel world, BlockPos pos, boolean baby) {
        TiansuluoPinkScarfEntity entity = ModEntityTypes.TIANSULUO_PINK_SCARF.get().create(world);
        if (entity == null) return null;

        Vec3 spawnPos = Vec3.atBottomCenterOf(pos);
        entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, world.getRandom().nextFloat() * 360.0F, 0.0F);
        entity.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
        
        if (baby) {
            entity.setBaby(true);
            entity.setAge(ModSpeciesConfigs.pinkScarf().breeding().babyGrowthAgeTicks());
        } else {
            entity.setAge(0);
        }
        return entity;
    }

    private static List<BlockPos> findSpawnPositions(ServerLevel world, BlockPos bedPos, int radius, int needed) {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int distance = 1; distance <= radius; distance++) {
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dz = -distance; dz <= distance; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != distance) continue;
                    for (int dy = 0; dy >= -2; dy--) {
                        mutable.set(bedPos.getX() + dx, bedPos.getY() + dy, bedPos.getZ() + dz);
                        if (!isSafeSpawnPos(world, mutable, bedPos)) continue;
                        BlockPos candidate = mutable.immutable();
                        if (!positions.contains(candidate)) {
                            positions.add(candidate);
                            if (positions.size() >= needed) return positions;
                        }
                    }
                }
            }
        }
        return positions;
    }

    private static boolean isSafeSpawnPos(ServerLevel world, BlockPos pos, BlockPos bedPos) {
        if (pos.equals(bedPos)) return false;
        BlockState floor = world.getBlockState(pos.below());
        if (floor.isAir()) return false;
        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.above()).isAir()) return false;
        if (!world.getFluidState(pos).isEmpty() || !world.getFluidState(pos.above()).isEmpty()) return false;
        if (isBed(world, pos) || isBed(world, pos.above())) return false;

        Vec3 spawnPos = Vec3.atBottomCenterOf(pos);
        AABB box = ModEntityTypes.TIANSULUO_PINK_SCARF.get().getDimensions().makeBoundingBox(spawnPos);
        return world.noCollision(box);
    }

    private static boolean isBed(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BedBlock) return true;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = pos.relative(direction);
            if (world.getBlockState(adjacent).getBlock() instanceof BedBlock) return true;
        }
        return false;
    }
}
