package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.entity.SuxiaEntity;
import com.ohyeah.ohyeahmod.entity.TiansuluoBattleFaceEntity;
import com.ohyeah.ohyeahmod.entity.TiansuluoPinkScarfEntity;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class ModLootTableProvider {
    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModEntityLoot::new, LootContextParamSets.ENTITY)
        ), registries);
    }

    public static class ModBlockLoot extends BlockLootSubProvider {
        protected ModBlockLoot(HolderLookup.Provider registries) {
            super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK.get());
            dropSelf(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return Stream.of(
                    ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK,
                    ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK
            ).map(supplier -> (Block) supplier.get())::iterator;
        }
    }

    public static class ModEntityLoot extends EntityLootSubProvider {
        protected ModEntityLoot(HolderLookup.Provider registries) {
            super(FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        public void generate() {
            generateForSpecies(ModEntityTypes.TIANSULUO_PINK_SCARF.get(), TiansuluoPinkScarfEntity.FOOD_FAVORITE); // 示例：掉落喜爱食物
            generateForSpecies(ModEntityTypes.TIANSULUO_BATTLE_FACE.get(), TiansuluoBattleFaceEntity.FOOD_FAVORITE);
            generateForSpecies(ModEntityTypes.SUXIA.get(), SuxiaEntity.ADULT_LOOT_ITEMS);
        }

        private void generateForSpecies(EntityType<?> type, List<String> lootItems) {
            LootTable.Builder builder = LootTable.lootTable();
            LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
            
            for (String itemId : lootItems) {
                pool.add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))));
            }
            
            this.add(type, builder.withPool(pool));
        }

        @Override
        protected Stream<EntityType<?>> getKnownEntityTypes() {
            return Stream.of(
                    ModEntityTypes.TIANSULUO_PINK_SCARF.get(),
                    ModEntityTypes.TIANSULUO_BATTLE_FACE.get(),
                    ModEntityTypes.SUXIA.get()
            );
        }
    }
}
