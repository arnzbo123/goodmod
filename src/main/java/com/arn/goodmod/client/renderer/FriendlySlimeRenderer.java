package com.arn.goodmod.client.renderer;

import com.arn.goodmod.entity.auxiliary.FriendlySlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;

public class FriendlySlimeRenderer extends MobRenderer<FriendlySlimeEntity, SlimeModel<FriendlySlimeEntity>> {
    private static final ResourceLocation SLIME_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/slime/slime.png");

    public FriendlySlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
        this.addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(FriendlySlimeEntity entity) {
        return SLIME_LOCATION;
    }

    @Override
    protected void scale(FriendlySlimeEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }
}
