package com.arn.goodmod.ai;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.config.GoodModConfig;
import com.arn.goodmod.entity.BaseGirlEntity;
import com.arn.goodmod.entity.girls.BiaEntity;
import com.arn.goodmod.entity.girls.EllieEntity;
import com.arn.goodmod.entity.girls.JennyEntity;
import com.arn.goodmod.entity.girls.LunaEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = GoodMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class GirlChatHandler {
    private static final Set<Integer> BUSY_GIRLS = Collections.synchronizedSet(new HashSet<>());
    private static final Map<UUID, List<GroqClient.ChatMessage>> CONVERSATION_HISTORIES = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) return;

        String rawMessage = event.getRawText();
        if (rawMessage == null || rawMessage.trim().isEmpty()) return;

        // Check if AI chat is enabled
        if (!GoodModConfig.AI_CHAT_ENABLED.get()) return;

        int radius = GoodModConfig.AI_CHAT_RADIUS.get();
        ServerLevel level = player.serverLevel();

        // Find eligible girls within chat radius
        List<BaseGirlEntity> nearbyGirls = level.getEntitiesOfClass(
                BaseGirlEntity.class,
                player.getBoundingBox().inflate(radius),
                girl -> girl.isAlive() && GirlPersonality.isSupportedGirl(girl) && girl.distanceTo(player) <= radius
        );

        if (nearbyGirls.isEmpty()) return;

        String lower = rawMessage.toLowerCase();

        // Determine which girl should respond
        BaseGirlEntity targetGirl = null;

        // 1. Check for explicit name mentions
        List<BaseGirlEntity> mentionedGirls = new ArrayList<>();
        for (BaseGirlEntity girl : nearbyGirls) {
            if (girl instanceof JennyEntity && lower.contains("jenny")) {
                mentionedGirls.add(girl);
            } else if (girl instanceof EllieEntity && lower.contains("ellie")) {
                mentionedGirls.add(girl);
            } else if (girl instanceof BiaEntity && lower.contains("bia")) {
                mentionedGirls.add(girl);
            } else if (girl instanceof LunaEntity && (lower.contains("luna") || lower.contains("cat"))) {
                mentionedGirls.add(girl);
            }
        }

        if (!mentionedGirls.isEmpty()) {
            // Pick closest mentioned girl
            mentionedGirls.sort(Comparator.comparingDouble(g -> g.distanceToSqr(player)));
            targetGirl = mentionedGirls.get(0);
        } else {
            // 2. Otherwise pick the closest girl within radius
            nearbyGirls.sort(Comparator.comparingDouble(g -> g.distanceToSqr(player)));
            targetGirl = nearbyGirls.get(0);
        }

        if (targetGirl == null) return;

        // Check if message is a mining/gathering task command
        if (handleTaskCommands(targetGirl, player, rawMessage, level, radius)) {
            return;
        }

        String apiKey = GoodModConfig.GROQ_API_KEY.get();

        // If API key is empty, give a subtle hint if the player specifically addressed a girl
        if (apiKey == null || apiKey.trim().isEmpty()) {
            boolean mentionsGirl = lower.contains("jenny") || lower.contains("ellie") || lower.contains("bia") || lower.contains("luna") || lower.contains("cat");
            if (mentionsGirl) {
                player.sendSystemMessage(Component.literal("§6[Goodcraft] §eTo chat with girls using AI, enter your Groq API key in §fMods -> Goodcraft -> Config§e!"));
            }
            return;
        }

        int girlId = targetGirl.getId();
        UUID girlUUID = targetGirl.getUUID();

        // Avoid overlapping requests to the same girl
        if (BUSY_GIRLS.contains(girlId)) return;
        BUSY_GIRLS.add(girlId);

        // Visual cue: subtle heart particle showing she heard the player
        level.sendParticles(ParticleTypes.HEART, targetGirl.getX(), targetGirl.getY() + targetGirl.getEyeHeight() + 0.3D, targetGirl.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.02D);
        targetGirl.getLookControl().setLookAt(player, 40.0F, 40.0F);

        // Snapshot of conversation history
        List<GroqClient.ChatMessage> history = CONVERSATION_HISTORIES.computeIfAbsent(girlUUID, k -> Collections.synchronizedList(new ArrayList<>()));
        List<GroqClient.ChatMessage> historySnapshot;
        synchronized (history) {
            historySnapshot = new ArrayList<>(history);
        }

        String systemPrompt = GirlPersonality.buildSystemPrompt(targetGirl, player.getName().getString());
        String model = GoodModConfig.getEffectiveModel();
        final BaseGirlEntity finalGirl = targetGirl;

        GroqClient.askGroq(apiKey, model, systemPrompt, historySnapshot, rawMessage)
                .thenAccept(reply -> {
                    player.server.execute(() -> {
                        BUSY_GIRLS.remove(girlId);
                        if (!finalGirl.isAlive() || reply == null || reply.trim().isEmpty()) return;

                        if (reply.startsWith("§c[")) {
                            // Display error directly to player who chatted
                            player.sendSystemMessage(Component.literal(reply));
                            return;
                        }

                        // Append to conversation history
                        synchronized (history) {
                            history.add(new GroqClient.ChatMessage("user", rawMessage));
                            history.add(new GroqClient.ChatMessage("assistant", reply));
                            while (history.size() > 8) {
                                history.remove(0);
                            }
                        }

                        // Broadcast response to player and nearby players
                        Component messageComponent = Component.literal(GirlPersonality.getChatPrefix(finalGirl) + reply);
                        double hearDistance = radius * 1.5D;

                        Set<ServerPlayer> recipients = new HashSet<>();
                        if (player.isAlive() && player.serverLevel() == level) {
                            recipients.add(player);
                        }
                        for (ServerPlayer sp : level.players()) {
                            if (sp.distanceTo(finalGirl) <= hearDistance) {
                                recipients.add(sp);
                            }
                        }

                        for (ServerPlayer sp : recipients) {
                            sp.sendSystemMessage(messageComponent);
                        }

                        // Heart particles and audio chime
                        level.sendParticles(ParticleTypes.HEART, finalGirl.getX(), finalGirl.getY() + finalGirl.getEyeHeight() + 0.35D, finalGirl.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.05D);
                        level.playSound(null, finalGirl.getX(), finalGirl.getY(), finalGirl.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.7F, 1.3F);
                        finalGirl.getLookControl().setLookAt(player, 40.0F, 40.0F);
                    });
                })
                .exceptionally(err -> {
                    BUSY_GIRLS.remove(girlId);
                    player.server.execute(() -> {
                        player.sendSystemMessage(Component.literal("§c[Goodcraft-AI Exception: " + err.getMessage() + "]"));
                    });
                    return null;
                });
    }

    public static boolean handleTaskCommands(BaseGirlEntity girl, ServerPlayer player, String rawMessage, ServerLevel level, int radius) {
        String lower = rawMessage.toLowerCase().trim();

        // 1. Stop command
        if (lower.equals("stop") || lower.contains("stop mining") || lower.contains("stop chopping") || lower.contains("cancel task") || lower.contains("come back")) {
            if (girl.hasActiveHarvestTask()) {
                girl.stopHarvestTask();
                sendGirlResponse(girl, player, level, radius, "Stopping right now! I'm right here with you~");
                return true;
            }
        }

        // 2. Wood gathering
        boolean asksWood = lower.contains("wood") || lower.contains("tree") || lower.contains("log") || lower.contains("chop");
        if (asksWood && (lower.contains("get") || lower.contains("mine") || lower.contains("chop") || lower.contains("gather") || lower.contains("cut") || lower.contains("fetch") || lower.contains("bring"))) {
            String subtype = null;
            if (lower.contains("birch")) subtype = "birch";
            else if (lower.contains("oak")) subtype = "oak";
            else if (lower.contains("spruce")) subtype = "spruce";
            else if (lower.contains("jungle")) subtype = "jungle";
            else if (lower.contains("acacia")) subtype = "acacia";
            else if (lower.contains("dark oak")) subtype = "dark_oak";
            else if (lower.contains("cherry")) subtype = "cherry";
            else if (lower.contains("mangrove")) subtype = "mangrove";

            ItemStack axe = girl.findAxeInInventory();
            if (axe.isEmpty()) {
                sendGirlResponse(girl, player, level, radius, "I'd love to help, but I need an axe in my inventory to chop wood! Put one in my inventory first~");
                return true;
            }

            girl.startHarvestTask("wood", subtype);
            String targetName = (subtype != null ? subtype + " " : "") + "wood";
            sendGirlResponse(girl, player, level, radius, "On it! Let me grab my axe and gather some " + targetName + " for you~");
            return true;
        }

        // 3. Stone gathering
        boolean asksStone = lower.contains("stone") || lower.contains("cobblestone") || lower.contains("deepslate");
        if (asksStone && (lower.contains("get") || lower.contains("mine") || lower.contains("gather") || lower.contains("fetch") || lower.contains("dig"))) {
            ItemStack pickaxe = girl.findPickaxeInInventory();
            if (pickaxe.isEmpty()) {
                sendGirlResponse(girl, player, level, radius, "I don't have a pickaxe to mine stone! Put one in my inventory first~");
                return true;
            }

            String subtype = lower.contains("deepslate") ? "deepslate" : (lower.contains("cobble") ? "cobble" : null);
            girl.startHarvestTask("stone", subtype);
            sendGirlResponse(girl, player, level, radius, "Got it! Starting to mine stone for you right away~");
            return true;
        }

        // 4. Ore gathering
        boolean asksOre = lower.contains("ore") || lower.contains("iron") || lower.contains("coal") || lower.contains("copper")
                || lower.contains("gold") || lower.contains("diamond") || lower.contains("emerald") || lower.contains("redstone") || lower.contains("lapis");
        if (asksOre && (lower.contains("get") || lower.contains("mine") || lower.contains("gather") || lower.contains("find") || lower.contains("dig"))) {
            ItemStack pickaxe = girl.findPickaxeInInventory();
            if (pickaxe.isEmpty()) {
                sendGirlResponse(girl, player, level, radius, "I need a pickaxe in my inventory before I can mine ores for you!");
                return true;
            }

            String subtype = null;
            if (lower.contains("diamond")) subtype = "diamond";
            else if (lower.contains("iron")) subtype = "iron";
            else if (lower.contains("coal")) subtype = "coal";
            else if (lower.contains("copper")) subtype = "copper";
            else if (lower.contains("gold")) subtype = "gold";
            else if (lower.contains("redstone")) subtype = "redstone";
            else if (lower.contains("lapis")) subtype = "lapis";
            else if (lower.contains("emerald")) subtype = "emerald";

            girl.startHarvestTask("ore", subtype);
            String targetName = (subtype != null ? subtype + " " : "") + "ore";
            sendGirlResponse(girl, player, level, radius, "Yes! I'm on my way to mine " + targetName + " for you~");
            return true;
        }

        return false;
    }

    public static void sendGirlResponse(BaseGirlEntity girl, ServerPlayer player, ServerLevel level, int radius, String reply) {
        Component messageComponent = Component.literal(GirlPersonality.getChatPrefix(girl) + reply);
        double hearDistance = radius * 1.5D;

        Set<ServerPlayer> recipients = new HashSet<>();
        if (player.isAlive() && player.serverLevel() == level) {
            recipients.add(player);
        }
        for (ServerPlayer sp : level.players()) {
            if (sp.distanceTo(girl) <= hearDistance) {
                recipients.add(sp);
            }
        }

        for (ServerPlayer sp : recipients) {
            sp.sendSystemMessage(messageComponent);
        }

        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getY() + girl.getEyeHeight() + 0.35D, girl.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.05D);
        level.playSound(null, girl.getX(), girl.getY(), girl.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.7F, 1.3F);
        girl.getLookControl().setLookAt(player, 40.0F, 40.0F);
    }

    public static void clearHistory(UUID girlUUID) {
        CONVERSATION_HISTORIES.remove(girlUUID);
    }
}
