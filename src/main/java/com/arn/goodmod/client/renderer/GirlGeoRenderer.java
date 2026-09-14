package com.arn.goodmod.client.renderer;

import com.arn.goodmod.client.model.GirlGeoModel;
import com.arn.goodmod.entity.BaseGirlEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GirlGeoRenderer<T extends BaseGirlEntity> extends GeoEntityRenderer<T> {

    public static final String[] HELMET_BONES = {
            "armorHelmet"
    };

    public static final String[] CHEST_BONES = {
            "armorShoulderL", "armorShoulderR", "armorChest", "armorTorso", "armorBoobs",
            "armorBoobL", "armorBoobR", "armorNippleL", "armorNippleR"
    };

    public static final String[] CHEST_FLESH_BONES = {
            "boobsFlesh", "upperBodyL", "upperBodyR", "meatTorso", "boobR", "boobL", "boobR1", "boobR2"
    };

    public static final String[] LEGGINGS_BONES = {
            "armorHip", "armorBootyL", "armorBootyR", "armorCheekL", "armorCheekR",
            "armorPantsLowL", "armorPantsLowR", "armorPantsUpL", "armorPantsUpR",
            "armorLegL", "armorLegR", "armorKneeL", "armorKneeR", "armorShinL", "armorShinR"
    };

    public static final String[] LEGGINGS_FLESH_BONES = {
            "fleshL", "fleshR", "vagina", "curvesL", "curvesR", "kneeL", "kneeR",
            "meatCheekL", "meatCheekR", "meatLegL", "meatLegR", "meatShinL", "meatShinR"
    };

    public static final String[] BOOTS_BONES = {
            "armorShoesL", "armorShoesR", "armorFootL", "armorFootR"
    };

    public GirlGeoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GirlGeoModel<>());
        this.shadowRadius = 0.45F;
        addRenderLayer(new StevePlayerRenderLayer<>(this));
    }

    private boolean isSteveAncestor(GeoBone bone, BakedGeoModel model) {
        GeoBone steve = model.getBone("steve").orElse(null);
        if (steve == null) return false;
        GeoBone parent = steve.getParent();
        while (parent != null) {
            if (parent == bone) return true;
            parent = parent.getParent();
        }
        return false;
    }

    public static void hideSteve(GeoBone steve) {
        if (steve == null) return;
        steve.setHidden(true);
        steve.setChildrenHidden(true);
        for (GeoBone child : steve.getChildBones()) {
            hideSteve(child);
        }
    }

    public static void unhideSteve(GeoBone steve) {
        if (steve == null) return;
        steve.setHidden(false);
        steve.setChildrenHidden(false);
        for (GeoBone child : steve.getChildBones()) {
            unhideSteve(child);
        }
    }

    private void hideNonSteveBranches(GeoBone parent, BakedGeoModel model) {
        for (GeoBone child : parent.getChildBones()) {
            if (child.getName().equals("steve")) {
                unhideSteve(child);
            } else if (isSteveAncestor(child, model)) {
                child.setHidden(false);
                child.setChildrenHidden(false);
                hideNonSteveBranches(child, model);
            } else {
                child.setHidden(true);
                child.setChildrenHidden(true);
            }
        }
    }

    private void unhideGirlBranches(GeoBone bone) {
        if (bone == null) return;
        if (bone.getName().equals("steve") || bone.getName().equals("alex")) {
            hideSteve(bone);
            return;
        }
        bone.setHidden(false);
        bone.setChildrenHidden(false);
        for (GeoBone child : bone.getChildBones()) {
            unhideGirlBranches(child);
        }
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        // Enable tracking matrices for camera bones and Steve head
        model.getBone("boyCam").ifPresent(b -> {
            b.setTrackingMatrices(true);
            b.setHidden(true);
        });
        model.getBone("girlCam").ifPresent(b -> {
            b.setTrackingMatrices(true);
            b.setHidden(true);
        });
        model.getBone("Head2").ifPresent(b -> {
            b.setTrackingMatrices(true);
        });

        if (isReRender) {
            // Player dummy pass: hide girl top-level branches, reveal steve.
            for (GeoBone bone : model.topLevelBones()) {
                if (bone.getName().equals("steve")) {
                    unhideSteve(bone);
                } else if (isSteveAncestor(bone, model)) {
                    bone.setHidden(false);
                    bone.setChildrenHidden(false);
                    hideNonSteveBranches(bone, model);
                } else {
                    bone.setHidden(true);
                    bone.setChildrenHidden(true);
                }
            }
            model.getBone("steve").ifPresent(GirlGeoRenderer::unhideSteve);

            // Determine if slim (Alex) model
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            boolean isLocalPartner = (animatable.getPartnerUUID().isPresent() &&
                    mc.player != null &&
                    animatable.getPartnerUUID().get().equals(mc.player.getUUID()))
                    || (com.arn.goodmod.client.ClientGameEvents.getActiveSceneGirl() == animatable)
                    || (animatable.getPartnerUUID().isEmpty() && animatable.isInScene());

            net.minecraft.world.entity.player.Player partnerPlayer = null;
            if (animatable.getPartnerUUID().isPresent() && mc.level != null) {
                partnerPlayer = mc.level.getPlayerByUUID(animatable.getPartnerUUID().get());
            }
            if (partnerPlayer == null && isLocalPartner) {
                partnerPlayer = mc.player;
            }
            boolean isSlim = false;
            if (partnerPlayer instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer) {
                isSlim = "slim".equals(clientPlayer.getSkin().model().id());
            } else if (mc.player != null) {
                isSlim = "slim".equals(mc.player.getSkin().model().id());
            }

            boolean slimFinal = isSlim;
            model.getBone("rightArmAlex").ifPresent(b -> b.setHidden(!slimFinal));
            model.getBone("rightLowerArmAlex").ifPresent(b -> b.setHidden(!slimFinal));
            model.getBone("rightArmSteve").ifPresent(b -> b.setHidden(slimFinal));
            model.getBone("rightLowerArmSteve").ifPresent(b -> b.setHidden(slimFinal));
            model.getBone("leftArmAlex").ifPresent(b -> b.setHidden(!slimFinal));
            model.getBone("leftLowerArmAlex").ifPresent(b -> b.setHidden(!slimFinal));
            model.getBone("leftArmSteve").ifPresent(b -> b.setHidden(slimFinal));
            model.getBone("leftLowerArmSteve").ifPresent(b -> b.setHidden(slimFinal));

            // Hide Steve's head in first person so player's camera is not obstructed, but keep child camera bones
            boolean isFirstPerson = isLocalPartner && mc.options.getCameraType() == net.minecraft.client.CameraType.FIRST_PERSON;
            model.getBone("Head2").ifPresent(b -> {
                b.setHidden(isFirstPerson);
            });
            return;
        }

        // Girl mesh pass: restore all girl branches, completely hide dummy player
        for (GeoBone bone : model.topLevelBones()) {
            if (bone.getName().equals("steve") || bone.getName().equals("alex")) {
                hideSteve(bone);
            } else {
                unhideGirlBranches(bone);
            }
        }
        model.getBone("steve").ifPresent(GirlGeoRenderer::hideSteve);
        model.getBone("alex").ifPresent(GirlGeoRenderer::hideSteve);

        // Armor slot bone visibility
        applyArmorBoneVisibility(model, animatable);
    }

    public static <T extends BaseGirlEntity> void applyArmorBoneVisibility(BakedGeoModel model, T animatable) {
        if (animatable == null || model == null) return;
        boolean isNude = animatable.getClothState() == 1;
        boolean hasHelmet = !isNude && !animatable.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        boolean hasChest = !isNude && !animatable.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        boolean hasLegs = !isNude && !animatable.getItemBySlot(EquipmentSlot.LEGS).isEmpty();
        boolean hasBoots = !isNude && !animatable.getItemBySlot(EquipmentSlot.FEET).isEmpty();

        for (String bone : HELMET_BONES) {
            model.getBone(bone).ifPresent(b -> b.setHidden(!hasHelmet));
        }

        for (String bone : CHEST_BONES) {
            model.getBone(bone).ifPresent(b -> b.setHidden(!hasChest));
        }
        for (String bone : CHEST_FLESH_BONES) {
            model.getBone(bone).ifPresent(b -> b.setHidden(hasChest));
        }

        for (String bone : LEGGINGS_BONES) {
            model.getBone(bone).ifPresent(b -> b.setHidden(!hasLegs));
        }
        for (String bone : LEGGINGS_FLESH_BONES) {
            model.getBone(bone).ifPresent(b -> b.setHidden(hasLegs));
        }

        for (String bone : BOOTS_BONES) {
            model.getBone(bone).ifPresent(b -> b.setHidden(!hasBoots));
        }
        model.getBone("customShoeL").ifPresent(b -> b.setHidden(isNude || hasBoots));
        model.getBone("customShoeR").ifPresent(b -> b.setHidden(isNude || hasBoots));
    }

    @Override
    public void postRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                           VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        if (animatable != null && animatable.isInScene()) {
            model.getBone("boyCam").ifPresent(b -> {
                org.joml.Vector3d pos = b.getWorldPosition();
                if (pos.x != 0 || pos.y != 0 || pos.z != 0) {
                    animatable.clientBoyCamPos = new net.minecraft.world.phys.Vec3(pos.x, pos.y, pos.z);
                }
            });
            model.getBone("girlCam").ifPresent(b -> {
                org.joml.Vector3d pos = b.getWorldPosition();
                if (pos.x != 0 || pos.y != 0 || pos.z != 0) {
                    animatable.clientGirlCamPos = new net.minecraft.world.phys.Vec3(pos.x, pos.y, pos.z);
                }
            });
            model.getBone("Head2").ifPresent(b -> {
                org.joml.Vector3d pos = b.getWorldPosition();
                if (pos.x != 0 || pos.y != 0 || pos.z != 0) {
                    animatable.clientHead2Pos = new net.minecraft.world.phys.Vec3(pos.x, pos.y, pos.z);
                }
            });
        }

        if (isReRender) {
            model.getBone("steve").ifPresent(GirlGeoRenderer::hideSteve);
            model.getBone("alex").ifPresent(GirlGeoRenderer::hideSteve);
            for (GeoBone bone : model.topLevelBones()) {
                if (!bone.getName().equals("steve") && !bone.getName().equals("alex")) {
                    bone.setHidden(false);
                    bone.setChildrenHidden(false);
                }
            }
            applyArmorBoneVisibility(model, animatable);
        }
    }
}
