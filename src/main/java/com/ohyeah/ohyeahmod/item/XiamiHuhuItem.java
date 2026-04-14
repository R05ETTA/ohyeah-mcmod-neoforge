package com.ohyeah.ohyeahmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * 带有特殊增益效果的食物物品：虾米呼呼。
 * <p>
 * 食用后有 50% 的概率获得 5 秒的生命恢复效果。
 */
public class XiamiHuhuItem extends Item {

    public static final FoodProperties PROPERTIES = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6F)
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.5F)
            .build();

    public XiamiHuhuItem() {
        super(new Item.Properties().food(PROPERTIES));
    }
}
