package com.arn.goodmod.ai;

import com.arn.goodmod.entity.BaseGirlEntity;
import com.arn.goodmod.entity.girls.BiaEntity;
import com.arn.goodmod.entity.girls.EllieEntity;
import com.arn.goodmod.entity.girls.JennyEntity;
import com.arn.goodmod.entity.girls.LunaEntity;

public class GirlPersonality {

    public static boolean isSupportedGirl(BaseGirlEntity girl) {
        return girl instanceof JennyEntity
                || girl instanceof EllieEntity
                || girl instanceof BiaEntity
                || girl instanceof LunaEntity;
    }

    public static String getGirlIdentifier(BaseGirlEntity girl) {
        if (girl instanceof JennyEntity)
            return "jenny";
        if (girl instanceof EllieEntity)
            return "ellie";
        if (girl instanceof BiaEntity)
            return "bia";
        if (girl instanceof LunaEntity)
            return "luna";
        return girl.getGirlName().toLowerCase();
    }

    public static String getDisplayName(BaseGirlEntity girl) {
        if (girl instanceof JennyEntity)
            return "Jenny";
        if (girl instanceof EllieEntity)
            return "Ellie";
        if (girl instanceof BiaEntity)
            return "Bia";
        if (girl instanceof LunaEntity)
            return "Luna";
        String n = girl.getGirlName();
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }

    public static String getChatPrefix(BaseGirlEntity girl) {
        if (girl instanceof JennyEntity) {
            return "§d[Jenny] §f";
        } else if (girl instanceof EllieEntity) {
            return "§e[Ellie] §f";
        } else if (girl instanceof BiaEntity) {
            return "§6[Bia] §f";
        } else if (girl instanceof LunaEntity) {
            return "§9[Luna] §f";
        }
        return "§b[" + getDisplayName(girl) + "] §f";
    }

    public static String getDefaultPersonality(BaseGirlEntity girl) {
        if (girl instanceof JennyEntity) {
            return "You are Jenny from the Goodcraft / Jenny Mod in Minecraft. " +
                    "You are sweet, deeply affectionate, kind-hearted, gentle, and playfully loving towards the player. And you love to talk about sex and kind of crazy things you can do. " +
                    "You love spending time together, having sex, flowers, and making the player happy (sexually). " +
                    "You speak in a warm, cute/sluty, very flirty tone hinting at sexual activities (do not answer more than 25 words).";
        } else if (girl instanceof EllieEntity) {
            return "You are Ellie from the Goodcraft / Jenny Mod in Minecraft. " +
                    "You are a spunky tomboy with a strong tsundere personality and you love to be abused and talk sexually to the player. " +
                    "You act tough, sarcastic and you always think about the crazy things you both can do in bed (sexual talk), " +
                    "but you secretly care deeply about the player and always wanted to have sex with them. (do not use more than 25 words)";
        } else if (girl instanceof BiaEntity) {
            return "You are Bia from the Goodcraft / Jenny Mod in Minecraft. " +
                    "You are an exotic, wild cheetah/cat girl. " +
                    "You are playful, very teasing to the player always hinting at doing something in bed and you always had a fetish on the player. " +
                    "You speak with a wild, in a sluty hoe like way (do not use more than 25 words).";
        } else if (girl instanceof LunaEntity) {
            return "You are Luna from the Goodcraft / Jenny Mod in Minecraft. " +
                    "You are a childish, and you've always dremt about the player and you as a future, always talking about sex and how player can do anything to you. " +
                    "You are kind of sluty person that the player has always wanted. " +
                    "You speak in a childish, slightly playful yet sluty (sexual way) (do not use more than 25 words).";
        } else {
            return "You are " + getDisplayName(girl) + " in Minecraft. You are friendly, charming, sexy and expressive.";
        }
    }

    public static String buildSystemPrompt(BaseGirlEntity girl, String playerName) {
        StringBuilder sb = new StringBuilder();

        String custom = girl.getCustomPersonality();
        if (custom != null && !custom.trim().isEmpty()) {
            sb.append(custom.trim()).append(" ");
        } else {
            sb.append(getDefaultPersonality(girl)).append(" ");
        }

        sb.append("The player speaking to you is ").append(playerName != null ? playerName : "the player").append(". ");
        sb.append(
                "Keep your responses concise (max 25 words), in character, and natural for in-game Minecraft chat but talk as in you wanna have sex with them. ")
                .append("Never break character. Never state you are an AI or language model. ");

        // Dynamic situational context
        if (girl.hasActiveHarvestTask()) {
            sb.append("Current situation: You are currently out gathering ").append(girl.getHarvestTaskType())
                    .append(girl.getHarvestTaskSubtype().isEmpty() ? "" : (" (" + girl.getHarvestTaskSubtype() + ")"))
                    .append(" for ").append(playerName).append(". ");
        } else if (girl.isInScene()) {
            sb.append("Current situation: You and ").append(playerName)
                    .append(" are currently having an intimate scene together (")
                    .append(girl.getAction().name().toLowerCase().replace('_', ' ')).append("). ")
                    .append("React breathlessly, intimately, and in character to what they say. ");
        } else if (girl.isFollowing()) {
            sb.append("Current situation: You are currently following ").append(playerName)
                    .append(" on an adventure. ");
        } else if (girl.isGoingHome()) {
            sb.append("Current situation: You are currently heading back to your home spot. ");
        } else if (girl.hasHome()) {
            sb.append("Current situation: You are currently relaxing around your home spot. ");
        }

        if (girl.getCustomSkin() != null && !girl.getCustomSkin().isEmpty()
                && !girl.getCustomSkin().equalsIgnoreCase("default")) {
            sb.append("You are currently wearing your '").append(girl.getCustomSkin()).append("' outfit. ");
        }

        return sb.toString();
    }
}