package com.arn.goodmod.client.model;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.client.renderer.GirlGeoRenderer;
import com.arn.goodmod.client.skin.CustomSkinManager;
import com.arn.goodmod.entity.ActionState;
import com.arn.goodmod.entity.BaseGirlEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class GirlGeoModel<T extends BaseGirlEntity> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(T entity) {
        String girl = entity.getGirlName().toLowerCase();
        String skin = entity.getCustomSkin();
        boolean isNude = entity.getClothState() == 1;

        if (skin != null && !skin.isEmpty() && !skin.equalsIgnoreCase(CustomSkinManager.DEFAULT_SKIN)) {
            ResourceLocation customModel = CustomSkinManager.getSkinModel(girl, skin, isNude);
            if (customModel != null) {
                return customModel;
            }
        }

        if (isNude) {
            switch (girl) {
                case "jenny":
                    return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/jenny/jennynude.geo.json");
                case "ellie":
                    return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/ellie/nude.geo.json");
                case "bia":
                    return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/bia/bianude.geo.json");
                case "slime":
                    return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/slime/nude.geo.json");
            }
        }

        boolean hasArmor = !isNude && (!entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty() ||
                !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ||
                !entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty() ||
                !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty());

        if (hasArmor) {
            switch (girl) {
                case "slime":
                    return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/slime/armored.geo.json");
                case "bee":
                    return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/bee/armored.geo.json");
                case "allie":
                    return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/allie/armored.geo.json");
                case "kobold":
                    return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/kobold/armored.geo.json");
                case "goblin":
                    return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/goblin/armored.geo.json");
            }
        }

        switch (girl) {
            case "jenny":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/jenny/jennydressed.geo.json");
            case "ellie":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/ellie/dressed.geo.json");
            case "bia":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/bia/biadressed.geo.json");
            case "slime":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/slime/dressed.geo.json");
            case "bee":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/bee/bee.geo.json");
            case "cat":
            case "luna":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/cat/cat.geo.json");
            case "allie":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/allie/allie.geo.json");
            case "kobold":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/kobold/kobold.geo.json");
            case "goblin":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/goblin/goblin.geo.json");
            case "galath":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/galath/galath.geo.json");
            case "manglelie":
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/manglelie/manglelie.geo.json");
            default:
                return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "geo/" + girl + "/" + girl + ".geo.json");
        }
    }

    private BakedGeoModel lastActiveBakedModel;

    @Override
    public BakedGeoModel getBakedModel(ResourceLocation location) {
        BakedGeoModel custom = CustomSkinManager.getBakedModel(location);
        if (custom != null) {
            if (custom != this.lastActiveBakedModel) {
                this.lastActiveBakedModel = custom;
                getAnimationProcessor().setActiveModel(custom);
                getAnimationProcessor().reloadAnimations = true;
            }
            return custom;
        }
        BakedGeoModel model = super.getBakedModel(location);
        if (model != this.lastActiveBakedModel) {
            this.lastActiveBakedModel = model;
            if (model != null) {
                getAnimationProcessor().setActiveModel(model);
                getAnimationProcessor().reloadAnimations = true;
            }
        }
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(T entity) {
        String girl = entity.getGirlName().toLowerCase();
        String customSkin = entity.getCustomSkin();
        if (customSkin != null && !customSkin.isEmpty() && !customSkin.equalsIgnoreCase(CustomSkinManager.DEFAULT_SKIN)) {
            return CustomSkinManager.getSkinTexture(girl, customSkin);
        }
        return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/entity/" + girl + "/" + girl + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T entity) {
        String girl = entity.getGirlName().toLowerCase();
        String customSkin = entity.getCustomSkin();
        if (customSkin != null && !customSkin.isEmpty() && !customSkin.equalsIgnoreCase(CustomSkinManager.DEFAULT_SKIN)) {
            CustomSkinManager.CustomSkin skinData = CustomSkinManager.getSkinData(girl, customSkin);
            if (skinData != null && skinData.hasCustomAnimations()) {
                ResourceLocation customAnim = skinData.getAnimationLocation();
                if (customAnim != null) {
                    return customAnim;
                }
            }
        }
        return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "animations/" + girl + "/" + girl + ".animation.json");
    }

    @Override
    public Animation getAnimation(T animatable, String name) {
        String girl = animatable.getGirlName().toLowerCase();
        String customSkin = animatable.getCustomSkin();
        if (customSkin != null && !customSkin.isEmpty() && !customSkin.equalsIgnoreCase(CustomSkinManager.DEFAULT_SKIN)) {
            CustomSkinManager.CustomSkin skinData = CustomSkinManager.getSkinData(girl, customSkin);
            if (skinData != null && skinData.hasCustomAnimations()) {
                Animation anim = skinData.getAnimation(name);
                if (anim != null) {
                    return anim;
                }
            }
        }

        // Check custom animations registry by active location
        ResourceLocation activeAnimLoc = getAnimationResource(animatable);
        BakedAnimations customAnims = CustomSkinManager.getBakedAnimations(activeAnimLoc);
        if (customAnims != null) {
            Animation anim = customAnims.getAnimation(name);
            if (anim != null) return anim;
        }

        // Default animation lookup
        ResourceLocation defaultLoc = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "animations/" + girl + "/" + girl + ".animation.json");
        BakedAnimations defaultAnimations = GeckoLibCache.getBakedAnimations().get(defaultLoc);
        if (defaultAnimations != null) {
            return defaultAnimations.getAnimation(name);
        }

        return super.getAnimation(animatable, name);
    }

    @Override
    public boolean crashIfBoneMissing() {
        return false;
    }

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        // Head tracking when idle/walking
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (entityData != null) {
            GeoBone head = getAnimationProcessor().getBone("head");
            if (head != null) {
                ActionState action = animatable.getAction();
                if (action == ActionState.NULL || !action.isAdultScene()) {
                    head.setRotX(entityData.headPitch() * ((float) Math.PI / 180F));
                    head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
                }
            }
        }

        // Hide cameras
        hideBone("boyCam", true);
        hideBone("girlCam", true);

        // Always hide dummy player in girl pass
        hideBone("steve", true);
        hideBone("alex", true);
        hideBone("Torso2", true);
        hideBone("RightLeg", true);
        hideBone("LeftLeg", true);

        // Clothing & Armor slot bone visibility
        boolean isNude = animatable.getClothState() == 1;

        boolean hasHelmet = !animatable.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        boolean hasChest = !animatable.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        boolean hasLegs = !animatable.getItemBySlot(EquipmentSlot.LEGS).isEmpty();
        boolean hasBoots = !animatable.getItemBySlot(EquipmentSlot.FEET).isEmpty();

        // 1. Helmet (Armor bone: only shown if helmet equipped and not nude)
        for (String bone : GirlGeoRenderer.HELMET_BONES) {
            hideBone(bone, isNude || !hasHelmet);
        }

        // 2. Chest Armor (Armor bone: only shown if chestplate equipped and not nude)
        for (String bone : GirlGeoRenderer.CHEST_BONES) {
            hideBone(bone, isNude || !hasChest);
        }
        // Chest flesh: hidden only if covered by an equipped chestplate
        for (String bone : GirlGeoRenderer.CHEST_FLESH_BONES) {
            hideBone(bone, !isNude && hasChest);
        }

        // 3. Leggings Armor (Armor bone: only shown if leggings equipped and not nude)
        for (String bone : GirlGeoRenderer.LEGGINGS_BONES) {
            hideBone(bone, isNude || !hasLegs);
        }
        // Leggings flesh: hidden only if covered by equipped leggings
        for (String bone : GirlGeoRenderer.LEGGINGS_FLESH_BONES) {
            hideBone(bone, !isNude && hasLegs);
        }

        // 4. Boots Armor (Armor bone: only shown if boots equipped and not nude)
        for (String bone : GirlGeoRenderer.BOOTS_BONES) {
            hideBone(bone, isNude || !hasBoots);
        }
        // Normal shoes: hidden if nude OR if covered by armor boots
        hideBone("customShoeL", isNude || hasBoots);
        hideBone("customShoeR", isNude || hasBoots);
    }

    private void hideBone(String boneName, boolean hidden) {
        GeoBone bone = getAnimationProcessor().getBone(boneName);
        if (bone != null) {
            bone.setHidden(hidden);
        }
    }
}
