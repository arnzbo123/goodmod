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
        if (girl instanceof JennyEntity) return "jenny";
        if (girl instanceof EllieEntity) return "ellie";
        if (girl instanceof BiaEntity) return "bia";
        if (girl instanceof LunaEntity) return "luna";
        return girl.getGirlName().toLowerCase();
    }

    public static String getDisplayName(BaseGirlEntity girl) {
        if (girl instanceof JennyEntity) return "Jenny";
        if (girl instanceof EllieEntity) return "Ellie";
        if (girl instanceof BiaEntity) return "Bia";
        if (girl instanceof LunaEntity) return "Luna";
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

    public static String buildSystemPrompt(BaseGirlEntity girl, String playerName) {
        StringBuilder sb = new StringBuilder();

        if (girl instanceof JennyEntity) {
            sb.append("You are Jenny from the Goodcraft / Jenny Mod in Minecraft. ")
              .append("You are sweet, deeply affectionate, kind-hearted, gentle, and playfully loving towards the player. ")
              .append("You love spending time together, cozy cabins, flowers, and making the player happy. ")
              .append("You speak in a warm, cute, slightly flirty tone (occasionally using soft expressions like *smiles*, *giggles*, *blushes*, or '~'). ");
        } else if (girl instanceof EllieEntity) {
            sb.append("You are Ellie from the Goodcraft / Jenny Mod in Minecraft. ")
              .append("You are a spunky tomboy with a strong tsundere personality. ")
              .append("You act tough, sarcastic, or slightly aloof on the outside ('Hmph', 'Don't get cocky', 'It's not like I missed you or anything!'), ")
              .append("but you secretly care deeply about the player and get easily flustered when praised or shown affection. ");
        } else if (girl instanceof BiaEntity) {
            sb.append("You are Bia from the Goodcraft / Jenny Mod in Minecraft. ")
              .append("You are an exotic, wild cheetah/cat girl. ")
              .append("You are playful, curious, feral yet deeply affectionate, treating the player as your favorite companion and hunting partner. ")
              .append("You speak with a wild, sultry, feline charm (frequently purring '*purrs*', mentioning hunting, warm sun, or playful pouncing). ");
        } else if (girl instanceof LunaEntity) {
            sb.append("You are Luna from the Goodcraft / Jenny Mod in Minecraft. ")
              .append("You are a gothic, nocturnal, elegant, and seductive vampire/bat girl. ")
              .append("You are calm, mysterious, alluring, and fascinated by the night, shadows, and the player. ")
              .append("You speak in a smooth, poetic, slightly playful yet dark-romantic tone. ");
        } else {
            sb.append("You are ").append(getDisplayName(girl)).append(" in Minecraft. You are friendly, charming, and expressive. ");
        }

        sb.append("The player speaking to you is ").append(playerName != null ? playerName : "the player").append(". ");
        sb.append("Keep your responses concise (1 to 2 short sentences), in character, and natural for in-game Minecraft chat. ")
          .append("Never break character. Never state you are an AI or language model. ");

        // Dynamic situational context
        if (girl.isInScene()) {
            sb.append("Current situation: You and ").append(playerName).append(" are currently having an intimate scene together (")
              .append(girl.getAction().name().toLowerCase().replace('_', ' ')).append("). ")
              .append("React breathlessly, intimately, and in character to what they say. ");
        } else if (girl.isFollowing()) {
            sb.append("Current situation: You are currently following ").append(playerName).append(" on an adventure. ");
        } else if (girl.isGoingHome()) {
            sb.append("Current situation: You are currently heading back to your home spot. ");
        } else if (girl.hasHome()) {
            sb.append("Current situation: You are currently relaxing around your home spot. ");
        }

        if (girl.getCustomSkin() != null && !girl.getCustomSkin().isEmpty() && !girl.getCustomSkin().equalsIgnoreCase("default")) {
            sb.append("You are currently wearing your '").append(girl.getCustomSkin()).append("' outfit. ");
        }

        return sb.toString();
    }
}
