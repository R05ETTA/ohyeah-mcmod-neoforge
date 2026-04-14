package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.worldgen.ModEntityBiomeModifiers;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 生物群系修改器数据生成器 - 物种聚合 Tag 方案
 */
public class ModDatapackBuiltinEntriesProvider extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
                HolderGetter<EntityType<?>> entityTypes = context.lookup(Registries.ENTITY_TYPE);

                for (ModEntityBiomeModifiers.NaturalSpawnPlan plan : ModEntityBiomeModifiers.naturalSpawns()) {
                    if (plan.biomes().isEmpty()) continue;

                    // 引用物种专用生成 Tag: ohyeah:has_spawn_<species_id>
                    TagKey<Biome> speciesTag = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, "has_spawn_" + plan.speciesId()));

                    ResourceLocation entityLoc = ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, plan.speciesId());
                    
                    // 注册单一聚合的 Biome Modifier
                    ResourceKey<net.neoforged.neoforge.common.world.BiomeModifier> modifierKey = 
                            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, 
                            ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, plan.planId()));

                    context.register(modifierKey, new BiomeModifiers.AddSpawnsBiomeModifier(
                            biomes.getOrThrow(speciesTag),
                            List.of(new MobSpawnSettings.SpawnerData(
                                    entityTypes.getOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, entityLoc)).value(),
                                    plan.weight(),
                                    plan.minGroup(),
                                    plan.maxGroup()
                            ))
                    ));
                }
            });

    public ModDatapackBuiltinEntriesProvider(PackOutput output, CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(OhYeah.MODID));
    }
}
