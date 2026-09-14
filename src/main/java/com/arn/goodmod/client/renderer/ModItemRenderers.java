package com.arn.goodmod.client.renderer;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.client.model.SimpleItemGeoModel;
import com.arn.goodmod.item.AlliesLampItem;
import com.arn.goodmod.item.DragonStaffItem;
import com.arn.goodmod.item.GalathCoinItem;
import com.arn.goodmod.item.KoboldEggItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ModItemRenderers {
    private static GeoItemRenderer<GalathCoinItem> galathCoinRenderer;
    private static GeoItemRenderer<DragonStaffItem> dragonStaffRenderer;
    private static GeoItemRenderer<AlliesLampItem> alliesLampRenderer;
    private static GeoItemRenderer<KoboldEggItem> koboldEggRenderer;

    public static GeoItemRenderer<GalathCoinItem> getGalathCoinRenderer() {
        if (galathCoinRenderer == null) {
            galathCoinRenderer = new GeoItemRenderer<>(new SimpleItemGeoModel<>(
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/galath/galath_coin.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/item/galath_coin/galath_coin.png"),
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "animations/galath/galath_coin.animation.json")
            ));
        }
        return galathCoinRenderer;
    }

    public static GeoItemRenderer<DragonStaffItem> getDragonStaffRenderer() {
        if (dragonStaffRenderer == null) {
            dragonStaffRenderer = new GeoItemRenderer<>(new SimpleItemGeoModel<>(
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/kobold/staff.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/entity/kobold/staff.png"),
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "animations/kobold/staff.animation.json")
            ));
        }
        return dragonStaffRenderer;
    }

    public static GeoItemRenderer<AlliesLampItem> getAlliesLampRenderer() {
        if (alliesLampRenderer == null) {
            alliesLampRenderer = new GeoItemRenderer<>(new SimpleItemGeoModel<>(
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/allie/lamp.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/entity/allie/lamp.png"),
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "animations/allie/lamp.animation.json")
            ));
        }
        return alliesLampRenderer;
    }

    public static GeoItemRenderer<KoboldEggItem> getKoboldEggRenderer() {
        if (koboldEggRenderer == null) {
            koboldEggRenderer = new GeoItemRenderer<KoboldEggItem>(new SimpleItemGeoModel<>(
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/kobold/koboldegg.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/entity/kobold/koboldegg.png"),
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "animations/kobold/egg.animation.json")
            )) {
                @Override
                public void renderRecursively(PoseStack poseStack, KoboldEggItem animatable, GeoBone bone,
                                              RenderType renderType, MultiBufferSource bufferSource,
                                              VertexConsumer buffer, boolean isReRender, float partialTick,
                                              int packedLight, int packedOverlay, int colour) {
                    if ("shell".equals(bone.getName())) {
                        colour = 0xFFDFCE9B;
                    } else if ("colorSpots".equals(bone.getName())) {
                        colour = 0xFFC83232;
                    }
                    super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
                }
            };
        }
        return koboldEggRenderer;
    }
}
