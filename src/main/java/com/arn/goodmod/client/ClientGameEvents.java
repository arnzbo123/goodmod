package com.arn.goodmod.client;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.entity.ActionState;
import com.arn.goodmod.entity.BaseGirlEntity;
import com.arn.goodmod.network.ModPackets;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = GoodMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGameEvents {
    private static float thrustBob = 0.0F;
    private static boolean wasInScene = false;
    private static BaseGirlEntity activeSceneGirl = null;
    private static ActionState lastActionState = null;
    private static int activeGirlId = -1;
    private static int sceneTicksInCurrentScene = 0;
    private static long sceneSessionStartTime = 0L;
    private static int thrustCooldown = 0;
    private static boolean freeLookEnabled = false;

    public static void setActiveGirlId(int id) {
        activeGirlId = id;
    }

    public static BaseGirlEntity getActiveSceneGirl() {
        return activeSceneGirl;
    }

    public static void flushStopKeys() {
        while (ModKeyBindings.KEY_STOP_SCENE.consumeClick()) {}
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null && mc.options.keyShift != null) {
            while (mc.options.keyShift.consumeClick()) {}
        }
    }

    public static void bindActiveScene(BaseGirlEntity girl, ActionState action) {
        Minecraft mc = Minecraft.getInstance();
        activeGirlId = girl.getId();
        activeSceneGirl = girl;
        girl.setAction(action);
        if (mc.player != null) {
            girl.setPartnerUUID(mc.player.getUUID());
        }
        wasInScene = true;
        lastActionState = null;
        sceneTicksInCurrentScene = 0;
        sceneSessionStartTime = System.currentTimeMillis();
        freeLookEnabled = false;
        flushStopKeys();
        if (mc.options != null) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    public static void startLocalScene(BaseGirlEntity girl, ActionState action) {
        bindActiveScene(girl, action);
    }

    public static void stopActiveScene() {
        Minecraft mc = Minecraft.getInstance();
        if (activeSceneGirl != null) {
            activeSceneGirl.stopScene();
        }
        activeGirlId = -1;
        activeSceneGirl = null;
        wasInScene = false;
        lastActionState = null;
        sceneTicksInCurrentScene = 0;
        thrustCooldown = 0;
        thrustBob = 0.0F;
        freeLookEnabled = false;
        flushStopKeys();
        if (mc.player != null) {
            mc.player.setPose(Pose.STANDING);
        }
    }

    public static void onSceneSync(int entityId, String actionName, java.util.Optional<java.util.UUID> partnerUUID) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        net.minecraft.world.entity.Entity entity = mc.level.getEntity(entityId);
        if (entity instanceof BaseGirlEntity girl) {
            try {
                ActionState action = ActionState.valueOf(actionName);
                girl.setAction(action);
                girl.setPartnerUUID(partnerUUID.orElse(null));

                boolean isPartner = partnerUUID.isPresent() && partnerUUID.get().equals(mc.player.getUUID());
                if (action.isAdultScene() && (isPartner || activeGirlId == entityId)) {
                    bindActiveScene(girl, action);
                } else if (!action.isAdultScene()) {
                    // Ignore stale NULL stop packets if a new scene was recently started
                    if (System.currentTimeMillis() - sceneSessionStartTime < 1500L) {
                        return;
                    }
                    if (activeGirlId == entityId || activeSceneGirl == girl) {
                        stopActiveScene();
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            activeSceneGirl = null;
            wasInScene = false;
            lastActionState = null;
            activeGirlId = -1;
            sceneTicksInCurrentScene = 0;
            return;
        }

        if (thrustCooldown > 0) {
            thrustCooldown--;
        }

        // Find girl entity where player is partner
        BaseGirlEntity currentGirl = null;
        if (activeGirlId != -1) {
            net.minecraft.world.entity.Entity ent = mc.level.getEntity(activeGirlId);
            if (ent instanceof BaseGirlEntity girl && girl.isInScene()) {
                if (girl.getPartnerUUID().isEmpty() || girl.getPartnerUUID().get().equals(player.getUUID()) || activeSceneGirl == girl) {
                    currentGirl = girl;
                }
            }
        }
        if (currentGirl == null) {
            // Check all nearby girl entities around player to avoid frustum culling misses
            var nearbyGirls = mc.level.getEntitiesOfClass(BaseGirlEntity.class, player.getBoundingBox().inflate(16.0D));
            for (BaseGirlEntity girl : nearbyGirls) {
                if (girl.isInScene()) {
                    boolean partnerMatches = girl.getPartnerUUID().isEmpty() || girl.getPartnerUUID().get().equals(player.getUUID());
                    if (partnerMatches || activeSceneGirl == girl || activeGirlId == girl.getId()) {
                        currentGirl = girl;
                        activeGirlId = girl.getId();
                        break;
                    }
                }
            }
        }
        if (currentGirl == null) {
            for (var entity : mc.level.entitiesForRendering()) {
                if (entity instanceof BaseGirlEntity girl && girl.isInScene()) {
                    if (girl.getPartnerUUID().isPresent() && girl.getPartnerUUID().get().equals(player.getUUID())) {
                        currentGirl = girl;
                        activeGirlId = girl.getId();
                        break;
                    }
                }
            }
        }

        // Scene ended or girl no longer in scene
        if (currentGirl == null) {
            if (wasInScene) {
                wasInScene = false;
                lastActionState = null;
                activeGirlId = -1;
                sceneTicksInCurrentScene = 0;
                player.setPose(Pose.STANDING);
            }
        } else {
            activeGirlId = currentGirl.getId();
        }

        activeSceneGirl = currentGirl;

        if (currentGirl != null) {
            ActionState action = currentGirl.getAction();
            String name = action.name();
            boolean faceGirl = ActionState.shouldFaceGirlDirection(name);

            // First time entering scene: initialize camera
            if (!wasInScene) {
                wasInScene = true;
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                lastActionState = null;
            }

            // Action changed or scene started: initialize view orientation towards action
            if (action != lastActionState) {
                lastActionState = action;
                currentGirl.clientBoyCamPos = null;
                currentGirl.clientGirlCamPos = null;
                currentGirl.clientHead2Pos = null;
                float baseYaw = currentGirl.isOnBed() ? currentGirl.getYRot() : (faceGirl ? currentGirl.getYRot() : (currentGirl.getYRot() + 180.0F));

                float initialPitch;
                if (name.contains("BLOWJOB") || name.contains("DEEPTHROAT")) {
                    initialPitch = 50.0F;
                } else if (name.contains("DOGGY") || name.contains("ANAL")) {
                    initialPitch = 25.0F;
                } else if (name.contains("PAIZURI") || name.contains("TOUCH_BOOBS")) {
                    initialPitch = 35.0F;
                } else if (name.contains("COWGIRL")) {
                    initialPitch = 10.0F;
                } else if (name.contains("MISSIONARY") || name.contains("MATING_PRESS")) {
                    initialPitch = 55.0F;
                } else if (name.contains("NELSON")) {
                    initialPitch = 35.0F;
                } else if (name.contains("CORRUPT") || name.contains("RAPE")) {
                    initialPitch = 50.0F;
                } else if (name.contains("RICH")) {
                    initialPitch = 5.0F;
                } else {
                    initialPitch = 20.0F;
                }
                float minP = Math.min(action.minGirlPitch, action.maxGirlPitch);
                float maxP = Math.max(action.minGirlPitch, action.maxGirlPitch);
                initialPitch = Mth.clamp(initialPitch, minP, maxP);

                player.setYRot(baseYaw);
                player.setYHeadRot(baseYaw);
                player.setYBodyRot(baseYaw);
                player.setXRot(initialPitch);
            }

            // Decay thrust bob
            thrustBob *= 0.75F;

            // Handle rhythmic thrust input: tapping allows responsive pacing, holding provides steady rhythm
            boolean thrustClicked = ModKeyBindings.KEY_THRUST.consumeClick();
            boolean thrustDown = ModKeyBindings.KEY_THRUST.isDown();
            if ((thrustClicked || thrustDown) && thrustCooldown <= 0) {
                thrustCooldown = thrustClicked ? 5 : 8;
                thrustBob = 1.0F;
                com.arn.goodmod.client.gui.HornyMeterOverlay.onThrust();
                PacketDistributor.sendToServer(new ModPackets.ThrustPayload(currentGirl.getId()));
            }

            // Handle Free-look toggle/hold
            boolean toggleMode = com.arn.goodmod.config.GoodModConfig.FREE_LOOK_TOGGLE.get();
            if (toggleMode) {
                if (ModKeyBindings.KEY_FREE_LOOK.consumeClick()) {
                    freeLookEnabled = !freeLookEnabled;
                    player.displayClientMessage(Component.literal("§6[Goodcraft] §eFree-look: " + (freeLookEnabled ? "§aON" : "§cOFF")), true);
                }
            } else {
                freeLookEnabled = ModKeyBindings.KEY_FREE_LOOK.isDown();
            }

            sceneTicksInCurrentScene++;

            // DRAIN clicks from both mappings every tick to prevent accumulation
            boolean stopClicked = ModKeyBindings.KEY_STOP_SCENE.consumeClick();
            boolean shiftClicked = (mc.options.keyShift != null) && mc.options.keyShift.consumeClick();

            // Handle stop scene (Left Shift or Stop Scene Key) - only after grace period of 10 ticks
            if (sceneTicksInCurrentScene > 10 && (stopClicked || shiftClicked)) {
                PacketDistributor.sendToServer(new ModPackets.StopScenePayload(currentGirl.getId()));
                stopActiveScene();
                return;
            }

            // Contextual player poses
            if (name.contains("DOGGY") || name.contains("ANAL") || name.contains("COWGIRL")
                    || name.contains("MISSIONARY") || name.contains("MATING_PRESS")
                    || name.contains("CORRUPT") || name.contains("RAPE")
                    || name.contains("PAIZURI")) {
                player.setPose(Pose.CROUCHING);
            } else {
                player.setPose(Pose.STANDING);
            }

            // Sync camera position and clamped look rotation
            updatePlayerCameraPosition(player, currentGirl);
        }
    }

    @SubscribeEvent
    public static void onRenderFramePre(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || activeSceneGirl == null || !activeSceneGirl.isInScene()) {
            return;
        }

        // Keep camera positioned precisely at the animated bone location on every frame
        updatePlayerCameraPosition(player, activeSceneGirl);
    }

    public static void updatePlayerCameraPosition(LocalPlayer player, BaseGirlEntity currentGirl) {
        if (player == null || currentGirl == null) return;
        ActionState action = currentGirl.getAction();
        String name = action.name();
        boolean faceGirl = ActionState.shouldFaceGirlDirection(name);

        // Determine bone camera position (boyCam for POV if action uses it, Head2 fallback).
        // NEVER use girlCam because the player is Steve/the male partner looking at the girl!
        Vec3 camPos = null;
        if (!name.contains("RICH")) {
            if (action.useBoyCam && currentGirl.clientBoyCamPos != null) {
                camPos = currentGirl.clientBoyCamPos;
            } else if (currentGirl.clientHead2Pos != null) {
                // Head2 pivot is at neck/base of head; offset slightly up to eye level
                camPos = currentGirl.clientHead2Pos.add(0, 0.14, 0);
            }
        }

        double targetX, targetY, targetZ;
        if (camPos != null && (camPos.x != 0 || camPos.y != 0 || camPos.z != 0)
                && camPos.distanceToSqr(currentGirl.position()) < 25.0) {

            Vec3 adjustedCamPos = camPos;
            if (!currentGirl.isOnBed()) {
                // If not on a bed, bed animations have Steve's Head2 positioned 1.84 - 2.5 blocks behind the girl.
                // Clamp the backward offset so player camera is not moved into void or walls!
                double girlYawRad = Math.toRadians(currentGirl.getYRot());
                double forwardX = -Math.sin(girlYawRad);
                double forwardZ = Math.cos(girlYawRad);

                Vec3 rel = camPos.subtract(currentGirl.position());
                // Calculate distance along girl's backward direction (-forwardX, -forwardZ)
                double backDist = rel.x * (-forwardX) + rel.z * (-forwardZ);
                if (backDist > 0.85D) {
                    double excess = backDist - 0.85D;
                    adjustedCamPos = new Vec3(
                            adjustedCamPos.x + forwardX * excess,
                            adjustedCamPos.y,
                            adjustedCamPos.z + forwardZ * excess
                    );
                }
            }

            // Wall collision check: raycast from girl's eye position to camera so we never clip inside solid blocks!
            if (player.level() != null) {
                Vec3 rayStart = currentGirl.getEyePosition();
                net.minecraft.world.phys.HitResult hit = player.level().clip(new net.minecraft.world.level.ClipContext(
                        rayStart, adjustedCamPos,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        player
                ));
                if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                    boolean isBed = false;
                    if (hit instanceof net.minecraft.world.phys.BlockHitResult bhr) {
                        net.minecraft.world.level.block.state.BlockState hitState = player.level().getBlockState(bhr.getBlockPos());
                        if (hitState.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                            isBed = true;
                        }
                    }
                    if (!isBed) {
                        Vec3 hitPos = hit.getLocation();
                        Vec3 toGirl = rayStart.subtract(hitPos).normalize().scale(0.15D);
                        adjustedCamPos = hitPos.add(toGirl);
                    }
                }
            }

            targetX = adjustedCamPos.x;
            targetY = adjustedCamPos.y - player.getEyeHeight();
            targetZ = adjustedCamPos.z;

            // Make sure player's feet never clip below the girl's feet level when standing on ground
            if (!currentGirl.isOnBed()) {
                targetY = Math.max(currentGirl.getY(), targetY);
            }
        } else {
            // Initial fallback offset if bone has not been tracked yet or for standalone animations
            double girlYawRad = Math.toRadians(currentGirl.getYRot());
            double forwardX = -Math.sin(girlYawRad);
            double forwardZ = Math.cos(girlYawRad);
            double offset;
            double heightOffset;
            if (currentGirl.isOnBed()) {
                offset = -1.69D;
                heightOffset = 0.95D;
            } else if (name.contains("DOGGY") || name.contains("ANAL")) {
                offset = -0.7D;
                heightOffset = 0.85D;
            } else if (name.contains("NELSON")) {
                offset = -0.4D;
                heightOffset = 1.35D;
            } else if (name.contains("MATING_PRESS")) {
                offset = -0.15D;
                heightOffset = 0.95D;
            } else if (name.contains("MISSIONARY")) {
                offset = 0.2D;
                heightOffset = 1.05D;
            } else if (name.contains("CORRUPT") || name.contains("RAPE")) {
                offset = 0.25D;
                heightOffset = 0.95D;
            } else if (name.contains("PAIZURI") || name.contains("TOUCH_BOOBS")) {
                boolean isGoblin = currentGirl.getGirlName().equals("goblin");
                offset = isGoblin ? 0.5D : 0.65D;
                heightOffset = isGoblin ? 0.95D : 1.25D;
            } else if (name.contains("RICH")) {
                offset = 2.2D;
                heightOffset = 1.6D;
            } else if (name.contains("COWGIRL")) {
                offset = 0.2D;
                heightOffset = 1.0D;
            } else if (name.contains("BLOWJOB") || name.contains("DEEPTHROAT")) {
                offset = 0.8D;
                heightOffset = 1.1D;
            } else {
                offset = 1.0D;
                heightOffset = 1.0D;
            }
            targetX = currentGirl.getX() + forwardX * offset;
            targetY = currentGirl.getY() + heightOffset - player.getEyeHeight();
            targetZ = currentGirl.getZ() + forwardZ * offset;
        }

        player.setPos(targetX, targetY, targetZ);
        player.setDeltaMovement(Vec3.ZERO);
        player.xo = targetX;
        player.yo = targetY;
        player.zo = targetZ;
        player.xOld = targetX;
        player.yOld = targetY;
        player.zOld = targetZ;

        if (!freeLookEnabled) {
            // Clamp mouse look rotation within allowable pitch/yaw bounds
            float baseYaw = currentGirl.isOnBed() ? currentGirl.getYRot() : (faceGirl ? currentGirl.getYRot() : (currentGirl.getYRot() + 180.0F));

            float minP = Math.min(action.minGirlPitch, action.maxGirlPitch);
            float maxP = Math.max(action.minGirlPitch, action.maxGirlPitch);
            float clampedPitch = Mth.clamp(player.getXRot(), minP, maxP);
            player.setXRot(clampedPitch);

            float yawDiff = Mth.wrapDegrees(player.getYRot() - baseYaw);
            if (yawDiff > 85.0F) {
                player.setYRot(baseYaw + 85.0F);
            } else if (yawDiff < -85.0F) {
                player.setYRot(baseYaw - 85.0F);
            }
        } else {
            // Free-look: unrestricted 360 rotation with standard Minecraft pitch limits
            player.setXRot(Mth.clamp(player.getXRot(), -89.0F, 89.0F));
        }
        player.setYHeadRot(player.getYRot());
        player.setYBodyRot(player.getYRot());
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (activeSceneGirl != null) {
            // Suppress default player movement while in an adult scene
            event.getInput().forwardImpulse = 0;
            event.getInput().leftImpulse = 0;
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (activeSceneGirl != null && activeSceneGirl.getPartnerUUID().isPresent()
                && activeSceneGirl.getPartnerUUID().get().equals(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (activeSceneGirl != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (activeSceneGirl == null || !activeSceneGirl.isInScene()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return;

        // Apply camera recoil on thrust additively to current view
        event.setPitch(event.getPitch() + (thrustBob * 2.5F));
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        com.arn.goodmod.client.gui.HornyMeterOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onEntityInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof BaseGirlEntity girl) {
            net.minecraft.world.item.ItemStack held = event.getItemStack();
            if (held.getItem() instanceof com.arn.goodmod.item.NpcEditorWandItem) {
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(event.getLevel().isClientSide));
                if (event.getLevel().isClientSide) {
                    girl.openNpcEditorScreen(event.getEntity());
                }
            }
        }
    }
}
