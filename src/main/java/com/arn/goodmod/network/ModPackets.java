package com.arn.goodmod.network;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.entity.ActionState;
import com.arn.goodmod.entity.BaseGirlEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = GoodMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModPackets {

    // 1. StartScenePayload
    public record StartScenePayload(int entityId, String actionName) implements CustomPacketPayload {
        public static final Type<StartScenePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "start_scene"));
        public static final StreamCodec<ByteBuf, StartScenePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, StartScenePayload::entityId,
                ByteBufCodecs.STRING_UTF8, StartScenePayload::actionName,
                StartScenePayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity target = player.level().getEntity(entityId);
                if (target instanceof BaseGirlEntity girl) {
                    try {
                        ActionState action = ActionState.valueOf(actionName);
                        girl.startScene(action, player);

                        boolean faceGirl = ActionState.shouldFaceGirlDirection(actionName);
                        boolean isBedScene = BaseGirlEntity.isBedAction(action)
                                || (girl.getCustomSkin() != null && !girl.getCustomSkin().isEmpty() && !girl.getCustomSkin().equalsIgnoreCase("default")
                                    && (actionName.contains("DOGGY") || actionName.contains("COWGIRL") || actionName.contains("MISSIONARY")
                                        || actionName.contains("HUG") || actionName.contains("PRONE") || actionName.contains("ANAL")));

                        int bedRadius = com.arn.goodmod.config.GoodModConfig.BED_RANGE.get();
                        BlockPos bedHeadPos = isBedScene ? findNearbyBed(girl.level(), girl.blockPosition(), bedRadius) : null;
                        if (bedHeadPos != null) {
                            net.minecraft.world.level.block.state.BlockState bedState = girl.level().getBlockState(bedHeadPos);
                            Direction facing = (bedState.getBlock() instanceof net.minecraft.world.level.block.BedBlock)
                                    ? bedState.getValue(net.minecraft.world.level.block.BedBlock.FACING) : Direction.NORTH;
                            BlockPos footPos = bedHeadPos.relative(facing.getOpposite());
                            float bedYaw = facing.toYRot();
                            double dirX = -Math.sin(Math.toRadians(bedYaw));
                            double dirZ = Math.cos(Math.toRadians(bedYaw));
                            double girlX = footPos.getX() + 0.5D - dirX * 0.75D;
                            double girlZ = footPos.getZ() + 0.5D - dirZ * 0.75D;
                            double bedBaseY = bedHeadPos.getY();

                            girl.noPhysics = true;
                            girl.setNoGravity(true);
                            girl.teleportTo(girlX, bedBaseY, girlZ);
                            girl.setYRot(bedYaw);
                            girl.setYHeadRot(bedYaw);
                            girl.setYBodyRot(bedYaw);
                            girl.yRotO = bedYaw;
                            girl.yHeadRotO = bedYaw;
                            girl.yBodyRotO = bedYaw;

                            girl.setOnBed(true);
                            girl.setBedPos(bedHeadPos);

                            float targetYaw = bedYaw;
                            double targetX = girlX - dirX * 1.69D;
                            double targetZ = girlZ - dirZ * 1.69D;

                            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                                serverPlayer.teleportTo(serverPlayer.serverLevel(), targetX, bedBaseY, targetZ, targetYaw, player.getXRot());
                                PacketDistributor.sendToPlayer(serverPlayer, new SyncScenePayload(girl.getId(), action.name(), java.util.Optional.of(player.getUUID())));
                            } else {
                                player.teleportTo(targetX, bedBaseY, targetZ);
                                player.setYRot(targetYaw);
                                player.setYHeadRot(targetYaw);
                                player.setYBodyRot(targetYaw);
                            }
                        } else {
                            girl.setOnBed(false);
                            girl.setBedPos(null);

                            if (isBedScene) {
                                player.displayClientMessage(Component.literal("§e[GoodMod] A bed nearby is recommended for this animation!"), true);
                            }

                            double girlYawRad = Math.toRadians(girl.getYRot());
                            double forwardX = -Math.sin(girlYawRad);
                            double forwardZ = Math.cos(girlYawRad);
                            double offset;
                            if (actionName.contains("DOGGY") || actionName.contains("ANAL")) {
                                offset = -0.75D;
                            } else if (actionName.contains("NELSON")) {
                                offset = -0.4D;
                            } else if (actionName.contains("MATING_PRESS")) {
                                offset = -0.15D;
                            } else if (actionName.contains("MISSIONARY")) {
                                offset = 0.2D;
                            } else if (actionName.contains("CORRUPT") || actionName.contains("RAPE")) {
                                offset = 0.25D;
                            } else if (actionName.contains("PAIZURI") || actionName.contains("TOUCH_BOOBS")) {
                                offset = girl.getGirlName().equals("goblin") ? 0.5D : 0.65D;
                            } else if (actionName.contains("RICH")) {
                                offset = 2.2D;
                            } else if (actionName.contains("COWGIRL")) {
                                offset = 0.2D;
                            } else if (actionName.contains("BLOWJOB") || actionName.contains("DEEPTHROAT")) {
                                offset = 0.8D;
                            } else if (actionName.contains("HUG")) {
                                offset = 0.5D;
                            } else {
                                offset = 0.8D;
                            }
                            double targetX = girl.getX() + forwardX * offset;
                            double targetZ = girl.getZ() + forwardZ * offset;
                            float targetYaw = faceGirl ? girl.getYRot() : (girl.getYRot() + 180.0F);

                            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                                serverPlayer.teleportTo(serverPlayer.serverLevel(), targetX, girl.getY(), targetZ, targetYaw, player.getXRot());
                                PacketDistributor.sendToPlayer(serverPlayer, new SyncScenePayload(girl.getId(), action.name(), java.util.Optional.of(player.getUUID())));
                            } else {
                                player.teleportTo(targetX, girl.getY(), targetZ);
                                player.setYRot(targetYaw);
                                player.setYHeadRot(targetYaw);
                                player.setYBodyRot(targetYaw);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            });
        }

        private static BlockPos findNearbyBed(net.minecraft.world.level.Level level, BlockPos center, int radius) {
            BlockPos closestHead = null;
            double closestDistSq = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.betweenClosed(
                    center.offset(-radius, -2, -radius),
                    center.offset(radius, 2, radius))) {
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                    net.minecraft.world.level.block.state.properties.BedPart part =
                            state.getValue(net.minecraft.world.level.block.BedBlock.PART);
                    Direction facing = state.getValue(net.minecraft.world.level.block.BedBlock.FACING);
                    BlockPos head = (part == net.minecraft.world.level.block.state.properties.BedPart.HEAD)
                            ? pos.immutable() : pos.relative(facing).immutable();
                    double d = center.distSqr(head);
                    if (d < closestDistSq) {
                        closestDistSq = d;
                        closestHead = head;
                    }
                }
            }
            return closestHead;
        }
    }

    // 2. StopScenePayload
    public record StopScenePayload(int entityId) implements CustomPacketPayload {
        public static final Type<StopScenePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "stop_scene"));
        public static final StreamCodec<ByteBuf, StopScenePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, StopScenePayload::entityId,
                StopScenePayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity target = player.level().getEntity(entityId);
                if (target instanceof BaseGirlEntity girl) {
                    girl.stopScene();
                }
            });
        }
    }

    // 3. ThrustPayload
    public record ThrustPayload(int entityId) implements CustomPacketPayload {
        public static final Type<ThrustPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "thrust"));
        public static final StreamCodec<ByteBuf, ThrustPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ThrustPayload::entityId,
                ThrustPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity target = player.level().getEntity(entityId);
                if (target instanceof BaseGirlEntity girl) {
                    girl.handleSceneThrust(player);
                }
            });
        }
    }

    // 4. OutfitChangePayload
    public record OutfitChangePayload(int entityId, int outfitState) implements CustomPacketPayload {
        public static final Type<OutfitChangePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "outfit_change"));
        public static final StreamCodec<ByteBuf, OutfitChangePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, OutfitChangePayload::entityId,
                ByteBufCodecs.VAR_INT, OutfitChangePayload::outfitState,
                OutfitChangePayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity target = player.level().getEntity(entityId);
                if (target instanceof BaseGirlEntity girl) {
                    if (outfitState == 1 && girl.hasStripAnimation() && girl.getClothState() == 0) {
                        girl.startStripAnimation();
                    } else {
                        girl.setClothState(outfitState);
                        if (girl.getAction() == ActionState.STRIP) {
                            girl.setAction(ActionState.NULL);
                        }
                    }
                }
            });
        }
    }

    // 5. SyncScenePayload (Server -> Client)
    public record SyncScenePayload(int entityId, String actionName, java.util.Optional<java.util.UUID> partnerUUID) implements CustomPacketPayload {
        public static final Type<SyncScenePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "sync_scene"));
        public static final StreamCodec<ByteBuf, SyncScenePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SyncScenePayload::entityId,
                ByteBufCodecs.STRING_UTF8, SyncScenePayload::actionName,
                ByteBufCodecs.optional(net.minecraft.core.UUIDUtil.STREAM_CODEC), SyncScenePayload::partnerUUID,
                SyncScenePayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext context) {
            context.enqueueWork(() -> {
                com.arn.goodmod.client.ClientHooks.handleSceneSync(entityId, actionName, partnerUUID);
            });
        }
    }

    // 6. ToggleFollowPayload
    public record ToggleFollowPayload(int entityId) implements CustomPacketPayload {
        public static final Type<ToggleFollowPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "toggle_follow"));
        public static final StreamCodec<ByteBuf, ToggleFollowPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ToggleFollowPayload::entityId,
                ToggleFollowPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity target = player.level().getEntity(entityId);
                if (target instanceof BaseGirlEntity girl) {
                    if (girl.isFollowing()) {
                        girl.setFollowing(false);
                        player.displayClientMessage(Component.translatable("chat.goodmod.stop_following", girl.getGirlName()), true);
                    } else {
                        girl.setFollowing(true);
                        girl.setGoingHome(false);
                        girl.setOwnerUUID(player.getUUID());
                        player.displayClientMessage(Component.translatable("chat.goodmod.following", girl.getGirlName()), true);
                    }
                }
            });
        }
    }

    // 7. SetHomePayload
    public record SetHomePayload(int entityId) implements CustomPacketPayload {
        public static final Type<SetHomePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "set_home"));
        public static final StreamCodec<ByteBuf, SetHomePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SetHomePayload::entityId,
                SetHomePayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity target = player.level().getEntity(entityId);
                if (target instanceof BaseGirlEntity girl) {
                    BlockPos pos = girl.blockPosition();
                    girl.setHomePos(pos);
                    player.displayClientMessage(Component.translatable("chat.goodmod.set_home", girl.getGirlName(), pos.getX(), pos.getY(), pos.getZ()), true);
                    girl.level().playSound(null, pos, net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
            });
        }
    }

    // 8. GoHomePayload
    public record GoHomePayload(int entityId) implements CustomPacketPayload {
        public static final Type<GoHomePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "go_home"));
        public static final StreamCodec<ByteBuf, GoHomePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, GoHomePayload::entityId,
                GoHomePayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity target = player.level().getEntity(entityId);
                if (target instanceof BaseGirlEntity girl) {
                    if (girl.hasHome()) {
                        girl.setFollowing(false);
                        girl.setGoingHome(true);
                        player.displayClientMessage(Component.translatable("chat.goodmod.go_home", girl.getGirlName()), true);
                    } else {
                        player.displayClientMessage(Component.translatable("chat.goodmod.no_home", girl.getGirlName()), true);
                    }
                }
            });
        }
    }

    // 9. SetGirlCustomizationPayload
    public record SetGirlCustomizationPayload(int entityId, String customSkin, int clothState) implements CustomPacketPayload {
        public static final Type<SetGirlCustomizationPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "set_girl_customization"));
        public static final StreamCodec<ByteBuf, SetGirlCustomizationPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SetGirlCustomizationPayload::entityId,
                ByteBufCodecs.STRING_UTF8, SetGirlCustomizationPayload::customSkin,
                ByteBufCodecs.VAR_INT, SetGirlCustomizationPayload::clothState,
                SetGirlCustomizationPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(net.neoforged.neoforge.network.handling.IPayloadContext context) {
            context.enqueueWork(() -> {
                Player player = context.player();
                Entity target = player.level().getEntity(entityId);
                if (target instanceof BaseGirlEntity girl) {
                    if (player.distanceToSqr(girl) <= 64.0D) {
                        girl.setCustomSkin(customSkin);
                        girl.setClothState(clothState);
                        player.displayClientMessage(Component.translatable("chat.goodmod.customization_applied", girl.getGirlName(), customSkin), true);
                        girl.level().playSound(null, girl.blockPosition(), net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER.value(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(StartScenePayload.TYPE, StartScenePayload.STREAM_CODEC, StartScenePayload::handle);
        registrar.playToServer(StopScenePayload.TYPE, StopScenePayload.STREAM_CODEC, StopScenePayload::handle);
        registrar.playToServer(ThrustPayload.TYPE, ThrustPayload.STREAM_CODEC, ThrustPayload::handle);
        registrar.playToServer(OutfitChangePayload.TYPE, OutfitChangePayload.STREAM_CODEC, OutfitChangePayload::handle);
        registrar.playToClient(SyncScenePayload.TYPE, SyncScenePayload.STREAM_CODEC, SyncScenePayload::handle);
        registrar.playToServer(ToggleFollowPayload.TYPE, ToggleFollowPayload.STREAM_CODEC, ToggleFollowPayload::handle);
        registrar.playToServer(SetHomePayload.TYPE, SetHomePayload.STREAM_CODEC, SetHomePayload::handle);
        registrar.playToServer(GoHomePayload.TYPE, GoHomePayload.STREAM_CODEC, GoHomePayload::handle);
        registrar.playToServer(SetGirlCustomizationPayload.TYPE, SetGirlCustomizationPayload.STREAM_CODEC, SetGirlCustomizationPayload::handle);
    }
}
