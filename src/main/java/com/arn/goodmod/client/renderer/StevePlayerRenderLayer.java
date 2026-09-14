package com.arn.goodmod.client.renderer;

import com.arn.goodmod.entity.BaseGirlEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class StevePlayerRenderLayer<T extends BaseGirlEntity> extends GeoRenderLayer<T> {
    public StevePlayerRenderLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (!animatable.getAction().hasPlayer) {
            // Safety fallback: If entity is in an adult scene with a partner and not a spectator action (like RICH),
            // ensure player dummy is rendered so the scene is never left without the player.
            if (!animatable.isInScene() || animatable.getAction().name().contains("RICH")) {
                return;
            }
        }

        GeoBone steveBone = bakedModel.getBone("steve").orElse(null);
        if (steveBone == null) return;

        boolean isLocalPartner = (animatable.getPartnerUUID().isPresent() &&
                Minecraft.getInstance().player != null &&
                animatable.getPartnerUUID().get().equals(Minecraft.getInstance().player.getUUID()))
                || (com.arn.goodmod.client.ClientGameEvents.getActiveSceneGirl() == animatable)
                || (animatable.getPartnerUUID().isEmpty() && animatable.isInScene());

        ResourceLocation skin = DefaultPlayerSkin.getDefaultTexture();
        boolean isSlim = false;
        net.minecraft.world.entity.player.Player partnerPlayer = null;
        if (animatable.getPartnerUUID().isPresent() && Minecraft.getInstance().level != null) {
            partnerPlayer = Minecraft.getInstance().level.getPlayerByUUID(animatable.getPartnerUUID().get());
        }
        if (partnerPlayer == null && isLocalPartner) {
            partnerPlayer = Minecraft.getInstance().player;
        }
        if (partnerPlayer instanceof AbstractClientPlayer clientPlayer) {
            skin = clientPlayer.getSkin().texture();
            isSlim = "slim".equals(clientPlayer.getSkin().model().id());
        } else if (isLocalPartner && Minecraft.getInstance().player != null) {
            skin = Minecraft.getInstance().player.getSkin().texture();
            isSlim = "slim".equals(Minecraft.getInstance().player.getSkin().model().id());
        } else if (animatable.getPartnerUUID().isPresent()) {
            skin = DefaultPlayerSkin.get(animatable.getPartnerUUID().get()).texture();
        } else if (Minecraft.getInstance().player != null) {
            skin = Minecraft.getInstance().player.getSkin().texture();
            isSlim = "slim".equals(Minecraft.getInstance().player.getSkin().model().id());
        }

        boolean slimFinal = isSlim;
        bakedModel.getBone("rightArmAlex").ifPresent(b -> b.setHidden(!slimFinal));
        bakedModel.getBone("rightLowerArmAlex").ifPresent(b -> b.setHidden(!slimFinal));
        bakedModel.getBone("rightArmSteve").ifPresent(b -> b.setHidden(slimFinal));
        bakedModel.getBone("rightLowerArmSteve").ifPresent(b -> b.setHidden(slimFinal));
        bakedModel.getBone("leftArmAlex").ifPresent(b -> b.setHidden(!slimFinal));
        bakedModel.getBone("leftLowerArmAlex").ifPresent(b -> b.setHidden(!slimFinal));
        bakedModel.getBone("leftArmSteve").ifPresent(b -> b.setHidden(slimFinal));
        bakedModel.getBone("leftLowerArmSteve").ifPresent(b -> b.setHidden(slimFinal));

        boolean hideHead = isLocalPartner && Minecraft.getInstance().options.getCameraType() == net.minecraft.client.CameraType.FIRST_PERSON;
        bakedModel.getBone("Head2").ifPresent(b -> {
            b.setHidden(hideHead);
            b.setChildrenHidden(false);
        });

        steveBone.setHidden(false);
        steveBone.setChildrenHidden(false);
        GeoBone parent = steveBone.getParent();
        while (parent != null) {
            parent.setHidden(false);
            parent.setChildrenHidden(false);
            parent = parent.getParent();
        }

        RenderType playerRenderType = RenderType.entityCutoutNoCull(skin);
        VertexConsumer playerBuffer = bufferSource.getBuffer(playerRenderType);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, playerRenderType, playerBuffer, partialTick, packedLight, packedOverlay, 0xFFFFFFFF);

        // Immediately hide steve/alex and restore girl bones cleanly
        GirlGeoRenderer.hideSteve(steveBone);
        bakedModel.getBone("alex").ifPresent(GirlGeoRenderer::hideSteve);
        for (GeoBone bone : bakedModel.topLevelBones()) {
            if (!bone.getName().equals("steve") && !bone.getName().equals("alex")) {
                bone.setHidden(false);
                bone.setChildrenHidden(false);
            }
        }
        parent = steveBone.getParent();
        while (parent != null) {
            parent.setHidden(false);
            parent.setChildrenHidden(false);
            for (GeoBone child : parent.getChildBones()) {
                if (!child.getName().equals("steve") && !child.getName().equals("alex")) {
                    child.setHidden(false);
                    child.setChildrenHidden(false);
                }
            }
            parent = parent.getParent();
        }
    }
}
