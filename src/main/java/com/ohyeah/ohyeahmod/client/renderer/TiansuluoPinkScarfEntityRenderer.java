package com.ohyeah.ohyeahmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.client.model.TiansuluoPinkScarfEntityModel;
import com.ohyeah.ohyeahmod.entity.TiansuluoPinkScarfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TiansuluoPinkScarfEntityRenderer extends MobRenderer<TiansuluoPinkScarfEntity, TiansuluoPinkScarfEntityModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, "textures/entity/tiansuluo_pink_scarf.png");

    public TiansuluoPinkScarfEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new TiansuluoPinkScarfEntityModel(context.bakeLayer(TiansuluoPinkScarfEntityModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(TiansuluoPinkScarfEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(TiansuluoPinkScarfEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        float renderScale = 0.61F;
        if (entity.isBaby()) {
            renderScale *= 0.55F;
        }
        poseStack.scale(renderScale, renderScale, renderScale);
    }
}
