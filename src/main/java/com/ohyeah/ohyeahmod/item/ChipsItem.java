package com.ohyeah.ohyeahmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * 带有特殊增益效果的食物物品：薯片 (Chips)。
 * <p>
 * 这是一种快餐（进食速度快）。食用后 100% 获得 10 秒的速度 II 效果。
 * 同时它也是天素罗的极度喜爱食物（可用于驯服与瞬间催熟）。
 */
public class ChipsItem extends Item {

    public static final FoodProperties PROPERTIES = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.3F)
            .fast() // 快速食用特性
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1), 1.0F)
            .build();

    public ChipsItem() {
        super(new Item.Properties().food(PROPERTIES));
    }
}
