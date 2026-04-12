package com.ohyeah.ohyeahmod.registry;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.entity.SuxiaEntity;
import com.ohyeah.ohyeahmod.entity.TiansuluoBattleFaceEntity;
import com.ohyeah.ohyeahmod.entity.TiansuluoPinkScarfEntity;
import com.ohyeah.ohyeahmod.entity.projectile.TiansuluoPinkScarfProjectileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntityTypes {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, OhYeah.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TiansuluoPinkScarfEntity>> TIANSULUO_PINK_SCARF = ENTITY_TYPES.register(
            "tiansuluo_pink_scarf",
            () -> EntityType.Builder.of(TiansuluoPinkScarfEntity::new, MobCategory.CREATURE)
                    .sized(TiansuluoPinkScarfEntity.WIDTH, TiansuluoPinkScarfEntity.HEIGHT)
                    .clientTrackingRange(8)
                    .build("tiansuluo_pink_scarf")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<TiansuluoPinkScarfProjectileEntity>> TIANSULUO_PINK_SCARF_PROJECTILE = ENTITY_TYPES.register(
            "tiansuluo_pink_scarf_projectile",
            () -> EntityType.Builder.<TiansuluoPinkScarfProjectileEntity>of(TiansuluoPinkScarfProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("tiansuluo_pink_scarf_projectile")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<TiansuluoBattleFaceEntity>> TIANSULUO_BATTLE_FACE = ENTITY_TYPES.register(
            "tiansuluo_battle_face",
            () -> EntityType.Builder.of(TiansuluoBattleFaceEntity::new, MobCategory.CREATURE)
                    .sized(TiansuluoBattleFaceEntity.TARGET_ADULT_WIDTH, TiansuluoBattleFaceEntity.TARGET_ADULT_HEIGHT)
                    .clientTrackingRange(8)
                    .build("tiansuluo_battle_face")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<SuxiaEntity>> SUXIA = ENTITY_TYPES.register(
            "suxia",
            () -> EntityType.Builder.of(SuxiaEntity::new, MobCategory.WATER_CREATURE)
                    .sized(0.4F, 0.8F)
                    .eyeHeight(0.4F)
                    .clientTrackingRange(8)
                    .build("suxia")
    );

    private ModEntityTypes() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(ModEntityTypes::onEntityAttributeCreation);
        modEventBus.addListener(ModEntityTypes::onSpawnPlacementRegister);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(TIANSULUO_PINK_SCARF.get(), TiansuluoPinkScarfEntity.createAttributes().build());
        event.put(TIANSULUO_BATTLE_FACE.get(), TiansuluoBattleFaceEntity.createAttributes().build());
        event.put(SUXIA.get(), SuxiaEntity.createAttributes().build());
    }

    private static void onSpawnPlacementRegister(RegisterSpawnPlacementsEvent event) {
        event.register(TIANSULUO_PINK_SCARF.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TiansuluoPinkScarfEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TIANSULUO_BATTLE_FACE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TiansuluoBattleFaceEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SUXIA.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SuxiaEntity::canSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
