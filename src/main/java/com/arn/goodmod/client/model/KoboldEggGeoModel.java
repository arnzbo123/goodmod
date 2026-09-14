package com.arn.goodmod.client.model;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.entity.auxiliary.KoboldEggEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

@SuppressWarnings("removal")
public class KoboldEggGeoModel extends GeoModel<KoboldEggEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/kobold/koboldegg.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/entity/kobold/koboldegg.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "animations/kobold/egg.animation.json");

    @Override
    public ResourceLocation getModelResource(KoboldEggEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getModelResource(KoboldEggEntity animatable, @Nullable GeoRenderer<KoboldEggEntity> renderer) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(KoboldEggEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getTextureResource(KoboldEggEntity animatable, @Nullable GeoRenderer<KoboldEggEntity> renderer) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(KoboldEggEntity animatable) {
        return ANIMATION;
    }
}
