package com.ohyeah.ohyeahmod.registry;

import com.ohyeah.ohyeahmod.OhYeah;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OhYeah.MODID);

    // --- 食物属性定义 (Food Properties) ---
    public static final FoodProperties XIAMI_HUHU_PROPS = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.6F)
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.5F) // 50% 概率生命恢复 5 秒
            .build();

    public static final FoodProperties CHIPS_PROPS = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.3F).fast() // 快速食用
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1), 1.0F) // 100% 概率速度 II 持续 10 秒
            .build();

    public static final DeferredItem<Item> TIANSULUO_PINK_SCARF_EGG = ITEMS.register("tiansuluo_pink_scarf_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.TIANSULUO_PINK_SCARF, 0xF3D7A6, 0x9D6E52, new Item.Properties()));
    public static final DeferredItem<Item> TIANSULUO_BATTLE_FACE_EGG = ITEMS.register("tiansuluo_battle_face_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.TIANSULUO_BATTLE_FACE, 0xF3D7A6, 0x9D6E52, new Item.Properties()));
    public static final DeferredItem<Item> SUXIA_EGG = ITEMS.register("suxia_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.SUXIA, 0x4E617B, 0xB7C9D9, new Item.Properties()));
    public static final DeferredItem<BlockItem> TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK);
    public static final DeferredItem<BlockItem> TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK);
    public static final DeferredItem<Item> XIAMI_HUHU = ITEMS.registerItem("xiami_huhu", Item::new, new Item.Properties().food(XIAMI_HUHU_PROPS));
    public static final DeferredItem<Item> CHIPS = ITEMS.registerItem("chips", Item::new, new Item.Properties().food(CHIPS_PROPS));
    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
