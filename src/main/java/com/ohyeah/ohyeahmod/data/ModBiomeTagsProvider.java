package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.worldgen.ModEntityBiomeModifiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * 为每个物种生成聚合的生物群系 Tag
 */
public class ModBiomeTagsProvider extends BiomeTagsProvider {

    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, OhYeah.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (ModEntityBiomeModifiers.NaturalSpawnPlan plan : ModEntityBiomeModifiers.naturalSpawns()) {
            if (plan.biomes().isEmpty()) continue;

            // 创建物种专用的生成 Tag: ohyeah:has_spawn_<species_id>
            TagKey<Biome> speciesTag = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, "has_spawn_" + plan.speciesId()));
            TagAppender<Biome> appender = tag(speciesTag);

            for (String biomeStr : plan.biomes()) {
                if (biomeStr.startsWith("#")) {
                    // 添加原始 Tag
                    appender.addTag(TagKey.create(Registries.BIOME, ResourceLocation.parse(biomeStr.substring(1))));
                } else {
                    // 添加具体 Biome ID
                    appender.add(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(biomeStr)));
                }
            }
        }
    }
}
