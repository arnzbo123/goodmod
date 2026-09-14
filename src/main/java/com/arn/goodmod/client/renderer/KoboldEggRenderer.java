package com.arn.goodmod.client.renderer;

import com.arn.goodmod.client.model.KoboldEggGeoModel;
import com.arn.goodmod.entity.auxiliary.KoboldEggEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KoboldEggRenderer extends GeoEntityRenderer<KoboldEggEntity> {
    private static final int SHELL_COLOR = 0xFFDFCE9B;
    private static final int SPOT_COLOR = 0xFFC83232;

    public KoboldEggRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KoboldEggGeoModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, KoboldEggEntity animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, int colour) {
        if ("shell".equals(bone.getName())) {
            colour = SHELL_COLOR;
        } else if ("colorSpots".equals(bone.getName())) {
            colour = SPOT_COLOR;
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}
