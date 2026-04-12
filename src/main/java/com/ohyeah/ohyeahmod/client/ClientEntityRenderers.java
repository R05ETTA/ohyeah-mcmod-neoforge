package com.ohyeah.ohyeahmod.client;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.client.model.SuxiaEntityModel;
import com.ohyeah.ohyeahmod.client.model.TiansuluoBattleFaceEntityModel;
import com.ohyeah.ohyeahmod.client.model.TiansuluoPinkScarfEntityModel;
import com.ohyeah.ohyeahmod.client.renderer.SuxiaEntityRenderer;
import com.ohyeah.ohyeahmod.client.renderer.TiansuluoBattleFaceEntityRenderer;
import com.ohyeah.ohyeahmod.client.renderer.TiansuluoPinkScarfEntityRenderer;
import com.ohyeah.ohyeahmod.entity.projectile.TiansuluoPinkScarfProjectileEntity;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = OhYeah.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientEntityRenderers {
    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SuxiaEntityModel.LAYER_LOCATION, SuxiaEntityModel::createBodyLayer);
        event.registerLayerDefinition(TiansuluoPinkScarfEntityModel.LAYER_LOCATION, TiansuluoPinkScarfEntityModel::createBodyLayer);
        event.registerLayerDefinition(TiansuluoBattleFaceEntityModel.LAYER_LOCATION, TiansuluoBattleFaceEntityModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.TIANSULUO_PINK_SCARF.get(), TiansuluoPinkScarfEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TIANSULUO_BATTLE_FACE.get(), TiansuluoBattleFaceEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TIANSULUO_PINK_SCARF_PROJECTILE.get(), context -> new ThrownItemRenderer<TiansuluoPinkScarfProjectileEntity>(context, 1.0F, true));
        event.registerEntityRenderer(ModEntityTypes.SUXIA.get(), SuxiaEntityRenderer::new);
    }
}
